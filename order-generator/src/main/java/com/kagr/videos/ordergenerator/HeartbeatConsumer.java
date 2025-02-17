package com.kagr.videos.ordergenerator;





import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;





@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HeartbeatConsumer implements MessageListener {



    @Override
    public void onMessage(final Message message) {
        try {
            if (message instanceof final TextMessage textMessage) {
                String text = textMessage.getText();
                logger.info("Received JMS event: {}", text);
            }
            else {
                logger.error("Received non-text JMS message");
            }
        }
        catch (JMSException e) {
            logger.error("Error processing JMS message", e);
        }
    }



}
