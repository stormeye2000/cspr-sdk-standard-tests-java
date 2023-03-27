package com.stormeye.exceptions;

public class NctlCommandException extends RuntimeException{

    public NctlCommandException(final String message) {
        super("NCTL command failed with:" + message);
    }

}
