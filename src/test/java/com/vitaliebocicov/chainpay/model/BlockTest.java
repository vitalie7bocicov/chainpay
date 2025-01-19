package com.vitaliebocicov.chainpay.model;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlockTest {

    @Mock
    PublicKey publicKey;

    @Test
    void testComputeMessagesHash() {
        Block block = getBlock();

        List<Message> emptyMessages = new ArrayList<>();
        String hash = block.computeMessagesHash(emptyMessages);
        assertNotNull(hash, "Hash should not be null for empty messages");

        List<Message> messages = new ArrayList<>();
        messages.add(new Message(1, publicKey));
        hash = block.computeMessagesHash(messages);
        assertNotNull(hash, "Hash should not be null for non-empty messages");
    }

    @Test
    void testGetPreviousBlockHash() {
        Block block = getBlock();
        String previousBlockHash = block.getPreviousBlockHash();
        assertEquals("previousBlockHash", previousBlockHash, "Previous block hash should be correct");
    }

    @Test
    void testGetHashCode() {
        Block block = getBlock();

        block.setMagicNumber(12345L);
        String hashCode = block.getHashCode();
        assertNotNull(hashCode, "Block hash should not be null");
        assertFalse(hashCode.isEmpty(), "Block hash should not be empty");
    }

    @Test
    void testSetGeneratedSeconds() {
        Block block = getBlock();
        block.setGeneratedSeconds(100L);

        assertEquals(100L, block.getGenerateSeconds(), "Generated seconds should be set and retrieved correctly");
    }

    @Test
    void testSetAndGetMagicNumber() {
        Block block = getBlock();
        block.setMagicNumber(12345L);

        assertEquals(12345L, block.getMagicNumber(), "Magic number should be set and retrieved correctly");
    }

    @Test
    void testGetGenerateSeconds() {
        Block block = getBlock();
        block.setGeneratedSeconds(150L);

        assertEquals(150L, block.getGenerateSeconds(), "Generated seconds should be set and retrieved correctly");
    }

    @Test
    void testGetId() {
        Block block = getBlock();
        assertEquals(1L, block.getId(), "Block ID should be correct");
    }

    @Test
    void testSetNumberOfZerosStatus() {
        Block block = getBlock();
        block.setNumberOfZerosStatus("5 zeros");

        assertEquals("5 zeros", block.getNumberOfZerosStatus(), "Number of zeros status should be set correctly");
    }

    @Test
    void testGetZeroesInHash() {
        Block block = getBlock();
        assertEquals(3, block.getZeroesInHash(), "Zeroes in hash should be correct");
    }

    @Test
    void testGetMaxMessageIdPreviousBlock() {
        Block block = getBlock();
        assertEquals(2L, block.getMaxMessageIdPreviousBlock(), "Max message ID from previous block should be correct");
    }

    @Test
    void testGetMessages() {
        Block block = getBlock();
        List<Message> messages = block.getMessages();
        assertNotNull(messages, "Messages should not be null");
        assertTrue(messages.isEmpty(), "Messages should be empty initially");
    }

    @Test
    void testGetMinerId() {
        Block block = getBlock();
        assertEquals("miner1", block.getMinerId(), "Miner ID should be correct");
    }

    @Test
    void testToString() {
        Block block = getBlock();
        System.out.println(block);
    }

    private Block getBlock() {
        String previousBlockHash = "previousBlockHash";
        List<Message> messages = new ArrayList<>();
        String minerId = "miner1";
        int zeroesInHash = 3;
        long maxMessageIdPreviousBlock = 2L;
        int rewardAmount = 100;

        return new Block(1L,
                previousBlockHash,
                messages,
                minerId,
                zeroesInHash,
                maxMessageIdPreviousBlock,
                rewardAmount);
    }
}