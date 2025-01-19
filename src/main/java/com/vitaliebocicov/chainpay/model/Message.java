package com.vitaliebocicov.chainpay.model;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Arrays;

public final class Message {
    private final long id;
    private String text;
    private final PublicKey publicKey;
    private Transaction transaction;
    private byte[] signature;

    public Message(long id, PublicKey publicKey) {
        this.id = id;
        this.publicKey = publicKey;
    }

    public long getId() {
        return this.id;
    }

    public String getText() {
        return this.text;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public byte[] getData() {
        byte[] data;
        if (transaction != null) {
            data = (id + text + transaction.getHashCode()).getBytes(StandardCharsets.UTF_8);
        } else {
            data = (id + text).getBytes(StandardCharsets.UTF_8);
        }
        return data;
    }

    public byte[] getSignature() {
        return Arrays.copyOf(this.signature, this.signature.length);
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        if (transaction == null) {
            return;
        }
        this.transaction = transaction;
        this.setText(transaction.sender(), transaction.receiver(), transaction.virtualCoinsAmount());
    }

    private void setText(String sender, String receiver, int amount) {
        this.text = String.format("%s send %d VC to %s", sender, amount, receiver);
    }

    @Override
    public String toString() {
        return "\n ID: " + id + " : " + text + " \n";
    }
}
