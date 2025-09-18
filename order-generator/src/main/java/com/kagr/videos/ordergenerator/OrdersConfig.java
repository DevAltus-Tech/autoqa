package com.kagr.videos.ordergenerator;





import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
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
public class OrdersConfig {

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

    @Value("${config.gateway.topic}")
    private String configGatewayTopic;

    @Value("${config.gateway.filter}")
    private String configGatewayFilter;

    @Value("${spring.application.name}")
    private String appName;




    @Bean
    OrderGenController orderGenController(OrderProducer producer) {
        logger.info("Creating OrderGenController");
        return new OrderGenController(producer);
    }




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
    public MessageConsumer heartbeatMessageConsumer(Connection connection, HeartbeatConsumer heartbeatConsumer) throws JMSException {
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final Topic heartbeatTopic = session.createTopic(this.heartbeatTopic);
        logger.info("Creating MessageConsumer for HeartbeatConsumer on topic: {}", this.heartbeatTopic);
        final MessageConsumer consumer = session.createConsumer(heartbeatTopic);
        consumer.setMessageListener(heartbeatConsumer);
        return consumer;
    }


    @Bean
    public Session jmsSession(Connection connection) throws JMSException {
        logger.info("Creating Session for connection: {}", connection);
        return connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
    }



    @Bean
    public OrderProducer orderProducer(Session session) throws JMSException {
        final Queue theQueue = session.createQueue(this.ordersQueue);
        logger.info("Creating {} OrderProducer for queue: {}", theQueue, this.ordersQueue);
        final MessageProducer ordersMessageProducer = session.createProducer(theQueue);
        return new OrderProducer(ordersMessageProducer, session);
    }

}
