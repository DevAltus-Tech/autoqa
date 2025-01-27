package com.kagr.videos.validator;





import com.kagr.videos.validator.logs.DockerLogsReader;
import com.kagr.videos.validator.reports.TestStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;





@Data
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestCollector implements Runnable {

    private final ConcurrentHashMap<String, TestStatus> pendingTests;
    private final ConcurrentHashMap<String, TestStatus> completedTests;
    private final RestTemplate restTemplate;
    private final String ordersLog;
    private final String heartbeatLog;

    private final HashMap<String, DockerLogsReader> logReaders = new HashMap<>();
    private final LinkedBlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<String> relevantLogs = new LinkedBlockingQueue<>();





    public void handleJmsEvent(String status, String service) {
        logger.warn("{}/{}", status, service);

        if (StringUtils.equals(Defaults.CONSUMER_CREATED, status)) {
            var name = service + Defaults.DEFAULT_CONNECT;
            var actual = pendingTests.remove(name);
            if (actual != null) {
                logger.info("Test {} completed", name);
                actual.setStatus("PASS");
                actual.setNotes("Consumer created successfully");
                completedTests.put(name, actual);
                startLogListener(service);
            }
        }
        else {
            logger.warn("Ignoring event:{}/{}", status, service);
        }


        logger.info("total pending tests: {}", pendingTests.size());
        pendingTests.forEach((k, v) -> {
            logger.info("...Pending test: {}", v);
        });

    }





    private void startLogListener(final String name) {
        if (logger.isTraceEnabled()) {
            logger.trace("Starting log listener for: {}", name);
        }
        if (StringUtils.equalsIgnoreCase(name, Defaults.HEARTBEAT)) {
            startLogReader(heartbeatLog);
        }
        else if (StringUtils.equalsIgnoreCase(name, Defaults.ORDERS)) {
            startLogReader(ordersLog);
        }
        else {
            logger.error("Unknown service: {}", name);
        }
    }





    private void startLogReader(String name) {
        logger.info("starting log reader for: {}", name);
        try (var logReader = new DockerLogsReader(name, logQueue)) {
            new Thread(logReader).start();
            logReaders.put(name, logReader);
            logger.info("log reader started for: {}", name);
        }
        catch (Exception ex) {
            logger.error(ex.toString(), ex);
        }
    }





    @Override
    public void run() {
        while (true) {
            try {
                var log = logQueue.take();
                synchronized (pendingTests) {
                    for (var entry : pendingTests.entrySet()) {
                        var test = entry.getValue();
                        if (StringUtils.contains(log, test.getTestName())) {
                            test.setStatus("PASS");
                            test.setNotes("Log entry found");
                            pendingTests.remove(entry.getKey());
                            completedTests.put(entry.getKey(), test);
                            relevantLogs.add(log);
                            logger.info("Test completed, exact match: {}", test);
                        }
                        else if (Pattern.matches(test.getTestName(), log)) {
                            test.setStatus("PASS");
                            test.setNotes("Log entry found, regex");
                            pendingTests.remove(entry.getKey());
                            completedTests.put(entry.getKey(), test);
                            relevantLogs.add(log);
                            logger.info("Test completed, regex: {}", test);
                        }
                        else {
                            if (logger.isTraceEnabled()) {
                                logger.trace("{} does not match:{}", test.getTestName(), log);
                            }
                        }
                    }
                }

                checkForAndPerformTermination();
            }
            catch (InterruptedException e) {
                logger.error(e.toString(), e);
                Thread.currentThread().interrupt();
            }
        }
    }





    private void checkForAndPerformTermination() {
        if (pendingTests.isEmpty()) {
            logger.warn("All tests completed, terminating log readers");
            logReaders.forEach((k, v) -> {
                logger.info("shutting down log reader: {}", k);
                v.setShouldContinue(false);
            });
            logReaders.clear();
            logger.error("SHUTDOWN SHUTDOWN SHUTDOWN");
            int shutdownCount = sendShutdownCommand("order-generator");
            shutdownCount += sendShutdownCommand("heartbeat");
            logger.error("SHUTDOWN SHUTDOWN SHUTDOWN");
            if (shutdownCount == 2) {
                logger.error("SHUTDOWN SHUTDOWN SHUTDOWN");
                System.exit(0);
            }
        }
    }





    private int sendShutdownCommand(final String serviceName) {
        String url = "http://" + serviceName + ":8080/actuator/shutdown";

        try {
            RequestEntity<Void> request =
                RequestEntity.post(URI.create(url))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            logger.info("Shutdown command sent for service: {}, response:{}", serviceName, response);
            if (response.getStatusCode().is2xxSuccessful()) {
                return 1;
            }
            else {
                logger.error("Failed to send shutdown command for service: {}, response:{}", serviceName, response);
            }
        }
        catch (Exception e) {
            logger.error("Failed to send shutdown command for service: {}", serviceName, e);
        }

        return 0;
    }





    @Scheduled(fixedDelayString = "${tests.termination.timeout}", initialDelayString = "${tests.termination.timeout}")
    public void performPostTimeoutActions() {
        logger.warn("Performing actions after termination timeout");
        int shutdownCount = sendShutdownCommand(Defaults.ORDER_GENERATOR);
        shutdownCount += sendShutdownCommand(Defaults.HEARTBEAT);
        for (var entry : pendingTests.entrySet()) {
            logger.error("Service shutdown successfull, marking test as failed: {}", entry.getKey());
            var test = entry.getValue();
            test.setStatus("FAIL");
            test.setNotes("Timeout");
            completedTests.put(entry.getKey(), test);
        }

        checkForAndPerformTermination();
    }

}
