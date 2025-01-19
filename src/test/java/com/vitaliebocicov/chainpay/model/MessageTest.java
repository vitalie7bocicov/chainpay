package com.vitaliebocicov.chainpay.model;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    private final PublicKey publicKey = getPublicKey();
    private final Message message = new Message(1L, publicKey);

    @Test
    void testGetId() {
        assertEquals(1L, message.getId(), "Message ID should be correct");
    }

    @Test
    void testGetPublicKey() {
        assertEquals(publicKey, message.getPublicKey(), "Public key should be correct");
    }

    @Test
    void testGetDataWithoutTransaction() {
        byte[] expectedData = (message.getId() + message.getText()).getBytes(StandardCharsets.UTF_8);
        byte[] data = message.getData();

        assertArrayEquals(expectedData, data, "Data should be correct when no transaction is set");
    }

    @Test
    void testGetDataWithTransaction() {

        Transaction transaction = new Transaction("Alice", "Bob", 50);
        message.setTransaction(transaction);

        byte[] expectedData = ("1" + "Alice send 50 VC to Bob" + transaction.getHashCode()).getBytes(StandardCharsets.UTF_8);
        byte[] data = message.getData();

        assertArrayEquals(expectedData, data, "Data should be correct when transaction is set");
    }

    @Test
    void testGetSignature() {

        byte[] signature = new byte[]{1, 2, 3, 4};
        message.setSignature(signature);

        byte[] retrievedSignature = message.getSignature();
        assertArrayEquals(signature, retrievedSignature, "Signature should be correctly retrieved");
    }

    @Test
    void testSetTransaction() {

        Transaction transaction = new Transaction("Alice", "Bob", 50);
        message.setTransaction(transaction);

        assertEquals(transaction, message.getTransaction(), "Transaction should be correctly set");
        assertEquals("ID: 1 : Alice send 50 VC to Bob", message.toString().trim(), "Text should be set correctly based on transaction");
    }

    @Test
    void testToString() {

        assertEquals("\n ID: 1 : null \n", message.toString(), "toString should correctly represent the message");

        Transaction transaction = new Transaction("Alice", "Bob", 50);
        message.setTransaction(transaction);

        assertTrue(message.toString().contains("Alice send 50 VC to Bob"), "toString should contain transaction text");
    }

    private PublicKey getPublicKey() {
        return new PublicKey() {
            @Override
            public String getAlgorithm() {
                return "RSA";
            }

            @Override
            public String getFormat() {
                return "X.509";
            }

            @Override
            public byte[] getEncoded() {
                return new byte[]{1, 2, 3, 4}; // Mocked byte array for testing
            }
        };
    }
}