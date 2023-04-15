package com.stormeye.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Utility method that call a node using nctl exec comands
 *
 * @author ian@meywood.com
 */
public class NctlUtils {

    private static TestProperties testProperties = new TestProperties();
    private static final ExecUtils execUtils = new ExecUtils();

    public static String getAccountMainPurse(final int userId) {
        final JsonNode node = execUtils.execute(ExecCommands.NCTL_VIEW_USER_ACCOUNT.getCommand(testProperties.getDockerName(), "user=" + userId), s -> {
            try {
                return new ObjectMapper().readTree(s.substring(s.indexOf("{")));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        final TextNode mainPurse = (TextNode) node.at("/stored_value/Account/main_purse");
        assertThat(mainPurse, is(notNullValue()));
        assertThat(mainPurse.asText(), startsWith("uref-"));
        return mainPurse.asText();
    }

    public static String getAccountHash(final int userId) {
        final JsonNode node = execUtils.execute(ExecCommands.NCTL_VIEW_USER_ACCOUNT.getCommand(testProperties.getDockerName(), "user=" + userId), s -> {
            try {
                return new ObjectMapper().readTree(s.substring(s.indexOf("{")));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        final TextNode accountHash = (TextNode) node.at("/stored_value/Account/account_hash");
        assertThat(accountHash, is(notNullValue()));
        assertThat(accountHash.asText(), startsWith("account-hash-"));
        return accountHash.asText();
    }
}
