package com.stormeye.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stormeye.exception.NctlCommandException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Executes NCTL bash commands and parses the resulting console stream into a json object
 */
public class ExecUtils {

    /**
     * Default execute response function the converts the response string to a JsonNode.
     */
    static class JsonNodeResponse implements Function<String, JsonNode> {

        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public JsonNode apply(final String response) {
            try {
                return mapper.readTree(response);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
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

    private static final Logger logger = LoggerFactory.getLogger(ExecUtils.class);

    public JsonNode execute(final List<String> params) {
        return execute(params, new JsonNodeResponse());
    }

    public <T> T execute(final List<String> params, final Function<String, T> responseFunction) {

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

            if (process.exitValue() != 0) {
                throw new NctlCommandException(Integer.toString(process.exitValue()));
            }

            future.get(10, TimeUnit.SECONDS);

            return responseFunction.apply(replaceAnsiConsoleCodes(response.toString()));

        } catch (Exception e) {
            throw new NctlCommandException(e);
        }
    }

    @NotNull
    private String replaceAnsiConsoleCodes(final String response) {
        //remove any console colour ANSI info
        return response.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}
