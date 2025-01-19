package com.vitaliebocicov.chainpay.mining;

import com.vitaliebocicov.chainpay.Blockchain;
import com.vitaliebocicov.chainpay.model.Block;
import com.vitaliebocicov.chainpay.security.KeysGenerator;
import com.vitaliebocicov.chainpay.util.TransactionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinerTest {
    @Mock
    private Blockchain blockchain;

    @Mock
    private KeysGenerator keysGenerator;

    @Mock
    private KeyPair keyPair;

    @Mock
    private Random random;

    private Miner miner;
    private Miner contact;

    @BeforeEach
    void setUp() {
        try (MockedStatic<Blockchain> mockedBlockchain = mockStatic(Blockchain.class)) {
            mockedBlockchain.when(Blockchain::getInstance).thenReturn(blockchain);
            when(keysGenerator.createKeyPair()).thenReturn(keyPair);
            miner = new Miner(1, keysGenerator);
            contact = new Miner(2, keysGenerator);
            miner.setContacts(List.of(contact).toArray(new Miner[0]));
        }
    }

    @Test
    void testMinerInitialization() {
        assertEquals("miner1", miner.getMinerId(), "Miner ID should be initialized correctly");
        assertNotNull(miner, "Miner should be initialized");
    }

    @Test
    void testSetContactsExcludesSelf() {
        try (MockedStatic<Blockchain> mockedBlockchain = mockStatic(Blockchain.class)) {
            mockedBlockchain.when(Blockchain::getInstance).thenReturn(blockchain);
            when(keysGenerator.createKeyPair()).thenReturn(keyPair);
            miner = new Miner(1, keysGenerator);
            Miner miner2 = new Miner(2, keysGenerator);
            Miner miner3 = new Miner(3, keysGenerator);
            Miner[] miners = {miner, miner2, miner3};

            miner.setContacts(miners);

            assertEquals(2, miner.getContacts().size(), "Contacts list should exclude the miner itself");
            assertTrue(miner.getContacts().contains(miner2), "Contacts should contain miner2");
            assertTrue(miner.getContacts().contains(miner3), "Contacts should contain miner3");
        }
    }

    @Test
    void testFindMagicNumberReturnsTrueWhenFound() {
        Block block = mock(Block.class);
        when(blockchain.isMagicNumberFound()).thenReturn(false);
        when(blockchain.getBlockCounter()).thenReturn(0L);
        when(block.getId()).thenReturn(1L);
        when(block.getHashCode()).thenReturn("00hash");
        when(random.nextDouble(1)).thenReturn(0.99);

        boolean result = miner.findMagicNumber(block, 2, random);

        assertTrue(result, "findMagicNumber should return true when the magic number is found");
    }

    @Test
    void testRunCallsAcceptBlock() throws InterruptedException {
        when(blockchain.getSize()).thenReturn(0, 1, 3);
        when(blockchain.getChainSize()).thenReturn(1, 2, 2);
        when(blockchain.isMagicNumberFound()).thenReturn(false);
        when(blockchain.isMagicNumberFound()).thenReturn(false);
        when(blockchain.getBlockCounter()).thenReturn(0L);

        Thread minerThread = new Thread(miner);
        minerThread.start();

        minerThread.join();

    }

    @Test
    void testRunCallsAcceptBlockWithSleep() throws InterruptedException {
        when(blockchain.getSize()).thenReturn(0, 1, 3);
        when(blockchain.getChainSize()).thenReturn(1, 2, 2);
        when(blockchain.isMagicNumberFound()).thenReturn(true, false, false);
        when(blockchain.getBlockCounter()).thenReturn(0L);

        Thread minerThread = new Thread(miner);
        minerThread.start();

        minerThread.join();

        verify(blockchain, times(1)).acceptBlock(any());
    }

    @Test
    void testSendVirtualCoins() {
        try (MockedStatic<TransactionUtil> transactionUtilMockedStatic = mockStatic(TransactionUtil.class)) {
            Random random = mock(Random.class);

            when(random.nextInt(miner.getContacts().size())).thenReturn(0);
            when(random.nextInt(0, Miner.BOUND)).thenReturn(10);

            miner.sendVirtualCoins(random);

            transactionUtilMockedStatic.verify(() ->
                    TransactionUtil.sendVirtualCoins(miner, contact, 11, keyPair)
            );
        }
    }

    @Test
    void testPrintAmountCallsGetVirtualCoinsAmountOnce() {
        when(blockchain.getVirtualCoinsAmount("miner1")).thenReturn(50);

        miner.printAmount();

        verify(blockchain).getVirtualCoinsAmount("miner1");
    }
}