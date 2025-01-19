package com.vitaliebocicov.chainpay.security;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class KeysGeneratorTest {

    @Test
    void testConstructor() {
        try {
            KeysGenerator keysGenerator = new KeysGenerator();
            assertNotNull(keysGenerator, "KeysGenerator instance should be created successfully");
        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown during KeysGenerator initialization");
        }
    }

    @Test
    void testCreateKeyPair() {
        try {
            KeysGenerator keysGenerator = new KeysGenerator();
            KeyPair keyPair = keysGenerator.createKeyPair();

            assertNotNull(keyPair, "KeyPair should not be null");
            assertNotNull(keyPair.getPublic(), "Public key should not be null");
            assertNotNull(keyPair.getPrivate(), "Private key should not be null");

        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown during key pair creation");
        }
    }

    @Test
    void testKeyPairGenerationIsUnique() {
        try {
            KeysGenerator keysGenerator = new KeysGenerator();
            KeyPair keyPair1 = keysGenerator.createKeyPair();
            KeyPair keyPair2 = keysGenerator.createKeyPair();

            assertNotEquals(keyPair1.getPublic(), keyPair2.getPublic(), "Public keys should be unique");
            assertNotEquals(keyPair1.getPrivate(), keyPair2.getPrivate(), "Private keys should be unique");

        } catch (NoSuchAlgorithmException e) {
            fail("NoSuchAlgorithmException should not be thrown during key pair generation");
        }
    }
}