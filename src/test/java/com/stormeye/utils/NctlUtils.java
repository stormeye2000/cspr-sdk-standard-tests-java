package com.stormeye.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.stormeye.exception.NctlCommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Utility method that call a node using nctl exec commands
 *
 * @author ian@meywood.com
 */
public class NctlUtils {

    private static final TestProperties testProperties = new TestProperties();
    private static final Logger logger = LoggerFactory.getLogger(NctlUtils.class);

    public static String getAccountMainPurse(final int userId) {

        final JsonNode node = execute("view_user_account.sh", "user=" + userId, s -> {
            try {
                return new ObjectMapper().readTree(s.substring(s.indexOf("{")));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        final String mainPurse = getJsonValue(node, "/stored_value/Account/main_purse");
        assertThat(mainPurse, is(notNullValue()));
        assertThat(mainPurse, startsWith("uref-"));
        return mainPurse;
    }

    public static String getStateRootHash(final int nodeId) {
        return execute("view_chain_state_root_hash.sh", "node=" + nodeId, s ->
                s.split("=")[1].trim()
        );
    }

    public static String getAccountHash(final int userId) {
        final JsonNode node = getUserAccount(userId);
        final String accountHash = getJsonValue(node, "/stored_value/Account/account_hash");
        assertThat(accountHash, is(notNullValue()));
        assertThat(accountHash, startsWith("account-hash-"));
        return accountHash;
    }

    public static String getAccountMerkelProof(final int userId) {
        final JsonNode node = getUserAccount(userId);
        final String merkleProof = getJsonValue(node, "/merkle_proof");
        assertThat(merkleProof, is(notNullValue()));
        assertThat(merkleProof, startsWith("["));
        return merkleProof;
    }

    public static JsonNode getUserAccount(final int userId) {
        return execute("view_user_account.sh", "user=" + userId, NctlUtils::removePreamble);
    }

    public static JsonNode getNodeStatus(final int nodeId) {
        return execute("view_node_status.sh", "node=" + nodeId, NctlUtils::removePreamble);
    }

    static JsonNode removePreamble(final String response) {
        try {
            return new ObjectMapper().readTree(response.substring(response.indexOf("{")));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getJsonValue(final JsonNode jsonNode, final String jsonPath) {

        final JsonNode at = jsonNode.at(jsonPath);
        if (at instanceof NumericNode) {
            //noinspection unchecked
            return (T) at.bigIntegerValue();
        } else {
            //noinspection unchecked
            return (T) at.asText();
        }
    }

    public static BigInteger geAccountBalance(final String purseUref) {
        return execute("view_chain_balance.sh", "purse-uref=" + purseUref,
                s -> new BigInteger(s.split("=")[1].trim())
        );
    }

    public static JsonNode getStateAuctionInfo() {
        return execute("view_chain_auction_info.sh", null);
    }

    public static JsonNode getChainBlock(final String blockHash) {
        return execute("view_chain_block.sh", "block=" + blockHash);
    }

    public static JsonNode getChainEraInfo() {
        return execute("view_chain_era_info.sh", null);
    }

    public static JsonNode getChainBlockTransfers(final String blockHash) {
        return execute("view_chain_block_transfers.sh", "block=" + blockHash);
    }

    private static List<String> buildCommand(final String shellCommand, final String params) {
        return List.of("bash", "-c", String.format(
                "docker exec -t %s /bin/bash -c 'source casper-node/utils/nctl/sh/views/%s %s'",
                testProperties.getDockerName(),
                shellCommand,
                params != null ? params : "")
        );
    }

    public static <T> T execute(final String shellCommand, final String params, final Function<String, T> responseFunction) {

        try {
            final StringBuilder response = new StringBuilder();
            final Process process = new ProcessBuilder()
                    .command(buildCommand(shellCommand, params))
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

    public static JsonNode execute(final String shellCommand, final String params) {
        return execute(shellCommand, params, new JsonNodeResponse());
    }

    private static String replaceAnsiConsoleCodes(final String response) {
        //remove any console colour ANSI info
        return response.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}


