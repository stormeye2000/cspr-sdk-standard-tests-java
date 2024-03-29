package com.stormeye.evaluation;

import com.casper.sdk.model.validator.ValidatorChangeData;
import com.fasterxml.jackson.databind.JsonNode;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.ContextMap;
import com.stormeye.utils.SimpleRcpClient;
import com.stormeye.utils.TestProperties;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.stormeye.evaluation.StepConstants.EXPECTED_VALIDATOR_CHANGES;
import static com.stormeye.evaluation.StepConstants.VALIDATORS_CHANGES;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * info_get_validator_changes RCP method step definitions.
 *
 * @author ian@meywood.com
 */
public class InfoGetValidatorChangesStepDefinitions {

    private final ContextMap contextMap = ContextMap.getInstance();
    private final Logger logger = LoggerFactory.getLogger(StateGetDictionaryItemStepDefinitions.class);
    private final TestProperties testProperties = new TestProperties();
    private final SimpleRcpClient simpleRcpClient = new SimpleRcpClient(testProperties.getHostname(), testProperties.getRcpPort());

    @Given("that the info_get_validator_changes method is invoked against a node")
    public void thatTheInfo_get_validator_changesMethodIsInvokedAgainstNode() throws Exception {

        logger.info("Given that the info_get_validator_changes method is invoked against a node");

        final ValidatorChangeData validatorsChanges = CasperClientProvider.getInstance().getCasperService().getValidatorsChanges();
        contextMap.put(VALIDATORS_CHANGES, validatorsChanges);

        final JsonNode expectedValidatorChanges = simpleRcpClient.getValidatorChanges();
        assertThat(expectedValidatorChanges, is(notNullValue()));
        contextMap.put(EXPECTED_VALIDATOR_CHANGES, expectedValidatorChanges);
    }

    @Then("a valid info_get_validator_changes_result is returned")
    public void aValidInfo_get_validator_changes_resultIsReturned() {
        logger.info("Then a valid info_get_validator_changes_result is returned");
        final ValidatorChangeData validatorsChanges = contextMap.get(VALIDATORS_CHANGES);
        assertThat(validatorsChanges, is(notNullValue()));
        final JsonNode expectedValidatorChanges = contextMap.get(EXPECTED_VALIDATOR_CHANGES);
        assertThat(validatorsChanges.getChanges().size(), is(expectedValidatorChanges.at("/result/changes").size()));
    }

    @And("the info_get_validator_changes_result contains a valid API version")
    public void theInfo_get_validator_changes_resultContainsAValidAPIVersion() {
        logger.info("And the info_get_validator_changes_result contains a valid API version");
        final ValidatorChangeData validatorsChanges = contextMap.get(VALIDATORS_CHANGES);
        final JsonNode expectedValidatorChanges = contextMap.get(EXPECTED_VALIDATOR_CHANGES);
        assertThat(validatorsChanges.getApiVersion().contains("."), is(true));
        assertThat(validatorsChanges.getApiVersion(), is(expectedValidatorChanges.at("/result/api_version").asText()));
    }
}
