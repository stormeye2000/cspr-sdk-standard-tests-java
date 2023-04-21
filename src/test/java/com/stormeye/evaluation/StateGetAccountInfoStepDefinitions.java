package com.stormeye.evaluation;

import com.casper.sdk.identifier.block.BlockIdentifier;
import com.casper.sdk.identifier.block.HashBlockIdentifier;
import com.casper.sdk.model.account.AccountData;
import com.casper.sdk.model.account.ActionThresholds;
import com.casper.sdk.model.block.JsonBlockData;
import com.casper.sdk.model.key.PublicKey;
import com.fasterxml.jackson.databind.JsonNode;
import com.stormeye.utils.AssetUtils;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.NctlUtils;
import com.stormeye.utils.ParameterMap;
import com.syntifi.crypto.key.AbstractPublicKey;
import com.syntifi.crypto.key.Ed25519PrivateKey;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static com.stormeye.evaluation.StepConstants.STATE_ACCOUNT_INFO;
import static com.stormeye.matcher.NctlMatchers.isValidMerkelProof;
import static com.stormeye.utils.NctlUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Step definitions for the state_get_account_info RPC method.
 *
 * @author ian@meywood.com
 */
public class StateGetAccountInfoStepDefinitions {

    private static final ParameterMap parameterMap = ParameterMap.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(StateGetAccountInfoStepDefinitions.class);

    @Given("that the state_get_account_info RCP method is invoked against nctl")
    public void thatTheStateGetAccountInfoRcpMethodIsInvoked() throws IOException {

        logger.info("Given that the state_get_account_info RCP method is invoked against nctl");

        final String hexPublicKey = getUserOneHexPublicKey();
        final JsonBlockData block = CasperClientProvider.getInstance().getCasperService().getBlock();
        final BlockIdentifier identifier = new HashBlockIdentifier(block.getBlock().getHash().toString());

        final AccountData stateAccountInfo = CasperClientProvider.getInstance().getCasperService().getStateAccountInfo(hexPublicKey, identifier);
        parameterMap.put(STATE_ACCOUNT_INFO, stateAccountInfo);
    }

    @Then("a valid state_get_account_info_result is returned")
    public void aValidState_get_account_info_resultIsReturned() {
        logger.info("Then a valid state_get_account_info_result is returned");
        final AccountData stateAccountInfo = parameterMap.get(STATE_ACCOUNT_INFO);
        assertThat(stateAccountInfo, is(notNullValue()));
    }

    @And("the state_get_account_info_result contain a valid account hash")
    public void theState_get_account_info_resultContainAValidAccountHash() {
        logger.info("And the state_get_account_info_result contain a valid account hash");
        final AccountData stateAccountInfo = parameterMap.get(STATE_ACCOUNT_INFO);
        final String expectedAccountHash = getAccountHash(1);
        assertThat(stateAccountInfo.getAccount().getHash(), is(expectedAccountHash));
    }

    @And("the state_get_account_info_result contain a valid main purse uref")
    public void theState_get_account_info_resultContainAValidMainPurseUref() {
        logger.info("And the state_get_account_info_result contain a valid main purse uref");
        final AccountData stateAccountInfo = parameterMap.get(STATE_ACCOUNT_INFO);
        final String accountMainPurse = getAccountMainPurse(1);
        assertThat(stateAccountInfo.getAccount().getMainPurse(), is(accountMainPurse));
    }

    @And("the state_get_account_info_result contain a valid merkel proof")
    public void theState_get_account_info_resultContainAValidMerkelProof() {
        logger.info("And the state_get_account_info_result contain a valid merkelProof");
        final AccountData stateAccountInfo = parameterMap.get(STATE_ACCOUNT_INFO);
        assertThat(stateAccountInfo.getMerkelProof(), is(notNullValue()));
        assertThat(stateAccountInfo.getMerkelProof(), is(isValidMerkelProof(NctlUtils.getAccountMerkelProof(1))));
    }

    @And("the state_get_account_info_result contain a valid associated keys")
    public void theState_get_account_info_resultContainAValidAssociatedKeys() {
        logger.info("And the state_get_account_info_result contain a valid associated keys");
        final AccountData stateAccountInfo = parameterMap.get(STATE_ACCOUNT_INFO);
        final String expectedAccountHash = getAccountHash(1);
        assertThat(stateAccountInfo.getAccount().getAssociatedKeys().get(0).getAccountHash(), is(expectedAccountHash));
        assertThat(stateAccountInfo.getAccount().getAssociatedKeys().get(0).getWeight(), is(1));
    }


    @And("the state_get_account_info_result contain a valid action thresholds")
    public void theState_get_account_info_resultContainAValidActionThresholds() {
        logger.info("And the state_get_account_info_result contain a valid action thresholds");
        final AccountData stateAccountInfo = parameterMap.get(STATE_ACCOUNT_INFO);
        final JsonNode userAccountJson = getUserAccount(1);
        final ActionThresholds deployment = stateAccountInfo.getAccount().getDeployment();
        assertThat(deployment, is(notNullValue()));
        assertThat(deployment.getDeployment(), is(userAccountJson.at("/stored_value/Account/action_thresholds/deployment").asInt()));
        assertThat(deployment.getKeyManagement(), is(userAccountJson.at("/stored_value/Account/action_thresholds/key_management").asInt()));
    }

    @And("the state_get_account_info_result contain a valid named keys")
    public void theState_get_account_info_resultContainAValidNamedKeys() {
        logger.info("And the state_get_account_info_result contain a valid action thresholds");
        final AccountData stateAccountInfo = parameterMap.get(STATE_ACCOUNT_INFO);
        final JsonNode userAccountJson = getUserAccount(1);
        assertThat(stateAccountInfo.getAccount().getNamedKeys().size(), is(userAccountJson.at("/stored_value/Account/named_keys").size()));
    }

    private static String getUserOneHexPublicKey() throws IOException {
        final URL keyUrl = AssetUtils.getUserKeyAsset(1, 1, "secret_key.pem");
        final Ed25519PrivateKey privateKey = new Ed25519PrivateKey();
        privateKey.readPrivateKey(keyUrl.getFile());
        final AbstractPublicKey publicKey = privateKey.derivePublicKey();
        return PublicKey.fromAbstractPublicKey(publicKey).getAlgoTaggedHex();
    }
}
