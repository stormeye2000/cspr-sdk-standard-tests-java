package com.stormeye.evaluation;

import com.casper.sdk.exception.CasperClientException;
import com.casper.sdk.exception.NoSuchTypeException;
import com.casper.sdk.helper.CasperTransferHelper;
import com.casper.sdk.identifier.block.HashBlockIdentifier;
import com.casper.sdk.identifier.block.HeightBlockIdentifier;
import com.casper.sdk.model.block.JsonBlockData;
import com.casper.sdk.model.block.JsonProof;
import com.casper.sdk.model.common.Digest;
import com.casper.sdk.model.common.Ttl;
import com.casper.sdk.model.deploy.*;
import com.casper.sdk.model.era.EraInfoData;
import com.casper.sdk.model.event.Event;
import com.casper.sdk.model.event.EventTarget;
import com.casper.sdk.model.event.EventType;
import com.casper.sdk.model.event.blockadded.BlockAdded;
import com.casper.sdk.model.event.step.Step;
import com.casper.sdk.model.key.PublicKey;
import com.casper.sdk.model.transfer.TransferData;
import com.casper.sdk.service.CasperService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stormeye.event.EventHandler;
import com.stormeye.matcher.EraMatcher;
import com.stormeye.matcher.ExpiringMatcher;
import com.stormeye.utils.*;
import com.syntifi.crypto.key.Ed25519PrivateKey;
import com.syntifi.crypto.key.Ed25519PublicKey;
import dev.oak3.sbs4j.exception.ValueSerializationException;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.stormeye.matcher.BlockAddedMatchers.hasTransferHashWithin;
import static com.stormeye.matcher.NctlMatchers.isValidMerkleProof;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Step Definitions for Block Cucumber Tests.
 */
public class BlockStepDefinitions {

    private final Logger logger = LoggerFactory.getLogger(BlockStepDefinitions.class);
    private static final String invalidBlockHash = "2fe9630b7790852e4409d815b04ca98f37effcdf9097d317b9b9b8ad658f47c8";
    private static final long invalidHeight = 9999999999L;
    private static final String blockErrorMsg = "block not known";
    private static final String blockErrorCode = "-32001";
    private final ContextMap contextMap = ContextMap.getInstance();
    private static EventHandler blockEventHandler;
    private static EventHandler eraEventHandler;
    private final ObjectMapper mapper = new ObjectMapper();
    private final TestProperties testProperties = new TestProperties();

    @BeforeAll
    public static void setUp() {
        blockEventHandler = new EventHandler(EventTarget.POJO);
        eraEventHandler = new EventHandler(EventTarget.RAW);
        ContextMap.getInstance().clear();
    }

    @SuppressWarnings("unused")
    @AfterAll
    void tearDown() {
        blockEventHandler.close();
        eraEventHandler.close();
    }

    private static CasperService getCasperService() {
        return CasperClientProvider.getInstance().getCasperService();
    }


    @Given("that the latest block is requested")
    public void thatTheLatestBlockIsRequested() {

        logger.info("Given that the latest block is requested");

        contextMap.put("blockDataSdk", getCasperService().getBlock());
    }

    @Then("a valid error message is returned")
    public void aValidErrorMessageIsReturned() {

        logger.info("Then a valid error message is returned");

        final CasperClientException csprClientException = contextMap.get("csprClientException");

        assertThat(csprClientException.getMessage(), is(notNullValue()));
        assertThat(csprClientException.getMessage().toLowerCase().contains(blockErrorMsg), is(true));
        assertThat(csprClientException.getMessage().toLowerCase().contains(blockErrorCode), is(true));
    }

    private PublicKey getPublicKey(final String key) {
        try {
            final PublicKey publicKey = new PublicKey();
            publicKey.createPublicKey(key);
            return publicKey;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateBlockHash(final Digest hash) {
        assertThat(hash, is(notNullValue()));
        assertThat(hash.getDigest(), is(notNullValue()));
        assertThat(hash.getClass(), is(Digest.class));
        assertThat(hash.isValid(), is(true));
    }

    @Then("the deploy response contains a valid deploy hash")
    public void theDeployResponseContainsAValidDeployHash() {

        logger.info("Then the deploy response contains a valid deploy hash");

        final DeployResult deployResult = contextMap.get("deployResult");
        assertThat(deployResult, is(notNullValue()));
        assertThat(deployResult.getDeployHash(), is(notNullValue()));

    }

    @Then("request the block transfer")
    public void requestTheBlockTransfer() throws Exception {

        logger.info("Then request the block transfer");

        final DeployResult deployResult = contextMap.get("deployResult");

        final ExpiringMatcher<Event<BlockAdded>> matcher = (ExpiringMatcher<Event<BlockAdded>>) blockEventHandler.addEventMatcher(
                EventType.MAIN,
                hasTransferHashWithin(
                        deployResult.getDeployHash(),
                        blockAddedEvent -> contextMap.put("matchingBlock", blockAddedEvent.getData())
                )
        );

        assertThat(matcher.waitForMatch(300), is(true));
        blockEventHandler.removeEventMatcher(EventType.MAIN, matcher);

        contextMap.put("transferBlockSdk", getCasperService().getBlockTransfers());

    }

    @Then("request the latest block via the test node")
    public void requestTheLatestBlockViaTheTestNode() {
        logger.info("Then request the latest block via the test node");
        contextMap.put("blockDataNode", NctlUtils.getChainBlock(testProperties.getDockerName()));
    }

    @Then("request a block by hash via the test node")
    public void requestABlockByHashViaTheTestNode() {
        logger.info("Then request a block by hash via the test node");

        contextMap.put("blockDataNode", NctlUtils.getChainBlock(contextMap.get("latestBlock")));
    }

    @Then("request the returned block from the test node via its hash")
    public void requestTheReturnedBlockFromTheTestNodeViaItsHash() {
        logger.info("Then request the returned block from the test node via its hash");

        //NCTL doesn't have get block via height, so we use the sdk's returned block has
        contextMap.put("blockDataNode", NctlUtils.getChainBlock(contextMap.get(contextMap.get("blockHashSdk"))));
    }

    @Given("that a test node era switch block is requested")
    public void thatATestNodeEraSwitchBlockIsRequested() {

        logger.info("Given that a test node era switch block is requested");

        contextMap.put("nodeEraSwitchBlockResult", NctlUtils.getChainEraInfo());
    }

    @Then("wait for the the test node era switch block step event")
    public void waitForTheTheTestNodeEraSwitchBlock() throws Exception {
        logger.info("Then wait for the test node era switch block step event");

        final ExpiringMatcher<Event<Step>> matcher = (ExpiringMatcher<Event<Step>>) eraEventHandler.addEventMatcher(
                EventType.MAIN,
                EraMatcher.theEraHasChanged()
        );

        assertThat(matcher.waitForMatch(5000L), is(true));

        final JsonNode result = NctlUtils.getChainEraInfo();

        eraEventHandler.removeEventMatcher(EventType.MAIN, matcher);

        assertThat(result.get("era_summary").get("block_hash").textValue(), is(notNullValue()));
        validateBlockHash(new Digest(result.get("era_summary").get("block_hash").textValue()));

        contextMap.put("nodeEraSwitchBlock", result.get("era_summary").get("block_hash").textValue());
        contextMap.put("nodeEraSwitchData", result);
    }

    @Then("request the block transfer from the test node")
    public void requestTheBlockTransferFromTheTestNode() {

        logger.info("Then request the block transfer from the test node");

        final TransferData transferData = contextMap.get("transferBlockSdk");
        contextMap.put("transferBlockNode", NctlUtils.getChainBlockTransfers(transferData.getBlockHash()));
    }

    @Then("the body of the returned block is equal to the body of the returned test node block")
    public void theBodyOfTheReturnedBlockIsEqualToTheBodyOfTheReturnedTestNodeBlock() throws JsonProcessingException {

        logger.info("Then the body of the returned block is equal to the body of the returned test node block");

        final JsonBlockData latestBlockSdk = contextMap.get("blockDataSdk");
        final JsonNode latestBlockNode = mapper.readTree(contextMap.get("blockDataNode").toString());

        assertThat(latestBlockSdk.getBlock().getBody(), is(notNullValue()));

        assertThat(latestBlockSdk.getBlock().getBody().getProposer().toString().equals(latestBlockNode.get("body").get("proposer").asText()), is(true));

        if (latestBlockNode.get("body").get("deploy_hashes").size() == 0) {
            assertThat(latestBlockSdk.getBlock().getBody().getDeployHashes().isEmpty(), is(true));
        } else {
            latestBlockNode.get("body").findValues("deploy_hashes").forEach(
                    d -> assertThat(latestBlockSdk.getBlock().getBody().getDeployHashes().contains(d.textValue()), is(true))
            );
        }
        if (latestBlockNode.get("body").get("transfer_hashes").size() == 0) {
            assertThat(latestBlockSdk.getBlock().getBody().getTransferHashes().isEmpty(), is(true));
        } else {
            latestBlockNode.get("body").findValues("transfer_hashes").forEach(
                    t -> assertThat(latestBlockSdk.getBlock().getBody().getTransferHashes().contains(t.textValue()), is(true))
            );
        }

    }

    @And("the hash of the returned block is equal to the hash of the returned test node block")
    public void theHashOfTheReturnedBlockIsEqualToTheHashOfTheReturnedTestNodeBlock() throws JsonProcessingException {
        logger.info("And the hash of the returned block is equal to the hash of the returned test node block");

        final JsonBlockData latestBlockSdk = contextMap.get("blockDataSdk");
        final JsonNode latestBlockNode = mapper.readTree(contextMap.get("blockDataNode").toString());

        assertThat(latestBlockSdk.getBlock().getHash().toString().equals(latestBlockNode.get("hash").asText()), is(true));
    }

    @And("the header of the returned block is equal to the header of the returned test node block")
    public void theHeaderOfTheReturnedBlockIsEqualToTheHeaderOfTheReturnedTestNodeBlock() throws JsonProcessingException {
        logger.info("And the header of the returned block is equal to the header of the returned test node block");

        final JsonBlockData latestBlockSdk = contextMap.get("blockDataSdk");
        final JsonNode latestBlockNode = mapper.readTree(contextMap.get("blockDataNode").toString());

        assertThat(latestBlockSdk.getBlock().getHeader().getEraId(), is(latestBlockNode.get("header").get("era_id").asLong()));
        assertThat(latestBlockSdk.getBlock().getHeader().getHeight(), is(latestBlockNode.get("header").get("height").asLong()));
        assertThat(latestBlockSdk.getBlock().getHeader().getProtocolVersion(), is(latestBlockNode.get("header").get("protocol_version").asText()));

        assertThat(latestBlockSdk.getBlock().getHeader().getAccumulatedSeed()
                .equals(new Digest(latestBlockNode.get("header").get("accumulated_seed").asText())), is(true));
        assertThat(latestBlockSdk.getBlock().getHeader().getBodyHash()
                .equals(new Digest(latestBlockNode.get("header").get("body_hash").asText())), is(true));
        assertThat(latestBlockSdk.getBlock().getHeader().getStateRootHash()
                .equals(new Digest(latestBlockNode.get("header").get("state_root_hash").asText())), is(true));
        assertThat(latestBlockSdk.getBlock().getHeader().getTimeStamp()
                .compareTo(new DateTime(latestBlockNode.get("header").get("timestamp").asText()).toDate()), is(0));
    }

    @And("the proofs of the returned block are equal to the proofs of the returned test node block")
    public void theProofsOfTheReturnedBlockAreEqualToTheProofsOfTheReturnedTestNodeBlock() throws JsonProcessingException {

        logger.info("And the proofs of the returned block are equal to the proofs of the returned test node block");

        final JsonBlockData latestBlockSdk = contextMap.get("blockDataSdk");
        final JsonNode latestBlockNode = mapper.readTree(contextMap.get("blockDataNode").toString());

        final List<JsonProof> proofsSdk = latestBlockSdk.getBlock().getProofs();
        assertThat(latestBlockNode.get("proofs").findValues("public_key").size() == proofsSdk.size(), is(true));

        latestBlockNode.get("proofs").findValues("public_key").forEach(
                p -> assertThat((int) proofsSdk.stream().filter(q -> p.asText().equals(q.getPublicKey().toString())).count(), is(1))
        );

        latestBlockNode.get("proofs").findValues("signature").forEach(
                p -> assertThat((int) proofsSdk.stream().filter(q -> p.asText().equals(q.getSignature().toString())).count(), is(1))
        );
    }

    @And("the switch block hashes of the returned block are equal to the switch block hashes of the returned test node block")
    public void theSwitchBlockHashesOfTheReturnedBlockAreEqualToTheSwitchBlockHashesOfTheReturnedTestNodeBlock() {

        logger.info("And the switch block hashes of the returned block are equal to the switch block hashes of the returned test node block");

        final EraInfoData data = contextMap.get("eraSwitchBlockData");

        assertThat(contextMap.get("nodeEraSwitchBlock").equals(data.getEraSummary().getBlockHash()), is(true));
    }

    @And("the switch block eras of the returned block are equal to the switch block eras of the returned test node block")
    public void theSwitchBlockErasOfTheReturnedBlockAreEqualToTheSwitchBlockErasOfTheReturnedTestNodeBlock() throws JsonProcessingException {
        logger.info("And the switch block eras are equal");

        final EraInfoData data = contextMap.get("eraSwitchBlockData");
        final JsonNode node = mapper.readTree(contextMap.get("nodeEraSwitchData").toString());

        assertThat(node.get("era_summary").get("era_id").toString().equals(data.getEraSummary().getEraId().toString()), is(true));
    }

    @And("the switch block merkle proofs of the returned block are equal to the switch block merkle proofs of the returned test node block")
    public void theSwitchBlockMerkleProofsOfTheReturnedBlockAreEqualToTheSwitchBlockMerkleProofsOfTheReturnedTestNodeBlock() throws JsonProcessingException {
        logger.info("And the switch block merkle proofs of the returned block are equal to the switch block merkle proofs of the returned test node block");

        final EraInfoData data = contextMap.get("eraSwitchBlockData");
        final JsonNode node = mapper.readTree(contextMap.get("nodeEraSwitchData").toString());

        assertThat(data.getEraSummary().getMerkleProof(), is(isValidMerkleProof(node.get("era_summary").get("merkle_proof").asText())));

        final Digest digest = new Digest(data.getEraSummary().getMerkleProof());
        assertThat(digest.isValid(), is(true));
    }

    @And("the switch block state root hashes of the returned block are equal to the switch block state root hashes of the returned test node block")
    public void theSwitchBlockStateRootHashesOfTheReturnedBlockAreEqualToTheSwitchBlockStateRootHashesOfTheReturnedTestNodeBlock() throws JsonProcessingException {

        logger.info("And the switch block state root hashes of the returned block are equal to the switch block state root hashes of the returned test node block");

        final EraInfoData data = contextMap.get("eraSwitchBlockData");
        final JsonNode node = mapper.readTree(contextMap.get("nodeEraSwitchData").toString());

        assertThat(node.get("era_summary").get("state_root_hash").asText().equals(data.getEraSummary().getStateRootHash()), is(true));
    }

    @And("the delegators data of the returned block is equal to the delegators data of the returned test node block")
    public void theDelegatorsDataOfTheReturnedBlockIsEqualToTheDelegatorsDataOfTheReturnedTestNodeBlock() throws JsonProcessingException {
        logger.info("And the delegators data of the returned block is equal to the delegators data of the returned test node block");

        final EraInfoData data = contextMap.get("eraSwitchBlockData");
        final JsonNode allocations = mapper.readTree(contextMap.get("nodeEraSwitchData").toString())
                .get("era_summary").get("stored_value").get("EraInfo").get("seigniorage_allocations");

        final List<SeigniorageAllocation> delegatorsSdk = data.getEraSummary()
                .getStoredValue().getValue().getSeigniorageAllocations()
                .stream()
                .filter(q -> q instanceof Delegator)
                .map(d -> (Delegator) d)
                .collect(Collectors.toList());

        allocations.findValues("Delegator").forEach(
                d -> {
                    final List<SeigniorageAllocation> found = delegatorsSdk
                            .stream()
                            .filter(q -> getPublicKey(d.get("delegator_public_key").asText()).equals(((Delegator) q).getDelegatorPublicKey()))
                            .collect(Collectors.toList());

                    assertThat(found.isEmpty(), is(false));
                    assertThat(d.get("validator_public_key").asText().equals(((Delegator) found.get(0)).getValidatorPublicKey().toString()), is(true));
                    assertThat(d.get("amount").asText().equals(found.get(0).getAmount().toString()), is(true));
                }
        );
    }

    @And("the validators data of the returned block is equal to the validators data of the returned test node block")
    public void theValidatorsDataOfTheReturnedBlockIsEqualToTheValidatorsDataOfTheReturnedTestNodeBlock() throws JsonProcessingException {

        logger.info("And the validators data of the returned block is equal to the validators data of the returned test node block");

        final EraInfoData data = contextMap.get("eraSwitchBlockData");

        final JsonNode allocations = mapper.readTree(contextMap.get("nodeEraSwitchData").toString())
                .get("era_summary").get("stored_value").get("EraInfo").get("seigniorage_allocations");

        final List<SeigniorageAllocation> validatorsSdk = data.getEraSummary()
                .getStoredValue().getValue().getSeigniorageAllocations()
                .stream()
                .filter(q -> q instanceof Validator)
                .map(d -> (Validator) d)
                .collect(Collectors.toList());

        allocations.findValues("Validator").forEach(
                d -> {
                    final List<SeigniorageAllocation> found = validatorsSdk
                            .stream()
                            .filter(q -> getPublicKey(d.get("validator_public_key").asText()).equals(((Validator) q).getValidatorPublicKey()))
                            .collect(Collectors.toList());

                    assertThat(found.isEmpty(), is(false));
                    assertThat(d.get("amount").asText().equals(found.get(0).getAmount().toString()), is(true));
                }
        );
    }

    @Given("that chain transfer data is initialised")
    public void thatChainTransferDataIsInitialised() throws IOException {
        logger.info("Given that chain transfer data is initialised");

        final Ed25519PrivateKey senderKey = new Ed25519PrivateKey();
        final Ed25519PublicKey receiverKey = new Ed25519PublicKey();

        senderKey.readPrivateKey(AssetUtils.getUserKeyAsset(1, 1, "secret_key.pem").getFile());
        receiverKey.readPublicKey(AssetUtils.getUserKeyAsset(1, 2, "public_key.pem").getFile());

        contextMap.put("senderKey", senderKey);
        contextMap.put("receiverKey", receiverKey);
        contextMap.put("transferAmount", BigInteger.valueOf(2500000000L));
        contextMap.put("gasPrice", 1L);
        contextMap.put("ttl", Ttl.builder().ttl(30 + "m").build());
    }

    @When("the deploy data is put on chain")
    public void theDeployDataIsPutOnChain() throws NoSuchTypeException, GeneralSecurityException, ValueSerializationException {
        logger.info("When the deploy data is put on chain");

        final Deploy deploy = CasperTransferHelper.buildTransferDeploy(
                contextMap.get("senderKey"),
                PublicKey.fromAbstractPublicKey(contextMap.get("receiverKey")),
                contextMap.get("transferAmount"),
                "casper-net-1",
                Math.abs(new Random().nextLong()),
                BigInteger.valueOf(100000000L),
                contextMap.get("gasPrice"),
                contextMap.get("ttl"),
                new Date(),
                new ArrayList<>());

        final CasperService casperService = CasperClientProvider.getInstance().getCasperService();

        contextMap.put("deployResult", casperService.putDeploy(deploy));
    }

    @And("the returned block contains the transfer hash returned from the test node block")
    public void theReturnedBlockContainsTheTransferHashReturnedFromTheTestNodeBlock() throws JsonProcessingException {

        logger.info("And the returned block contains the transfer hash returned from the test node block");

        final DeployResult deployResult = contextMap.get("deployResult");
        final List<String> transferHashes = new ArrayList<>();

        mapper.readTree(contextMap.get("transferBlockNode").toString()).get("body").get("transfer_hashes").forEach(
                t -> {
                    if (t.textValue().equals(deployResult.getDeployHash())) {
                        transferHashes.add(t.textValue());
                    }
                }
        );

        assertThat(transferHashes.size() > 0, is(true));
    }

    @Given("that the latest block is requested via the sdk")
    public void thatTheLatestBlockIsRequestedViaTheSdk() {

        logger.info("Given that the latest block is requested via the sdk");

        contextMap.put("blockDataSdk", getCasperService().getBlock());
    }

    @Given("that a block is returned by hash via the sdk")
    public void thatABlockIsReturnedByHashViaTheSdk() {
        logger.info("Given that a block is returned by hash via the sdk");

        contextMap.put("latestBlock", getCasperService().getBlock().getBlock().getHash().toString());
        contextMap.put("blockDataSdk", getCasperService().getBlock(new HashBlockIdentifier(contextMap.get("latestBlock"))));
    }

    @Given("that a block is returned by height {int} via the sdk")
    public void thatABlockIsReturnedByHeightViaTheSdk(int height) {
        logger.info("Given that a block is returned by height [{}] via the sdk", height);

        contextMap.put("blockDataSdk", getCasperService().getBlock(new HeightBlockIdentifier(height)));
        contextMap.put("blockHashSdk", getCasperService().getBlock(new HeightBlockIdentifier(height)).getBlock().getHash().toString());
    }

    @Given("that an invalid block hash is requested via the sdk")
    public void thatAnInvalidBlockHashIsRequestedViaTheSdk() {
        logger.info("Given that an invalid block hash is requested via the sdk");

        contextMap.put("csprClientException",
                assertThrows(CasperClientException.class,
                        () -> getCasperService().getBlock(new HashBlockIdentifier(invalidBlockHash)))
        );
    }

    @Given("that an invalid block height is requested via the sdk")
    public void thatAnInvalidBlockHeightIsRequestedViaTheSdk() {

        logger.info("Given that an invalid block height is requested");

        contextMap.put("csprClientException",
                assertThrows(CasperClientException.class,
                        () -> getCasperService().getBlock(new HeightBlockIdentifier(invalidHeight)))
        );
    }

    @Then("request the corresponding era switch block via the sdk")
    public void requestTheCorrespondingEraSwitchBlockViaTheSdk() {
        logger.info("Then request the corresponding era switch block via the sdk");

        contextMap.put("eraSwitchBlockData", getCasperService().getEraInfoBySwitchBlock(new HashBlockIdentifier(contextMap.get("nodeEraSwitchBlock"))));
    }

    @Given("that a step event is received")
    public void thatAStepEventIsReceived() throws Exception {
        logger.info("Then wait for the test node era switch block step event");

        final ExpiringMatcher<Event<Step>> matcher = (ExpiringMatcher<Event<Step>>) eraEventHandler.addEventMatcher(
                EventType.MAIN,
                EraMatcher.theEraHasChanged()
        );

        assertThat(matcher.waitForMatch(5000L), is(true));

        final JsonNode result = NctlUtils.getChainEraInfo();

        eraEventHandler.removeEventMatcher(EventType.MAIN, matcher);

        assertThat(result.get("era_summary").get("block_hash").textValue(), is(notNullValue()));
        validateBlockHash(new Digest(result.get("era_summary").get("block_hash").textValue()));

        contextMap.put("nodeEraSwitchBlock", result.get("era_summary").get("block_hash").textValue());
        contextMap.put("nodeEraSwitchData", result);
    }
}
