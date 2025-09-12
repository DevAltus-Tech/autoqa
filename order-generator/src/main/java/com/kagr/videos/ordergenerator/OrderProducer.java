package com.kagr.videos.ordergenerator;





import jakarta.annotation.PostConstruct;
import javax.jms.MessageProducer;
import javax.jms.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;





@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderProducer {
    private final MessageProducer ordersMessageProducer;
    private final Session session;





    @PostConstruct
    public void init() {

        try {
            logger.info("OrderProducer initialized");
        }
        catch (Exception e) {
            logger.error("Error initializing OrderProducer", e);
        }
    }




    public void sendOrder(String orderDetails) {
        try {
            var msg = session.createTextMessage();
            msg.setText(orderDetails);
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
