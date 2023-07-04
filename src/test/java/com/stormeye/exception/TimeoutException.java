package com.stormeye.exception;

/**
 * @author ian@meywood.com
 */
public class TimeoutException extends RuntimeException {

    public TimeoutException(final String message) {
        super(message);
    }
}
