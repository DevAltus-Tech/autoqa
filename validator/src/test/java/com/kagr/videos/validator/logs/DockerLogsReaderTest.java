package com.kagr.videos.validator.logs;





import com.kagr.videos.validator.loggers.DockerLogsReader;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;





class DockerLogsReaderTest {
    @Test
    public void testReadLogs() {
        try {
            // Create a temporary log file
            File tempLogFile = File.createTempFile("test", ".log");
            tempLogFile.deleteOnExit();

            LinkedBlockingQueue<String> logsRead = new LinkedBlockingQueue<>();
            Thread readerThread = new Thread(new DockerLogsReader(tempLogFile.getAbsolutePath(),logsRead));
            readerThread.start();
            final int sleepTime = 100;
            try {

                // Write some content to the log file
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempLogFile));
                for (int i = 0; i < 10; i++) {
                    writer.write("Line " + i + "\n");
                    writer.flush();
                    Thread.sleep(sleepTime);
                }
            }
            catch (InterruptedException ex_) {
                throw new RuntimeException(ex_);
            }


            assertEquals(10, logsRead.size());
        }
        catch (IOException e) {
            fail("Exception thrown while setting up the test");
        }
    }
}
