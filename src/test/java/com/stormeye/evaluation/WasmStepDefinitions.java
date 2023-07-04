package com.stormeye.evaluation;

import com.casper.sdk.exception.NoSuchTypeException;
import com.casper.sdk.helper.CasperConstants;
import com.casper.sdk.helper.CasperDeployHelper;
import com.casper.sdk.identifier.dictionary.StringDictionaryIdentifier;
import com.casper.sdk.model.account.Account;
import com.casper.sdk.model.clvalue.CLValueString;
import com.casper.sdk.model.clvalue.CLValueU256;
import com.casper.sdk.model.clvalue.CLValueU8;
import com.casper.sdk.model.common.Ttl;
import com.casper.sdk.model.contract.NamedKey;
import com.casper.sdk.model.deploy.Deploy;
import com.casper.sdk.model.deploy.DeployData;
import com.casper.sdk.model.deploy.DeployResult;
import com.casper.sdk.model.deploy.NamedArg;
import com.casper.sdk.model.deploy.executabledeploy.ModuleBytes;
import com.casper.sdk.model.deploy.executionresult.Success;
import com.casper.sdk.model.key.PublicKey;
import com.casper.sdk.model.stateroothash.StateRootHashData;
import com.casper.sdk.model.storedvalue.StoredValueAccount;
import com.casper.sdk.model.storedvalue.StoredValueData;
import com.casper.sdk.service.CasperService;
import com.stormeye.utils.AssetUtils;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.ContextMap;
import com.stormeye.utils.DeployUtils;
import com.syntifi.crypto.key.Ed25519PrivateKey;
import dev.oak3.sbs4j.exception.ValueSerializationException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.cxf.helpers.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.casper.sdk.helper.CasperDeployHelper.getPaymentModuleBytes;
import static com.stormeye.evaluation.StepConstants.DEPLOY_RESULT;
import static com.stormeye.evaluation.StepConstants.WASM_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Step definitions for smart contracts
 *
 * @author ian@meywood.com
 */
public class WasmStepDefinitions {

    private final ContextMap contextMap = ContextMap.getInstance();
    private final Logger logger = LoggerFactory.getLogger(StateGetDictionaryItemStepDefinitions.class);
    public final CasperService casperService = CasperClientProvider.getInstance().getCasperService();

    @Given("that a smart contract {string} is located in the {string} folder")
    public void thatASmartContractIsInTheFolder(String wasmFileName, String contractsFolder) throws IOException {
        logger.info("Give that a smart contract {string} is in the {string} folder");

        final String wasmPath = "/" + contractsFolder + "/" + wasmFileName;
        contextMap.put(WASM_PATH, wasmPath);
        final URL resource = getClass().getResource(wasmPath);
        //noinspection DataFlowIssue
        assertThat(resource.openStream(), is(notNullValue()));
    }

    @When("the wasm is loaded as from the file system")
    public void whenTheWasmIsLoadedAsFromTheFileSystem() throws IOException, ValueSerializationException, NoSuchTypeException, GeneralSecurityException {
        logger.info("Then when the wasm is loaded as from the file system");

        final URL resource = getClass().getResource(contextMap.get(WASM_PATH));

        //noinspection DataFlowIssue
        final byte[] bytes = IOUtils.readBytesFromStream(resource.openStream());
        assertThat(bytes.length, is(189336));

        final String chainName = "casper-net-1";
        final BigInteger payment = new BigInteger("200000000000");
        final byte tokenDecimals = 11;
        final String tokenName = "Acme Token";
        final BigInteger tokenTotalSupply = new BigInteger("500000000000");
        final String tokenSymbol = "ACME";

        // Load faucet private key
        final URL faucetPrivateKeyUrl = AssetUtils.getFaucetAsset(1, "secret_key.pem");
        assertThat(faucetPrivateKeyUrl, is(notNullValue()));
        final Ed25519PrivateKey privateKey = new Ed25519PrivateKey();
        privateKey.readPrivateKey(faucetPrivateKeyUrl.getFile());

        this.contextMap.put("faucetPrivateKey", privateKey);

        final List<NamedArg<?>> paymentArgs = new LinkedList<>();
        //paymentArgs.add(new NamedArg<>("amount", new CLValueU512(payment)));
        paymentArgs.add(new NamedArg<>("token_decimals", new CLValueU8(tokenDecimals)));
        paymentArgs.add(new NamedArg<>("token_name", new CLValueString(tokenName)));
        paymentArgs.add(new NamedArg<>("token_symbol", new CLValueString(tokenSymbol)));
        paymentArgs.add(new NamedArg<>("token_total_supply", new CLValueU256(tokenTotalSupply)));


        final ModuleBytes session = ModuleBytes.builder().bytes(bytes).args(paymentArgs).build();
        final ModuleBytes paymentModuleBytes = getPaymentModuleBytes(payment);

        final Deploy deploy = CasperDeployHelper.buildDeploy(
                privateKey,
                chainName,
                session,
                paymentModuleBytes,
                CasperConstants.DEFAULT_GAS_PRICE.value,
                Ttl.builder().ttl("30m").build(),
                new Date(),
                new ArrayList<>()
        );

        final DeployResult deployResult = casperService.putDeploy(deploy);
        assertThat(deployResult, is(notNullValue()));
        assertThat(deployResult.getDeployHash(), is(notNullValue()));
        contextMap.put(DEPLOY_RESULT, deployResult);
    }

    @And("the wasm has been successfully deployed")
    public void theWasmHasBeenSuccessfullyDeployed() {

        logger.info("the wasm has been successfully deployed");

        final DeployResult deployResult = contextMap.get(DEPLOY_RESULT);

        logger.info("the Deploy {} is accepted", deployResult.getDeployHash());

        final DeployData deployData = DeployUtils.waitForDeploy(deployResult.getDeployHash(), 300, casperService);

        assertThat(deployData, is(notNullValue()));
        assertThat(deployData.getDeploy(), is(notNullValue()));
        assertThat(deployData.getExecutionResults(), is(not(empty())));
        assertThat(deployData.getExecutionResults().get(0).getResult(), is(instanceOf(Success.class)));
    }

    @Then("the account named keys contain the {string} name")
    public void theAccountNamedKeysContainThe(final String contractName) throws IOException {

        final Ed25519PrivateKey privateKey = this.contextMap.get("faucetPrivateKey");
        PublicKey publicKey = PublicKey.fromAbstractPublicKey(privateKey.derivePublicKey());
        final String accountHash = publicKey.generateAccountHash(true);
        final StringDictionaryIdentifier key = StringDictionaryIdentifier.builder().dictionary(accountHash).build();

        final StateRootHashData stateRootHash = this.casperService.getStateRootHash();
        final StoredValueData stateItem = this.casperService.getStateItem(
                stateRootHash.getStateRootHash(),
                key.getDictionary(),
                new ArrayList<>());

        assertThat(stateItem, is(notNullValue()));
        assertThat(stateItem.getStoredValue(), is(instanceOf(StoredValueAccount.class)));

        Account account = (Account) stateItem.getStoredValue().getValue();
        assertThat(account.getAssociatedKeys(), is(not(empty())));
        account.getNamedKeys().forEach((NamedKey namedKey) ->
                assertThat(namedKey.getName(), startsWithIgnoringCase(contractName))
        );
    }
}