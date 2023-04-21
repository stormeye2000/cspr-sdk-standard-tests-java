package com.stormeye.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NumericNode;

import java.math.BigInteger;

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
    private static final ExecUtils execUtils = new ExecUtils();

    public static String getAccountMainPurse(final int userId) {
        final JsonNode node = execUtils.execute(ExecCommands.NCTL_VIEW_USER_ACCOUNT.getCommand(testProperties.getDockerName(), "user=" + userId), s -> {
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
        return execUtils.execute(ExecCommands.NCTL_VIEW_CHAIN_STATE_ROOT_HASH.getCommand(testProperties.getDockerName(), "node=" + nodeId), s ->
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
        final String merkelProof = getJsonValue(node, "/merkle_proof");
        assertThat(merkelProof, is(notNullValue()));
        assertThat(merkelProof, startsWith("["));
        return merkelProof;
    }

    public static JsonNode getUserAccount(final int userId) {
        return execUtils.execute(ExecCommands.NCTL_VIEW_USER_ACCOUNT.getCommand(testProperties.getDockerName(), "user=" + userId), NctlUtils::removePreamble);
    }

    public static JsonNode getNodeStatus(final int nodeId) {
        return execUtils.execute(ExecCommands.NCTL_VIEW_NODE_STATUS.getCommand(testProperties.getDockerName(), "node=" + nodeId), NctlUtils::removePreamble);
    }

    private static JsonNode removePreamble(final String response) {
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
        return execUtils.execute(ExecCommands.NCTL_VIEW_CHAIN_BALANCE.getCommand(
                        testProperties.getDockerName(),
                        "purse-uref=" + purseUref),
                s -> new BigInteger(s.split("=")[1].trim())
        );
    }

    public static JsonNode getStateAuctionInfo() {
        return execUtils.execute(ExecCommands.NCTL_VIEW_CHAIN_AUCTION_INFO.getCommand(testProperties.getDockerName()));
    }
}

