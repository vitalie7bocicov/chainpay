package com.vitaliebocicov.chainpay;

import com.vitaliebocicov.chainpay.model.Block;
import com.vitaliebocicov.chainpay.model.Message;
import com.vitaliebocicov.chainpay.model.Transaction;
import com.vitaliebocicov.chainpay.security.SignatureUtil;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.max;

public final class Blockchain {

    public static final int TESTING_VIRTUAL_COINS = 100;
    private static final long ONE_MINUTE = 60;
    private static final int VIRTUAL_COINS_REWARD = 100;
    private static final int MAX_NUMBERS_OF_ZEROES = 7;
    private static final long FIFTEEN_SECONDS = 15;

    private volatile long messageCounter = 0;
    private volatile long maxMessageIdPreviousBlock = 0;
    private final List<Block> chain = new LinkedList<>();
    private final AtomicLong blockCounter = new AtomicLong();
    private final AtomicInteger zeroesInHash = new AtomicInteger();
    private final int chainSize;
    private final AtomicReference<Queue<Message>> previousBlockMessages =
            new AtomicReference<>(new ConcurrentLinkedQueue<>());
    private final AtomicReference<Queue<Message>> currentBlockMessages =
            new AtomicReference<>(new ConcurrentLinkedQueue<>());
    private static volatile Blockchain blockchain;
    private volatile boolean magicNumberFound = false;

    private Blockchain(int chainSize) {
        this.chainSize = chainSize;
    }

    public static Blockchain getInstance(int chainSize) {
        synchronized (Blockchain.class) {
            if (blockchain == null) {
                blockchain = new Blockchain(chainSize);
            }
        }
        return blockchain;
    }

    public static Blockchain getInstance() {
        synchronized (Blockchain.class) {
            if (blockchain == null) {
                throw new IllegalStateException("Blockchain not initialized. Call getInstance(int chainSize) first.");
            }
        }
        return blockchain;
    }

    public int getZeroesInHash() {
        return zeroesInHash.get();
    }

    public void setZeroesInHash(int zeroesInHash) {
        this.zeroesInHash.set(zeroesInHash);
    }

    public void setBlockCounter(long blockCounter) {
        this.blockCounter.set(blockCounter);
    }

    public synchronized String getLastBlockHash() {
        Block lastBlock = getLastBlock();
        String lastBlockHash = "0";
        if (lastBlock != null) {
            lastBlockHash = lastBlock.getHashCode();
        }
        return lastBlockHash;
    }

    public synchronized int getSize() {
        return chain.size();
    }

    public int getChainSize() {
        return chainSize;
    }

    public void setMaxMessageIdPreviousBlock(long maxMessageIdPreviousBlock) {
        this.maxMessageIdPreviousBlock = maxMessageIdPreviousBlock;
    }

    public long getMaxMessageIdPreviousBlock() {
        return maxMessageIdPreviousBlock;
    }

    public List<Message> getPreviousBlockMessages() {
        return List.copyOf(previousBlockMessages.get());
    }

    public List<Message> getCurrentBlockMessages() {
        return List.copyOf(currentBlockMessages.get());
    }

    public synchronized void acceptBlock(Block block) {
        if (isBlockValid(block)) {
            chain.add(block);
            setBlockCounter(block.getId());
            adjustZeroesInHash(block);
            rotateMessages();
            setMagicNumberFound(false);
            System.out.println(block);
        }
    }

    private synchronized void rotateMessages() {
        setMaxMessageIdPreviousBlock(max(previousBlockMessages.get()
                        .stream()
                        .mapToLong(Message::getId)
                        .max()
                        .orElse(0L),
                maxMessageIdPreviousBlock));
        Queue<Message> oldMessages = currentBlockMessages.getAndSet(new ConcurrentLinkedQueue<>());
        previousBlockMessages.set(oldMessages);
        previousBlockMessages.get().removeIf(message -> message.getId() <= maxMessageIdPreviousBlock);
    }

    public boolean isBlockValid(Block block) {
        if (!block.getPreviousBlockHash().equals(getLastBlockHash())) {
            return false;
        }
        String startingZeros = "0".repeat(getZeroesInHash());
        return block.getHashCode().startsWith(startingZeros);
    }

    public boolean isChainValid() {
        Iterator<Block> iterator = chain.iterator();

        if (!iterator.hasNext()) {
            return true; // Chain is empty
        }

        Block previousBlock = iterator.next();

        while (iterator.hasNext()) {
            Block currentBlock = iterator.next();
            String startingZeros = "0".repeat(currentBlock.getZeroesInHash());
            if (!currentBlock.getPreviousBlockHash().equals(previousBlock.getHashCode())) {
                return false;
            }
            if (!currentBlock.getHashCode().startsWith(startingZeros)) {
                return false;
            }
            if (!areValidBlockMessages(currentBlock)) {
                return false;
            }

            previousBlock = currentBlock;
        }
        return true;
    }

    public boolean isMagicNumberFound() {
        return magicNumberFound;
    }

    public void setMagicNumberFound(boolean found) {
        this.magicNumberFound = found;
    }

    boolean areValidBlockMessages(Block currentBlock) {
        List<Message> messages = currentBlock.getMessages();
        long maxMessageId = 0;
        long maxMessageIdPreviousBlock = currentBlock.getMaxMessageIdPreviousBlock();
        for (var msg : messages) {
            maxMessageId = max(maxMessageId, msg.getId());
            if (maxMessageId <= maxMessageIdPreviousBlock) {
                return false;
            }
            try {
                if (!SignatureUtil.isSignatureValid(msg)) {
                    return false;
                }
            } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    public void print() {
        chain.forEach(System.out::println);
    }

    public synchronized void receiveMessage(Message message) {
        if (isMessageValid(message)) {
            currentBlockMessages.get().add(message);
        }
    }

    public int getVirtualCoinsAmount(String user) {
        int amount = 0;
        Iterator<Block> iterator = chain.iterator();
        Block block;
        while (iterator.hasNext()) {
            block = iterator.next();
            if (block.getMinerId().equals(user)) {
                amount += VIRTUAL_COINS_REWARD;
            }
            for (var msg : block.getMessages()) {
                Transaction transaction = msg.getTransaction();
                if (transaction == null) {
                    continue;
                }
                if (transaction.sender().equals(user)) {
                    amount -= transaction.virtualCoinsAmount();
                } else if (transaction.receiver().equals(user)) {
                    amount += transaction.virtualCoinsAmount();
                }
            }
        }

        return amount;
    }

    public int getRewardAmount() {
        return VIRTUAL_COINS_REWARD;
    }

    boolean isMessageValid(Message message) {
        if (message.getId() <= maxMessageIdPreviousBlock) {
            return false;
        }

        if (!isTransactionValid(message.getTransaction())) {
            return false;
        }

        try {
            if (!SignatureUtil.isSignatureValid(message)) {
                return false;
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e.getMessage());
        }

        return true;
    }

    private boolean isTransactionValid(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        int senderVirtualCoinsAmount = getVirtualCoinsAmount(transaction.sender());
        return transaction.virtualCoinsAmount() <= senderVirtualCoinsAmount + TESTING_VIRTUAL_COINS;
    }

    public synchronized long getMessageCounter() {
        return ++messageCounter;
    }

    public long getBlockCounter() {
        return blockCounter.get();
    }

    private void decreaseNumberOfZeros() {
        setZeroesInHash(max(0, getZeroesInHash() - 1));
    }

    private void increaseNumberOfZeros() {
        setZeroesInHash(getZeroesInHash() + 1);
    }

    private Block getLastBlock() {
        if (chain.isEmpty()) {
            return null;
        }
        return chain.getLast();
    }

    void adjustZeroesInHash(Block block) {
        if (block.getGenerateSeconds() > ONE_MINUTE) {
            decreaseNumberOfZeros();
            block.setNumberOfZerosStatus("N was decreased by 1");
            return;
        }
        if (block.getZeroesInHash() == MAX_NUMBERS_OF_ZEROES) {
            block.setNumberOfZerosStatus("N stays the same");
            return;
        }
        if (block.getGenerateSeconds() < FIFTEEN_SECONDS) {
            increaseNumberOfZeros();
            block.setNumberOfZerosStatus("N was increased to " + getZeroesInHash());
        } else {
            block.setNumberOfZerosStatus("N stays the same");
        }
    }
}
