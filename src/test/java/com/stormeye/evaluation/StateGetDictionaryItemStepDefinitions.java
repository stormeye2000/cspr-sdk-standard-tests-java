package com.stormeye.evaluation;

import com.casper.sdk.exception.DynamicInstanceException;
import com.casper.sdk.identifier.dictionary.URefDictionaryIdentifier;
import com.casper.sdk.identifier.dictionary.URefSeed;
import com.casper.sdk.model.dictionary.DictionaryData;
import com.casper.sdk.model.stateroothash.StateRootHashData;
import com.casper.sdk.model.uref.URef;
import com.casper.sdk.service.CasperService;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.ContextMap;
import com.stormeye.utils.Nctl;
import com.stormeye.utils.TestProperties;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.stormeye.evaluation.StepConstants.STATE_GET_DICTIONARY_ITEM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Step definitions for the state_get_dictionary_item_result RCP method call.
 *
 * @author ian@meywood.com
 */
public class StateGetDictionaryItemStepDefinitions {

    private final ContextMap contextMap = ContextMap.getInstance();
    private final Logger logger = LoggerFactory.getLogger(StateGetDictionaryItemStepDefinitions.class);
    public final CasperService casperService = CasperClientProvider.getInstance().getCasperService();
    private final Nctl nctl = new Nctl(new TestProperties().getDockerName());

    @Given("that the state_get_dictionary_item RCP method is invoked")
    public void thatTheState_get_dictionary_itemRCPMethodIsInvoked() throws IOException, DynamicInstanceException {
        logger.info("Given that the state_get_dictionary_item RCP method is invoked");
        StateRootHashData stateRootHash = casperService.getStateRootHash();
        final String accountMainPurse = nctl.getAccountMainPurse(1);

        // TODO cannot find any working examples of what the parameters should be here
        final DictionaryData dictionaryData = casperService.getStateDictionaryItem(
                stateRootHash.getStateRootHash(),
                new URefDictionaryIdentifier(URefSeed.builder().uref(URef.fromString(accountMainPurse)).dictionaryItemKey("main_purse").build())
        );

        contextMap.put(STATE_GET_DICTIONARY_ITEM, dictionaryData);
    }

    @Then("a valid state_get_dictionary_item_result is returned")
    public void aValidState_get_dictionary_item_resultIsReturned() {
        logger.info("Then a valid state_get_dictionary_item_result is returned");
        final DictionaryData dictionaryData = contextMap.get(STATE_GET_DICTIONARY_ITEM);
        assertThat(dictionaryData, is(notNullValue()));
    }
}
