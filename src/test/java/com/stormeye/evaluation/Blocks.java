package com.stormeye.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import com.casper.sdk.identifier.block.HashBlockIdentifier;
import com.casper.sdk.identifier.block.HeightBlockIdentifier;
import com.casper.sdk.model.block.JsonBlockData;
import com.casper.sdk.model.common.Digest;
import com.casper.sdk.model.deploy.Deploy;
import com.casper.sdk.model.transfer.Transfer;
import com.casper.sdk.model.transfer.TransferData;
import com.casper.sdk.service.CasperService;

import java.net.MalformedURLException;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class Blocks {

    private static JsonBlockData blockData;
    private static final String url = "localhost";
    private static final Integer port = 11101;
    protected static CasperService casperServiceNctl;

    @BeforeAll
    public static void setUp() throws MalformedURLException {
        casperServiceNctl = CasperService.usingPeer(url, port);
        blockData = null;
    }

    @Then("block returns required data")
    public void blockReturnsRequiredData() {
        assertBlock(blockData);
    }

    @Given("I have block transfers")
    public void iHaveBlockTransfers() {
        final TransferData transferData = casperServiceNctl.getBlockTransfers();
    }

    @Given("that the latest block is requested")
    public void thatTheLatestBlockIsRequested() {
        blockData = casperServiceNctl.getBlock();
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
        final JsonBlockData latestBlock = casperServiceNctl.getBlock();
        final String hash = latestBlock.getBlock().getHash().toString();

        blockData = casperServiceNctl.getBlock(new HashBlockIdentifier(hash));
    }

    @Given("that a block is returned by height {int}")
    public void thatABlockIsReturnedByHeight(long height) {
        blockData = casperServiceNctl.getBlock(new HeightBlockIdentifier(height));
    }

    @Given("that a transfer block is requested")
    public void thatATransferBlockIsRequested() {

        final TransferData blockTransfers = casperServiceNctl.getBlockTransfers();

    }

    private void assertBlock(final JsonBlockData block){
        assertNotNull(block);
        assertNotNull(block.getBlock());
        assertNotNull(block.getBlock().getHash());
        assertNotNull(block.getBlock().getBody());
        assertNotNull(block.getBlock().getHeader());
        assertNotNull(block.getBlock().getProofs());
    }

    private void doTransfer(){


    }


}
