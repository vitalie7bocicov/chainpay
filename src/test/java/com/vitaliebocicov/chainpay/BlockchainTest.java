package com.vitaliebocicov.chainpay;

import com.vitaliebocicov.chainpay.mining.Miner;
import com.vitaliebocicov.chainpay.model.Block;
import com.vitaliebocicov.chainpay.model.Message;
import com.vitaliebocicov.chainpay.model.Transaction;
import com.vitaliebocicov.chainpay.security.KeysGenerator;
import com.vitaliebocicov.chainpay.security.SignatureUtil;
import com.vitaliebocicov.chainpay.util.StringUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockchainTest {

    private static final int VIRTUAL_COINS_REWARD = 100;
    private static final int MAX_NUMBER_OF_ZEROES = 7;
    private Blockchain blockchain;
    private Block block;
    private Message message;
    @Mock
    private Miner sender;

    KeyPair keys;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchFieldException, IllegalAccessException {
        Field instance = Blockchain.class.getDeclaredField("blockchain");
        instance.setAccessible(true);
        instance.set(null, null);
        keys = new KeysGenerator().createKeyPair();
        blockchain = Blockchain.getInstance(10);

        Transaction transaction = new Transaction("miner0", "miner1", 10);
        message = new Message(1, keys.getPublic());
        message.setTransaction(transaction);
        message.setSignature(SignatureUtil.signMessage(message, keys.getPrivate()));

        block = new Block(1,
                "0",
                List.of(message),
                "miner1",
                0,
                0,
                100);
        block.setMagicNumber(0);
    }

    @Test
    void testBlockchainInstanceInitialization() {
        assertNotNull(blockchain);
        assertEquals(10, blockchain.getChainSize());
    }

    @Test
    void testBlockchainInstanceInitializationThrowsException() throws NoSuchFieldException, IllegalAccessException {
        Field instance = Blockchain.class.getDeclaredField("blockchain");
        instance.setAccessible(true);
        instance.set(null, null);
        IllegalStateException exception = assertThrows(IllegalStateException.class, Blockchain::getInstance);
        assertEquals("Blockchain not initialized. Call getInstance(int chainSize) first.", exception.getMessage());
    }

    @Test
    void testBlockchainInstanceInitializationWithoutChainSize() {
        Blockchain blockchainInstance = Blockchain.getInstance(10);
        Blockchain blockchain1 = Blockchain.getInstance();
        assertNotNull(blockchainInstance);
        assertNotNull(blockchain1);
        assertEquals(10, blockchainInstance.getChainSize());
    }

    @Test
    void testAcceptBlock() {
        blockchain.acceptBlock(block);
        assertEquals(1, blockchain.getSize());
    }

    @Test
    void testIsMagicNumberFoundWhenFalse() {
        blockchain.setMagicNumberFound(false);
        assertFalse(blockchain.isMagicNumberFound(), "magicNumberFound should be false initially.");
    }

    @Test
    void testIsMagicNumberFoundWhenTrue() {
        blockchain.setMagicNumberFound(true);
        assertTrue(blockchain.isMagicNumberFound(), "magicNumberFound should be true when set.");
    }

    @Test
    void testBlockValidity() {
        blockchain.acceptBlock(block);
        assertTrue(blockchain.isChainValid(), "Blockchain should be valid after adding a valid block.");
    }

    @Test
    void testVirtualCoinsAmount() {
        when(sender.getMinerId()).thenReturn("miner1");
        blockchain.acceptBlock(block);
        int senderVirtualCoins = blockchain.getVirtualCoinsAmount(sender.getMinerId());
        assertEquals(VIRTUAL_COINS_REWARD + 10, senderVirtualCoins, "Miner should have 100 VC as a reward.");
    }

    @Test
    void testIsMessageValid() {
        boolean isValidMessage = blockchain.isMessageValid(message);
        assertTrue(isValidMessage, "The message should be valid.");
    }

    @Test
    void testAreValidBlockMessagesWithValidMessages() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Block testBlock = getBlockWithTwoMessages();
        assertTrue(blockchain.areValidBlockMessages(testBlock), "Messages in block should be valid.");
    }

    @Test
    void testAreValidBlockMessagesThrowsNoSuchAlgorithmException() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Block testBlock = getBlockWithTwoMessages();

        try (MockedStatic<SignatureUtil> mockedStatic = mockStatic(SignatureUtil.class)) {
            mockedStatic.when(() -> SignatureUtil.isSignatureValid(any(Message.class)))
                    .thenThrow(NoSuchAlgorithmException.class);

            assertThrows(RuntimeException.class, () -> blockchain.areValidBlockMessages(testBlock));
        }
    }

    @Test
    void testAreValidBlockMessagesThrowsInvalidKeyException() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Block testBlock = getBlockWithTwoMessages();

        try (MockedStatic<SignatureUtil> mockedStatic = mockStatic(SignatureUtil.class)) {
            mockedStatic.when(() -> SignatureUtil.isSignatureValid(any(Message.class)))
                    .thenThrow(InvalidKeyException.class);

            assertThrows(RuntimeException.class, () -> blockchain.areValidBlockMessages(testBlock));
        }
    }

    @Test
    void testAreValidBlockMessagesThrowsSignatureException() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Block testBlock = getBlockWithTwoMessages();

        try (MockedStatic<SignatureUtil> mockedStatic = mockStatic(SignatureUtil.class)) {
            mockedStatic.when(() -> SignatureUtil.isSignatureValid(any(Message.class)))
                    .thenThrow(SignatureException.class);

            assertThrows(RuntimeException.class, () -> blockchain.areValidBlockMessages(testBlock));
        }
    }

    @Test
    void testAreValidBlockMessagesWithInvalidSignature() {
        Message invalidMessage = new Message(1, keys.getPublic());
        invalidMessage.setTransaction(new Transaction("miner1", "miner2", 10));

        // Generate a 128-byte invalid signature
        byte[] invalidSignature = new byte[128];
        Arrays.fill(invalidSignature, (byte) 1);
        invalidMessage.setSignature(invalidSignature);

        Block testBlock = new Block(1, "0", List.of(invalidMessage), "miner1", 0, 0, 100);

        assertFalse(blockchain.areValidBlockMessages(testBlock), "Block messages should be invalid due to incorrect signature.");
    }

    @Test
    void testPrint() {
        blockchain.acceptBlock(block);

        assertDoesNotThrow(() -> blockchain.print(), "Print method should execute without throwing an exception.");
    }

    @Test
    void testReceiveMessageWithValidMessage() {
        int initialSize = blockchain.getCurrentBlockMessages().size();

        blockchain.receiveMessage(message);
        assertEquals(initialSize + 1, blockchain.getCurrentBlockMessages().size(), "Valid message should be added.");
    }

    @Test
    void testReceiveMessageWithInvalidMessage() {
        Message invalidMessage = new Message(0, keys.getPublic());
        blockchain.receiveMessage(invalidMessage);

        assertFalse(blockchain.getCurrentBlockMessages().contains(invalidMessage), "Invalid message should not be added.");
    }

    @Test
    void testIsChainValidWithValidChainWithOneBlock() {
        blockchain.acceptBlock(block);

        assertTrue(blockchain.isChainValid(), "Blockchain should be valid after adding valid blocks.");
    }

    @Test
    void testGetMessageCounter() {
        long initialCounter = blockchain.getMessageCounter();
        assertEquals(initialCounter + 1, blockchain.getMessageCounter(), "Message counter should increment by 1.");
    }

    @Test
    void testGetRewardAmount() {
        assertEquals(100, blockchain.getRewardAmount());
    }

    @Test
    void testGetBlockCounter() {
        long initialBlockCounter = blockchain.getBlockCounter();
        blockchain.acceptBlock(block);

        assertEquals(initialBlockCounter + 1, blockchain.getBlockCounter(), "Block counter should increment when a new block is added.");
    }

    @Test
    void testGetMaxMessageIdPreviousBlock() {
        assertEquals(0, blockchain.getMaxMessageIdPreviousBlock(), "maxMessageIdPreviousBlock should be 2");
    }

    @Test
    void testGetPreviousBlockMessages() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Block testBlock = getBlockWithTwoMessages();
        blockchain.receiveMessage(testBlock.getMessages().getFirst());
        blockchain.receiveMessage(testBlock.getMessages().get(1));

        blockchain.acceptBlock(testBlock);
        List<Message> retrievedMessages = blockchain.getPreviousBlockMessages();

        assertEquals(1, blockchain.getSize());
        assertEquals(testBlock.getMessages(), retrievedMessages, "previousBlockMessages should match the expected list");
    }

    @Test
    void testDecreaseNumberOfZeros() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        blockchain.setZeroesInHash(5);
        Method decreaseNumberOfZerosMethod = Blockchain.class.getDeclaredMethod("decreaseNumberOfZeros");
        decreaseNumberOfZerosMethod.setAccessible(true);  // Make the private method accessible

        decreaseNumberOfZerosMethod.invoke(blockchain);
        assertEquals(4, blockchain.getZeroesInHash(), "Number of zeros should decrease by 1");

        blockchain.setZeroesInHash(1);
        decreaseNumberOfZerosMethod.invoke(blockchain);
        assertEquals(0, blockchain.getZeroesInHash(), "Number of zeros should not go below 0");

        decreaseNumberOfZerosMethod.invoke(blockchain);
        assertEquals(0, blockchain.getZeroesInHash(), "Number of zeros should remain 0 when already at minimum");
    }

    @Test
    void testIsChainValidWithEmptyChain() {
        assertTrue(blockchain.isChainValid(), "Blockchain should be valid if no blocks have been added.");
    }

    @Test
    void testMessageIdLessThanOrEqualToMaxMessageIdPreviousBlock() {
        blockchain.setMaxMessageIdPreviousBlock(5);
        Message message = new Message(4, null);  // Message ID <= maxMessageIdPreviousBlock
        message.setTransaction(new Transaction("miner1", "miner2", 10));  // Valid transaction
        assertFalse(blockchain.isMessageValid(message), "Message should be invalid due to low ID.");
    }

    @Test
    void testInvalidSignature() {
        blockchain = Blockchain.getInstance(10);
        Message message = new Message(6, null);
        message.setTransaction(new Transaction("miner1", "miner2", 10));  // Valid transaction

        try (MockedStatic<SignatureUtil> mockedStatic = mockStatic(SignatureUtil.class)) {
            mockedStatic.when(() -> SignatureUtil.isSignatureValid(any(Message.class)))
                    .thenReturn(false);

            assertFalse(blockchain.isMessageValid(message), "Message should be invalid due to invalid signature.");
        }
    }

    @Test
    void testSignatureException() {
        blockchain = Blockchain.getInstance(10);
        Message message = new Message(6, null);
        message.setTransaction(new Transaction("miner1", "miner2", 10));  // Valid transaction

        // Mock SignatureUtil to throw a NoSuchAlgorithmException
        try (MockedStatic<SignatureUtil> mockedStatic = mockStatic(SignatureUtil.class)) {
            mockedStatic.when(() -> SignatureUtil.isSignatureValid(any(Message.class)))
                    .thenThrow(NoSuchAlgorithmException.class);

            assertThrows(RuntimeException.class, () -> blockchain.isMessageValid(message),
                    "Expected RuntimeException due to NoSuchAlgorithmException in signature validation.");
        }
    }


    @Test
    void testInvalidChainPreviousHash() {
        blockchain = Blockchain.getInstance(10);
        Block genesisBlock = new Block(1, "0", new ArrayList<>(), "miner1", 0, 0, 100);
        Block secondBlock = new Block(2, "0Valid", new ArrayList<>(), "miner1", 0, 0, 100);

        try (MockedStatic<StringUtil> mockedStatic = mockStatic(StringUtil.class)) {
            mockedStatic.when(() -> StringUtil.applySha256(anyString()))
                    .thenReturn("validHash")
                    .thenReturn("validHash")
                    .thenReturn("0Valid")
                    .thenReturn("0Valid")
                    .thenReturn("0InValid");

            blockchain.acceptBlock(genesisBlock);
            blockchain.acceptBlock(secondBlock);

            assertFalse(blockchain.isChainValid(), "Chain should be invalid due to the invalid hash.");
        }
    }

    @Test
    void testAdjustZeroesInHash_DecreaseZeros() {
        block.setGenerateSeconds(65);

        blockchain.adjustZeroesInHash(block);

        assertEquals("N was decreased by 1", block.getNumberOfZerosStatus());
    }

    @Test
    void testAdjustZeroesInHash_MaxZeroes() {
        block.setGenerateSeconds(30);
        block.setZeroesInHash(MAX_NUMBER_OF_ZEROES);

        blockchain.adjustZeroesInHash(block);

        assertEquals("N stays the same", block.getNumberOfZerosStatus());
    }

    @Test
    void testAdjustZeroesInHash_IncreaseZeros() {
        blockchain = Blockchain.getInstance(10);
        block.setGenerateSeconds(10);
        block.setZeroesInHash(MAX_NUMBER_OF_ZEROES - 1);

        blockchain.adjustZeroesInHash(block);

        assertEquals("N was increased to " + blockchain.getZeroesInHash(), block.getNumberOfZerosStatus());
    }

    @Test
    void testAdjustZeroesInHash_NoChange() {
        blockchain = Blockchain.getInstance(10);
        block.setGenerateSeconds(30);
        block.setZeroesInHash(MAX_NUMBER_OF_ZEROES);

        blockchain.adjustZeroesInHash(block);

        assertEquals("N stays the same", block.getNumberOfZerosStatus());
    }

    @Test
    void testVirtualCoinsAmountMinerRewardOnly() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        Message validMessage1 = new Message(1, keys.getPublic());
        validMessage1.setTransaction(new Transaction("miner1", "miner2", 10));
        validMessage1.setSignature(SignatureUtil.signMessage(validMessage1, keys.getPrivate()));

        List<Message> messages = List.of(validMessage1);
        blockchain.receiveMessage(validMessage1);

        Block testBlock = new Block(1, "0", messages, "miner1", 0, 0, 100);
        blockchain.acceptBlock(testBlock);

        int minerCoins = blockchain.getVirtualCoinsAmount("miner1");

        assertEquals(VIRTUAL_COINS_REWARD - 10, minerCoins, "Miner should have only 90 VC left");
    }

    @Test
    void testVirtualCoinsAmountMinerWithTransactions() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Message validMessage1 = new Message(1, keys.getPublic());
        validMessage1.setTransaction(new Transaction("miner1", "miner2", 10));
        validMessage1.setSignature(SignatureUtil.signMessage(validMessage1, keys.getPrivate()));

        Message validMessage2 = new Message(2, keys.getPublic());
        validMessage2.setTransaction(new Transaction("miner2", "miner1", 20));
        validMessage2.setSignature(SignatureUtil.signMessage(validMessage2, keys.getPrivate()));

        List<Message> messages = List.of(validMessage1, validMessage2);
        blockchain.receiveMessage(validMessage1);
        blockchain.receiveMessage(validMessage2);

        Block testBlock = new Block(1, "0", messages, "miner1", 0, 0, 100);
        blockchain.acceptBlock(testBlock);

        int minerCoins = blockchain.getVirtualCoinsAmount("miner1");

        // Reward + Received 20 - Sent 10 = VIRTUAL_COINS_REWARD + 10
        assertEquals(VIRTUAL_COINS_REWARD + 10, minerCoins, "Miner should have reward plus net transaction amount.");
    }

    @Test
    void testVirtualCoinsAmountUserReceivingOnly() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Message validMessage1 = new Message(1, keys.getPublic());
        validMessage1.setTransaction(new Transaction("miner1", "user2", 15));
        validMessage1.setSignature(SignatureUtil.signMessage(validMessage1, keys.getPrivate()));

        List<Message> messages = List.of(validMessage1);
        blockchain.receiveMessage(validMessage1);

        Block testBlock = new Block(1, "0", messages, "otherMiner", 0, 0, 100);
        blockchain.acceptBlock(testBlock);

        int userCoins = blockchain.getVirtualCoinsAmount("user2");

        assertEquals(15, userCoins, "User should have only the coins received in the transaction.");
    }

    @Test
    void testVirtualCoinsAmountUserSendingOnly() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Message validMessage1 = new Message(1, keys.getPublic());
        validMessage1.setTransaction(new Transaction("user3", "user2", 10));
        validMessage1.setSignature(SignatureUtil.signMessage(validMessage1, keys.getPrivate()));

        List<Message> messages = List.of(validMessage1);
        blockchain.receiveMessage(validMessage1);

        Block testBlock = new Block(1, "0", messages, "otherMiner", 0, 0, 100);
        blockchain.acceptBlock(testBlock);

        int userCoins = blockchain.getVirtualCoinsAmount("user3");

        assertEquals(-10, userCoins, "User should have only the coins sent in the transaction.");
    }

    @Test
    void testVirtualCoinsAmountTransactionNull() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        blockchain = Blockchain.getInstance(10);

        Message validMessage1 = new Message(1, keys.getPublic());
        validMessage1.setTransaction(null);
        validMessage1.setSignature(SignatureUtil.signMessage(validMessage1, keys.getPrivate()));

        List<Message> messages = List.of(validMessage1);
        blockchain.receiveMessage(validMessage1);

        Block testBlock = new Block(1, "0", messages, "miner1", 0, 0, 100);
        blockchain.acceptBlock(testBlock);

        int minerCoins = blockchain.getVirtualCoinsAmount("miner1");

        assertEquals(VIRTUAL_COINS_REWARD, minerCoins, "Miner should have only the reward coins due to null transaction.");
    }

    private Block getBlockWithTwoMessages() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Message validMessage1 = new Message(1, keys.getPublic());
        validMessage1.setTransaction(new Transaction("miner1", "miner2", 10));
        validMessage1.setSignature(SignatureUtil.signMessage(validMessage1, keys.getPrivate()));

        Message validMessage2 = new Message(2, keys.getPublic());
        validMessage2.setTransaction(new Transaction("miner1", "miner3", 15));
        validMessage2.setSignature(SignatureUtil.signMessage(validMessage2, keys.getPrivate()));

        return new Block(1,
                "0",
                List.of(validMessage1, validMessage2),
                "miner1",
                0,
                0,
                100);
    }

}