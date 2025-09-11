package com.kagr.videos.orderclient;





import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;





@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderReceiver {




    @PostConstruct
    public void init() {
        logger.info("OrderProducer initialized");
    }





    public void handleJmsEvent(String event, String name) {
        logger.warn("Received event: {} for name: {}", event, name);
    }
}
