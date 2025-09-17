package com.kagr.videos.validator;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemExitShutdownHandler implements ShutdownHandler {
    public void terminateProcess(int exitCode) {
        logger.info("Terminating process with exit code: {}", exitCode);
        System.exit(exitCode);
    }
}
