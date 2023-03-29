package com.stormeye.evaluation;

import com.casper.sdk.exception.CasperClientException;
import com.casper.sdk.exception.NoSuchTypeException;
import com.casper.sdk.identifier.block.HashBlockIdentifier;
import com.casper.sdk.identifier.block.HeightBlockIdentifier;
import com.casper.sdk.model.block.JsonBlockData;
import com.casper.sdk.model.block.JsonProof;
import com.casper.sdk.model.common.Digest;
import com.casper.sdk.model.deploy.Delegator;
import com.casper.sdk.model.deploy.SeigniorageAllocation;
import com.casper.sdk.model.deploy.Validator;
import com.casper.sdk.model.era.EraInfoData;
import com.casper.sdk.model.key.PublicKey;
import com.casper.sdk.model.storedvalue.StoredValueEraInfo;
import com.casper.sdk.service.CasperService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.ExecCommands;
import com.stormeye.utils.ExecUtils;
import com.stormeye.utils.ParameterMap;
import dev.oak3.sbs4j.exception.ValueSerializationException;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.joda.time.DateTime;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class EvaluateBlocks {

    private static final String invalidBlockHash = "2fe9630b7790852e4409d815b04ca98f37effcdf9097d317b9b9b8ad658f47c8";
    private static final long invalidHeight = 9999999999L;
    private static final String blockErrorMsg = "block not known";
    private static final String blockErrorCode = "-32001";
    private static CasperClientException csprClientException;
    private static final ParameterMap parameterMap = ParameterMap.getInstance();

    private final ExecUtils execUtils = new ExecUtils();
    private final ObjectMapper mapper = new ObjectMapper();
    private static EventHandler eventHandler;


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
        parameterMap.put("blockDataSdk", getCasperService().getBlock());
    }

    @Given("that a block is returned by hash")
    public void thatABlockIsReturnedByHash() {

        parameterMap.put("latestBlock", getCasperService().getBlock().getBlock().getHash().toString());
        parameterMap.put("blockDataSdk", getCasperService().getBlock(new HashBlockIdentifier(parameterMap.get("latestBlock"))));
    }

    @Given("that a block is returned by height {int}")
    public void thatABlockIsReturnedByHeight(long height) {
        parameterMap.put("blockDataSdk", getCasperService().getBlock(new HeightBlockIdentifier(height)));
        parameterMap.put("blockHashSdk", getCasperService().getBlock(new HeightBlockIdentifier(height)).getBlock().getHash().toString());
    }

    @Given("that an invalid block hash is requested")
    public void thatAnInvalidBlockHashIsRequested() {
        parameterMap.put("csprClientException",
            csprClientException = assertThrows(CasperClientException.class,
                    () -> getCasperService().getBlock(new HashBlockIdentifier(invalidBlockHash)))
        );
    }

    @Given("that an invalid block height is requested")
    public void thatAnInvalidBlockHeightIsRequested() {
        parameterMap.put("csprClientException",
                csprClientException = assertThrows(CasperClientException.class,
                        () -> getCasperService().getBlock(new HeightBlockIdentifier(invalidHeight)))
        );
    }

    @Then("a valid error message is returned")
    public void aValidErrorMessageIsReturned() {

        final CasperClientException csprClientException = parameterMap.get("csprClientException");

        assertThat(csprClientException.getMessage(), is(notNullValue()));
        assertThat(csprClientException.getMessage().toLowerCase().contains(blockErrorMsg), is(true));
        assertThat(csprClientException.getMessage().toLowerCase().contains(blockErrorCode), is(true));
    }


    @Given("that a transfer block is requested")
    public void thatATransferBlockIsRequested() throws NoSuchTypeException, GeneralSecurityException, ValueSerializationException, IOException {

//        DeployResult result = doTransfer();
//
//        DeployData deploy = getCasperService().getDeploy(result.getDeployHash());
//
//        final TransferData blockTransfers = getCasperService().getBlockTransfers();

    }


    @Given("that a NCTL era switch block is requested")
    public void thatANCTLEraSwitchBlockIsRequested() {
        parameterMap.put("nctlEraSwitchBlockResult", execUtils.execute(ExecCommands.NCTL_VIEW_ERA_INFO.getCommand()));
    }


    @Then("wait for the NCTL era switch block")
    public void waitForTheNCTLEraSwitchBlock() {

        //Query NCTL to get the next era switch info
        JsonNode result = parameterMap.get("nctlEraSwitchBlockResult");
        while (!result.hasNonNull("era_summary")){
            result = execUtils.execute(ExecCommands.NCTL_VIEW_ERA_INFO.getCommand());
        }

        assertThat(result.get("era_summary").get("block_hash").textValue(), is(notNullValue()));
        validateBlockHash(new Digest(result.get("era_summary").get("block_hash").textValue()));

        parameterMap.put("nctlEraSwitchBlock", result.get("era_summary").get("block_hash").textValue());
        parameterMap.put("nctlEraSwitchData", result);

    }


    @Then("request the corresponding era switch block")
    public void requestTheCorrespondingEraSwitchBlock() {
        parameterMap.put("eraSwitchBlockData", getCasperService().getEraInfoBySwitchBlock(new HashBlockIdentifier(parameterMap.get("nctlEraSwitchBlock"))));
    }

    @And("the switch block hashes are equal")
    public void theSwitchBlockHashesAreEqual() {
        final EraInfoData data = parameterMap.get("eraSwitchBlockData");

        assertThat(parameterMap.get("nctlEraSwitchBlock").equals(data.getEraSummary().getBlockHash()), is(true));

    }

    @And("the switch block eras are equal")
    public void theSwitchBlockErasAreEqual() throws JsonProcessingException {
        final EraInfoData data = parameterMap.get("eraSwitchBlockData");
        final JsonNode node = mapper.readTree(parameterMap.get("nctlEraSwitchData").toString());

        assertThat(node.get("era_summary").get("era_id").toString().equals(data.getEraSummary().getEraId().toString()), is(true));
    }

    @And("the switch block merkle proofs are equal")
    public void theSwitchBlockMerkleProofsAreEqual() throws JsonProcessingException {

        //The merkle proof returned from NCTL is abbreviated eg [10634 hex chars]
        //We can compare string lengths
        final EraInfoData data = parameterMap.get("eraSwitchBlockData");
        final JsonNode node = mapper.readTree(parameterMap.get("nctlEraSwitchData").toString());

        final String merkleCharCount = node.get("era_summary").get("merkle_proof").asText().replaceAll("\\D+","");
        assertThat(Integer.valueOf(merkleCharCount), is(data.getEraSummary().getMerkleProof().length()));

        final Digest digest = new Digest(data.getEraSummary().getMerkleProof());
        assertThat(digest.isValid(), is(true));

    }

    @And("the switch block state root hashes are equal")
    public void theSwitchBlockStateRootHashesAreEqual() throws JsonProcessingException {

        final EraInfoData data = parameterMap.get("eraSwitchBlockData");
        final JsonNode node = mapper.readTree(parameterMap.get("nctlEraSwitchData").toString());

        assertThat(node.get("era_summary").get("state_root_hash").asText().equals(data.getEraSummary().getStateRootHash()), is(true));

    }


    @And("the delegator data is equal")
    public void theDelegatorDataIsEqual() throws JsonProcessingException {
        final EraInfoData data = parameterMap.get("eraSwitchBlockData");
        final StoredValueEraInfo info = data.getEraSummary().getStoredValue();
        final JsonNode node = mapper.readTree(parameterMap.get("nctlEraSwitchData").toString());

        final JsonNode allocations = node.get("era_summary").get("stored_value").get("EraInfo").get("seigniorage_allocations");
        final List<JsonNode> delegatorsNctl = allocations.findValues("Delegator");

        final List<SeigniorageAllocation> delegatorsSdk = info.getValue().getSeigniorageAllocations()
                .stream()
                .filter(q -> q instanceof Delegator)
                .map (d -> (Delegator) d)
                .collect(Collectors.toList());

        delegatorsNctl.forEach(
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
        final EraInfoData data = parameterMap.get("eraSwitchBlockData");
        final StoredValueEraInfo info = data.getEraSummary().getStoredValue();
        final JsonNode node = mapper.readTree(parameterMap.get("nctlEraSwitchData").toString());

        final JsonNode allocations = node.get("era_summary").get("stored_value").get("EraInfo").get("seigniorage_allocations");
        final List<JsonNode> validatorsNctl = allocations.findValues("Validator");

        final List<SeigniorageAllocation> validatorsSdk = info.getValue().getSeigniorageAllocations()
                .stream()
                .filter(q -> q instanceof Validator)
                .map (d -> (Validator) d)
                .collect(Collectors.toList());

        validatorsNctl.forEach(
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
        parameterMap.put("blockDataSdk", getCasperService().getBlock());
    }

    @Then("request the latest block via nctl")
    public void requestTheLatestBlockViaNctl() {
        parameterMap.put("blockDataNctl", execUtils.execute(ExecCommands.NCTL_VIEW_CHAIN_BLOCK.getCommand()));
    }


    @Then("the body is equal")
    public void theBodyIsEqual() throws JsonProcessingException {

        final JsonBlockData latestBlockSdk = parameterMap.get("blockDataSdk");
        final JsonNode latestBlockNctl = mapper.readTree(parameterMap.get("blockDataNctl").toString());

        assertThat(latestBlockSdk.getBlock().getBody(), is(notNullValue()));

        assertThat(latestBlockSdk.getBlock().getBody().getProposer().toString().equals(latestBlockNctl.get("body").get("proposer").asText()), is(true));

        if (latestBlockNctl.get("body").get("deploy_hashes").size() == 0){
            assertThat(latestBlockSdk.getBlock().getBody().getDeployHashes().isEmpty(), is(true));
        } else {
            latestBlockNctl.get("body").findValues("deploy_hashes").forEach(
                    d -> assertThat(latestBlockSdk.getBlock().getBody().getDeployHashes().contains(d.textValue()), is(true))
            );
        }
        if (latestBlockNctl.get("body").get("transfer_hashes").size() == 0){
            assertThat(latestBlockSdk.getBlock().getBody().getTransferHashes().isEmpty(), is(true));
        } else {
            latestBlockNctl.get("body").findValues("transfer_hashes").forEach(
                    t -> assertThat(latestBlockSdk.getBlock().getBody().getTransferHashes().contains(t.textValue()), is(true))
            );
        }


    }

    @And("the hash is equal")
    public void theHashIsEqual() throws JsonProcessingException {

        final JsonBlockData latestBlockSdk = parameterMap.get("blockDataSdk");
        final JsonNode latestBlockNctl = mapper.readTree(parameterMap.get("blockDataNctl").toString());

        assertThat(latestBlockSdk.getBlock().getHash().toString().equals(latestBlockNctl.get("hash").asText()), is(true));
        
    }

    @And("the header is equal")
    public void theHeaderIsEqual() throws JsonProcessingException {

        final JsonBlockData latestBlockSdk = parameterMap.get("blockDataSdk");
        final JsonNode latestBlockNctl = mapper.readTree(parameterMap.get("blockDataNctl").toString());

        assertThat(latestBlockSdk.getBlock().getHeader().getEraId(), is(latestBlockNctl.get("header").get("era_id").asLong()));
        assertThat(latestBlockSdk.getBlock().getHeader().getHeight(), is(latestBlockNctl.get("header").get("height").asLong()));
        assertThat(latestBlockSdk.getBlock().getHeader().getProtocolVersion(), is(latestBlockNctl.get("header").get("protocol_version").asText()));

        assertThat(latestBlockSdk.getBlock().getHeader().getAccumulatedSeed()
                .equals(new Digest(latestBlockNctl.get("header").get("accumulated_seed").asText())), is(true));
        assertThat(latestBlockSdk.getBlock().getHeader().getBodyHash()
                .equals(new Digest(latestBlockNctl.get("header").get("body_hash").asText())), is(true));
        assertThat(latestBlockSdk.getBlock().getHeader().getStateRootHash()
                .equals(new Digest(latestBlockNctl.get("header").get("state_root_hash").asText())), is(true));
        assertThat(latestBlockSdk.getBlock().getHeader().getTimeStamp()
                .compareTo(new DateTime(latestBlockNctl.get("header").get("timestamp").asText()).toDate()), is(0));

    }

    @And("the proofs are equal")
    public void theProofsAreEqual() throws JsonProcessingException {

        final JsonBlockData latestBlockSdk = parameterMap.get("blockDataSdk");
        final JsonNode latestBlockNctl = mapper.readTree(parameterMap.get("blockDataNctl").toString());

        final List<JsonProof> proofsSdk = latestBlockSdk.getBlock().getProofs();
        assertThat(latestBlockNctl.get("proofs").findValues("public_key").size() == proofsSdk.size(), is(true));

        latestBlockNctl.get("proofs").findValues("public_key").forEach(
                p -> assertThat((int) proofsSdk.stream().filter(q -> p.asText().equals(q.getPublicKey().toString())).count(), is(1))
        );

        latestBlockNctl.get("proofs").findValues("signature").forEach(
                p -> assertThat((int) proofsSdk.stream().filter(q -> p.asText().equals(q.getSignature().toString())).count(), is(1))
        );

    }

    @Then("request a block by hash via nctl")
    public void requestABlockByHashViaNctl() {

        parameterMap.put("blockDataNctl", execUtils.execute(ExecCommands.NCTL_VIEW_CHAIN_BLOCK.getCommand(
                "block=" + parameterMap.get("latestBlock"))
        ));

    }

    @Then("request the returned block from nctl via its hash")
    public void requestTheReturnedBlockFromNctlViaItsHash() {
        //NCTL doesn't have get block via height, so we use the sdk's returned block has
        parameterMap.put("blockDataNctl", execUtils.execute(ExecCommands.NCTL_VIEW_CHAIN_BLOCK.getCommand(
                "block=" + parameterMap.get("blockHashSdk")
        )));
    }
}
