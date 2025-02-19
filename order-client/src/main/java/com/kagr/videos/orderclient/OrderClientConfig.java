package com.kagr.videos.orderclient;





import com.kagr.videos.jms.monitor.ArtemisNotificationsListener;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;





@Slf4j
@Data
@Configuration
@ConfigurationProperties
public class OrderClientConfig {

    private final HashSet<BiConsumer<String, String>> jmsConsumers;

    @Value("${broker.url}")
    private String brokerAddress;

    @Value("${broker.username}")
    private String username;

    @Value("${broker.password}")
    private String password;

    @Value("${orders.topic}")
    private String ordersTopic;

    @Value("${heartbeat.topic}")
    private String heartbeatTopic;

    @Value("${spring.application.name}")
    private String appName;





    @Bean
    public Connection jmsConnection() throws javax.jms.JMSException {
        final TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName());
        final ActiveMQConnectionFactory connectionFactory = ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);
        connectionFactory.setBrokerURL(brokerAddress);
        connectionFactory.setUser(username);
        connectionFactory.setPassword(password);
        connectionFactory.setClientID(appName);
        logger.info("Creating ConnectionFactory for broker URL: {}, client-id:{}, username:{}", brokerAddress, appName, username);
        final var connection = connectionFactory.createConnection();
        connection.start();
        return connection;
    }





    @Bean
    public MessageConsumer heartbeatMessageConsumer(Session session, HeartbeatConsumer heartbeatConsumer) throws JMSException {
        Topic heartbeatTopic = session.createTopic(this.heartbeatTopic);
        logger.info("Creating MessageConsumer for HeartbeatConsumer on topic: {}", this.heartbeatTopic);
        MessageConsumer consumer = session.createConsumer(heartbeatTopic);
        consumer.setMessageListener(heartbeatConsumer);
        return consumer;
    }





    @Bean
    public Session jmsSession(Connection connection) throws JMSException {
        logger.info("Creating Session for connection: {}", connection);
        Session session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        return session;
    }





    @Bean
    public MessageConsumer ordersMessageConsumer(Session session) throws JMSException {
        Queue theQ = session.createQueue(ordersTopic);
        logger.info("Creating {} OrderProducer for topic: {}", theQ, ordersTopic);
        return session.createConsumer(theQ);
    }





    @Bean
    public OrderReceiver orderProducer(MessageProducer ordersMessageProducer, Set<BiConsumer<String, String>> jmsConsumers) {
        var producer = new OrderReceiver(ordersMessageProducer);
        logger.info("{} for topic: {}", producer.getClass().getSimpleName(), ordersTopic);
        jmsConsumers.add(producer::handleJmsEvent);
        return producer;
    }





    @Bean
    public Set<BiConsumer<String, String>> consumers() {
        if (jmsConsumers == null) {
            logger.info("Creating empty set of consumers");
            return new HashSet<>();
        }

        jmsConsumers.add(new JmsEventHandler()::onJmsEvent);
        return jmsConsumers;
    }





    @Bean
    public ArtemisNotificationsListener artemisNotificationsListener(@NonNull final Connection jmsConnection,
                                                                     @NonNull final Set<BiConsumer<String, String>> jmsEventConsumers) throws JMSException {
        logger.info("Creating ArtemisNotificationsListener for broker URL: {}", brokerAddress);
        return new ArtemisNotificationsListener(jmsConnection, jmsEventConsumers).startListening();
    }
}
