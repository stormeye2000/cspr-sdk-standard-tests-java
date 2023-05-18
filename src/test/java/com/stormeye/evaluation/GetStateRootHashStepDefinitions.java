package com.stormeye.evaluation;

import com.casper.sdk.model.stateroothash.StateRootHashData;
import com.casper.sdk.service.CasperService;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.ContextMap;
import com.stormeye.utils.NctlUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.stormeye.evaluation.StepConstants.STATE_ROOT_HASH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Step Definitions for the chain_get_state_root_hash RPC method.
 *
 * @author ian@meywood.com
 */
public class GetStateRootHashStepDefinitions {

    private final ContextMap contextMap = ContextMap.getInstance();
    public final CasperService casperService = CasperClientProvider.getInstance().getCasperService();
    private final Logger logger = LoggerFactory.getLogger(QueryGlobalStateStepDefinitions.class);

    @Given("that the chain_get_state_root_hash RCP method is invoked against nctl")
    public void thatTheChain_get_state_root_hashRCPMethodIsInvoked() {

        logger.info("Given that the chain_get_state_root_hash RCP method is invoked");
        contextMap.put(STATE_ROOT_HASH, casperService.getStateRootHash());
    }

    @Then("a valid chain_get_state_root_hash_result is returned")
    public void aValidChain_get_state_root_hash_resultIsReturned() {
        logger.info("Then a valid chain_get_state_root_hash_result is returned");
        final StateRootHashData stateRootHashData = contextMap.get(STATE_ROOT_HASH);
        assertThat(stateRootHashData, is(notNullValue()));

        final String expectedStateRootHash = NctlUtils.getStateRootHash(1);
        assertThat(stateRootHashData.getStateRootHash(), is(expectedStateRootHash));
    }
}
