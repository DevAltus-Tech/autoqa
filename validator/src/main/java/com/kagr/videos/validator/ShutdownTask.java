package com.kagr.videos.validator;

import com.kagr.videos.validator.reports.TestStatus;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShutdownTask {
    private final ConcurrentHashMap<String, TestStatus> pendingTests;
    private final ConcurrentHashMap<String, TestStatus> completedTests;


}
