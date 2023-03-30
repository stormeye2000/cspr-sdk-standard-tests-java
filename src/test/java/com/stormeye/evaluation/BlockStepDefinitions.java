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
import com.casper.sdk.model.event.EventType;
import com.casper.sdk.model.event.blockadded.BlockAdded;
import com.casper.sdk.model.key.PublicKey;
import com.casper.sdk.model.transfer.TransferData;
import com.casper.sdk.service.CasperService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static com.stormeye.evaluation.BlockAddedMatchers.hasTransferHashWithin;
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
    private static CasperClientException csprClientException;
    private static final ParameterMap parameterMap = ParameterMap.getInstance();

    private final ExecUtils execUtils = new ExecUtils();
    private final ObjectMapper mapper = new ObjectMapper();
    private static EventHandler eventHandler;

    private final TestProperties testProperties = new TestProperties();

    @BeforeAll
    public static void setUp() throws InterruptedException {
        parameterMap.clear();
        eventHandler = new EventHandler();
    }


    @SuppressWarnings("unused")
    @AfterAll
    void tearDown() {
        eventHandler.close();
    }

    private static  CasperService getCasperService() {
        return CasperClientProvider.getInstance().getCasperService();
    }


    @Given("that the latest block is requested")
    public void thatTheLatestBlockIsRequested() {

        logger.info("Given that the latest block is requested");

        parameterMap.put("blockDataSdk", getCasperService().getBlock());
    }

    @Given("that a block is returned by hash")
    public void thatABlockIsReturnedByHash() {

        logger.info("Given that a block is returned by hash");

        parameterMap.put("latestBlock", getCasperService().getBlock().getBlock().getHash().toString());
        parameterMap.put("blockDataSdk", getCasperService().getBlock(new HashBlockIdentifier(parameterMap.get("latestBlock"))));
    }

    @Given("that a block is returned by height {int}")
    public void thatABlockIsReturnedByHeight(long height) {

        logger.info("Given that a block is returned by height [{}]", height);

        parameterMap.put("blockDataSdk", getCasperService().getBlock(new HeightBlockIdentifier(height)));
        parameterMap.put("blockHashSdk", getCasperService().getBlock(new HeightBlockIdentifier(height)).getBlock().getHash().toString());
    }

    @Given("that an invalid block hash is requested")
    public void thatAnInvalidBlockHashIsRequested() {

        logger.info("Given that an invalid block hash is requested");

        parameterMap.put("csprClientException",
            csprClientException = assertThrows(CasperClientException.class,
                    () -> getCasperService().getBlock(new HashBlockIdentifier(invalidBlockHash)))
        );
    }

    @Given("that an invalid block height is requested")
    public void thatAnInvalidBlockHeightIsRequested() {

        logger.info("Given that an invalid block height is requested");

        parameterMap.put("csprClientException",
                csprClientException = assertThrows(CasperClientException.class,
                        () -> getCasperService().getBlock(new HeightBlockIdentifier(invalidHeight)))
        );
    }

    @Then("a valid error message is returned")
    public void aValidErrorMessageIsReturned() {

        logger.info("Then a valid error message is returned");

        final CasperClientException csprClientException = parameterMap.get("csprClientException");

        assertThat(csprClientException.getMessage(), is(notNullValue()));
        assertThat(csprClientException.getMessage().toLowerCase().contains(blockErrorMsg), is(true));
        assertThat(csprClientException.getMessage().toLowerCase().contains(blockErrorCode), is(true));
    }

    @Then("request the corresponding era switch block")
    public void requestTheCorrespondingEraSwitchBlock() {

        logger.info("Then request the corresponding era switch block");

        parameterMap.put("eraSwitchBlockData", getCasperService().getEraInfoBySwitchBlock(new HashBlockIdentifier(parameterMap.get("nodeEraSwitchBlock"))));
    }

    @And("the switch block hashes are equal")
    public void theSwitchBlockHashesAreEqual() {

        logger.info("And the switch block hashes are equal");

        final EraInfoData data = parameterMap.get("eraSwitchBlockData");

        assertThat(parameterMap.get("nodeEraSwitchBlock").equals(data.getEraSummary().getBlockHash()), is(true));

    }

    @And("the switch block eras are equal")
    public void theSwitchBlockErasAreEqual() throws JsonProcessingException {

        logger.info("And the switch block eras are equal");

        final EraInfoData data = parameterMap.get("eraSwitchBlockData");
        final JsonNode node = mapper.readTree(parameterMap.get("nodeEraSwitchData").toString());

        assertThat(node.get("era_summary").get("era_id").toString().equals(data.getEraSummary().getEraId().toString()), is(true));
    }

    @And("the switch block merkle proofs are equal")
    public void theSwitchBlockMerkleProofsAreEqual() throws JsonProcessingException {

        logger.info("And the switch block merkle proofs are equal");

        //The merkle proof returned from NCTL is abbreviated eg [10634 hex chars]
        //We can compare string lengths
        final EraInfoData data = parameterMap.get("eraSwitchBlockData");
        final JsonNode node = mapper.readTree(parameterMap.get("nodeEraSwitchData").toString());

        final String merkleCharCount = node.get("era_summary").get("merkle_proof").asText().replaceAll("\\D+","");
        assertThat(Integer.valueOf(merkleCharCount), is(data.getEraSummary().getMerkleProof().length()));

        final Digest digest = new Digest(data.getEraSummary().getMerkleProof());
        assertThat(digest.isValid(), is(true));

    }

    @And("the switch block state root hashes are equal")
    public void theSwitchBlockStateRootHashesAreEqual() throws JsonProcessingException {

        logger.info("And the switch block state root hashes are equal");

        final EraInfoData data = parameterMap.get("eraSwitchBlockData");
        final JsonNode node = mapper.readTree(parameterMap.get("nodeEraSwitchData").toString());

        assertThat(node.get("era_summary").get("state_root_hash").asText().equals(data.getEraSummary().getStateRootHash()), is(true));

    }


    @And("the delegator data is equal")
    public void theDelegatorDataIsEqual() throws JsonProcessingException {

        logger.info("And the delegator data is equal");

        final EraInfoData data = parameterMap.get("eraSwitchBlockData");
        final JsonNode allocations = mapper.readTree(parameterMap.get("nodeEraSwitchData").toString())
                .get("era_summary").get("stored_value").get("EraInfo").get("seigniorage_allocations");

        final List<SeigniorageAllocation> delegatorsSdk = data.getEraSummary()
                .getStoredValue().getValue().getSeigniorageAllocations()
                .stream()
                .filter(q -> q instanceof Delegator)
                .map (d -> (Delegator) d)
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

    @And("the validator data is equal")
    public void theValidatorDataIsEqual() throws JsonProcessingException {

        logger.info("And the validator data is equal");

        final EraInfoData data = parameterMap.get("eraSwitchBlockData");

        final JsonNode allocations = mapper.readTree(parameterMap.get("nodeEraSwitchData").toString())
                .get("era_summary").get("stored_value").get("EraInfo").get("seigniorage_allocations");

        final List<SeigniorageAllocation> validatorsSdk = data.getEraSummary()
                .getStoredValue().getValue().getSeigniorageAllocations()
                .stream()
                .filter(q -> q instanceof Validator)
                .map (d -> (Validator) d)
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

    private PublicKey getPublicKey(final String key){
        try {
            final PublicKey publicKey = new PublicKey();
            publicKey.createPublicKey(key);
            return publicKey;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateBlockHash(final Digest hash){
        assertThat(hash, is(notNullValue()));
        assertThat(hash.getDigest(), is(notNullValue()));
        assertThat(hash.getClass(), is(Digest.class));
        assertThat(hash.isValid(), is(true));
    }


    @Given("that the latest block is returned")
    public void thatTheLatestBlockIsReturned() {

        logger.info("Given that the latest block is returned");

        parameterMap.put("blockDataSdk", getCasperService().getBlock());
    }


    @Then("the body is equal")
    public void theBodyIsEqual() throws JsonProcessingException {

        logger.info("Then the body is equal");

        final JsonBlockData latestBlockSdk = parameterMap.get("blockDataSdk");
        final JsonNode latestBlockNode = mapper.readTree(parameterMap.get("blockDataNode").toString());

        assertThat(latestBlockSdk.getBlock().getBody(), is(notNullValue()));

        assertThat(latestBlockSdk.getBlock().getBody().getProposer().toString().equals(latestBlockNode.get("body").get("proposer").asText()), is(true));

        if (latestBlockNode.get("body").get("deploy_hashes").size() == 0){
            assertThat(latestBlockSdk.getBlock().getBody().getDeployHashes().isEmpty(), is(true));
        } else {
            latestBlockNode.get("body").findValues("deploy_hashes").forEach(
                    d -> assertThat(latestBlockSdk.getBlock().getBody().getDeployHashes().contains(d.textValue()), is(true))
            );
        }
        if (latestBlockNode.get("body").get("transfer_hashes").size() == 0){
            assertThat(latestBlockSdk.getBlock().getBody().getTransferHashes().isEmpty(), is(true));
        } else {
            latestBlockNode.get("body").findValues("transfer_hashes").forEach(
                    t -> assertThat(latestBlockSdk.getBlock().getBody().getTransferHashes().contains(t.textValue()), is(true))
            );
        }


    }

    @And("the hash is equal")
    public void theHashIsEqual() throws JsonProcessingException {

        logger.info("And the hash is equal");

        final JsonBlockData latestBlockSdk = parameterMap.get("blockDataSdk");
        final JsonNode latestBlockNode = mapper.readTree(parameterMap.get("blockDataNode").toString());

        assertThat(latestBlockSdk.getBlock().getHash().toString().equals(latestBlockNode.get("hash").asText()), is(true));
        
    }

    @And("the header is equal")
    public void theHeaderIsEqual() throws JsonProcessingException {

        logger.info("And the header is equal");

        final JsonBlockData latestBlockSdk = parameterMap.get("blockDataSdk");
        final JsonNode latestBlockNode = mapper.readTree(parameterMap.get("blockDataNode").toString());

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

    @And("the proofs are equal")
    public void theProofsAreEqual() throws JsonProcessingException {

        logger.info("And the proofs are equal");

        final JsonBlockData latestBlockSdk = parameterMap.get("blockDataSdk");
        final JsonNode latestBlockNode = mapper.readTree(parameterMap.get("blockDataNode").toString());

        final List<JsonProof> proofsSdk = latestBlockSdk.getBlock().getProofs();
        assertThat(latestBlockNode.get("proofs").findValues("public_key").size() == proofsSdk.size(), is(true));

        latestBlockNode.get("proofs").findValues("public_key").forEach(
                p -> assertThat((int) proofsSdk.stream().filter(q -> p.asText().equals(q.getPublicKey().toString())).count(), is(1))
        );

        latestBlockNode.get("proofs").findValues("signature").forEach(
                p -> assertThat((int) proofsSdk.stream().filter(q -> p.asText().equals(q.getSignature().toString())).count(), is(1))
        );

    }


    @Given("that a transfer is initiated")
    public void thatATransferIsInitiated() throws IOException {

        logger.info("Given that a transfer is initiated");

        final Ed25519PrivateKey senderKey = new Ed25519PrivateKey();
        final Ed25519PublicKey receiverKey = new Ed25519PublicKey();

        senderKey.readPrivateKey(AssetUtils.getUserKeyAsset(1, 1, "secret_key.pem").getFile());
        receiverKey.readPublicKey(AssetUtils.getUserKeyAsset(1, 2, "public_key.pem").getFile());

        parameterMap.put("senderKey", senderKey);
        parameterMap.put("receiverKey", receiverKey);
        parameterMap.put("transferAmount", BigInteger.valueOf(2500000000L));
        parameterMap.put("gasPrice", 1L);
        parameterMap.put("ttl", Ttl.builder().ttl(30 + "m").build());

    }

    @When("the deploy is put on chain")
    public void theDeployIsPutOnChain() throws NoSuchTypeException, GeneralSecurityException, ValueSerializationException {

        logger.info("When the deploy is put on chain");

        final Deploy deploy = CasperTransferHelper.buildTransferDeploy(
                parameterMap.get("senderKey"),
                PublicKey.fromAbstractPublicKey(parameterMap.get("receiverKey")),
                parameterMap.get("transferAmount"),
                "casper-net-1",
                Math.abs(new Random().nextLong()),
                BigInteger.valueOf(100000000L),
                parameterMap.get("gasPrice"),
                parameterMap.get("ttl"),
                new Date(),
                new ArrayList<>());


        final CasperService casperService = CasperClientProvider.getInstance().getCasperService();

        parameterMap.put("deployResult", casperService.putDeploy(deploy));

    }

    @Then("the deploy response contains a valid deploy hash")
    public void theDeployResponseContainsAValidDeployHash() {

        logger.info("Then the deploy response contains a valid deploy hash");

        final DeployResult deployResult = parameterMap.get("deployResult");
        assertThat(deployResult, is(notNullValue()));
        assertThat(deployResult.getDeployHash(), is(notNullValue()));

    }

    @Then("request the block transfer")
    public void requestTheBlockTransfer() throws Exception {

        logger.info("Then request the block transfer");

        final DeployResult deployResult = parameterMap.get("deployResult");

        final ExpiringMatcher<Event<BlockAdded>> matcher = eventHandler.addEventMatcher(
                EventType.MAIN,
                hasTransferHashWithin(
                        deployResult.getDeployHash(),
                        blockAddedEvent -> parameterMap.put("matchingBlock", blockAddedEvent.getData())
                )
        );

        assertThat(matcher.waitForMatch(300), is(true));

        parameterMap.put("transferBlockSdk", getCasperService().getBlockTransfers());

    }


    @And("the block contains the transfer hash")
    public void theBlockContainsTheTransferHash() throws JsonProcessingException {

        logger.info("And the block contains the transfer hash");

        final DeployResult deployResult = parameterMap.get("deployResult");
        final List<String> transferHashes = new ArrayList<>();

        mapper.readTree(parameterMap.get("transferBlockNode").toString()).get("body").get("transfer_hashes").forEach(
            t -> {
                if (t.textValue().equals(deployResult.getDeployHash())){
                    transferHashes.add(t.textValue());
                }
            }
        );

        assertThat(transferHashes.size() > 0, is(true));

    }

    @Then("request the latest block via the test node")
    public void requestTheLatestBlockViaTheTestNode() {
        logger.info("Then request the latest block via the test node");
        parameterMap.put("blockDataNode", execUtils.execute(ExecCommands.NCTL_VIEW_CHAIN_BLOCK.getCommand(testProperties.getDockerName())));
    }

    @Then("request a block by hash via the test node")
    public void requestABlockByHashViaTheTestNode() {
        logger.info("Then request a block by hash via the test node");

        parameterMap.put("blockDataNode", execUtils.execute(ExecCommands.NCTL_VIEW_CHAIN_BLOCK.getCommand(
                testProperties.getDockerName(), "block=" + parameterMap.get("latestBlock"))
        ));

    }

    @Then("request the returned block from the test node via its hash")
    public void requestTheReturnedBlockFromTheTestNodeViaItsHash() {
        logger.info("Then request the returned block from the test node via its hash");

        //NCTL doesn't have get block via height, so we use the sdk's returned block has
        parameterMap.put("blockDataNode", execUtils.execute(ExecCommands.NCTL_VIEW_CHAIN_BLOCK.getCommand(
                testProperties.getDockerName(), "block=" + parameterMap.get("blockHashSdk")
        )));
    }

    @Given("that a test node era switch block is requested")
    public void thatATestNodeEraSwitchBlockIsRequested() {

        logger.info("Given that a test node era switch block is requested");

        parameterMap.put("nodeEraSwitchBlockResult", execUtils.execute(ExecCommands.NCTL_VIEW_ERA_INFO.getCommand(testProperties.getDockerName())));
    }

    @Then("wait for the the test node era switch block")
    public void waitForTheTheTestNodeEraSwitchBlock() {
        logger.info("Then wait for the test node era switch block");

        //Query NCTL to get the next era switch info
        JsonNode result = parameterMap.get("nodeEraSwitchBlockResult");
        while (!result.hasNonNull("era_summary")){
            result = execUtils.execute(ExecCommands.NCTL_VIEW_ERA_INFO.getCommand(testProperties.getDockerName()));
        }

        assertThat(result.get("era_summary").get("block_hash").textValue(), is(notNullValue()));
        validateBlockHash(new Digest(result.get("era_summary").get("block_hash").textValue()));

        parameterMap.put("nodeEraSwitchBlock", result.get("era_summary").get("block_hash").textValue());
        parameterMap.put("nodeEraSwitchData", result);
    }

    @Then("request the block transfer from the test node")
    public void requestTheBlockTransferFromTheTestNode() {

        logger.info("Then request the block transfer from the test node");

        final TransferData transferData = parameterMap.get("transferBlockSdk");
        parameterMap.put("transferBlockNode", execUtils.execute(ExecCommands.NCTL_VIEW_CHAIN_BLOCK_TRANSFER.getCommand(
                testProperties.getDockerName(), "block=" + transferData.getBlockHash())));
    }
}
