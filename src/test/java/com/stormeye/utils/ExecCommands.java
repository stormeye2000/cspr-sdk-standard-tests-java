package com.stormeye.utils;

import java.util.ArrayList;
import java.util.List;

public enum ExecCommands {

    NCTL_VIEW_ERA_INFO{
        @Override public List<String> getCommand() {
            return List.of("bash", "-c", "docker exec -t storm-nctl /bin/bash -c 'source casper-node/utils/nctl/sh/views/view_chain_era_info.sh'");
        }

        @Override
        public List<String> getCommand(String params) {return null;}
    },
    NCTL_VIEW_CHAIN_BLOCK{
        @Override public List<String> getCommand() {
            return List.of("bash", "-c", "docker exec -t storm-nctl /bin/bash -c 'source casper-node/utils/nctl/sh/views/view_chain_block.sh'");
        }

        @Override
        public List<String> getCommand(String params) {
            return List.of("bash", "-c", "docker exec -t storm-nctl /bin/bash -c 'source casper-node/utils/nctl/sh/views/view_chain_block.sh " + params + "'");
        }
    };

    public abstract List<String> getCommand();
    public abstract List<String> getCommand(final String params);

    private static final List<String> MAP = new ArrayList<>();

    static {
        for (final ExecCommands command : values()) {
            MAP.add(command.name());
        }
    }

}
