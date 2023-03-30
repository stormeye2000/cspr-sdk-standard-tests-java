package com.stormeye.utils;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stormeye.exceptions.NctlCommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Executes NCTL bash commands and parses the resulting console stream into a json object
 */
public class ExecUtils {
    private static final Logger logger = LoggerFactory.getLogger(ExecUtils.class);
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode execute(final List<String> params) {

        final StringBuilder response = new StringBuilder();

        try {
            final Process process = new ProcessBuilder()
                    .command(params)
                    .redirectErrorStream(true)
                    .start();

            logger.info("Executing NCTL bash command: " + String.join(" ", params));

            final ConsoleStream consoleStream = new ConsoleStream(process.getInputStream(), s -> response.append(s).append("\n"));
            final Future<?> future = Executors.newSingleThreadExecutor().submit(consoleStream);

            process.waitFor();
            if (process.exitValue() != 0){
                throw new NctlCommandException(Integer.toString(process.exitValue()));
            }

            future.get(10, TimeUnit.SECONDS);

            //remove any console colour ansii info
            return mapper.readTree(response.toString().replaceAll("\u001B\\[[;\\d]*m", ""));

        }
        catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
            throw new NctlCommandException(e.getMessage());
        }

    }

    private static class ConsoleStream implements Runnable {
        private final InputStream inputStream;
        private final Consumer<String> consumer;

        public ConsoleStream(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }

    }

}
