package com.kagr.videos.validator.reports;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class JUnitXmlReportWriterTest {

    private JUnitXmlReportWriter writer;

    @BeforeEach
    void setUp() {
        writer = new JUnitXmlReportWriter();
    }


    @Test
    void testGenerateXmlReport_AllTestsPassed() {
        List<TestStatus> completed = Arrays.asList(
                new TestStatus("heartbeat-connect", "PASS", "Consumer created successfully"),
                new TestStatus("order-generator-connect", "PASS", "Consumer created successfully")
        );
        List<TestStatus> pending = Collections.emptyList();

        String xml = writer.generateXmlReport(completed, pending, 5000);

        assertNotNull(xml);
        assertTrue(xml.contains("<?xml version=\"1.0\""));
        assertTrue(xml.contains("<testsuites"));
        assertTrue(xml.contains("name=\"AutoQA Validator Tests\""));
        assertTrue(xml.contains("tests=\"2\""));
        assertTrue(xml.contains("failures=\"0\""));
        assertTrue(xml.contains("<testsuite"));
        assertTrue(xml.contains("<testcase"));
        assertTrue(xml.contains("heartbeat-connect"));
        assertTrue(xml.contains("order-generator-connect"));
        assertFalse(xml.contains("<failure"));
    }


    @Test
    void testGenerateXmlReport_WithFailures() {
        List<TestStatus> completed = Arrays.asList(
                new TestStatus("heartbeat-connect", "PASS", "Consumer created successfully"),
                new TestStatus("order-test-1", "FAIL", "Log entry not found")
        );
        List<TestStatus> pending = Collections.emptyList();

        String xml = writer.generateXmlReport(completed, pending, 5000);

        assertNotNull(xml);
        assertTrue(xml.contains("tests=\"2\""));
        assertTrue(xml.contains("failures=\"1\""));
        assertTrue(xml.contains("<failure"));
        assertTrue(xml.contains("Log entry not found"));
    }


    @Test
    void testGenerateXmlReport_WithPendingTestsAreFailures() {
        List<TestStatus> completed = Arrays.asList(
                new TestStatus("heartbeat-connect", "PASS", "Consumer created successfully")
        );
        List<TestStatus> pending = Arrays.asList(
                new TestStatus("order-generator-connect", "PENDING", "Timeout")
        );

        String xml = writer.generateXmlReport(completed, pending, 15000);

        assertNotNull(xml);
        assertTrue(xml.contains("tests=\"2\""));
        assertTrue(xml.contains("failures=\"1\"")); // pending counts as failure
        assertTrue(xml.contains("<failure"));
        assertTrue(xml.contains("Timeout"));
        assertTrue(xml.contains("TimeoutException"));
    }


    @Test
    void testGenerateXmlReport_EmptyTests() {
        List<TestStatus> completed = Collections.emptyList();
        List<TestStatus> pending = Collections.emptyList();

        String xml = writer.generateXmlReport(completed, pending, 0);

        assertNotNull(xml);
        assertTrue(xml.contains("tests=\"0\""));
        assertTrue(xml.contains("failures=\"0\""));
    }


    @Test
    void testWriteXmlReport_WritesToFile(@TempDir Path tempDir) throws Exception {
        List<TestStatus> completed = Arrays.asList(
                new TestStatus("test-1", "PASS", "OK"),
                new TestStatus("test-2", "FAIL", "Error")
        );
        List<TestStatus> pending = Collections.emptyList();

        String filePath = tempDir.resolve("test-results.xml").toString();
        String result = writer.writeXmlReport(completed, pending, 1000, filePath);

        assertEquals(filePath, result);

        File file = new File(filePath);
        assertTrue(file.exists());

        String content = Files.readString(file.toPath());
        assertTrue(content.contains("<testsuites"));
        assertTrue(content.contains("test-1"));
        assertTrue(content.contains("test-2"));
    }


    @Test
    void testGenerateXmlReport_ClassNameExtraction() {
        List<TestStatus> completed = Arrays.asList(
                new TestStatus("heartbeat-connect", "PASS", "OK")
        );
        List<TestStatus> pending = Collections.emptyList();

        String xml = writer.generateXmlReport(completed, pending, 1000);

        // Should extract a class name like "com.autoqa.HeartbeatConnectTest"
        assertTrue(xml.contains("classname=\"com.autoqa."));
        assertTrue(xml.contains("Test\""));
    }


    @Test
    void testGenerateXmlReport_TimeFormatting() {
        List<TestStatus> completed = Arrays.asList(
                new TestStatus("test-1", "PASS", "OK")
        );
        List<TestStatus> pending = Collections.emptyList();

        String xml = writer.generateXmlReport(completed, pending, 5500);

        // 5500ms = 5.5 seconds, should be formatted as 5.500
        assertTrue(xml.contains("time=\"5.500\""));
    }


    @Test
    void testGenerateXmlReport_ContainsTimestamp() {
        List<TestStatus> completed = Arrays.asList(
                new TestStatus("test-1", "PASS", "OK")
        );
        List<TestStatus> pending = Collections.emptyList();

        String xml = writer.generateXmlReport(completed, pending, 1000);

        assertTrue(xml.contains("timestamp=\""));
    }


    @Test
    void testWriteXmlReport_InvalidPath_ReturnsNull(@TempDir Path tempDir) {
        List<TestStatus> completed = Arrays.asList(
                new TestStatus("test-1", "PASS", "OK")
        );
        List<TestStatus> pending = Collections.emptyList();

        // Invalid path (directory that doesn't exist)
        String invalidPath = "/nonexistent/path/to/file.xml";
        String result = writer.writeXmlReport(completed, pending, 1000, invalidPath);

        assertNull(result);
    }
}
