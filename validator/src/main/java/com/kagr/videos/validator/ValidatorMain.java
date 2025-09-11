package com.kagr.videos.validator;





import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.Random;





@Slf4j
@SpringBootApplication(scanBasePackages = {"com.kagr.videos.validator"})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@EnableConfigurationProperties({TestConfig.class, ValidatorConfig.class})
public class ValidatorMain {

    public final Template reportTemplate;
    private final Random random = new Random();





    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ValidatorMain.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }





    @PostConstruct
    public void generateReport() {
        VelocityContext context = new VelocityContext();

        // Data Section
        context.put("orderEngineClass", getStatusClass());
        context.put("orderEngineSymbol", getStatusSymbol());
        context.put("orderFillerClass", getStatusClass());
        context.put("orderFillerSymbol", getStatusSymbol());
        context.put("heartbeatClass", getStatusClass());
        context.put("heartbeatSymbol", getStatusSymbol());

        // Graceful Startup Section
        context.put("startupOrderEngineClass", getStatusClass());
        context.put("startupOrderEngineSymbol", getStatusSymbol());
        context.put("startupOrderFillerClass", getStatusClass());
        context.put("startupOrderFillerSymbol", getStatusSymbol());
        context.put("startupHeartbeatClass", getStatusClass());
        context.put("startupHeartbeatSymbol", getStatusSymbol());

        // Execution Section
        context.put("executionOrderEngineClass", getStatusClass());
        context.put("executionOrderEngineSymbol", getStatusSymbol());
        context.put("executionOrderFillerClass", getStatusClass());
        context.put("executionOrderFillerSymbol", getStatusSymbol());
        context.put("executionHeartbeatClass", getStatusClass());
        context.put("executionHeartbeatSymbol", getStatusSymbol());

        // Graceful Shutdown Section
        context.put("shutdownOrderEngineClass", getStatusClass());
        context.put("shutdownOrderEngineSymbol", getStatusSymbol());
        context.put("shutdownOrderFillerClass", getStatusClass());
        context.put("shutdownOrderFillerSymbol", getStatusSymbol());
        context.put("shutdownHeartbeatClass", getStatusClass());
        context.put("shutdownHeartbeatSymbol", getStatusSymbol());

        StringWriter writer = new StringWriter();
        reportTemplate.merge(context, writer);


        try (FileOutputStream fileOutputStream = new FileOutputStream("/app/var/log/report.html")) {
            fileOutputStream.write(writer.toString().getBytes());
        }
        catch (Exception e) {
            logger.error("Error writing report", e);
        }
    }





    private String getStatusClass() {
        return random.nextBoolean() ? "success" : "failure";
    }





    private String getStatusSymbol() {
        return random.nextBoolean() ? "&#10004;" : "&#10008;";
    }
}
