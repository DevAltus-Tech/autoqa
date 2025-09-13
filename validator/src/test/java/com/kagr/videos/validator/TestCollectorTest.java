package com.kagr.videos.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.kagr.videos.validator.reports.TestStatus;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class TestCollectorTest {
    private static final Logger log = LoggerFactory.getLogger(TestCollectorTest.class);
    @Mock RestTemplate restTemplate;
    @InjectMocks TestCollector collector;
    ConcurrentHashMap<String, TestStatus> pending;
    ConcurrentHashMap<String, TestStatus> completed;
    boolean terminateCalled;





    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pending = new ConcurrentHashMap<>();
        completed = new ConcurrentHashMap<>();
        collector = new TestCollector(pending,
                                      completed,
                                      restTemplate,
                                      "ordersGeneratorLog",
                                      "ordersClientLog",
                                      "heartbeatLog",
                                      "http://report",
                                      new TestableCollector());
    }





    @Test
    void testHandleJmsEvent_ConsumerCreated_RemovesPendingAndCompletes() {
        TestStatus status = new TestStatus();
        status.setTestName("service1-connect"); // Use correct suffix
        pending.put("service1-connect", status);
        collector.handleJmsEvent("CONSUMER_CREATED", "service1");
        assertTrue(completed.containsKey("service1-connect"));
        assertEquals("PASS", completed.get("service1-connect").getStatus());
        assertEquals("Consumer created successfully", completed.get("service1-connect").getNotes());
    }





    @Test
    void testHandleJmsEvent_IgnoresOtherEvents() {
        TestStatus status = new TestStatus();
        status.setTestName("service2_connect");
        pending.put("service2_connect", status);
        collector.handleJmsEvent("OTHER_EVENT", "service2");
        assertTrue(pending.containsKey("service2_connect"));
        assertFalse(completed.containsKey("service2_connect"));
    }





    @Test
    void testSendShutdownCommand_Success() {
        ResponseEntity<String> response = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplate.exchange(any(RequestEntity.class), eq(String.class))).thenReturn(response);
        int result = collector.sendShutdownCommand("serviceX");
        assertEquals(1, result);
    }





    @Test
    void testSendShutdownCommand_Failure() {
        ResponseEntity<String> response = new ResponseEntity<>("FAIL", HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(any(RequestEntity.class), eq(String.class))).thenReturn(response);
        int result = collector.sendShutdownCommand("serviceX");
        assertEquals(0, result);
    }





    @Test
    void testSendShutdownCommand_Exception() {
        when(restTemplate.exchange(any(RequestEntity.class), eq(String.class))).thenThrow(new RuntimeException("fail"));
        int result = collector.sendShutdownCommand("serviceX");
        assertEquals(0, result);
    }





    @Test
    void testWriteShutdownReport_Success() {
        ResponseEntity<String> response = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);
        assertDoesNotThrow(() -> collector.writeShutdownReport());
    }





    @Test
    void testWriteShutdownReport_Failure() {
        ResponseEntity<String> response = new ResponseEntity<>("FAIL", HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);
        assertDoesNotThrow(() -> collector.writeShutdownReport());
    }





    @Test
    void testWriteShutdownReport_Exception() {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenThrow(new RestClientException("fail"));
        assertDoesNotThrow(() -> collector.writeShutdownReport());
    }





    @Test
    void testPerformPostTimeoutActions_MovesPendingToCompleted() {
        TestStatus status = new TestStatus();
        status.setTestName("timeoutTest");
        pending.put("timeoutTest", status);
        collector.performPostTimeoutActions();
        assertTrue(completed.containsKey("timeoutTest"));
        assertEquals("FAIL", completed.get("timeoutTest").getStatus());
        assertEquals("Timeout", completed.get("timeoutTest").getNotes());
        assertTrue(pending.isEmpty());
    }





    static class TestableCollector implements ShutdownHandler {
        @Override
        public void terminateProcess() {
            log.info("terminateProcess");
        }
    }
}
