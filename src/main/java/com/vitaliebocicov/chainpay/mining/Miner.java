package com.vitaliebocicov.chainpay.mining;

import com.vitaliebocicov.chainpay.Blockchain;
import com.vitaliebocicov.chainpay.model.Block;
import com.vitaliebocicov.chainpay.security.KeysGenerator;
import com.vitaliebocicov.chainpay.util.TransactionUtil;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class Miner extends Thread {

    public static final int MILISECONDS_IN_SECOND = 1000;
    public static final int ONE_SECOND = 1000;
    public static final int BOUND = 50;
    private final String id;
    private final Blockchain blockchain;
    private final KeyPair keys;
    private List<Miner> contacts;

    public Miner(int id, KeysGenerator keysGenerator) {
        this.id = "miner" + id;
        this.blockchain = Blockchain.getInstance();
        this.keys = keysGenerator.createKeyPair();
    }

    @Override
    public void run() {
        while (blockchain.getSize() < blockchain.getChainSize()) {
            while (blockchain.isMagicNumberFound()) {
                try {
                    sleep(ONE_SECOND);
                } catch (InterruptedException e) {
                    currentThread().interrupt();
                    return;
                }
            }
            if (blockchain.getSize() >= blockchain.getChainSize()) {
                break;
            }
            Block block = generate();
            if (block != null) {
                blockchain.acceptBlock(block);
            }
        }
    }

    public Block generate() {
        long startTime = System.currentTimeMillis();
        long blockId = blockchain.getBlockCounter() + 1;
        Block block = new Block(blockId,
                blockchain.getLastBlockHash(),
                blockchain.getPreviousBlockMessages(),
                id,
                blockchain.getZeroesInHash(),
                blockchain.getMaxMessageIdPreviousBlock(),
                blockchain.getRewardAmount());
        boolean isMagicNumberFound = findMagicNumber(block, blockchain.getZeroesInHash(), ThreadLocalRandom.current());
        long endTime = System.currentTimeMillis();
        block.setGeneratedSeconds((endTime - startTime) / MILISECONDS_IN_SECOND);
        if (isMagicNumberFound) {
            return block;
        }
        return null;
    }

    public String getMinerId() {
        return id;
    }

    public void setContacts(Miner[] miners) {
        this.contacts = Arrays.stream(miners)
                .filter(miner -> !this.id.equals(miner.getMinerId()))
                .toList();
    }

    public List<Miner> getContacts() {
        return new ArrayList<>(contacts);
    }

    boolean findMagicNumber(Block block, int numberOfZeros, Random random) {
        final double chanceToSendMessage = 1 / Math.pow(10, numberOfZeros + 1);
        String zeroesInHash = "0".repeat(numberOfZeros);
        String hashCode;

        System.out.println(id + " searching for block: " + block.getId());

        while (!blockchain.isMagicNumberFound()) {
            long magicNumber = random.nextLong();
            block.setMagicNumber(magicNumber);
            hashCode = block.getHashCode();

            if (blockchain.getBlockCounter() + 1 != block.getId()) {
                break;
            }

            if (random.nextDouble(1) < chanceToSendMessage) {
                sendVirtualCoins(random);
            }

            if (hashCode.startsWith(zeroesInHash) && !blockchain.isMagicNumberFound()) {
                blockchain.setMagicNumberFound(true);
                return true;
            }
        }

        return false;
    }

    void sendVirtualCoins(Random random) {
        System.out.println("SENDING");
        Miner receiver = contacts.get(random.nextInt(contacts.size()));
        int amountToSend = random.nextInt(0, BOUND) + 1;
        TransactionUtil.sendVirtualCoins(this, receiver, amountToSend, keys);
    }

    public void printAmount() {
        System.out.println(id + " has " + (blockchain.getVirtualCoinsAmount(id) + Blockchain.TESTING_VIRTUAL_COINS) + " VC.");
    }
}
