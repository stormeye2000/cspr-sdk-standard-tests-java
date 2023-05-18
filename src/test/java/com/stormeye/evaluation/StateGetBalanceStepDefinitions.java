package com.stormeye.evaluation;

import com.casper.sdk.model.balance.GetBalanceData;
import com.casper.sdk.model.uref.URef;
import com.casper.sdk.service.CasperService;
import com.fasterxml.jackson.databind.JsonNode;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.ContextMap;
import com.stormeye.utils.CurlUtils;
import com.stormeye.utils.NctlUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

import static com.stormeye.evaluation.StepConstants.EXPECTED_JSON;
import static com.stormeye.evaluation.StepConstants.STATE_GET_BALANCE_RESULT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Step definitions for state_get_balance RPC method cucumber test
 *
 * @author ian@meywood.com
 */
public class StateGetBalanceStepDefinitions {

    private final ContextMap contextMap = ContextMap.getInstance();
    private final Logger logger = LoggerFactory.getLogger(StateGetBalanceStepDefinitions.class);
    public final CasperService casperService = CasperClientProvider.getInstance().getCasperService();

    @Given("that the state_get_balance RPC method is invoked against nclt user-1 purse")
    public void thatTheState_get_balanceRPCMethodIsInvoked() throws Exception {
        logger.info("Given that the state_get_balance RPC method is invoked");
        final String stateRootHash = NctlUtils.getStateRootHash(1);
        final String accountMainPurse = NctlUtils.getAccountMainPurse(1);
        final GetBalanceData balance = casperService.getBalance(stateRootHash, URef.fromString(accountMainPurse));
        contextMap.put(STATE_GET_BALANCE_RESULT, balance);
        final JsonNode json = CurlUtils.getBalance(stateRootHash, accountMainPurse);
        contextMap.put(EXPECTED_JSON, json);
    }

    @Then("a valid state_get_balance_result is returned")
    public void aValidState_get_balance_resultIsReturned() {
        logger.info("Then a valid state_get_balance_result is returned");
        final GetBalanceData balanceData = contextMap.get(STATE_GET_BALANCE_RESULT);
        assertThat(balanceData, is(notNullValue()));
    }

    @And("the state_get_balance_result contains the purse amount")
    public void theState_get_balance_resultContainsThePurseAmount() {
        logger.info("And the state_get_balance_result contains the purse amount");
        final String accountMainPurse = NctlUtils.getAccountMainPurse(1);
        final BigInteger balance = NctlUtils.geAccountBalance(accountMainPurse);
        final GetBalanceData balanceData = contextMap.get(STATE_GET_BALANCE_RESULT);
        assertThat(balanceData.getValue(), is(balance));
    }

    @And("the state_get_balance_result contains api version {string}")
    public void theState_get_balance_resultContainsIsForApiVersion(final String apiVersion) {
        logger.info("And the state_get_balance_result contains api version {}", apiVersion);
        final GetBalanceData balanceData = contextMap.get(STATE_GET_BALANCE_RESULT);
        assertThat(balanceData.getApiVersion(), is(apiVersion));
    }

    @And("the state_get_balance_result contains a valid merkle proof")
    public void theState_get_balance_resultContainsAValidMerkleProof() {
        logger.info("And the state_get_balance_result contains a valid merkle proof");
        final GetBalanceData balanceData = contextMap.get(STATE_GET_BALANCE_RESULT);
        assertThat(balanceData.getMerkleProof(), is(notNullValue()));
        final String expectedMerkleProof = ((JsonNode) contextMap.get(EXPECTED_JSON)).at("/result/merkle_proof").asText();
        assertThat(balanceData.getMerkleProof(), is(expectedMerkleProof));
    }
}
