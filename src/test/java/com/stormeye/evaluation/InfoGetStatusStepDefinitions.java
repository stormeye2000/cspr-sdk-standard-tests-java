package com.stormeye.evaluation;

import com.casper.sdk.model.status.StatusData;
import com.fasterxml.jackson.databind.JsonNode;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.ContextMap;
import com.stormeye.utils.Nctl;
import com.stormeye.utils.TestProperties;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

import static com.stormeye.evaluation.StepConstants.EXPECTED_STATUS_DATA;
import static com.stormeye.evaluation.StepConstants.STATUS_DATA;
import static com.stormeye.utils.JsonUtils.getJsonValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;

/**
 * Step definitions for the info_get_status feature.
 *
 * @author ian@meywood.com
 */
public class InfoGetStatusStepDefinitions {

    private final ContextMap contextMap = ContextMap.getInstance();
    private final Logger logger = LoggerFactory.getLogger(InfoGetStatusStepDefinitions.class);
    private final Nctl nctl = new Nctl(new TestProperties().getDockerName());

    @Given("that the info_get_status is invoked against nctl")
    public void thatTheInfo_get_statusIsInvoked() {

        final JsonNode expectedJsonNodeStatus = nctl.getNodeStatus(1);
        assertThat(expectedJsonNodeStatus, is(notNullValue()));
        contextMap.put(EXPECTED_STATUS_DATA, expectedJsonNodeStatus);

        logger.info("Given that the info_get_status is invoked");
        contextMap.put(STATUS_DATA, CasperClientProvider.getInstance().getCasperService().getStatus());
    }

    @Then("an info_get_status_result is returned")
    public void anInfo_get_status_resultIsReturned() {
        logger.info("Then an info_get_status_result is returned");
        final StatusData statusData = contextMap.get(STATUS_DATA);
        assertThat(statusData, is(notNullValue()));
    }

    @And("the info_get_status_result api_version is {string}")
    public void theInfo_get_status_resultApi_versionIs(String apiVersion) {
        final StatusData statusData = contextMap.get(STATUS_DATA);
        assertThat(statusData.getApiVersion(), is(apiVersion));
    }

    @And("the info_get_status_result chainspec_name is {string}")
    public void theInfo_get_status_resultChainspec_nameIs(String chainSpecName) {
        final StatusData statusData = contextMap.get(STATUS_DATA);
        assertThat(statusData.getChainSpecName(), is(chainSpecName));
    }

    @And("the info_get_status_result has a valid last_added_block_info")
    public void theInfo_get_status_resultHasAValidLast_added_block_info() {
        final StatusData statusData = contextMap.get(STATUS_DATA);
        final JsonNode jsonNode = contextMap.get(EXPECTED_STATUS_DATA);
        final String expectedHash = getJsonValue(jsonNode, "/last_added_block_info/hash");
        final String expectedTimestamp = getJsonValue(jsonNode, "/last_added_block_info/timestamp");
        final BigInteger expectedEraId = getJsonValue(jsonNode, "/last_added_block_info/era_id");
        final BigInteger expectedHeight = getJsonValue(jsonNode, "/last_added_block_info/height");
        final String expectedStateRootHash = getJsonValue(jsonNode, "/last_added_block_info/state_root_hash");
        final String expectedCreator = getJsonValue(jsonNode, "/last_added_block_info/creator");

        assertThat(statusData.getLastAddedBlockInfo().getHash(), is(expectedHash));
        assertThat(statusData.getLastAddedBlockInfo().getTimeStamp().getTime(), is(new DateTime(expectedTimestamp).toDate().getTime()));
        assertThat(statusData.getLastAddedBlockInfo().getEraId(), is(expectedEraId));
        assertThat(statusData.getLastAddedBlockInfo().getHeight(), is(expectedHeight));
        assertThat(statusData.getLastAddedBlockInfo().getStateRootHash(), is(expectedStateRootHash));
        assertThat(statusData.getLastAddedBlockInfo().getCreator().getAlgoTaggedHex(), is(expectedCreator));
    }

    @And("the info_get_status_result has a valid our_public_signing_key")
    public void theInfo_get_status_resultHasAValidOur_public_signing_key() {
        final StatusData statusData = contextMap.get(STATUS_DATA);
        final String expectedPublicKey = getJsonValue(contextMap.get(EXPECTED_STATUS_DATA), "/our_public_signing_key");
        assertThat(statusData.getPublicKey().getAlgoTaggedHex(), is(expectedPublicKey));
    }

    @And("the info_get_status_result has a valid starting_state_root_hash")
    public void theInfo_get_status_resultHasAValidStarting_state_root_hash() {
        final StatusData statusData = contextMap.get(STATUS_DATA);
        final String expectedStartingStateRoot = getJsonValue(contextMap.get(EXPECTED_STATUS_DATA), "/starting_state_root_hash");
        assertThat(statusData.getStartStateRootHash(), is(expectedStartingStateRoot));
    }

    @And("the info_get_status_result has a valid build_version")
    public void theInfo_get_status_resultHasAValidBuild_version() {
        final StatusData statusData = contextMap.get(STATUS_DATA);
        final String expectedBuildVersion = getJsonValue(contextMap.get(EXPECTED_STATUS_DATA), "/build_version");
        assertThat(statusData.getBuildVersion(), is(expectedBuildVersion));
    }

    @And("the info_get_status_result has a valid round_length")
    public void theInfo_get_status_resultHasAValidRound_length() {
        final StatusData statusData = contextMap.get(STATUS_DATA);
        final String expectedRoundLength = getJsonValue(contextMap.get(EXPECTED_STATUS_DATA), "/round_length");
        assertThat(statusData.getRoundLength(), is(expectedRoundLength));
    }

    @And("the info_get_status_result has a valid uptime")
    public void theInfo_get_status_resultHasAValidUptime() {
        final StatusData statusData = contextMap.get(STATUS_DATA);
        final String expectedUptime = getJsonValue(contextMap.get(EXPECTED_STATUS_DATA), "/uptime");
        assertThat(expectedUptime, is(notNullValue()));

        final int expectedSeconds = this.getTimeInSecondsFromUptime(expectedUptime);
        final int actualSeconds = this.getTimeInSecondsFromUptime(statusData.getUptime());

        assertThat(expectedSeconds, is(greaterThan(10)));
        assertThat(actualSeconds, is(greaterThan(10)));

        // assert there are less than 5seconds between both times
        assertThat(actualSeconds - expectedSeconds, is(lessThanOrEqualTo(5)));

        assertThat(statusData.getUptime(), containsString("m "));
        assertThat(statusData.getUptime(), containsString("s "));
        assertThat(statusData.getUptime(), endsWith("ms"));
    }

    @And("the info_get_status_result has a valid peers")
    public void theInfo_get_status_resultHasAValidPeers() {
        final StatusData statusData = contextMap.get(STATUS_DATA);
        final JsonNode jsonNode = contextMap.get(EXPECTED_STATUS_DATA);

        assertThat(statusData.getPeers(), hasSize(jsonNode.at("/peers").size()));
        assertThat(statusData.getPeers().get(0).getAddress(), is((String) getJsonValue(jsonNode, "/peers/0/address")));
        assertThat(statusData.getPeers().get(3).getAddress(), is((String) getJsonValue(jsonNode, "/peers/3/address")));
        assertThat(statusData.getPeers().get(3).getNodeId(), is((String) getJsonValue(jsonNode, "/peers/3/node_id")));
    }

    private int getTimeInSecondsFromUptime(final String uptime) {

        final String[] uptimeParts = uptime.split(" ");
        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        for (String part : uptimeParts) {
            if (part.endsWith("h")) {
                hours = extractNumber(part);
            } else if (part.endsWith("m")) {
                minutes = extractNumber(part);
            } else if (part.endsWith("s") && !part.endsWith("ms")) {
                seconds = extractNumber(part);
            }
        }

        return (hours * 60 * 60) + (minutes * 60) + seconds;
    }

    private int extractNumber(final String part) {
        return Integer.parseInt(part.substring(0, part.length() - 1));
    }
}
