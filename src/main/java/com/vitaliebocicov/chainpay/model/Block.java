package com.vitaliebocicov.chainpay.model;

import com.vitaliebocicov.chainpay.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Block {
    private final String minerId;
    private final long id;
    private final long timestamp;
    private final String previousBlockHash;
    private long magicNumber;
    private long generateSeconds;
    private String numberOfZerosStatus;
    private final List<Message> messages;
    private int zeroesInHash;
    private final long maxMessageIdPreviousBlock;
    private final int rewardAmount;
    private final String staticHashData;

    public Block(long blockId,
                 String previousBlockHash,
                 List<Message> messages,
                 String minerId,
                 int zeroesInHash,
                 long maxMessageIdPreviousBlock,
                 int rewardAmount) {
        this.id = blockId;
        this.previousBlockHash = previousBlockHash;
        this.zeroesInHash = zeroesInHash;
        this.maxMessageIdPreviousBlock = maxMessageIdPreviousBlock;
        this.rewardAmount = rewardAmount;
        this.timestamp = new Date().getTime();
        this.minerId = minerId;
        this.messages = messages;
        String messagesHash = computeMessagesHash(messages);
        this.staticHashData = String.valueOf(id)
                + timestamp
                + previousBlockHash
                + messagesHash
                + maxMessageIdPreviousBlock;
    }

    String computeMessagesHash(List<Message> messages) {
        StringBuilder messageData = new StringBuilder();
        for (var msg : messages) {
            messageData.append(msg.toString());
        }
        return StringUtil.applySha256(messageData.toString());
    }

    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    public String getHashCode() {
        return StringUtil.applySha256(
                staticHashData
                        + magicNumber
        );
    }

    public void setGeneratedSeconds(long l) {
        this.generateSeconds = l;
    }

    public void setMagicNumber(long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public long getGenerateSeconds() {
        return generateSeconds;
    }

    public long getId() {
        return this.id;
    }

    public void setNumberOfZerosStatus(String s) {
        this.numberOfZerosStatus = s;
    }

    public int getZeroesInHash() {
        return this.zeroesInHash;
    }

    public long getMaxMessageIdPreviousBlock() {
        return maxMessageIdPreviousBlock;
    }

    public List<Message> getMessages() {
        return new ArrayList<>(this.messages);
    }

    public String getMinerId() {
        return this.minerId;
    }

    public long getMagicNumber() {
        return this.magicNumber;
    }

    public String getNumberOfZerosStatus() {
        return this.numberOfZerosStatus;

    }

    public void setGenerateSeconds(long generateSeconds) {
        this.generateSeconds = generateSeconds;
    }

    @Override
    public String toString() {
        return "\nBlock:"
                + "\nCreated by " + minerId
                + "\n" + minerId + " gets " + rewardAmount + " VC"
                + "\nId: " + id
                + "\nTimestamp: " + timestamp
                + "\nMagic number: " + magicNumber
                + "\nHash of the previous block: \n" + previousBlockHash
                + "\nHash of the block: \n" + getHashCode()
                + "\nBlock data: " + (messages.isEmpty() ? "\nNo transactions" : messages)
                + "\nBlock was generating for " + generateSeconds + " seconds"
                + "\n" + numberOfZerosStatus;
    }

    public void setZeroesInHash(int zeroesInHash) {
        this.zeroesInHash = zeroesInHash;
    }
}
