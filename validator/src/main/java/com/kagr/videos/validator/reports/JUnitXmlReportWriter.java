package com.kagr.videos.validator.reports;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;


/**
 * Writes test results in JUnit XML format for compatibility with Azure DevOps
 * and other CI/CD pipeline test result publishers.
 *
 * JUnit XML format:
 * <testsuites name="..." tests="..." failures="..." errors="..." time="...">
 *   <testsuite name="..." tests="..." failures="..." errors="..." time="...">
 *     <testcase name="..." classname="..." time="...">
 *       <failure message="..." type="...">...</failure>  (optional)
 *     </testcase>
 *   </testsuite>
 * </testsuites>
 */
@Slf4j
@Component
public class JUnitXmlReportWriter {

    private static final String SUITE_NAME = "AutoQA Validator Tests";
    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault());

    /**
     * Generates a JUnit XML report string from the test results.
     *
     * @param completedTests Collection of completed test statuses
     * @param pendingTests   Collection of pending (timed-out/failed) test statuses
     * @param totalTimeMs    Total execution time in milliseconds
     * @return JUnit XML formatted string
     */
    public String generateXmlReport(
            @NonNull Collection<TestStatus> completedTests,
            @NonNull Collection<TestStatus> pendingTests,
            long totalTimeMs) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            // Create root <testsuites> element
            Element testSuitesElement = document.createElement("testsuites");
            document.appendChild(testSuitesElement);

            int totalTests = completedTests.size() + pendingTests.size();
            int failures = countFailures(completedTests) + pendingTests.size(); // pending = timeout = failure
            int errors = 0;
            double totalTimeSec = totalTimeMs / 1000.0;

            testSuitesElement.setAttribute("name", SUITE_NAME);
            testSuitesElement.setAttribute("tests", String.valueOf(totalTests));
            testSuitesElement.setAttribute("failures", String.valueOf(failures));
            testSuitesElement.setAttribute("errors", String.valueOf(errors));
            testSuitesElement.setAttribute("time", String.format("%.3f", totalTimeSec));

            // Create <testsuite> element
            Element testSuiteElement = document.createElement("testsuite");
            testSuitesElement.appendChild(testSuiteElement);

            testSuiteElement.setAttribute("name", SUITE_NAME);
            testSuiteElement.setAttribute("tests", String.valueOf(totalTests));
            testSuiteElement.setAttribute("failures", String.valueOf(failures));
            testSuiteElement.setAttribute("errors", String.valueOf(errors));
            testSuiteElement.setAttribute("skipped", "0");
            testSuiteElement.setAttribute("time", String.format("%.3f", totalTimeSec));
            testSuiteElement.setAttribute("timestamp", ISO_FORMATTER.format(Instant.now()));

            // Add completed test cases
            for (TestStatus test : completedTests) {
                Element testCaseElement = createTestCaseElement(document, test);
                testSuiteElement.appendChild(testCaseElement);
            }

            // Add pending (timed-out) tests as failures
            for (TestStatus test : pendingTests) {
                Element testCaseElement = createTestCaseElement(document, test);
                testSuiteElement.appendChild(testCaseElement);
            }

            // Convert to string
            return documentToString(document);

        } catch (Exception ex) {
            logger.error("Failed to generate JUnit XML report", ex);
            return createMinimalErrorReport(ex.getMessage());
        }
    }

    /**
     * Writes the JUnit XML report to a file.
     *
     * @param completedTests Collection of completed test statuses
     * @param pendingTests   Collection of pending (timed-out/failed) test statuses
     * @param totalTimeMs    Total execution time in milliseconds
     * @param filePath       Path to write the XML file
     * @return The file path written, or null if failed
     */
    public String writeXmlReport(
            @NonNull Collection<TestStatus> completedTests,
            @NonNull Collection<TestStatus> pendingTests,
            long totalTimeMs,
            @NonNull String filePath) {

        String xmlContent = generateXmlReport(completedTests, pendingTests, totalTimeMs);

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(xmlContent.getBytes());
            logger.info("JUnit XML report written to: {}", filePath);
            return filePath;
        } catch (Exception ex) {
            logger.error("Failed to write JUnit XML report to {}", filePath, ex);
            return null;
        }
    }

    private Element createTestCaseElement(Document document, TestStatus test) {
        Element testCaseElement = document.createElement("testcase");

        String testName = test.getTestName();
        String className = extractClassName(testName);

        testCaseElement.setAttribute("name", testName);
        testCaseElement.setAttribute("classname", className);
        testCaseElement.setAttribute("time", "0.000"); // Individual test time not tracked

        // Add failure element if test failed
        if (!"PASS".equalsIgnoreCase(test.getStatus())) {
            Element failureElement = document.createElement("failure");
            failureElement.setAttribute("message", test.getNotes() != null ? test.getNotes() : "Test failed");
            failureElement.setAttribute("type", determineFailureType(test));
            failureElement.setTextContent(buildFailureDescription(test));
            testCaseElement.appendChild(failureElement);
        }

        return testCaseElement;
    }

    private String extractClassName(String testName) {
        // Extract a meaningful class name from the test name
        // e.g., "heartbeat-connect" -> "com.autoqa.HeartbeatConnectTest"
        if (testName == null) {
            return "com.autoqa.UnknownTest";
        }

        String normalized = testName.replaceAll("[^a-zA-Z0-9]", " ")
                                    .trim()
                                    .replaceAll("\\s+", " ");

        StringBuilder className = new StringBuilder("com.autoqa.");
        String[] parts = normalized.split(" ");
        for (String part : parts) {
            if (!part.isEmpty()) {
                className.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    className.append(part.substring(1).toLowerCase());
                }
            }
        }
        className.append("Test");

        return className.toString();
    }

    private String determineFailureType(TestStatus test) {
        String notes = test.getNotes();
        if (notes != null && notes.toLowerCase().contains("timeout")) {
            return "TimeoutException";
        }
        return "AssertionError";
    }

    private String buildFailureDescription(TestStatus test) {
        StringBuilder sb = new StringBuilder();
        sb.append("Test: ").append(test.getTestName()).append("\n");
        sb.append("Status: ").append(test.getStatus()).append("\n");
        if (test.getNotes() != null && !test.getNotes().isEmpty()) {
            sb.append("Notes: ").append(test.getNotes()).append("\n");
        }
        return sb.toString();
    }

    private int countFailures(Collection<TestStatus> tests) {
        int count = 0;
        for (TestStatus test : tests) {
            if (!"PASS".equalsIgnoreCase(test.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private String documentToString(Document document) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }

    private String createMinimalErrorReport(String errorMessage) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<testsuites name=\"AutoQA Validator Tests\" tests=\"1\" failures=\"1\" errors=\"0\">\n" +
               "  <testsuite name=\"AutoQA Validator Tests\" tests=\"1\" failures=\"1\" errors=\"0\">\n" +
               "    <testcase name=\"ReportGeneration\" classname=\"com.autoqa.ReportGenerationTest\">\n" +
               "      <failure message=\"Failed to generate report\" type=\"ReportException\">" +
               errorMessage + "</failure>\n" +
               "    </testcase>\n" +
               "  </testsuite>\n" +
               "</testsuites>";
    }
}
