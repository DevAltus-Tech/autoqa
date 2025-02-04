package com.kagr.videos.validator.reports;





import com.kagr.videos.validator.TestCollector;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
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
    //private final List<TestStatus> tests;
    private final List<String> logs;
    private final TestCollector testCollector;
    private final TestStatus globalStatus = new TestStatus("Global Status", "PENDING", "");





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
}
