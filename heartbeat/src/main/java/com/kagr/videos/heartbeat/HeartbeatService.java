package com.kagr.videos.heartbeat;





import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.concurrent.atomic.AtomicLong;





@Slf4j
@Service
public class HeartbeatService {

    private final MessageProducer messageProducer;
    private final Session session;
    private final AtomicLong sequenceNumber = new AtomicLong(0);





    @Autowired
    public HeartbeatService(MessageProducer messageProducer, Session session) {
        this.messageProducer = messageProducer;
        this.session = session;
    }





    @Scheduled(fixedRate = 1000)
    public void sendHeartbeat() {
        long seqNum = sequenceNumber.incrementAndGet();
        String heartbeatMessage = "Heartbeat sequence: " + seqNum;
        try {
            TextMessage message = session.createTextMessage(heartbeatMessage);
            messageProducer.send(message);
            logger.info("Sent heartbeat message: {}", heartbeatMessage);
        }
        catch (JMSException e) {
            logger.error("Failed to send heartbeat message", e);
        }
    }
}
