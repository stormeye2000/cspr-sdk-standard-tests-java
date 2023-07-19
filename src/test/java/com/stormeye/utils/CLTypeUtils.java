package com.stormeye.utils;

import com.stormeye.exception.NotImplementedException;

import java.math.BigInteger;

/**
 * Utility methods for CLValue Types
 *
 * @author ian@meywood.com
 */
public class CLTypeUtils {

    /**
     * Converts a string the internal value of a CLValue
     *
     * @param typeName the name of the type
     * @param value    the string representation of the value
     * @return the CLValue's internal parsed value
     */
    public static Object convertToCLTypeValue(final String typeName, final String value) {
        switch (typeName) {
            case "String":
                return value;
            case "U8":
                return Byte.parseByte(value);
            case "U256":
                return new BigInteger(value);
            default:
                throw new NotImplementedException("Not implemented conversion for type " + typeName);
        }
    }
}
