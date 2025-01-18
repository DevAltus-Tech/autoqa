package com.kagr.videos.heartbeat;





import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;





/**
 * This is the Spring Boot entry point for the application. It scans the entire com.kagr.videos
 * package to pull in all relevant beans.
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"com.kagr.videos.heartbeat"})
public class HeartbeatMain
{
    public static void main(String[] args)
    {
        SpringApplication app = new SpringApplication(HeartbeatMain.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }





    @PostConstruct
    public void init()
    {
        logger.info("HeartbeatMain initialized");
    }
}
