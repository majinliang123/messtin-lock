package org.messtin.lock.common.util;

public final class StringUtil {

    public static boolean isNotEmpty(String str) {
        if (str == null) {
            return false;
        }
        if (str.length() == 0) {
            return false;
        }
        return true;
    }
}
