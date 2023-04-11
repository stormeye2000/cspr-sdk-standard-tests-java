package com.stormeye.evaluation;

import com.casper.sdk.model.peer.PeerData;
import com.casper.sdk.model.peer.PeerEntry;
import com.stormeye.utils.CasperClientProvider;
import com.stormeye.utils.ParameterMap;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * The Step definitions for info_get_peers tests
 *
 * @author ian@meywood.com
 */
public class InfoGetPeersStepDefinitions {

    private static final ParameterMap parameterMap = ParameterMap.getInstance();

    @BeforeAll
    public static void setUp() {
        parameterMap.clear();
    }

    @Given("that the info_get_peers RPC method is invoked against a node")
    public void thatTheInfo_get_peersRPCMethodIsInvokedAgainstANode() {
        final PeerData peerData = CasperClientProvider.getInstance().getCasperService().getPeerData();
        parameterMap.put("peerData", peerData);
    }

    @Then("the node returns an info_get_peers_result")
    public void theNodeReturnsAnInfo_get_peers_result() {
        assertThat(getPeerData(), is(notNullValue()));
    }

    @And("the info_get_peers_result has an API version of {string}")
    public void theInfo_get_peers_resultHasAnAPIVersionOf(final String apiVersion) {
        assertThat(getPeerData().getApiVersion(), is(apiVersion));
    }

    @And("the info_get_peers_result contains {int} peers")
    public void theInfo_get_peers_resultContainsPeers(int peerCount) {
        assertThat(getPeerData().getPeers(), hasSize(peerCount));
    }

    @And("the info_get_peers_result contains a valid peer with a port number of {int}")
    public void theInfo_get_peers_resultContainAPeerWithAPortNumberOf(int port) {
        final Optional<PeerEntry> match = getPeerData().getPeers().stream()
                .filter(peerEntry -> isValidPeer(port, peerEntry))
                .findFirst();
        assertThat(match.isPresent(), is(true));
    }

    private static boolean isValidPeer(int port, final PeerEntry peerEntry) {
        return peerEntry.getAddress().endsWith(":" + port) && peerEntry.getNodeId().startsWith("tls:");
    }

    private static PeerData getPeerData() {
        return parameterMap.get("peerData");
    }
}
