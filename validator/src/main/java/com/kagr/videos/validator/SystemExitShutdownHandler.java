package com.kagr.videos.validator;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class SystemExitShutdownHandler implements ShutdownHandler {
    public void terminateProcess(int exitCode) {
        System.exit(exitCode);
    }
}
