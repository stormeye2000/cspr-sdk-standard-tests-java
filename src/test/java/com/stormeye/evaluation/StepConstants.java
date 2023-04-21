package com.stormeye.evaluation;

/**
 * Constant field/key names used as step parameters.
 *
 * @author ian@meywood.com
 */
public abstract class StepConstants {

    public static final String AMOUNT = "amount";
    public static final String DEPLOY_ACCEPTED = "deployAccepted";
    public static final String DEPLOY_RESULT = "deployResult";
    public static final String DEPLOY_TIMESTAMP = "deploy-timestamp";
    public static final String GAS_PRICE = "gasPrice";
    public static final String GLOBAL_STATE_DATA = "globalStateData";
    public static final String INFO_GET_DEPLOY = "info_get_deploy";
    public static final String LAST_BLOCK_ADDED = "lastBlockAdded";
    public static final String PEER_DATA = "peerData";
    public static final String PUBLIC_KEY_PEM = "public_key.pem";
    public static final String PUT_DEPLOY = "put-deploy";
    public static final String RECEIVER_KEY = "receiverKey";
    public static final String SECRET_KEY_PEM = "secret_key.pem";
    public static final String SENDER_KEY = "senderKey";
    public static final String TRANSFER_AMOUNT = "transferAmount";
    public static final String TTL = "ttl";
    public static final String STATE_ROOT_HASH = "stateRootHash";
    public static final String STATUS_DATA = "statusData";
    public static final String EXPECTED_STATUS_DATA = "expectedStatusData";

    private StepConstants() {
        // prevent construction
    }
}
