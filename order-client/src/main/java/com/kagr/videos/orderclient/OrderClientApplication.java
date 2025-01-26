package com.kagr.videos.orderclient;





import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;





/**
 * This is the Spring Boot entry point for the application. It scans the entire com.kagr.videos
 * package to pull in all relevant beans.
 */
@SpringBootApplication(scanBasePackages = {"com.kagr.videos.orderclient"})
public class OrderClientApplication
{
    public static void main(String[] args)
    {
        SpringApplication app = new SpringApplication(OrderClientApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
