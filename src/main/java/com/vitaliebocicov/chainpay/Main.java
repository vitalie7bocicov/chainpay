package com.vitaliebocicov.chainpay;

import com.vitaliebocicov.chainpay.mining.Miner;
import com.vitaliebocicov.chainpay.security.KeysGenerator;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.currentThread;


public final class Main {

    private static final int NUMBER_OF_MINERS = 10;
    private static final int TEN_MINUTES = 60 * 10;
    private static final int CHAIN_SIZE = 15;

    private Main() { }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        System.out.println("Available processord: " + Runtime.getRuntime().availableProcessors());
        Blockchain blockchain = Blockchain.getInstance(CHAIN_SIZE);
        KeysGenerator keysGenerator;
        try {
            keysGenerator = new KeysGenerator();
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        Miner[] miners = new Miner[NUMBER_OF_MINERS];

        for (int i = 0; i < NUMBER_OF_MINERS; i++) {
            miners[i] = new Miner(i, keysGenerator);
        }

        for (int i = 0; i < NUMBER_OF_MINERS; i++) {
            miners[i].setContacts(miners);
            executor.submit(miners[i]);
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(TEN_MINUTES, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            currentThread().interrupt();
        }

        blockchain.print();

        for (int i = 0; i < NUMBER_OF_MINERS; i++) {
            miners[i].printAmount();
        }

        System.out.println("Chain valid: " + blockchain.isChainValid());
    }
}
