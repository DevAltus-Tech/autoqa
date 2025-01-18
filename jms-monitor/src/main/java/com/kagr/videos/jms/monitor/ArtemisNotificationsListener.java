package com.kagr.videos.jms.monitor;





import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;





@Slf4j
@Component
public class ArtemisNotificationsListener implements MessageListener
{

    private final Connection jmsConnection;





    @Autowired
    public ArtemisNotificationsListener(Connection jmsConnection) throws JMSException
    {
        this.jmsConnection = jmsConnection;
        final Session session = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final Topic notificationsTopic = session.createTopic("activemq.notifications");
        final MessageConsumer notificationConsumer = session.createConsumer(notificationsTopic);
        notificationConsumer.setMessageListener(this);
    }





    public ArtemisNotificationsListener startListening() throws JMSException
    {
        jmsConnection.start();
        return this;
    }





    @Override
    public void onMessage(final Message message)
    {
        logger.info("Received message: {}", message);
    }
}
