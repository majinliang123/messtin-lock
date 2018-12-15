package org.messtin.lock.common.util;

import java.util.UUID;

public final class UUIDUtil {

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
