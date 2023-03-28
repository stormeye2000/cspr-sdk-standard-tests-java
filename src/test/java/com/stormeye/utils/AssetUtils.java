package com.stormeye.utils;

import java.net.URL;
import java.util.Objects;

/**
 * @author ian@meywood.com
 */
public class AssetUtils {

    public static URL getUserKeyAsset(final int networkId, final int userId, final String keyFilename) {
        String path = String.format("/net-%d/user-%d/%s", networkId, userId, keyFilename);
        return Objects.requireNonNull(AssetUtils.class.getResource(path), "missing resource " + path);
    }
}
