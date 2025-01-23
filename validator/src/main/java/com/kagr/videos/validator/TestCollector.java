package com.kagr.videos.validator;





import com.kagr.videos.validator.logs.DockerLogsReader;
import com.kagr.videos.validator.reports.ReportService;
import com.kagr.videos.validator.reports.TestStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;





@Data
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestCollector implements Runnable {

    private final ReportService reportService;
    private final ConcurrentHashMap<String, TestStatus> pendingTests;
    private final String ordersLog;
    private final String heartbeatLog;

    private final HashMap<String, DockerLogsReader> logReaders = new HashMap<>();
    private final LinkedBlockingQueue<String> logQueue = new LinkedBlockingQueue<>();





    public void handleJmsEvent(String status, String service) {
        logger.warn("{}/{}", status, service);

        if (StringUtils.equals(Defaults.CONSUMER_CREATED, status)) {
            var name = service + Defaults.DEFAULT_CONNECT;
            var actual = pendingTests.get(name);
            if (actual != null) {
                logger.info("Test {} completed", name);
                actual.setStatus("PASS");
                actual.setNotes("Consumer created successfully");
                //reportService.addTest(actual);
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
                    //logger.info("log: {}", log);
                    for (var entry : pendingTests.entrySet()) {
                        var test = entry.getValue();
                        if (StringUtils.contains(log, test.getTestName())) {
                            test.setStatus("PASS");
                            test.setNotes("Log entry found");
                            //reportService.addTest(test);
                            //pendingTests.remove(entry.getKey());
                            logger.info("Test completed: {}", test);
                        }
                        else if (Pattern.matches(test.getTestName(), log)) {
                            test.setStatus("PASS");
                            test.setNotes("Log entry found, regex");
                            //reportService.addTest(test);
                            //pendingTests.remove(entry.getKey());
                            logger.info("Test completed: {}", test);
                        }
                        else {
                            if (logger.isTraceEnabled()) {
                                logger.trace("{} does not match:{}", test.getTestName(), log);
                            }
                        }
                    }
                }
            }
            catch (InterruptedException e) {
                logger.error(e.toString(), e);
                Thread.currentThread().interrupt();
            }
        }
    }


}
