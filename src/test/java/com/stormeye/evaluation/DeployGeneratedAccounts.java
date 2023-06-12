package com.stormeye.evaluation;

import com.casper.sdk.exception.NoSuchTypeException;
import com.casper.sdk.helper.CasperKeyHelper;
import com.casper.sdk.helper.CasperTransferHelper;
import com.casper.sdk.model.common.Ttl;
import com.casper.sdk.model.deploy.Deploy;
import com.casper.sdk.model.key.PublicKey;
import com.casper.sdk.service.CasperService;
import com.stormeye.utils.AssetUtils;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.ContextMap;
import com.syntifi.crypto.key.*;
import dev.oak3.sbs4j.exception.ValueSerializationException;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static com.stormeye.evaluation.StepConstants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class DeployGeneratedAccounts {

    private final ContextMap contextMap = ContextMap.getInstance();
    private final Logger logger = LoggerFactory.getLogger(DeployGeneratedAccounts.class);

    @BeforeAll
    public static void setUp() {
        ContextMap.getInstance().clear();
    }


    @Given("that a Ed25519 sender key is generated")
    public void thatAEd25519SenderKeyIsGenerated(){

        final Ed25519PrivateKey sk = CasperKeyHelper.createRandomEd25519Key();
        final Ed25519PublicKey pk = CasperKeyHelper.derivePublicKey(sk);

        byte[] msg = "this is the sender".getBytes();
        byte[] signature = sk.sign(msg);
        assertTrue(pk.verify(msg, signature));

        contextMap.put("SENDER_KEY_PUBLIC", (AbstractPublicKey) pk);
        contextMap.put("SENDER_KEY_PRIVATE", (AbstractPrivateKey) sk);

    }

    @Then("fund the account from the faucet user")
    public void fundTheAccountFromTheFaucetUser() throws IOException, NoSuchTypeException, GeneralSecurityException, ValueSerializationException {
        final String chainName = "casper-net-1";

        final URL faucetPrivateKeyUrl = AssetUtils.getFaucetAsset(1, "secret_key.pem");
        assertThat(faucetPrivateKeyUrl, is(notNullValue()));
        final Ed25519PrivateKey privateKey = new Ed25519PrivateKey();
        privateKey.readPrivateKey(faucetPrivateKeyUrl.getFile());


        final Deploy deploy = CasperTransferHelper.buildTransferDeploy(
                privateKey,
                PublicKey.fromAbstractPublicKey(contextMap.get("SENDER_KEY_PUBLIC")),
                BigInteger.valueOf(1000000000000L),
                chainName,
                Math.abs(new Random().nextLong()),
                BigInteger.valueOf(100000L),
                1L,
                Ttl.builder().ttl("30m").build(),
                new Date(),
                new ArrayList<>());

        contextMap.put(PUT_DEPLOY, deploy);

        final CasperService casperService = CasperClientProvider.getInstance().getCasperService();

        contextMap.put(DEPLOY_RESULT, casperService.putDeploy(deploy));

    }

    @Given("that a Ed25519 receiver key is generated")
    public void thatAEd25519ReceiverKeyIsGenerated() {
        final Ed25519PrivateKey sk = CasperKeyHelper.createRandomEd25519Key();
        final Ed25519PublicKey pk = CasperKeyHelper.derivePublicKey(sk);

        byte[] msg = "this is the receiver".getBytes();
        byte[] signature = sk.sign(msg);
        assertTrue(pk.verify(msg, signature));

        contextMap.put(RECEIVER_KEY, (AbstractPublicKey) pk);

    }

    @Then("transfer to the receiver account")
    public void transferToTheReceiverAccount() throws NoSuchTypeException, GeneralSecurityException, ValueSerializationException {
        final String chainName = "casper-net-1";

        final Deploy deploy = CasperTransferHelper.buildTransferDeploy(
                contextMap.get("SENDER_KEY_PRIVATE"),
                PublicKey.fromAbstractPublicKey(contextMap.get(RECEIVER_KEY)),
                BigInteger.valueOf(2500000000L),
                chainName,
                Math.abs(new Random().nextLong()),
                BigInteger.valueOf(100000L),
                1L,
                Ttl.builder().ttl("30m").build(),
                new Date(),
                new ArrayList<>());

        contextMap.put(PUT_DEPLOY, deploy);

        final CasperService casperService = CasperClientProvider.getInstance().getCasperService();

        contextMap.put(DEPLOY_RESULT, casperService.putDeploy(deploy));

    }


    @Given("that a Secp256k1 sender key is generated")
    public void thatASecp256k1SenderKeyIsGenerated() throws IOException, GeneralSecurityException {
        final Secp256k1PrivateKey sk = CasperKeyHelper.createRandomSecp256k1Key();
        final Secp256k1PublicKey pk = CasperKeyHelper.derivePublicKey(sk);

        byte[] msg = "this is the sender".getBytes();
        byte[] signature = sk.sign(msg);
        assertTrue(pk.verify(msg, signature));

        contextMap.put("SENDER_KEY_PUBLIC", (AbstractPublicKey) pk);
        contextMap.put("SENDER_KEY_PRIVATE", (AbstractPrivateKey) sk);
    }

    @Given("that a Secp256k1 receiver key is generated")
    public void thatASecp256k1ReceiverKeyIsGenerated() throws IOException, GeneralSecurityException {
        final Secp256k1PrivateKey sk = CasperKeyHelper.createRandomSecp256k1Key();
        final Secp256k1PublicKey pk = CasperKeyHelper.derivePublicKey(sk);

        byte[] msg = "this is the receiver".getBytes();
        byte[] signature = sk.sign(msg);
        assertTrue(pk.verify(msg, signature));

        contextMap.put(RECEIVER_KEY, (AbstractPublicKey) pk);

    }
}
