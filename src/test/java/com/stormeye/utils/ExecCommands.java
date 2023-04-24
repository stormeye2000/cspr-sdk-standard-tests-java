package com.stormeye.utils;

import java.util.List;

/**
 * Enum to hold all NCTL bash commands
 */
public enum ExecCommands {

    NCTL_VIEW_ERA_INFO {
        @Override
        public List<String> getCommand(final String dockerName, final String params) {
            return this.buildCommand(dockerName, "view_chain_era_info", params);
        }
    },
    NCTL_VIEW_CHAIN_BALANCE {
        @Override
        public List<String> getCommand(String dockerName, String params) {
            return this.buildCommand(dockerName, "view_chain_balance", params);
        }
    },
    NCTL_VIEW_CHAIN_BLOCK_TRANSFER {
        @Override
        public List<String> getCommand(final String dockerName, final String params) {
            return this.buildCommand(dockerName, "view_chain_block_transfers", params);
        }
    },
    NCTL_VIEW_CHAIN_BLOCK {
        @Override
        public List<String> getCommand(final String dockerName, final String params) {
            return this.buildCommand(dockerName, "view_chain_block", params);
        }
    },
    NCTL_VIEW_CHAIN_STATE_ROOT_HASH {
        @Override
        public List<String> getCommand(final String dockerName, final String params) {
            return this.buildCommand(dockerName, "view_chain_state_root_hash", params);
        }
    },
    NCTL_VIEW_CHAIN_AUCTION_INFO {
        @Override
        public List<String> getCommand(final String dockerName, final String params) {
            return this.buildCommand(dockerName,"view_chain_auction_info","");
        }
    },
    NCTL_VIEW_NODE_STATUS {
        @Override
        public List<String> getCommand(final String dockerName, final String params) {
            return this.buildCommand(dockerName, "view_node_status", params);
        }
    },
    NCTL_VIEW_USER_ACCOUNT {
        @Override
        public List<String> getCommand(final String dockerName, final String params) {
            return this.buildCommand(dockerName, "view_user_account", params);
        }
    };

    List<String> buildCommand(final String dockerName, final String shellCommand, final String params) {
        return List.of("bash", "-c", String.format("docker exec -t %s /bin/bash -c 'source casper-node/utils/nctl/sh/views/%s.sh %s'", dockerName, shellCommand, params));
    }

    public List<String> getCommand(final String dockerName) {
        return getCommand(dockerName, "");
    }

    // FIXME this should really be: getCommand(final String dockerName, final Object... params);
    public abstract List<String> getCommand(final String dockerName, final String params);


}
