package com.kagr.videos.validator;

public class SystemExitShutdownHandler implements ShutdownHandler {
    public void terminateProcess() {
        System.exit(0);
    }
}
