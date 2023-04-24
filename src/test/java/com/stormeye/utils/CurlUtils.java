package com.stormeye.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Provides CURL like commands to a node to obtain raw JSON.
 *
 * @author ian@meywood.com
 */
public class CurlUtils {

    /** Properties to obtain RCP hostname and port from */
    private static final TestProperties testProperties = new TestProperties();

    /**
     * Obtains auction info as a JsonNode by block hash
     *
     * @param hash the block to obtain auction info for
     * @return the Json result
     * @throws Exception if an IO error occurs
     */
    public static JsonNode getAuctionInfoByHash(final String hash) throws Exception {
        return rcp("state_get_auction_info", "[{\"Hash\":  \"" + hash + "\"}]");
    }

    public static JsonNode getValidatorChanges() throws Exception {
        return rcp("info_get_validator_changes", "[]");
    }


    /**
     * Obtains auction info as a JsonNode by block hash
     *
     * @param stateRootHash the state root hash
     * @param purseUref     the ref to the purse whose balance is to be obtained
     * @return the Json result
     * @throws Exception if an IO error occurs
     */
    public static JsonNode getBalance(final String stateRootHash, final String purseUref) throws Exception {
        return rcp("state_get_balance",
                String.format("{\"state_root_hash\":\"%s\",\"purse_uref\":\"%s\"}", stateRootHash, purseUref)
        );
    }

    /**
     * Performs an old school java http rcp request to a node.
     *
     * @param method the rcp method to invoke
     * @param params the params to pass into the method
     * @return the JsonNode of the executed RCP request
     * @throws Exception on an invalid request
     */
    private static JsonNode rcp(final String method, final String params) throws Exception {

        final URL url = new URL("http", testProperties.getHostname(), testProperties.getRcpPort(), "/rpc");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        final String payload = "{\"id\":\"" + System.currentTimeMillis() + "\",\"jsonrpc\":\"2.0\",\"method\":\"" + method + "\",\"params\":" + params + "}";

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", Integer.toString(payload.length()));
        connection.setDoOutput(true);

        final OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
        out.write(payload);
        out.flush();
        out.close();

        return new ObjectMapper().readTree(connection.getInputStream());
    }
}
