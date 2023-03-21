package com.stormeye.evaluation;

import com.casper.sdk.helper.CasperTransferHelper;
import com.casper.sdk.model.common.Ttl;
import com.casper.sdk.model.deploy.Deploy;
import com.casper.sdk.model.deploy.DeployResult;
import com.casper.sdk.model.key.PublicKey;
import com.casper.sdk.service.CasperService;
import com.stormeye.utils.AssetUtils;
import com.syntifi.crypto.key.Ed25519PrivateKey;
import com.syntifi.crypto.key.Ed25519PublicKey;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author ian@meywood.com
 */
public class PutTransferDeployTest {

    @Test
    void putTransferDeploy() throws Exception {

        final CasperService casperService = CasperService.usingPeer("localhost", 11101);

        final Ed25519PrivateKey senderKey = new Ed25519PrivateKey();
        final Ed25519PublicKey receiverKey = new Ed25519PublicKey();

        senderKey.readPrivateKey(getUserKeyAsset(1, 1, "secret_key.pem").getFile());
        receiverKey.readPublicKey(getUserKeyAsset(1, 2, "public_key.pem").getFile());


        final Deploy deploy = CasperTransferHelper.buildTransferDeploy(
                senderKey,
                PublicKey.fromAbstractPublicKey(receiverKey),
                BigInteger.valueOf(2500000000L),
                "casper-net-1",
                Math.abs(new Random().nextLong()),
                BigInteger.valueOf(100000000L),
                1L,
                Ttl.builder().ttl("30m").build(),
                new Date(),
                new ArrayList<>());

        final DeployResult deployResult = casperService.putDeploy(deploy);
        assertThat(deployResult.getDeployHash().length(), is(64));
        assertThat(deployResult.getApiVersion(), is("1.0.0"));
    }


    /**
     * Obtains the user key from nctl assets folder
     */
    public static URL getUserKeyAsset(final int networkId, final int userId, final String keyFilename) {
        String path = String.format("/net-%d/user-%d/%s", networkId, userId, keyFilename);
        return Objects.requireNonNull(AssetUtils.class.getResource(path), "missing resource " + path);
    }
}
