package com.stormeye.evaluation;

import com.casper.sdk.helper.CasperTransferHelper;
import com.casper.sdk.model.common.Ttl;
import com.casper.sdk.model.deploy.Deploy;
import com.casper.sdk.model.deploy.DeployResult;
import com.casper.sdk.model.key.PublicKey;
import com.casper.sdk.service.CasperService;
import com.stormeye.utils.AssetUtils;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.ParameterMap;
import com.syntifi.crypto.key.Ed25519PrivateKey;
import com.syntifi.crypto.key.Ed25519PublicKey;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Step Definitions for Deploy Cucumber Tests.
 *
 * @author ian@meywood.com
 */
public class DeployStepDefinitions {

    private static final ParameterMap parameterMap = new ParameterMap();

    @BeforeAll
    public static void setUp() {
        parameterMap.clear();
    }


    @Given("that user-{int} initiates a transfer to user-{int}")
    public void thatUserCreatesATransferOfToUser(int senderId, int receiverId) throws IOException {

        final Ed25519PrivateKey senderKey = new Ed25519PrivateKey();
        final Ed25519PublicKey receiverKey = new Ed25519PublicKey();

        senderKey.readPrivateKey(AssetUtils.getUserKeyAsset(1, senderId, "secret_key.pem").getFile());
        receiverKey.readPublicKey(AssetUtils.getUserKeyAsset(1, receiverId, "public_key.pem").getFile());

        parameterMap.put("senderKey", senderKey);
        parameterMap.put("receiverKey", receiverKey);
    }

    @And("the deploy has a ttl of {int}m")
    public void theDeployHasATtlOfM(int ttlMinutes) {
        parameterMap.put("ttl", Ttl.builder().ttl(ttlMinutes + "m").build());
    }

    @And("the transfer amount is {long}")
    public void theTransferAmountIs(long amount) {
        parameterMap.put("transferAmount", BigInteger.valueOf(amount));
    }

    @And("the transfer gas price is {long}")
    public void theTransferPriceIs(long price) {
        parameterMap.put("gasPrice", price);
    }

    @When("the deploy is put")
    public void theDeployIsPut() throws Exception {


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


        CasperService casperService = CasperClientProvider.getInstance().getCasperService();

        parameterMap.put("deployResult", casperService.putDeploy(deploy));
    }


    @Then("the valid deploy hash is returned")
    public void theValidDeployHashIsReturned() {

        DeployResult deployResult = parameterMap.get("deployResult");
        assertThat(deployResult, is(notNullValue()));
        assertThat(deployResult.getDeployHash(), is(notNullValue()));
        assertThat(deployResult.getDeployHash().length(), is(greaterThan(0)));
    }
}

