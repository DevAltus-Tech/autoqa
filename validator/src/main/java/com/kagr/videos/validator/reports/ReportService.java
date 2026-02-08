package com.kagr.videos.validator.reports;


import com.kagr.videos.validator.TestCollector;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.List;


@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/report")
public class ReportService {

    private final VelocityEngine velocityEngine;
    private final Template reportTemplate;
    private final JUnitXmlReportWriter junitXmlReportWriter;
    //private final List<TestStatus> tests;
    private final List<String> logs;
    private final TestCollector testCollector;
    private final TestStatus globalStatus = new TestStatus("Global Status", "PENDING", "");

    private long testStartTime = System.currentTimeMillis();

    // Package-private getter for testing
    TestStatus getGlobalStatus() {
        return globalStatus;
    }


    @GetMapping("generate")
    public String generateReport() {
        VelocityContext context = new VelocityContext();
        logs.clear();
        logs.addAll(testCollector.getRelevantLogs());

        //logger.info("generating report for {} tests", tests.size());
        context.put("globalStatus", globalStatus);
        context.put("pendingTests", testCollector.getPendingTests().values());
        context.put("completedTests", testCollector.getCompletedTests().values());
        context.put("logs", logs);


        StringWriter writer = new StringWriter();
        reportTemplate.merge(context, writer);
        return writer.toString();
    }


    @PostMapping("write")
    public String writeReport(@NonNull String filePostpend) {
        String report = generateReport();
        final var fname = "/app/var/reports/report-" + filePostpend + ".html";
        logger.info("writing report to file, file: {}", fname);
        try (FileOutputStream fos = new FileOutputStream(fname)) {
            fos.write(report.getBytes());
        }
        catch (Exception e) {
            logger.error("Error writing report", e);
        }

        return fname;
    }


    public void updateGlobalStatus(boolean pass, String notes) {
        globalStatus.setNotes(notes);
        if (pass) {
            globalStatus.setStatus("PASS");
        }
        else {
            globalStatus.setStatus("FAIL");
        }
    }


    /**
     * Generates JUnit XML report and returns it as a string.
     * This format is compatible with Azure DevOps test result publishing.
     */
    @GetMapping(value = "junit", produces = MediaType.APPLICATION_XML_VALUE)
    public String generateJUnitXmlReport() {
        long elapsedTimeMs = System.currentTimeMillis() - testStartTime;
        return junitXmlReportWriter.generateXmlReport(
                testCollector.getCompletedTests().values(),
                testCollector.getPendingTests().values(),
                elapsedTimeMs
        );
    }


    /**
     * Writes JUnit XML report to a file.
     * Returns the path to the written file.
     */
    @PostMapping("write-junit")
    public String writeJUnitReport(@NonNull String filePostpend) {
        long elapsedTimeMs = System.currentTimeMillis() - testStartTime;
        final var fname = "/app/var/reports/test-results-" + filePostpend + ".xml";
        logger.info("writing JUnit XML report to file: {}", fname);

        String result = junitXmlReportWriter.writeXmlReport(
                testCollector.getCompletedTests().values(),
                testCollector.getPendingTests().values(),
                elapsedTimeMs,
                fname
        );

        return result != null ? result : "Failed to write JUnit XML report";
    }


    /**
     * Writes both HTML and JUnit XML reports.
     * Returns a summary of written files.
     */
    @PostMapping("write-all")
    public String writeAllReports(@NonNull String filePostpend) {
        String htmlReport = writeReport(filePostpend);
        String junitReport = writeJUnitReport(filePostpend);

        return String.format("HTML: %s, JUnit XML: %s", htmlReport, junitReport);
    }


    /**
     * Resets the test start time. Call this when tests begin.
     */
    public void resetTestStartTime() {
        this.testStartTime = System.currentTimeMillis();
    }
}
