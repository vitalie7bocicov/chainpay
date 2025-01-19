package com.vitaliebocicov.chainpay.util;

import com.vitaliebocicov.chainpay.Blockchain;
import com.vitaliebocicov.chainpay.mining.Miner;
import com.vitaliebocicov.chainpay.model.Message;
import com.vitaliebocicov.chainpay.model.Transaction;
import com.vitaliebocicov.chainpay.security.SignatureUtil;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public final class TransactionUtil {

    private TransactionUtil() { }

    public static synchronized void sendVirtualCoins(Miner sender, Miner receiver, int amount, KeyPair keys) {
        Blockchain blockchain = Blockchain.getInstance();
        int currentBalance = blockchain.getVirtualCoinsAmount(sender.getMinerId());

        if (currentBalance < -Blockchain.TESTING_VIRTUAL_COINS) {
            return;
        }

        if (amount > currentBalance + Blockchain.TESTING_VIRTUAL_COINS) {
            return;
        }
        Message message = new Message(blockchain.getMessageCounter(), keys.getPublic());
        message.setTransaction(new Transaction(sender.getMinerId(), receiver.getMinerId(), amount));
        try {
            message.setSignature(SignatureUtil.signMessage(message, keys.getPrivate()));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
        blockchain.receiveMessage(message);
    }
}
