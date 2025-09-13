package com.kagr.videos.orderclient;





import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import lombok.Data;
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





@Slf4j
@Data
@Configuration
@ConfigurationProperties
public class OrderClientConfig {

    @Value("${broker.url}")
    private String brokerAddress;

    @Value("${broker.username}")
    private String username;

    @Value("${broker.password}")
    private String password;

    @Value("${orders.topic}")
    private String ordersQueue;

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
    public Session jmsSession(Connection connection) throws JMSException {
        logger.info("Creating Session for connection: {}", connection);
        Session session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        return session;
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
    public OrderReceiver orderReceiver(Session session) {
        logger.info("Creating OrderReceiver");
        return new OrderReceiver(session);
    }





    @Bean
    public MessageConsumer ordersMessageConsumer(OrderReceiver receiver) throws JMSException {
        final Session session = receiver.getSession();
        final Queue theQ = session.createQueue(this.ordersQueue);
        logger.error("Creating {} OrderConsumer for queue: {}", theQ, this.ordersQueue);
        var consumer = session.createConsumer(theQ);
        consumer.setMessageListener(receiver);
        return consumer;
    }
}
