package com.stormeye.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import com.casper.sdk.exception.CasperClientException;
import com.casper.sdk.exception.NoSuchTypeException;
import com.casper.sdk.helper.CasperTransferHelper;
import com.casper.sdk.identifier.block.HashBlockIdentifier;
import com.casper.sdk.identifier.block.HeightBlockIdentifier;
import com.casper.sdk.model.block.JsonBlockData;
import com.casper.sdk.model.common.Digest;
import com.casper.sdk.model.common.Ttl;
import com.casper.sdk.model.deploy.Deploy;
import com.casper.sdk.model.deploy.DeployData;
import com.casper.sdk.model.deploy.DeployResult;
import com.casper.sdk.model.era.EraInfoData;
import com.casper.sdk.model.key.PublicKey;
import com.casper.sdk.model.transfer.TransferData;
import com.casper.sdk.service.CasperService;
import com.stormeye.utils.AssetUtils;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.ParameterMap;
import com.syntifi.crypto.key.Ed25519PrivateKey;
import dev.oak3.sbs4j.exception.ValueSerializationException;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


public class EvaluateBlocks {

    private static final String invalidBlockHash = "2fe9630b7790852e4409d815b04ca98f37effcdf9097d317b9b9b8ad658f47c8";
    private static final long invalidHeight = 9999999999L;
    private static final String blockErrorMsg = "block not known";
    private static final String blockErrorCode = "-32001";
    private static CasperClientException csprClientException;
    private static final ParameterMap parameterMap = new ParameterMap();

    @BeforeAll
    public static void setUp() {
        parameterMap.clear();
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

        validBlockHash(blockData.getBlock().getHash());

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

        DeployResult result = doTransfer();

        DeployData deploy = getCasperService().getDeploy(result.getDeployHash());

        final TransferData blockTransfers = getCasperService().getBlockTransfers();

    }

    @Given("that a block at era switch is requested")
    public void thatABlockAtEraSwitchIsRequested() {

        JsonBlockData block = getCasperService().getBlock();

        while (block.getBlock().getHeader().getEraEnd() == null) {
            block = getCasperService().getBlock(new HashBlockIdentifier(block.getBlock().getHeader().getParentHash().toString()));
        }

        parameterMap.put("eraChangeBlockData", getCasperService().getBlock(new HashBlockIdentifier(block.getBlock().getHeader().getParentHash().toString())));
        parameterMap.put("eraSwitchBlockData", getCasperService().getEraInfoBySwitchBlock(new HashBlockIdentifier(block.getBlock().getHash().toString())));

    }

    @Then("valid era switch data is returned")
    public void validEraSwitchDataIsReturned() {

        final EraInfoData eraInfoData = parameterMap.get("eraSwitchBlockData");
        assertNotNull(eraInfoData);
        assertNotNull(eraInfoData.getEraSummary());

    }

    @And("with a valid era switch block hash")
    public void withAValidEraSwitchBlockHash() {

        final EraInfoData eraInfoData = parameterMap.get("eraSwitchBlockData");
        validBlockHash(new Digest(eraInfoData.getEraSummary().getBlockHash()));

        //Assert block exists
        assertNotNull(getCasperService().getBlock(new HashBlockIdentifier(eraInfoData.getEraSummary().getBlockHash())));

    }

    @And("with a valid era id")
    public void withAValidEraId() {
        final EraInfoData eraInfoData = parameterMap.get("eraSwitchBlockData");
        assertNotNull(eraInfoData.getEraSummary().getEraId());
        assertTrue(eraInfoData.getEraSummary().getEraId() > 0);
    }

    @And("with a valid merkle proof")
    public void withAValidMerkleProof() {
        final EraInfoData eraInfoData = parameterMap.get("eraSwitchBlockData");
        assertNotNull(eraInfoData.getEraSummary().getMerkleProof());
    }


    @And("with a valid stored value")
    public void withAValidStoredValue() {
    }

    @And("with a valid state root hash")
    public void withAValidStateRootHash() {
        final EraInfoData eraInfoData = parameterMap.get("eraSwitchBlockData");
        validBlockHash(new Digest(eraInfoData.getEraSummary().getStateRootHash()));

        //Assert state root block exists
//        assertNotNull(getCasperService().getBlock(new HashBlockIdentifier(eraInfoData.getEraSummary().getStateRootHash())));
    }


    private void validBlockHash(final Digest hash){
        assertNotNull(hash);
        assertNotNull(hash.getDigest());
        assertEquals(hash.getClass(), Digest.class);
        assertTrue(hash.isValid());
    }


    private DeployResult doTransfer() throws IOException, NoSuchTypeException, GeneralSecurityException, ValueSerializationException {

        Ed25519PrivateKey user1 = new Ed25519PrivateKey();
        Ed25519PrivateKey user2 = new Ed25519PrivateKey();

        user1.readPrivateKey(AssetUtils.getUserKeyAsset(1, 1, "secret_key.pem").getFile());
        user2.readPrivateKey(AssetUtils.getUserKeyAsset(1, 2, "secret_key.pem").getFile());

        long id = Math.abs(new Random().nextInt());
        Ttl ttl = Ttl
                .builder()
                .ttl("30m")
                .build();
        Ed25519PrivateKey from = user1;
        PublicKey to = PublicKey.fromAbstractPublicKey(user2.derivePublicKey());

        Deploy deploy = CasperTransferHelper.buildTransferDeploy(from, to,
                BigInteger.valueOf(2500000000L), "casper-net-1",
                id, BigInteger.valueOf(100000000L), 1L, ttl, new Date(),
                new ArrayList<>());


        DeployResult deployResult = getCasperService().putDeploy(deploy);


        do {
            DeployData deploy1 = getCasperService().getDeploy(deployResult.getDeployHash());

            if (!deploy1.getExecutionResults().isEmpty()) {
                break;
            }

        } while (true);

        return deployResult;

    }


}
