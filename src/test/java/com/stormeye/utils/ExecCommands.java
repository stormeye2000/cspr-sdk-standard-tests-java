package com.stormeye.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

public enum ExecCommands {

    NCTL_VIEW_ERA_INFO{
        @Override public List<String> getCommand() {
            return List.of("bash", "-c", "docker exec -t storm-nctl /bin/bash -c 'source casper-node/utils/nctl/sh/views/view_chain_era_info.sh'");
        }
    };

    public abstract List<String> getCommand();

    private static final List<String> MAP = new ArrayList<>();

    static {
        for (final ExecCommands command : values()) {
            MAP.add(command.name());
        }
    }

}
