package com.kagr.videos.validator;


import java.net.URI;
import java.util.HashSet;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Data
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestDriver {
    private final HashSet<String> services = new HashSet<>();


    public void handleJmsEvent(String status, String service) {
        services.add(service);
        logger.warn("{}/{}", status, service);

        if (StringUtils.equals(Defaults.SESSION_CREATED, status)
            && StringUtils.equals(Defaults.ORDER_GENERATOR, service)) {
            logger.warn("SENDING ORDER TO: {}", service);
            sendOrderGeneratorStartCommand(service);
        }
    }



    private int sendOrderGeneratorStartCommand(final String serviceName) {
        String url = "http://" + serviceName + ":8080/orders/generate";
        logger.info("Sending start command to order generator: {}, url: {}", serviceName, url);
        try {
            RequestEntity<Void> request = RequestEntity.get(URI.create(url))
                                                       .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                                                       .build();
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            logger.info("Starting order sender: {}, response:{}", serviceName, response);
            if (response.getStatusCode().is2xxSuccessful()) {
                return 1;
            }
            else {
                logger.error("Failed to start generate order. Service: {}, response:{}", serviceName, response);
            }
        }
        catch (Exception e) {
            logger.error("Failed to generate order: {}, {}", serviceName, e.toString());
        }

        return 0;
    }


}
