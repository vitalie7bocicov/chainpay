package com.vitaliebocicov.chainpay.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public final class KeysGenerator {

    private static final int KEY_LENGTH = 1024;
    private final KeyPairGenerator keyGen;

    public KeysGenerator() throws NoSuchAlgorithmException {
        this.keyGen = KeyPairGenerator.getInstance("RSA");
        this.keyGen.initialize(KEY_LENGTH);
    }

    public synchronized KeyPair createKeyPair() {
       return this.keyGen.generateKeyPair();
    }
}
