package com.stormeye.evaluation;

import com.casper.sdk.exception.CasperClientException;
import com.casper.sdk.exception.NoSuchTypeException;
import com.casper.sdk.identifier.block.HashBlockIdentifier;
import com.casper.sdk.identifier.block.HeightBlockIdentifier;
import com.casper.sdk.model.block.JsonBlockData;
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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;


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
        parameterMap.put("blockData", getCasperService().getBlock());
    }

    @Then("a valid block is returned")
    public void aValidBlockIsReturned() {
        final JsonBlockData blockData = parameterMap.get("blockData");
        assertNotNull(blockData);
        assertEquals(blockData.getClass(), JsonBlockData.class);
    }

    @And("with a valid hash")
    public void withAValidHash() {
        final JsonBlockData blockData = parameterMap.get("blockData");

        validateBlockHash(blockData.getBlock().getHash());

    }

    @And("with a valid body")
    public void withAValidBody() {
        final JsonBlockData blockData = parameterMap.get("blockData");
        assertNotNull(blockData.getBlock().getBody());
    }

    @And("with valid headers")
    public void withValidHeaders() {
        final JsonBlockData blockData = parameterMap.get("blockData");

        assertNotNull(blockData.getBlock().getHeader());

        assertNotNull(blockData.getBlock().getHeader().getStateRootHash());
        assertNotNull(blockData.getBlock().getHeader().getBodyHash());
        assertNotNull(blockData.getBlock().getHeader().getParentHash());
        assertNotNull(blockData.getBlock().getHeader().getAccumulatedSeed());
        assertNotNull(blockData.getBlock().getHeader().getTimeStamp());
        assertNotNull(blockData.getBlock().getHeader().getProtocolVersion());

        assertTrue(blockData.getBlock().getHeader().getHeight() > 0L);
        assertTrue(blockData.getBlock().getHeader().getEraId() >= 0L);

        assertTrue(blockData.getBlock().getHeader().getStateRootHash().isValid());
        assertTrue(blockData.getBlock().getHeader().getBodyHash().isValid());
        assertTrue(blockData.getBlock().getHeader().getParentHash().isValid());
        assertTrue(blockData.getBlock().getHeader().getAccumulatedSeed().isValid());
    }

    @And("with valid proofs")
    public void withValidProofs() {
        final JsonBlockData blockData = parameterMap.get("blockData");
        assertNotNull(blockData.getBlock().getProofs());
    }

    @Given("that a block is returned by hash")
    public void thatABlockIsReturnedByHash() {
        final JsonBlockData latestBlock = getCasperService().getBlock();
        parameterMap.put("blockData", getCasperService().getBlock(new HashBlockIdentifier(latestBlock.getBlock().getHash().toString())));
    }

    @Given("that a block is returned by height {int}")
    public void thatABlockIsReturnedByHeight(long height) {
        parameterMap.put("blockData", getCasperService().getBlock(new HeightBlockIdentifier(height)));
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

        assertNotNull(csprClientException.getMessage());
        assertTrue(csprClientException.getMessage().toLowerCase().contains(blockErrorMsg));
        assertTrue(csprClientException.getMessage().toLowerCase().contains(blockErrorCode));
    }


    @Given("that a transfer block is requested")
    public void thatATransferBlockIsRequested() throws NoSuchTypeException, GeneralSecurityException, ValueSerializationException, IOException {

//        DeployResult result = doTransfer();
//
//        DeployData deploy = getCasperService().getDeploy(result.getDeployHash());
//
//        final TransferData blockTransfers = getCasperService().getBlockTransfers();

    }

    @Then("valid era switch data is returned")
    public void validEraSwitchDataIsReturned() {

        final EraInfoData eraInfoData = parameterMap.get("eraSwitchBlockData");
        assertNotNull(eraInfoData);
        assertNotNull(eraInfoData.getEraSummary());

    }

    @Given("that a NCTL era switch block is requested")
    public void thatANCTLEraSwitchBlockIsRequested() {

        //Query NCTL to get the next era switch info
        JsonNode result = execUtils.execute(ExecCommands.NCTL_VIEW_ERA_INFO.getCommand());
        while (!result.hasNonNull("era_summary")){
            result = execUtils.execute(ExecCommands.NCTL_VIEW_ERA_INFO.getCommand());
        }

        assertNotNull(result.get("era_summary").get("block_hash").textValue());
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

        assertEquals(parameterMap.get("nctlEraSwitchBlock"), data.getEraSummary().getBlockHash());

    }

    @And("the switch block eras are equal")
    public void theSwitchBlockErasAreEqual() throws JsonProcessingException {
        final EraInfoData data = parameterMap.get("eraSwitchBlockData");
        final JsonNode node = mapper.readTree(parameterMap.get("nctlEraSwitchData").toString());

        assertEquals(node.get("era_summary").get("era_id").toString(), data.getEraSummary().getEraId().toString());
    }

    @And("the switch block merkle proofs are equal")
    public void theSwitchBlockMerkleProofsAreEqual() throws JsonProcessingException {

        //The merkle proof returned from NCTL is abbreviated eg [10634 hex chars]
        //We can compare string lengths
        final EraInfoData data = parameterMap.get("eraSwitchBlockData");
        final JsonNode node = mapper.readTree(parameterMap.get("nctlEraSwitchData").toString());

        final String merkleCharCount = node.get("era_summary").get("merkle_proof").asText().replaceAll("\\D+","");
        assertEquals(Integer.valueOf(merkleCharCount), data.getEraSummary().getMerkleProof().length());

        final Digest digest = new Digest(data.getEraSummary().getMerkleProof());
        assertTrue(digest.isValid());

    }

    @And("the switch block state root hashes are equal")
    public void theSwitchBlockStateRootHashesAreEqual() throws JsonProcessingException {

        final EraInfoData data = parameterMap.get("eraSwitchBlockData");
        final JsonNode node = mapper.readTree(parameterMap.get("nctlEraSwitchData").toString());

        assertEquals(node.get("era_summary").get("state_root_hash").asText(), data.getEraSummary().getStateRootHash());

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
        assertNotNull(hash);
        assertNotNull(hash.getDigest());
        assertEquals(hash.getClass(), Digest.class);
        assertTrue(hash.isValid());
    }



}
