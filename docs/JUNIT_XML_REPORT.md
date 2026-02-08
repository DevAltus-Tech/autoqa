# JUnit XML Report Generation

## Overview

The AutoQA Validator now supports generating test results in the standard **JUnit XML format**, making it fully compatible with CI/CD pipelines like Azure DevOps, Jenkins, GitLab CI, and other systems that consume JUnit test reports.

This feature allows the AutoQA test results to be:
- Published to Azure DevOps Test Plans
- Displayed in pipeline test result dashboards
- Tracked for test trends and analytics
- Integrated with the `azure-pipeline-auto-regression` HTML report generator

## How It Works

When tests complete (either successfully or via timeout), the validator automatically generates two report files:

1. **HTML Report** (`report-<timestamp>.html`) - Human-readable report for viewing in a browser
2. **JUnit XML Report** (`test-results-<timestamp>.xml`) - Machine-readable report for CI/CD integration

Both reports are written to `/app/var/reports/` inside the Docker container.

## JUnit XML Format

The generated XML follows the standard JUnit XML schema:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuites name="AutoQA Validator Tests" tests="5" failures="1" errors="0" time="15.000">
  <testsuite name="AutoQA Validator Tests" tests="5" failures="1" errors="0" skipped="0" 
             time="15.000" timestamp="2026-02-08T10:30:00-05:00">
    
    <!-- Passed Test -->
    <testcase name="heartbeat-connect" classname="com.autoqa.HeartbeatConnectTest" time="0.000"/>
    
    <!-- Passed Test -->
    <testcase name="order-generator-connect" classname="com.autoqa.OrderGeneratorConnectTest" time="0.000"/>
    
    <!-- Failed Test -->
    <testcase name="order-validation-test" classname="com.autoqa.OrderValidationTestTest" time="0.000">
      <failure message="Timeout" type="TimeoutException">
        Test: order-validation-test
        Status: FAIL
        Notes: Timeout
      </failure>
    </testcase>
    
  </testsuite>
</testsuites>
```

### XML Elements

| Element | Description |
|---------|-------------|
| `<testsuites>` | Root element containing all test suites |
| `<testsuite>` | Container for related test cases |
| `<testcase>` | Individual test result |
| `<failure>` | Present only for failed tests, contains failure details |

### Attributes

| Attribute | Description |
|-----------|-------------|
| `name` | Test or suite name |
| `tests` | Total number of tests |
| `failures` | Number of failed tests |
| `errors` | Number of tests with errors (always 0 in current implementation) |
| `skipped` | Number of skipped tests (always 0 in current implementation) |
| `time` | Execution time in seconds |
| `timestamp` | ISO 8601 timestamp when report was generated |
| `classname` | Auto-generated class name for the test |

## REST API Endpoints

The validator exposes several REST endpoints for report generation:

### GET `/report/junit`

Returns the JUnit XML report as a string response.

**Response:** `application/xml`

```bash
curl http://validator:8080/report/junit
```

### POST `/report/write-junit`

Writes the JUnit XML report to a file.

**Parameters:**
- `filePostpend` (required) - Suffix for the filename (typically a timestamp)

**Response:** Path to the written file

```bash
curl -X POST -d "filePostpend=12345" http://validator:8080/report/write-junit
# Returns: /app/var/reports/test-results-12345.xml
```

### POST `/report/write-all`

Writes both HTML and JUnit XML reports.

**Parameters:**
- `filePostpend` (required) - Suffix for the filenames

**Response:** Summary of written files

```bash
curl -X POST -d "filePostpend=12345" http://validator:8080/report/write-all
# Returns: HTML: /app/var/reports/report-12345.html, JUnit XML: /app/var/reports/test-results-12345.xml
```

### GET `/report/generate`

Returns the HTML report (unchanged from before).

## Test Status Mapping

| AutoQA Status | JUnit Result |
|---------------|--------------|
| `PASS` | Passed (no `<failure>` element) |
| `FAIL` | Failed (includes `<failure>` element with `AssertionError` type) |
| `PENDING` | Failed (includes `<failure>` element with `TimeoutException` type) |

> **Note:** Tests that are still `PENDING` when the report is generated are treated as failures due to timeout.

## Azure DevOps Integration

### Pipeline Configuration

To publish the JUnit XML results to Azure DevOps, add the `PublishTestResults` task to your pipeline:

```yaml
- task: PublishTestResults@2
  displayName: 'Publish AutoQA Test Results'
  inputs:
    testResultsFormat: 'JUnit'
    testResultsFiles: '**/test-results-*.xml'
    searchFolder: '$(System.DefaultWorkingDirectory)/reports'
    mergeTestResults: true
    failTaskOnFailedTests: true
    testRunTitle: 'AutoQA Validation Tests'
  condition: always()
```

### Docker Compose Volume Mount

Ensure the reports directory is mounted as a volume in your `docker-compose.yaml`:

```yaml
services:
  validator:
    image: autoqa/validator:latest
    volumes:
      - ./reports:/app/var/reports
    # ... other configuration
```

### Retrieving Reports After Test Run

After the test run completes, the reports will be available in the mounted volume:

```bash
# List generated reports
ls -la ./reports/

# Expected output:
# report-1707401400000.html
# test-results-1707401400000.xml
```

## Integration with azure-pipeline-auto-regression

The generated JUnit XML report is fully compatible with the `azure-pipeline-auto-regression` project's `TestResultsParser`. You can use it to generate enhanced HTML reports:

```java
import com.devaltus.autoqa.azdo.report.TestResultsParser;
import com.devaltus.autoqa.azdo.report.HtmlReportGenerator;
import com.devaltus.autoqa.azdo.report.TestSuites;

// Parse the AutoQA JUnit XML report
TestResultsParser parser = new TestResultsParser();
TestSuites testSuites = parser.parse("/path/to/test-results-12345.xml");

// Generate enhanced HTML report
HtmlReportGenerator generator = new HtmlReportGenerator();
generator.generate(testSuites, "/path/to/output/enhanced-report.html");
```

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     AutoQA Validator                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────┐    ┌──────────────────────────────┐   │
│  │  TestCollector  │───▶│      ReportService           │   │
│  │                 │    │                              │   │
│  │ - pendingTests  │    │  GET  /report/generate       │   │
│  │ - completedTests│    │  GET  /report/junit          │   │
│  └────────┬────────┘    │  POST /report/write          │   │
│           │             │  POST /report/write-junit    │   │
│           │             │  POST /report/write-all      │   │
│           │             └──────────────┬───────────────┘   │
│           │                            │                   │
│           │             ┌──────────────┴───────────────┐   │
│           │             │                              │   │
│           ▼             ▼                              ▼   │
│  ┌─────────────────────────────┐  ┌────────────────────┐   │
│  │    Velocity Template        │  │ JUnitXmlReportWriter│  │
│  │    (HTML Report)            │  │  (XML Report)       │  │
│  └─────────────┬───────────────┘  └──────────┬─────────┘   │
│                │                             │             │
│                ▼                             ▼             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              /app/var/reports/                       │   │
│  │                                                      │   │
│  │   report-<timestamp>.html   test-results-<ts>.xml   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────────┐
              │     Azure DevOps Pipeline     │
              │                               │
              │  PublishTestResults@2 Task    │
              │  - Parses JUnit XML           │
              │  - Displays in Test Plans     │
              │  - Tracks test trends         │
              └───────────────────────────────┘
```

## Example Output

### Successful Test Run

```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuites name="AutoQA Validator Tests" tests="10" failures="0" errors="0" time="12.345">
  <testsuite name="AutoQA Validator Tests" tests="10" failures="0" errors="0" skipped="0" 
             time="12.345" timestamp="2026-02-08T10:30:00-05:00">
    <testcase name="order-generator-connect" classname="com.autoqa.OrderGeneratorConnectTest" time="0.000"/>
    <testcase name="heartbeat-connect" classname="com.autoqa.HeartbeatConnectTest" time="0.000"/>
    <testcase name="order-client-connect" classname="com.autoqa.OrderClientConnectTest" time="0.000"/>
    <testcase name="HEARTBEAT .* Sent heartbeat message.*" classname="com.autoqa.HeartbeatSentHeartbeatMessageTest" time="0.000"/>
    <testcase name="ORDR_GEN .* Heartbeat sequence: 10.*" classname="com.autoqa.OrdrGenHeartbeatSequence10Test" time="0.000"/>
    <!-- ... more test cases ... -->
  </testsuite>
</testsuites>
```

### Failed Test Run

```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuites name="AutoQA Validator Tests" tests="10" failures="2" errors="0" time="15.000">
  <testsuite name="AutoQA Validator Tests" tests="10" failures="2" errors="0" skipped="0" 
             time="15.000" timestamp="2026-02-08T10:30:00-05:00">
    <testcase name="order-generator-connect" classname="com.autoqa.OrderGeneratorConnectTest" time="0.000"/>
    <testcase name="heartbeat-connect" classname="com.autoqa.HeartbeatConnectTest" time="0.000"/>
    <testcase name="order-client-connect" classname="com.autoqa.OrderClientConnectTest" time="0.000">
      <failure message="Timeout" type="TimeoutException">
        Test: order-client-connect
        Status: PENDING
        Notes: Timeout
      </failure>
    </testcase>
    <testcase name="ORDR_GEN .* Received Order Acknowledgment: 12345.*" classname="com.autoqa.OrdrGenReceivedOrderAcknowledgment12345Test" time="0.000">
      <failure message="Log entry not found" type="AssertionError">
        Test: ORDR_GEN .* Received Order Acknowledgment: 12345.*
        Status: FAIL
        Notes: Log entry not found
      </failure>
    </testcase>
    <!-- ... more test cases ... -->
  </testsuite>
</testsuites>
```

## Troubleshooting

### Reports Not Generated

1. Check that the `/app/var/reports` directory exists and is writable
2. Verify the validator service is running and healthy
3. Check validator logs for errors during report generation

### Azure DevOps Not Showing Results

1. Verify the `testResultsFiles` pattern matches your XML files
2. Ensure the `searchFolder` points to the correct directory
3. Check that `condition: always()` is set to publish results even on failure

### XML Validation Errors

The generated XML conforms to the JUnit XML schema. If you encounter validation issues:

1. Check for special characters in test names that may need escaping
2. Verify the XML file is complete (not truncated due to disk space issues)
3. Use an XML validator to identify specific schema violations

## Related Documentation

- [Azure DevOps Test Results Integration](../../azure-pipeline-auto-regression/docs/AZURE_DEVOPS_INTEGRATION_GUIDE.md)
- [JUnit XML Format Reference](../../azure-pipeline-auto-regression/docs/JUNIT_TEST_RESULTS_GUIDE.md)
- [AutoQA Architecture](./architecture.png)
