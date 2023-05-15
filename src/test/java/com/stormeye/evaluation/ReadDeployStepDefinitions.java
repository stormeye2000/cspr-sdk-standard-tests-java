package com.stormeye.evaluation;

import com.casper.sdk.model.clvalue.CLValueByteArray;
import com.casper.sdk.model.clvalue.CLValueString;
import com.casper.sdk.model.clvalue.CLValueU512;
import com.casper.sdk.model.common.Ttl;
import com.casper.sdk.model.deploy.Deploy;
import com.casper.sdk.model.deploy.NamedArg;
import com.casper.sdk.model.deploy.executabledeploy.Transfer;
import com.casper.sdk.service.CasperService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.ParameterMap;
import com.syntifi.crypto.key.encdec.Hex;
import dev.oak3.sbs4j.exception.ValueSerializationException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * The step definitions for the read_deploy.feature that tests a transfer deploy can be read from JSON.
 *
 * @author ian@meywood.com
 */
public class ReadDeployStepDefinitions {

    private final ParameterMap parameterMap = ParameterMap.getInstance();
    public final CasperService casperService = CasperClientProvider.getInstance().getCasperService();
    private final Logger logger = LoggerFactory.getLogger(ReadDeployStepDefinitions.class);

    @Given("that the {string} JSON deploy is loaded")
    public void thatTheJSONDeployIsLoaded(final String jsonFilename) throws IOException {

        logger.info("Given that the {} JSON deploy is loaded", jsonFilename);

        //noinspection ConstantConditions
        final InputStream jsonIn = getClass().getResource("/json/" + jsonFilename).openStream();
        assertThat(jsonIn, is(notNullValue()));

        final Deploy transfer = new ObjectMapper().readValue(jsonIn, Deploy.class);
        parameterMap.put("transfer", transfer);
    }

    @Then("a valid transfer deploy is created")
    public void aValidTransferDeployIsCreated() {
        logger.info("Then a valid transfer deploy is created");

        final Deploy transfer = getDeploy();
        assertThat(transfer, is(notNullValue()));
    }

    @And("the deploy hash is {string}")
    public void theDeployHashIs(final String hash) {
        assertThat(getDeploy().getHash().toString(), is(hash));
    }

    @And("the account is {string}")
    public void theAccountIs(final String account) {
        assertThat(getDeploy().getHeader().getAccount().getAlgoTaggedHex(), is(account));
    }

    @And("the timestamp is {string}")
    public void theTimestampIs(final String timestamp) {
        assertThat(getDeploy().getHeader().getTimeStamp(), is(new DateTime(timestamp).toDate()));
    }

    @And("the ttl is {int}m")
    public void theTtlIsM(final int ttl) {
        assertThat(getDeploy().getHeader().getTtl().getTtl(), is(Ttl.builder().ttl(ttl + "m").build().getTtl()));
    }

    @And("the gas price is {long}")
    public void theGasPriceIs(final long gasPrice) {
        assertThat(getDeploy().getHeader().getGasPrice(), is(gasPrice));
    }

    @And("the body_hash is {string}")
    public void theBody_hashIs(final String bodyHash) {
        assertThat(getDeploy().getHeader().getBodyHash().toString(), is(bodyHash));
    }

    @And("the chain name is  {string}")
    public void theChainNameIs(final String chainName) {
        assertThat(getDeploy().getHeader().getChainName(), is(chainName));
    }

    private Deploy getDeploy() {
        return parameterMap.get("transfer");
    }

    @And("dependency {int} is {string}")
    public void dependencyIs(int index, final String hex) {
        assertThat(getDeploy().getHeader().getDependencies().get(index).toString(), is(hex));
    }

    @And("the payment amount is {long}")
    public void thePaymentAmountIs(long amount) throws ValueSerializationException {
        final NamedArg<?> payment = getNamedArg(getDeploy().getPayment().getArgs(), "amount");
        assertThat(payment.getClValue(), is(new CLValueU512(BigInteger.valueOf(amount))));
    }

    @And("the session is a transfer")
    public void theSessionIsATransfer() {
        assertThat(getDeploy().getSession(), is(instanceOf(Transfer.class)));
    }

    @And("the session amount is {long}")
    public void theSessionAmountIs(final long amount) throws ValueSerializationException {
        final NamedArg<?> namedArg = getNamedArg(getDeploy().getSession().getArgs(), "amount");
        assertThat(namedArg.getClValue(), is(new CLValueU512(BigInteger.valueOf(amount))));
    }

    @And("the session target is {string}")
    public void theSessionTargetIs(final String target) throws ValueSerializationException {
        final NamedArg<?> namedArg = getNamedArg(getDeploy().getSession().getArgs(), "target");
        assertThat(namedArg.getClValue(), is(new CLValueByteArray(Hex.decode(target))));
    }

    @And("the session additional_info is {string}")
    public void theSessionAdditional_infoIs(final String additionalInfo) throws ValueSerializationException {
        final NamedArg<?> namedArg = getNamedArg(getDeploy().getSession().getArgs(), "additional_info");
        assertThat(namedArg.getClValue(), (is(new CLValueString(additionalInfo))));
    }

    @And("the deploy has {int} approval")
    public void theDeployHasApproval(final int approvalSize) {
        assertThat(getDeploy().getApprovals(), hasSize(approvalSize));
    }

    @And("the approval signer is {string}")
    public void theApprovalSignerIs(final String signer) {
        assertThat(getDeploy().getApprovals().get(0).getSigner().toString(), is(signer));
    }

    @And("the approval signature is {string}")
    public void theApprovalSignatureIs(final String signature) {
        assertThat(getDeploy().getApprovals().get(0).getSignature().getAlgoTaggedHex(), is(signature));
    }

    private NamedArg<?> getNamedArg(final List<NamedArg<?>> args, final String name) {
        Optional<NamedArg<?>> namedArg = args.stream().filter(arg -> name.equals(arg.getType())).findFirst();
        assertThat(namedArg.isPresent(), is(true));
        return namedArg.get();
    }
}
