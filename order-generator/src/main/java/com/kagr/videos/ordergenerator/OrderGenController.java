package com.kagr.videos.ordergenerator;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderGenController {
    private final OrderProducer orderProducer;


    @GetMapping("/generate")
    public void generateOrder() {
        orderProducer.sendOrder("OrderID:" + System.currentTimeMillis());
    }
}
