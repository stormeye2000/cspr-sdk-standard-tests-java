package com.stormeye.evaluation;

import static org.junit.jupiter.api.Assertions.*;

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
import com.casper.sdk.model.key.PublicKey;
import com.casper.sdk.model.transfer.TransferData;
import com.casper.sdk.service.CasperService;
import com.stormeye.utils.AssetUtils;
import com.stormeye.utils.CasperClientProvider;
import com.syntifi.crypto.key.Ed25519PrivateKey;
import dev.oak3.sbs4j.exception.ValueSerializationException;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluateBlocks {

    private static JsonBlockData blockData;

    @BeforeAll
    public static void setUp() {
        blockData = null;
    }

    @Given("that the latest block is requested")
    public void thatTheLatestBlockIsRequested() {
        blockData = getCasperService().getBlock();
    }

    private static  CasperService getCasperService() {
        return CasperClientProvider.getInstance().getCasperService();
    }

    @Then("a valid block is returned")
    public void aValidBlockIsReturned() {
        assertNotNull(blockData);
        assertEquals(blockData.getClass(), JsonBlockData.class);
    }

    @And("with a valid hash")
    public void withAValidHash() {
        assertNotNull(blockData.getBlock().getHash());
        assertNotNull(blockData.getBlock().getHash().getDigest());
        assertEquals(blockData.getBlock().getHash().getClass(), Digest.class);
        assertTrue(blockData.getBlock().getHash().isValid());
    }

    @And("with a valid body")
    public void withAValidBody() {
        assertNotNull(blockData.getBlock().getBody());
    }

    @And("with valid headers")
    public void withValidHeaders() {
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
        assertNotNull(blockData.getBlock().getProofs());
    }

    @Given("that a block is returned by hash")
    public void thatABlockIsReturnedByHash() {
        final JsonBlockData latestBlock = getCasperService().getBlock();
        final String hash = latestBlock.getBlock().getHash().toString();

        blockData = getCasperService().getBlock(new HashBlockIdentifier(hash));
    }

    @Given("that a block is returned by height {int}")
    public void thatABlockIsReturnedByHeight(long height) {
        blockData = getCasperService().getBlock(new HeightBlockIdentifier(height));
    }

    @Given("that a transfer block is requested")
    public void thatATransferBlockIsRequested() throws NoSuchTypeException, GeneralSecurityException, ValueSerializationException, IOException {

        DeployResult result = doTransfer();

        DeployData deploy = getCasperService().getDeploy(result.getDeployHash());

        final TransferData blockTransfers = getCasperService().getBlockTransfers();

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
