package com.kagr.videos.orderclient;





import jakarta.annotation.PostConstruct;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;





@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderReceiver implements MessageListener {
    @Getter private final Session session;
    private MessageProducer producer;



    @PostConstruct
    public void init() {
        logger.info("OrderProducer initialized");
    }


    @Override
    public void onMessage(final Message message) {
        try {
            final var correlationId = message.getJMSCorrelationID();
            logger.info("Received Order Message: {}", correlationId);
            Destination replyToDestination = message.getJMSReplyTo();
            if (replyToDestination != null) {
                if (replyToDestination instanceof final Queue replyToQueue) {
                    var response = session.createTextMessage();
                    response.setText("Order Received: " + message.getJMSMessageID());
                    response.setJMSCorrelationID(correlationId);

                    if (producer == null) {
                        producer = session.createProducer(replyToQueue);
                        logger.warn("Created producer for queue: {}", replyToQueue.getQueueName());
                    }

                    producer.send(response);
                    logger.info("Sent acknowledgment for message ID: {}", correlationId);
                }
            }
        }
        catch (JMSException ex_) {
            logger.error("Failed to process message: {}", message, ex_);
        }

    }
}
