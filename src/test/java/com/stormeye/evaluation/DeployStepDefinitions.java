package com.stormeye.evaluation;

import com.casper.sdk.helper.CasperTransferHelper;
import com.casper.sdk.identifier.block.HashBlockIdentifier;
import com.casper.sdk.model.block.JsonBlockData;
import com.casper.sdk.model.common.Ttl;
import com.casper.sdk.model.deploy.Deploy;
import com.casper.sdk.model.deploy.DeployResult;
import com.casper.sdk.model.event.DataType;
import com.casper.sdk.model.event.EventTarget;
import com.casper.sdk.model.event.EventType;
import com.casper.sdk.model.event.blockadded.BlockAdded;
import com.casper.sdk.model.event.deployaccepted.DeployAccepted;
import com.casper.sdk.model.event.finalitysignature.FinalitySignature;
import com.casper.sdk.model.key.PublicKey;
import com.casper.sdk.service.CasperService;
import com.stormeye.utils.AssetUtils;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.ParameterMap;
import com.syntifi.crypto.key.Ed25519PrivateKey;
import com.syntifi.crypto.key.Ed25519PublicKey;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Step Definitions for Deploy Cucumber Tests.
 *
 * @author ian@meywood.com
 */
public class DeployStepDefinitions {

    private static final ParameterMap parameterMap = new ParameterMap();

    private static final Logger logger = LoggerFactory.getLogger(DeployStepDefinitions.class);
    private static Thread deploysThread;
    private static Thread mainThread;
    private static Thread sigsThread;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        parameterMap.clear();

        (deploysThread = new Thread(() -> {
            CasperClientProvider.getInstance().getEventService().consumeEvents(EventType.DEPLOYS, EventTarget.POJO, null, event -> {
                logger.info("Got deploy event {}", event);

                if (event.getDataType() == DataType.DEPLOY_ACCEPTED) {
                    DeployAccepted deployAccepted = (DeployAccepted) event.getData();
                    logger.info("Deploy accepted hash: {}", deployAccepted.getDeploy().getHash().toString());
                }
            });
        })).start();

        (mainThread = new Thread(() -> {
            CasperClientProvider.getInstance().getEventService().consumeEvents(EventType.MAIN, EventTarget.POJO, null, event -> {
                logger.info("Got main event {}", event);
                if (event.getDataType() == DataType.BLOCK_ADDED) {
                    final BlockAdded blockAdded = (BlockAdded) event.getData();
                    parameterMap.put("lastBlockAdded", blockAdded);

                    final String deployHashes = blockAdded.getBlock().getBody().getDeployHashes()
                            .stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", "));

                    final String transferHashes = blockAdded.getBlock().getBody().getTransferHashes()
                            .stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", "));

                    logger.info("Block added deploy hashes: [{}], transfer hashes: [{}]", deployHashes, transferHashes);
                }

            });
        })).start();

        (sigsThread = new Thread(() -> {
            CasperClientProvider.getInstance().getEventService().consumeEvents(EventType.SIGS, EventTarget.POJO, null, event -> {
                logger.info("Got signature event {}", event);

                if (event.getDataType() == DataType.FINALITY_SIGNATURE) {
                    FinalitySignature finalitySignature = (FinalitySignature) event.getData();
                    logger.info("Signature block hash {}", finalitySignature.getBlockHash());
                }
            });
        })).start();

        Thread.sleep(1000L);
    }


    @AfterAll
    static void afterAll() {
        deploysThread.stop();
        mainThread.stop();
        sigsThread.stop();
    }


    @Given("that user-{int} initiates a transfer to user-{int}")
    public void thatUserCreatesATransferOfToUser(int senderId, int receiverId) throws IOException {

        logger.info("Given that user-{} initiates a transfer to user-{} ", senderId, receiverId);

        final Ed25519PrivateKey senderKey = new Ed25519PrivateKey();
        final Ed25519PublicKey receiverKey = new Ed25519PublicKey();

        senderKey.readPrivateKey(AssetUtils.getUserKeyAsset(1, senderId, "secret_key.pem").getFile());
        receiverKey.readPublicKey(AssetUtils.getUserKeyAsset(1, receiverId, "public_key.pem").getFile());

        parameterMap.put("senderKey", senderKey);
        parameterMap.put("receiverKey", receiverKey);
    }

    @And("the deploy has a ttl of {int}m")
    public void theDeployHasATtlOfM(int ttlMinutes) {

        logger.info("And the deploy has a ttl of {}m", ttlMinutes);

        parameterMap.put("ttl", Ttl.builder().ttl(ttlMinutes + "m").build());
    }

    @And("the transfer amount is {long}")
    public void theTransferAmountIs(long amount) {

        logger.info("And the transfer amount is {}", amount);

        parameterMap.put("transferAmount", BigInteger.valueOf(amount));
    }

    @And("the transfer gas price is {long}")
    public void theTransferPriceIs(long price) {

        logger.info("And the transfer gas price is {}", price);

        parameterMap.put("gasPrice", price);
    }

    @When("the deploy is put on chain {string}")
    public void theDeployIsPut(final String chainName) throws Exception {

        logger.info("When the deploy is put on chain {}", chainName);

        final Deploy deploy = CasperTransferHelper.buildTransferDeploy(
                parameterMap.get("senderKey"),
                PublicKey.fromAbstractPublicKey(parameterMap.get("receiverKey")),
                parameterMap.get("transferAmount"),
                chainName,
                Math.abs(new Random().nextLong()),
                BigInteger.valueOf(100000000L),
                parameterMap.get("gasPrice"),
                parameterMap.get("ttl"),
                new Date(),
                new ArrayList<>());


        final CasperService casperService = CasperClientProvider.getInstance().getCasperService();

        parameterMap.put("deployResult", casperService.putDeploy(deploy));
    }


    @Then("the deploy response contains a valid deploy hash of length {int} and an API version {string}")
    public void theValidDeployHashIsReturned(final int hashLength, final String apiVersion) throws Exception {

        logger.info("Then the deploy response contains a valid deploy hash of length {} and an API version {}", hashLength, apiVersion);

        DeployResult deployResult = parameterMap.get("deployResult");
        assertThat(deployResult, is(notNullValue()));
        assertThat(deployResult.getDeployHash(), is(notNullValue()));
        assertThat(deployResult.getDeployHash().length(), is((hashLength)));
        assertThat(deployResult.getApiVersion(), is((apiVersion)));

        logger.info("deployResult.getDeployHash() {}", deployResult.getDeployHash());
    }


    @Then("wait for a block added event with a timout of {long} seconds")
    public void waitForABlockAddedEventWithATimoutOfSeconds(long timeout) throws InterruptedException {

        logger.info("Then wait for a block added event with a timout of {} seconds", timeout);

        // TODO use semaphores
        Thread.sleep(timeout * 1000L);

        DeployResult deployResult = parameterMap.get("deployResult");
        final BlockAdded lastBlockAdded = parameterMap.get("lastBlockAdded");
        assertThat(lastBlockAdded, is(notNullValue()));

        JsonBlockData block = CasperClientProvider.getInstance().getCasperService().getBlock(new HashBlockIdentifier(lastBlockAdded.getBlockHash().toString()));
        assertThat(block, is(notNullValue()));
        List<String> deployHashes = block.getBlock().getBody().getDeployHashes();
        assertThat(deployHashes, hasItem(deployResult.getDeployHash()));
    }
}

