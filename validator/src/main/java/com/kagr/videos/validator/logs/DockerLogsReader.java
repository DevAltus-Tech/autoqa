package com.kagr.videos.validator.logs;





import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Queue;





@Slf4j
@Data
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DockerLogsReader implements Runnable, AutoCloseable {
    private final String loggerFileName;
    private final Queue<String> logsRead;
    private boolean shouldContinue = true;





    @Override
    public void run() {
        Path loggerFile = Paths.get(loggerFileName);
        long lastKnownPosition = 0;

        try (RandomAccessFile file = new RandomAccessFile(loggerFile.toFile(), "r");
             BufferedReader reader = new BufferedReader(new FileReader(file.getFD()))) {
            logger.info("Starting to tail logger file: {}", loggerFileName);

            while (shouldContinue) {
                file.seek(lastKnownPosition);
                String line;
                while ((line = reader.readLine()) != null) {
                    logsRead.add(line);
                    lastKnownPosition = file.getFilePointer();
                    logger.trace("advanced pointer to last known position: {}", lastKnownPosition);
                }
                logger.debug("Sleeping for 100 milliseconds before checking for new logger entries");
                Thread.sleep(100);
            }
        }
        catch (IOException e) {
            logger.error("Error reading logger file", e);
        }
        catch (InterruptedException e) {
            logger.warn("logger reading interrupted", e);
            Thread.currentThread().interrupt();
        }
        finally {
            logger.info("Stopped tailing logger file: {}", loggerFileName);
        }
    }





    @Override
    public void close() throws Exception {
        logger.debug("Closing DockerLogsReader, file:{}", loggerFileName);
        shouldContinue = false;
    }
}
