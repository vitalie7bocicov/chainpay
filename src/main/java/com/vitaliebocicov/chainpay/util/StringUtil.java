package com.vitaliebocicov.chainpay.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class StringUtil {

    private StringUtil() { }

    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte elem : hash) {
                hexString.append(String.format("%02x", elem));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
