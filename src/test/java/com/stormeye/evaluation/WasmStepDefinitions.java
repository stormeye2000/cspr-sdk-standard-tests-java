package com.stormeye.evaluation;

import com.casper.sdk.exception.NoSuchTypeException;
import com.casper.sdk.helper.CasperConstants;
import com.casper.sdk.helper.CasperDeployHelper;
import com.casper.sdk.model.clvalue.CLValueString;
import com.casper.sdk.model.clvalue.CLValueU256;
import com.casper.sdk.model.clvalue.CLValueU512;
import com.casper.sdk.model.clvalue.CLValueU8;
import com.casper.sdk.model.common.Ttl;
import com.casper.sdk.model.deploy.Deploy;
import com.casper.sdk.model.deploy.DeployResult;
import com.casper.sdk.model.deploy.NamedArg;
import com.casper.sdk.model.deploy.executabledeploy.ModuleBytes;
import com.casper.sdk.model.key.PublicKey;
import com.casper.sdk.service.CasperService;
import com.stormeye.utils.AssetUtils;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.ParameterMap;
import com.syntifi.crypto.key.Ed25519PrivateKey;
import dev.oak3.sbs4j.exception.ValueSerializationException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.apache.cxf.helpers.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.casper.sdk.helper.CasperDeployHelper.getPaymentModuleBytes;
import static com.stormeye.evaluation.StepConstants.DEPLOY_RESULT;
import static com.stormeye.evaluation.StepConstants.WASM_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Step definitions for smart contracts
 *
 * @author ian@meywood.com
 */
public class WasmStepDefinitions {

    private static final ParameterMap parameterMap = ParameterMap.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(StateGetDictionaryItemStepDefinitions.class);
    public static final CasperService casperService = CasperClientProvider.getInstance().getCasperService();

    @Given("that a smart contract {string} is located in the {string} folder")
    public void thatASmartContractIsInTheFolder(String wasmFileName, String contractsFolder) throws IOException {
        logger.info("Give that a smart contract {string} is in the {string} folder");

        final String wasmPath = "/" + contractsFolder + "/" + wasmFileName;
        parameterMap.put(WASM_PATH, wasmPath);
        final URL resource = getClass().getResource(wasmPath);
        //noinspection ConstantConditions
        assertThat(resource.openStream(), is(notNullValue()));
    }

    @Then("when the wasm is loaded as from the file system")
    public void whenTheWasmIsLoadedAsFromTheFileSystem() throws IOException, ValueSerializationException, NoSuchTypeException, GeneralSecurityException {
        logger.info("Then when the wasm is loaded as from the file system");

        final URL resource = getClass().getResource(parameterMap.get(WASM_PATH));

        //noinspection ConstantConditions
        final byte[] bytes = IOUtils.readBytesFromStream(resource.openStream());
        assertThat(bytes.length, is(189336));

        final String chainName = "casper-net-1";
        final BigInteger payment = BigDecimal.valueOf(50e9).toBigInteger();
        final byte tokenDecimals = 11;
        final String tokenName = "Acme Token";
        final BigInteger tokenTotalSupply = BigDecimal.valueOf(1e15).toBigInteger();
        final String tokenSymbol = "ACME";

        // Load faucet private key
        final URL faucetPrivateKeyUrl = AssetUtils.getFaucetAsset(1, "secret_key.pem");
        final Ed25519PrivateKey privateKey = new Ed25519PrivateKey();
        privateKey.readPrivateKey(faucetPrivateKeyUrl.getFile());

        final PublicKey faucetPublicKey = PublicKey.fromAbstractPublicKey(privateKey.derivePublicKey());
        final List<NamedArg<?>> paymentArgs = new LinkedList<>();
        paymentArgs.add(new NamedArg<>("amount", new CLValueU512(payment)));
        paymentArgs.add(new NamedArg<>("token_decimals", new CLValueU8(tokenDecimals)));
        paymentArgs.add(new NamedArg<>("token_name", new CLValueString(tokenName)));
        paymentArgs.add(new NamedArg<>("token_symbol", new CLValueString(tokenSymbol)));
        paymentArgs.add(new NamedArg<>("token_total_supply", new CLValueU256(tokenTotalSupply)));


        final ModuleBytes session = ModuleBytes.builder().bytes(bytes).args(paymentArgs).build();
        final ModuleBytes paymentModuleBytes = getPaymentModuleBytes(payment);

        final Deploy deploy = CasperDeployHelper.buildDeploy(privateKey,
                chainName,
                session,
                paymentModuleBytes,
                CasperConstants.DEFAULT_GAS_PRICE.value,
                Ttl.builder().ttl("30m").build(),
                new Date(),
                null
        );

        final DeployResult deployResult = casperService.putDeploy(deploy);
        assertThat(deployResult, is(notNullValue()));
        assertThat(deployResult.getDeployHash(), is(notNullValue()));
        parameterMap.put(DEPLOY_RESULT, deployResult);
    }
}