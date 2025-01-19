package com.vitaliebocicov.chainpay.security;

import com.vitaliebocicov.chainpay.model.Message;
import com.vitaliebocicov.chainpay.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import static org.junit.jupiter.api.Assertions.*;

class SignatureUtilTest {

    private KeyPair keyPair;
    private Message message;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        KeysGenerator keysGenerator = new KeysGenerator();
        keyPair = keysGenerator.createKeyPair();

        message = new Message(1L, keyPair.getPublic());
        Transaction transaction = new Transaction("sender", "receiver", 100);
        message.setTransaction(transaction);
    }

    @Test
    void testSignMessage() {
        try {
            byte[] signature = SignatureUtil.signMessage(message, keyPair.getPrivate());

            assertNotNull(signature, "Signature should not be null");

            message.setSignature(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            fail("Exception should not be thrown during signing the message: " + e.getMessage());
        }
    }

    @Test
    void testIsSignatureValidTrue() {
        try {
            byte[] signature = SignatureUtil.signMessage(message, keyPair.getPrivate());
            message.setSignature(signature);

            boolean isValid = SignatureUtil.isSignatureValid(message);

            assertTrue(isValid, "Signature should be valid");

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            fail("Exception should not be thrown during signature verification: " + e.getMessage());
        }
    }

    @Test
    void testIsSignatureValidInvalid() {
        try {
            byte[] signature = SignatureUtil.signMessage(message, keyPair.getPrivate());
            message.setSignature(signature);

            Transaction transaction = new Transaction("tamperedSender", "tamperedReceiver", 200);
            message.setTransaction(transaction);

            boolean isValid = SignatureUtil.isSignatureValid(message);

            assertFalse(isValid, "Signature should be invalid due to message tampering");

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            fail("Exception should not be thrown during signature verification: " + e.getMessage());
        }
    }
}