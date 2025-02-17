package com.kagr.vidoes.configgateway;





import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.UUID;





@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PropertyPublisher {
    private final Connection jmsConnection;

    @Value("${config-gateway.topic}")
    private String topic;





    @PostConstruct
    public void publishProperties() {
        try {
            int id = 7;
            logger.info("Publishing properties to the broker, topic {}", topic);
            Session session = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer tpc = session.createProducer(session.createTopic(topic));
            TextMessage msg1 = session.createTextMessage();
            msg1.setStringProperty("appName", "config-gateway");
            msg1.setStringProperty("filter1", "A" + id);
            msg1.setStringProperty("filter2", "B" + id);
            msg1.setStringProperty("key", "property-1");
            msg1.setJMSMessageID("ID:" + UUID.randomUUID().toString());
            msg1.setText("value6");
            logger.info("Sending message: {}", msg1);
            tpc.send(msg1);


            TextMessage msg2 = session.createTextMessage();
            msg2.setStringProperty("appName", "config-gateway");
            msg2.setStringProperty("filter1", "C" + id);
            msg2.setStringProperty("filter2", "D" + id);
            msg2.setStringProperty("key", "property-2");
            msg1.setJMSMessageID("ID:" + UUID.randomUUID().toString());
            msg2.setText("");
            logger.info("Sending message: {}", msg2);
            tpc.send(msg2);


            session.close();
        }
        catch (JMSException ex) {
            logger.error(ex.toString(), ex);

        }

    }

}
