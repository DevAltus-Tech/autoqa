package com.kagr.videos.orders;






import com.kagr.videos.jms.monitor.ArtemisNotificationsListener;
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

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;





@Slf4j
@Data
@Configuration
@ConfigurationProperties
public class OrdersConfig
{

    @Value("${broker.url}")
    private String brokerAddress;

    @Value("${broker.username}")
    private String username;

    @Value("${broker.password}")
    private String password;

    @Value("${orders.topic}")
    private String ordersTopic;





    @Bean
    public Connection jmsConnection() throws javax.jms.JMSException
    {
        TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName());
        ActiveMQConnectionFactory connectionFactory = ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);
        connectionFactory.setBrokerURL(brokerAddress);
        connectionFactory.setUser(username);
        connectionFactory.setPassword(password);
        logger.info("Creating ConnectionFactory for broker URL: {}, username:{}", brokerAddress, username);
        connectionFactory.setClientID("order-producer");
        return connectionFactory.createConnection();
    }





    @Bean
    public Session jmsSession(Connection connection) throws JMSException
    {
        logger.info("Creating Session for connection: {}", connection);
        Session session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        return session;
    }





    @Bean
    public MessageProducer ordersMessageProducer(Session session) throws JMSException
    {
        logger.info("Creating OrderProducer for topic: {}", ordersTopic);
        Topic theTopic = ActiveMQJMSClient.createTopic(ordersTopic);
        return session.createProducer(theTopic);
    }





    @Bean
    public ArtemisNotificationsListener artemisNotificationsListener(Connection jmsConnection) throws JMSException
    {
        logger.info("Creating ArtemisNotificationsListener for broker URL: {}", brokerAddress);
        return new ArtemisNotificationsListener(jmsConnection).startListening();
    }
}
