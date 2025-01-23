package com.kagr.videos.validator;





import com.kagr.videos.jms.monitor.ArtemisNotificationsListener;
import com.kagr.videos.validator.reports.ReportService;
import com.kagr.videos.validator.reports.TestStatus;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;





@Slf4j
@Data
@Configuration
@ConfigurationProperties
public class ValidorConfig {
    private final HashSet<BiConsumer<String, String>> jmsConsumers;

    @Value("${broker.url}")
    private String brokerAddress;

    @Value("${broker.username}")
    private String username;

    @Value("${broker.password}")
    private String password;

    @Value("${orders.topic}")
    private String ordersTopic;

    @Value("${heartbeat.topic}")
    private String heartbeatTopic;

    @Value("${orders.log}")
    private String ordersLog;

    @Value("${heartbeat.log}")
    private String heartbeatLog;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${validator.output.report.template}")
    private String reportTemplate;





    @Bean
    public Connection jmsConnection() throws javax.jms.JMSException {
        TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName());
        ActiveMQConnectionFactory connectionFactory = ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);
        connectionFactory.setBrokerURL(brokerAddress);
        connectionFactory.setUser(username);
        connectionFactory.setPassword(password);
        connectionFactory.setClientID(appName);
        logger.info("Creating ConnectionFactory for broker URL: {}, client-id:{}, username:{}", brokerAddress, appName, username);
        return connectionFactory.createConnection();
    }





    @Bean
    public Session jmsSession(Connection connection) throws JMSException {
        logger.info("Creating Session for connection: {}", connection);
        Session session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        return session;
    }





    @Bean
    public String ordersLog() {
        logger.debug("ordersLog:{}", ordersLog);
        return ordersLog;
    }





    @Bean
    public String heartbeatLog() {
        logger.debug("heartbeatLog:{}", heartbeatLog);
        return heartbeatLog;
    }





    @Bean
    public ConcurrentHashMap<String, TestStatus> pendingTests(
        @NonNull final TestConfig testConfig) {
        if (logger.isTraceEnabled()) {
            logger.trace("ValidorConfig::pendingTests");
        }

        logger.info("adding connect tests");
        ConcurrentHashMap<String, TestStatus> pendingTests = new ConcurrentHashMap<>();
        var connectList = testConfig.getJmsConnect();
        while (connectList.size() > 0) {
            var name = connectList.remove(0) + Defaults.DEFAULT_CONNECT;
            TestStatus testStatus = new TestStatus(name, "PENDING", "");
            logger.info("adding test:{}", testStatus);
            pendingTests.put(name, testStatus);
        }

        logger.info("adding heart-beat tests");
        var list = testConfig.getLogValidation().getHeartbeat();
        if (list != null) {
            while (list.size() > 0) {
                var name = list.remove(0);
                TestStatus testStatus = new TestStatus(name, "PENDING", "");
                logger.info("adding test:{}", testStatus);
                pendingTests.put(name, testStatus);
            }
        }
        else {
            logger.error("no heartbeat tests found");
        }

        logger.info("adding heart-beat tests");
        list = testConfig.getLogValidation().getOrders();
        if (list != null) {
            while (list.size() > 0) {
                var name = list.remove(0);
                TestStatus testStatus = new TestStatus(name, "PENDING", "");
                logger.info("adding test:{}", testStatus);
                pendingTests.put(name, testStatus);
            }
        }
        else {
            logger.error("no heartbeat tests found");
        }


        return pendingTests;
    }





    @Bean
    public Set<TestStatus> completedTests() {
        if (logger.isTraceEnabled()) {
            logger.trace("ValidorConfig::completedTests");
        }
        return new HashSet<>();
    }





    @Bean
    public TestCollector testCollector(@NonNull final ReportService reportService,
                                       @NonNull final ConcurrentHashMap<String, TestStatus> pendingTests) {
        var collector = new TestCollector(
            reportService,
            pendingTests,
            ordersLog,
            heartbeatLog);
        new Thread(collector).start();
        return collector;
    }





    @Bean
    public Set<BiConsumer<String, String>> consumers(@NonNull final TestCollector collector) {
        if (jmsConsumers == null) {
            logger.info("Creating empty set of consumers");
            return new HashSet<>();
        }
        jmsConsumers.add(collector::handleJmsEvent);
        return jmsConsumers;
    }





    @Bean
    public ArtemisNotificationsListener artemisNotificationsListener(@NonNull final Connection jmsConnection,
                                                                     @NonNull final Set<BiConsumer<String, String>> jmsEventConsumers) throws JMSException {
        logger.info("Creating ArtemisNotificationsListener for broker URL: {}", brokerAddress);
        return new ArtemisNotificationsListener(jmsConnection, jmsEventConsumers).startListening();
    }





    @Bean
    public VelocityEngine velocityEngine() {
        logger.warn("starting velocity engine");
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        engine.init();
        return engine;
    }





    @Bean
    public Template reportTemplate(VelocityEngine velocityEngine) {
        Template template = null;
        try {
            logger.warn("getting report template:{}", reportTemplate);
            template = velocityEngine.getTemplate(reportTemplate);
            return template;
        }
        catch (ResourceNotFoundException | ParseErrorException ex_) {
            logger.error("Could not find report template", ex_);
        }
        catch (Throwable ex_) {
            logger.error("Error getting report template", ex_);
        }

        return template;
    }

}
