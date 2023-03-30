package com.stormeye.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum to hold all NCTL bash commands
 */
public enum ExecCommands {

    NCTL_VIEW_ERA_INFO{
        @Override public List<String> getCommand(final String dockerName) {
            return List.of("bash", "-c", "docker exec -t " + dockerName + " /bin/bash -c 'source casper-node/utils/nctl/sh/views/view_chain_era_info.sh'");
        }

        @Override
        public List<String> getCommand(final String dockerName, final String params) {return null;}
    },
    NCTL_VIEW_CHAIN_BLOCK_TRANSFER{
        @Override public List<String> getCommand(final String dockerName) {
            return List.of("bash", "-c", "docker exec -t " + dockerName + " /bin/bash -c 'source casper-node/utils/nctl/sh/views/view_chain_block_transfers.sh'");
        }

        @Override
        public List<String> getCommand(final String dockerName, final String params) {
            return List.of("bash", "-c", "docker exec -t " + dockerName + " /bin/bash -c 'source casper-node/utils/nctl/sh/views/view_chain_block_transfers.sh "  + params + "'");
        }
    },
    NCTL_VIEW_CHAIN_BLOCK{
        @Override public List<String> getCommand(final String dockerName) {
            return List.of("bash", "-c", "docker exec -t " + dockerName + " /bin/bash -c 'source casper-node/utils/nctl/sh/views/view_chain_block.sh'");
        }

        @Override
        public List<String> getCommand(final String dockerName, final String params) {
            return List.of("bash", "-c", "docker exec -t " + dockerName + " /bin/bash -c 'source casper-node/utils/nctl/sh/views/view_chain_block.sh " + params + "'");
        }
    };

    public abstract List<String> getCommand(final String dockerName);
    public abstract List<String> getCommand(final String dockerName, final String params);

    private static final List<String> MAP = new ArrayList<>();

    static {
        for (final ExecCommands command : values()) {
            MAP.add(command.name());
        }
    }

}
