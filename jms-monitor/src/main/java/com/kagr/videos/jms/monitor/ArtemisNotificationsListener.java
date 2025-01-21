package com.kagr.videos.jms.monitor;





import io.netty.util.internal.StringUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;





@Slf4j
public class ArtemisNotificationsListener implements MessageListener {

    public static final String ACTIVEMQ_NOTIFICATIONS = "activemq.notifications";
    private final Connection jmsConnection;
    private final Set<BiConsumer<String, String>> consumers;





    @Autowired
    public ArtemisNotificationsListener(@NonNull final Connection jmsConnection,
                                        @NonNull final Set<BiConsumer<String, String>> consumers)
        throws JMSException {
        this.jmsConnection = jmsConnection;
        this.consumers = consumers;
        final Session session = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final Topic notificationsTopic = session.createTopic(ACTIVEMQ_NOTIFICATIONS);
        final MessageConsumer notificationConsumer = session.createConsumer(notificationsTopic);
        notificationConsumer.setMessageListener(this);
    }





    public ArtemisNotificationsListener startListening() throws JMSException {
        logger.info("Starting to listen for notifications");
        if (jmsConnection == null) {
            throw new IllegalStateException("JMS connection is null");
        }

        jmsConnection.start();
        return this;
    }





    @Override
    public void onMessage(final Message message) {
        final var str = message.toString();
        if (StringUtil.isNullOrEmpty(str)) {
            logger.warn("Received empty message");
            return;
        }


        try {
            if (logger.isDebugEnabled()) {
                Iterator<String> itr = message.getPropertyNames().asIterator();
                String key;
                while (itr.hasNext()) {
                    key = itr.next();
                    logger.debug("Property: {}={}", key, message.getStringProperty(key));
                }
            }


            if (!StringUtil.isNullOrEmpty(message.getStringProperty("_AMQ_NotifType")) &&
                !StringUtil.isNullOrEmpty(message.getStringProperty("_AMQ_Client_ID"))) {
                logger.info("Notification message: {}, Client-ID:{}", message.getStringProperty("_AMQ_NotifType"), message.getStringProperty("_AMQ_Client_ID"));

                for (BiConsumer<String, String> consumer : consumers) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Message sent to consumer: {}", consumer);
                    }
                    consumer.accept(message.getStringProperty("_AMQ_NotifType"), message.getStringProperty("_AMQ_Client_ID"));

                }
            }
        }
        catch (JMSException ex_) {
            throw new RuntimeException(ex_);
        }


    }
}
