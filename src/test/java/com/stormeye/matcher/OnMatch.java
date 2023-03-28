package com.stormeye.matcher;

/**
 * @author ian@meywood.com
 */
public interface OnMatch<T> {
    void onMatch(final T match);
}
