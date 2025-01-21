package com.kagr.videos.validator.reports;





import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.StringWriter;
import java.util.List;





@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/report")
public class ReportService {

    private final VelocityEngine velocityEngine;
    private final Template reportTemplate;
    private final List<TestStatus> tests;





    @GetMapping("generate")
    public String generateReport() {
        VelocityContext context = new VelocityContext();
        logger.info("generating report for {} tests", tests.size());
        for (var test : tests) {
            context.put(test.getTestName() + "-name", test.getTestName());
            context.put(test.getTestName() + "-status", test.getStatus());
            context.put(test.getTestName() + "-notes", test.getNotes());
        }

        StringWriter writer = new StringWriter();
        reportTemplate.merge(context, writer);
        return writer.toString();
    }





    public void addTest(@NonNull final TestStatus test) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding test: {}", test);
        }
        tests.add(test);
    }
}
