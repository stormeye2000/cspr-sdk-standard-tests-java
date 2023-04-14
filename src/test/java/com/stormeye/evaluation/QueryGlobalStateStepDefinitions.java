package com.stormeye.evaluation;

import com.casper.sdk.exception.CasperClientException;
import com.casper.sdk.helper.CasperTransferHelper;
import com.casper.sdk.identifier.block.HashBlockIdentifier;
import com.casper.sdk.identifier.global.BlockHashIdentifier;
import com.casper.sdk.identifier.global.StateRootHashIdentifier;
import com.casper.sdk.model.block.JsonBlockData;
import com.casper.sdk.model.common.Digest;
import com.casper.sdk.model.common.Ttl;
import com.casper.sdk.model.deploy.Deploy;
import com.casper.sdk.model.deploy.DeployInfo;
import com.casper.sdk.model.deploy.DeployResult;
import com.casper.sdk.model.event.Event;
import com.casper.sdk.model.event.EventType;
import com.casper.sdk.model.event.blockadded.BlockAdded;
import com.casper.sdk.model.globalstate.GlobalStateData;
import com.casper.sdk.model.key.PublicKey;
import com.casper.sdk.model.stateroothash.StateRootHashData;
import com.casper.sdk.model.storedvalue.StoredValueDeployInfo;
import com.casper.sdk.service.CasperService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.stormeye.event.EventHandler;
import com.stormeye.matcher.ExpiringMatcher;
import com.stormeye.utils.*;
import com.syntifi.crypto.key.Ed25519PrivateKey;
import com.syntifi.crypto.key.Ed25519PublicKey;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.stormeye.evaluation.BlockAddedMatchers.hasTransferHashWithin;
import static com.stormeye.evaluation.StepConstants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author ian@meywood.com
 */
public class QueryGlobalStateStepDefinitions {

    private static final ParameterMap parameterMap = ParameterMap.getInstance();
    public static final CasperService casperService = CasperClientProvider.getInstance().getCasperService();
    private static final Logger logger = LoggerFactory.getLogger(QueryGlobalStateStepDefinitions.class);
    private static final EventHandler eventHandler = new EventHandler();
    private final TestProperties testProperties = new TestProperties();
    private final ExecUtils execUtils = new ExecUtils();

    @Before
    public static void setUp() {
        parameterMap.clear();
    }

    @After
    public static void after() {
        eventHandler.close();
    }

    @Given("that a valid block hash is known")
    public void thatAValidBlockHashIsKnown() throws Exception {

        // Create a transfer
        createTransfer();

        // Wait for a block to be added for the transfer
        waitForBlockAdded();

        logger.info("Given that a valid block hash is known");

        assertThat(parameterMap.get(LAST_BLOCK_ADDED), is(notNullValue()));
    }


    @When("the query_global_state RCP method is invoked with the block hash as the query identifier")
    public void theQuery_global_stateRCPMethodIsInvokedWithTheBlockHashAsTheQueryIdentifier() {

        final Digest blockHash = ((BlockAdded) parameterMap.get(LAST_BLOCK_ADDED)).getBlockHash();
        final BlockHashIdentifier globalStateIdentifier = new BlockHashIdentifier(blockHash.toString());

        final DeployResult deployResult = parameterMap.get(DEPLOY_RESULT);
        final String key = "deploy-" + deployResult.getDeployHash();

        final GlobalStateData globalStateData = casperService.queryGlobalState(globalStateIdentifier, key, new String[0]);
        assertThat(globalStateData, is(notNullValue()));

        parameterMap.put(GLOBAL_STATE_DATA, globalStateData);
    }


    @Then("a valid query_global_state_result is returned")
    public void aValidQuery_global_state_resultIsReturned() {

        final GlobalStateData globalStateData = parameterMap.get(GLOBAL_STATE_DATA);
        assertThat(globalStateData, is(notNullValue()));
        assertThat(globalStateData.getApiVersion(), is("1.0.0"));
        assertThat(globalStateData.getMerkleProof(), is(notNullValue()));
        assertThat(globalStateData.getHeader().getTimeStamp(), is(notNullValue()));
        assertThat(globalStateData.getHeader().getEraId(), is(greaterThan(0L)));
        assertThat(globalStateData.getHeader().getAccumulatedSeed().isValid(), is(true));
        assertThat(globalStateData.getHeader().getBodyHash().isValid(), is(true));
        assertThat(globalStateData.getHeader().getParentHash().isValid(), is(true));
    }

    @And("the query_global_state_result contains a valid deploy info stored value")
    public void theQuery_global_state_resultContainsAValidStoredValue() {
        final GlobalStateData globalStateData = parameterMap.get(GLOBAL_STATE_DATA);
        assertThat(globalStateData.getStoredValue(), is(instanceOf(StoredValueDeployInfo.class)));
    }

    @And("the query_global_state_result's stored value from is the user-{int} account hash")
    public void theQuery_global_state_resultSStoredValueSFromIsTheUserAccountHash(int userId) {

        final String accountHash = getAccountHash(userId);
        final DeployInfo storedValueDeployInfo = getGlobalDataDataStoredValue();
        assertThat(storedValueDeployInfo.getFrom(), is(accountHash));
    }


    @And("the query_global_state_result's stored value contains a gas price of {long}")
    public void theQuery_global_state_resultSStoredValueContainsAGasPriceOf(long gasPrice) {
        final DeployInfo storedValueDeployInfo = getGlobalDataDataStoredValue();
        assertThat(storedValueDeployInfo.getGas(), is(BigInteger.valueOf(gasPrice)));
    }

    @And("the query_global_state_result stored value contains the transfer hash")
    public void theQuery_global_state_resultSStoredValueContainsTheTransferHash() {

        final DeployInfo storedValueDeployInfo = getGlobalDataDataStoredValue();
        assertThat(storedValueDeployInfo.getTransfers().get(0), startsWith("transfer-"));
    }

    @Given("that the state root hash is known")
    public void thatTheStateRootHashIsKnown() {

        logger.info("Given that the state root hash is known");

        final StateRootHashData stateRootHash = casperService.getStateRootHash();
        assertThat(stateRootHash, is(notNullValue()));
        assertThat(stateRootHash.getStateRootHash(), notNullValue());
        parameterMap.put(STATE_ROOT_HASH, stateRootHash);
    }

    @When("the query_global_state RCP method is invoked with the state root hash as the query identifier and an invalid key")
    public void theQuery_global_stateRCPMethodIsInvokedWithTheStateRootHashAsTheQueryIdentifier() {

        logger.info("When the query_global_state RCP method is invoked with the state root hash as the query identifier");
        final StateRootHashData stateRootHash = parameterMap.get(STATE_ROOT_HASH);
        StateRootHashIdentifier globalStateIdentifier = new StateRootHashIdentifier(stateRootHash.getStateRootHash());
        // Need to invoke nctl-view-faucet-account to get uref
        final String key = "uref-d0343bb766946f9f850a67765aae267044fa79a6cd50235ffff248a37534";
        try {
            casperService.queryGlobalState(globalStateIdentifier, key, new String[0]);
        } catch (Exception e) {
            if (e instanceof CasperClientException) {
                parameterMap.put("clientException", e);
                return;
            } else {
                throw new RuntimeException(e);
            }
        }
        fail("Should have thrown a CasperClientException");
    }

    @Then("an error code of {int} is returned")
    public void anAnErrorCodeOfIsReturned(final int errorCode) {
        final CasperClientException clientException = parameterMap.get("clientException");
        assertThat(clientException.toString(), containsString("code: " + errorCode));
    }

    @And("an error message of {string} is returned")
    public void anErrorMessageOfIsReturned(String errorMessage) {
        final CasperClientException clientException = parameterMap.get("clientException");
        assertThat(clientException.toString(), containsString(errorMessage));
    }

    @Given("the query_global_state RCP method is invoked with an invalid block hash as the query identifier")
    public void theQuery_global_stateRCPMethodIsInvokedWithAnInvalidBlockHashAsTheQueryIdentifier() {
        final BlockHashIdentifier globalStateIdentifier = new BlockHashIdentifier("00112233441343670f71afb96018ab193855a85adc412f81571570dea34f2ca6500");
        final String key = "deploy-80fbb9c25eebda88e5d2eb9a0f7053ad6098d487aff841dc719e1526e0f59728";
        try {
            casperService.queryGlobalState(globalStateIdentifier, key, new String[0]);
        } catch (Exception e) {
            if (e instanceof CasperClientException) {
                parameterMap.put("clientException", e);
                return;
            } else {
                throw new RuntimeException(e);
            }
        }
        fail("Should have thrown a CasperClientException");
    }

    void createTransfer() throws Exception {

        logger.info("createTransfer");

        final Date timestamp = new Date();
        final Ed25519PrivateKey senderKey = new Ed25519PrivateKey();
        final Ed25519PublicKey receiverKey = new Ed25519PublicKey();

        senderKey.readPrivateKey(AssetUtils.getUserKeyAsset(1, 1, SECRET_KEY_PEM).getFile());
        receiverKey.readPublicKey(AssetUtils.getUserKeyAsset(1, 2, PUBLIC_KEY_PEM).getFile());

        final Deploy deploy = CasperTransferHelper.buildTransferDeploy(
                senderKey,
                PublicKey.fromAbstractPublicKey(receiverKey),
                BigInteger.valueOf(2500000000L),
                "casper-net-1",
                Math.abs(new Random().nextLong()),
                BigInteger.valueOf(100000000L),
                1L,
                Ttl.builder().ttl("30m").build(),
                timestamp,
                new ArrayList<>());

        parameterMap.put(PUT_DEPLOY, deploy);

        final CasperService casperService = CasperClientProvider.getInstance().getCasperService();

        parameterMap.put(DEPLOY_RESULT, casperService.putDeploy(deploy));
    }

    void waitForBlockAdded() throws Exception {

        logger.info("waitForBlockAdded");

        final DeployResult deployResult = parameterMap.get(DEPLOY_RESULT);

        final ExpiringMatcher<Event<BlockAdded>> matcher = (ExpiringMatcher<Event<BlockAdded>>) eventHandler.addEventMatcher(
                EventType.MAIN,
                hasTransferHashWithin(
                        deployResult.getDeployHash(),
                        blockAddedEvent -> parameterMap.put(LAST_BLOCK_ADDED, blockAddedEvent.getData())
                )
        );

        assertThat(matcher.waitForMatch(300), is(true));

        eventHandler.removeEventMatcher(EventType.MAIN, matcher);

        final Digest matchingBlockHash = ((BlockAdded) parameterMap.get(LAST_BLOCK_ADDED)).getBlockHash();
        assertThat(matchingBlockHash, is(notNullValue()));

        final JsonBlockData block = CasperClientProvider.getInstance().getCasperService().getBlock(new HashBlockIdentifier(matchingBlockHash.toString()));
        assertThat(block, is(notNullValue()));
        final List<String> transferHashes = block.getBlock().getBody().getTransferHashes();
        assertThat(transferHashes, hasItem(deployResult.getDeployHash()));
    }

    private String getAccountHash(final int userId) {
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

    private static <T> T getGlobalDataDataStoredValue() {
        final GlobalStateData globalStateData = parameterMap.get(GLOBAL_STATE_DATA);
        //noinspection unchecked
        return (T) globalStateData.getStoredValue().getValue();
    }

}