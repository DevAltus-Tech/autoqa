package com.kagr.videos.validator;





import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;





@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "tests")
public class TestConfig {
    private List<String> jmsConnect;
    private LogValidation logValidation;
    private Termination terminationProperties;





    @Data
    public static class LogValidation {
        private List<String> heartbeat;
        private List<String> orders;
    }





    @Data
    public static class Termination {
        private String timeout;
        private boolean shutdownOnSuccess;
    }
}
