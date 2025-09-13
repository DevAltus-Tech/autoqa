package com.kagr.videos.ordergenerator;





import jakarta.annotation.PostConstruct;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;





@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderProducer {
    private class OrderAkcListener implements javax.jms.MessageListener {
        @Override
        public void onMessage(final javax.jms.Message message) {
            try {
                logger.info("Received Order Acknowledgment: {}", message.getJMSCorrelationID());
            }
            catch (Exception e) {
                logger.error("Error processing acknowledgment message", e);
            }
        }
    }





    private final MessageProducer ordersMessageProducer;
    private final Session session;
    private Queue returnQ;
    private OrderAkcListener orderAkcListener;
    private int correlationId = 0;


    @PostConstruct
    public void init() {

        try {
            logger.info("OrderProducer initialized");
            returnQ = session.createTemporaryQueue();
            orderAkcListener = new OrderAkcListener();
            var consumer = session.createConsumer(returnQ);
            consumer.setMessageListener(orderAkcListener);
            logger.debug("Created temporary queue: {}", returnQ.getQueueName());
        }
        catch (Exception e) {
            logger.error("Error initializing OrderProducer", e);
        }
    }




    public void sendOrder(String orderDetails) {
        try {
            var msg = session.createTextMessage();
            msg.setText(orderDetails);

            if (returnQ == null) {
                logger.error("Return queue is not initialized");
                return;
            }

            msg.setJMSReplyTo(returnQ);
            msg.setJMSCorrelationID("" + correlationId++);
            ordersMessageProducer.send(msg);
            logger.info("Sent order message: {}", orderDetails);
        }
        catch (Exception e) {
            logger.error("Error sending order message", e);
        }
    }





    public void handleJmsEvent(String event, String name) {
        logger.warn("Received event: {} for name: {}", event, name);
    }


}
