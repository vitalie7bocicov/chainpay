package com.vitaliebocicov.chainpay.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    @Test
    void testApplySha256() {
        String input = "test input";

        String hash = StringUtil.applySha256(input);

        assertNotNull(hash, "SHA-256 hash should not be null");
        assertFalse(hash.isEmpty(), "SHA-256 hash should not be empty");

        assertEquals(64, hash.length(), "SHA-256 hash should be 64 characters long");

        String expectedHash = "9dfe6f15d1ab73af898739394fd22fd72a03db01834582f24bb2e1c66c7aaeae"; // precomputed hash
        assertEquals(expectedHash, hash, "SHA-256 hash should match the expected value");
    }

    @Test
    void testApplySha256WithEmptyString() {
        String input = "";

        String hash = StringUtil.applySha256(input);

        assertNotNull(hash, "SHA-256 hash should not be null for empty input");
        assertFalse(hash.isEmpty(), "SHA-256 hash should not be empty for empty input");
        assertEquals(64, hash.length(), "SHA-256 hash should be 64 characters long for empty input");

        String expectedHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"; // precomputed hash for empty string
        assertEquals(expectedHash, hash, "SHA-256 hash for empty string should match the expected value");
    }

    @Test
    void testApplySha256WithNullInput() {
        assertThrows(RuntimeException.class, () -> StringUtil.applySha256(null), "Expected NullPointerException for null input");
    }
}