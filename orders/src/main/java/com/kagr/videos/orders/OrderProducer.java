package com.kagr.videos.orders;





import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.MessageProducer;





@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderProducer
{
    private final MessageProducer ordersMessageProducer;





    @PostConstruct
    public void init()
    {
        logger.info("OrderProducer initialized");
    }


}
