package com.kagr.videos.validator;





import com.kagr.videos.validator.reports.ReportService;
import com.kagr.videos.validator.reports.TestStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Set;





@Data
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestCollector {

    private final ReportService reportService;

    private final HashMap<String, TestStatus> pendingTests;





    public void handleJmsEvent(String status, String service) {
        logger.warn("{}/{}", status, service);

        if (StringUtils.equals(Defaults.CONSUMER_CREATED, status)) {
            var name = service + Defaults.DEFAULT_CONNECT;
            var actual = pendingTests.remove(name);
            if (actual != null) {
                logger.info("Test {} completed", name);
                actual.setStatus("PASS");
                actual.setNotes("Consumer created successfully");
                reportService.addTest(actual);
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





    public void collectTests() {
        // Collect tests
    }
}
