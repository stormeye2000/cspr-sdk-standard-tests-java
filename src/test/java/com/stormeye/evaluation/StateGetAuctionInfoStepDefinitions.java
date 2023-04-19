package com.stormeye.evaluation;

import com.casper.sdk.identifier.block.HashBlockIdentifier;
import com.casper.sdk.model.auction.AuctionData;
import com.casper.sdk.model.block.JsonBlockData;
import com.casper.sdk.service.CasperService;
import com.fasterxml.jackson.databind.JsonNode;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.NctlUtils;
import com.stormeye.utils.ParameterMap;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.stormeye.evaluation.StepConstants.STATE_AUCTION_INFO_JSON;
import static com.stormeye.evaluation.StepConstants.STATE_GET_AUCTION_INFO_RESULT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Step definitions for the state_get_auction_info RCP method cucumber test.
 *
 * @author ian@meywood.com
 */
public class StateGetAuctionInfoStepDefinitions {

    private static final ParameterMap parameterMap = ParameterMap.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(StateGetAuctionInfoStepDefinitions.class);
    public static final CasperService casperService = CasperClientProvider.getInstance().getCasperService();

    @Given("that the state_get_auction_info RPC method is invoked by hash block identifier")
    public void thatTheState_get_auction_infoRPCMethodIsInvoked() {
        logger.info("Given that the state_get_auction_info RPC method is invoked by hash block identifier");
        final JsonBlockData block = casperService.getBlock();
        final AuctionData auctionData = casperService.getStateAuctionInfo(new HashBlockIdentifier(block.getBlock().getHash().toString()));
        parameterMap.put(STATE_GET_AUCTION_INFO_RESULT, auctionData);

        final JsonNode stateAuctionInfoJson = NctlUtils.getStateAuctionInfo();
        assertThat(stateAuctionInfoJson, is(notNullValue()));
        parameterMap.put(STATE_AUCTION_INFO_JSON, stateAuctionInfoJson);
    }

    @Then("a valid state_get_auction_info_result is returned")
    public void aValidState_get_auction_info_resultIsReturned() {
        logger.info("Given a valid state_get_auction_info_result is returned");
        final AuctionData auctionData = parameterMap.get(STATE_GET_AUCTION_INFO_RESULT);
        assertThat(auctionData, is(notNullValue()));
    }

    @And("the state_get_auction_info_result has and api version of {string}")
    public void theState_get_auction_info_resultHasAndApiVersionOf(String apiVersion) {
        logger.info("And the state_get_auction_info_result has and api version of {}", apiVersion);
        final AuctionData auctionData = parameterMap.get(STATE_GET_AUCTION_INFO_RESULT);
        assertThat(auctionData.getApiVersion(), is(apiVersion));
    }

    @And("the state_get_auction_info_result action_state has a valid state root hash")
    public void theState_get_auction_info_resultAction_stateHasAValidStateRootHash() {
        final AuctionData auctionData = parameterMap.get(STATE_GET_AUCTION_INFO_RESULT);
        final JsonNode jsonNode = parameterMap.get(STATE_AUCTION_INFO_JSON);
        final String expectedStateRootHash = jsonNode.at("/auction_state/state_root_hash").asText();
        assertThat(auctionData.getAuctionState().getStateRootHash(), is(expectedStateRootHash));
    }

    @And("the state_get_auction_info_result action_state has a valid height")
    public void theState_get_auction_info_resultAction_stateHasAValidHeight() {
        final AuctionData auctionData = parameterMap.get(STATE_GET_AUCTION_INFO_RESULT);
        final JsonNode jsonNode = parameterMap.get(STATE_AUCTION_INFO_JSON);
        final Long expectedBlockHeight = jsonNode.at("/auction_state/block_height").asLong();
        assertThat(auctionData.getAuctionState().getHeight(), is(expectedBlockHeight));
    }

    @And("the state_get_auction_info_result action_state has valid bids")
    public void theState_get_auction_info_resultAction_stateHasValidBids() {
        final AuctionData auctionData = parameterMap.get(STATE_GET_AUCTION_INFO_RESULT);

    }

    @And("the state_get_auction_info_result action_state has valid era validators")
    public void theState_get_auction_info_resultAction_stateHasValidEraValidators() {
        final AuctionData auctionData = parameterMap.get(STATE_GET_AUCTION_INFO_RESULT);

    }
}
