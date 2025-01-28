package com.kagr.videos.ordergenerator;





import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;





@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JmsEventHandler {





    public void onJmsEvent(final String event, final String service) {
        logger.error("Received JMS event: {} from service: {}", event, service);
    }
}
