package com.vitaliebocicov.chainpay.util;

import com.vitaliebocicov.chainpay.Blockchain;
import com.vitaliebocicov.chainpay.mining.Miner;
import com.vitaliebocicov.chainpay.model.Message;
import com.vitaliebocicov.chainpay.security.SignatureUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionUtilTest {

    @Mock
    private Miner sender;
    @Mock
    private Miner receiver;
    @Mock
    private Blockchain blockchain;

    private MockedStatic<Blockchain> mockedBlockchain;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        mockedBlockchain = mockStatic(Blockchain.class);
        mockedBlockchain.when(Blockchain::getInstance).thenReturn(blockchain);

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair realKeyPair = keyGen.generateKeyPair();
        privateKey = realKeyPair.getPrivate();
        publicKey = realKeyPair.getPublic();

        when(blockchain.getVirtualCoinsAmount(sender.getMinerId())).thenReturn(100);
    }

    @AfterEach
    void tearDown() {
        mockedBlockchain.close();
    }

    @Test
    void testSendVirtualCoinsSuccess() {
        int transferAmount = 50;
        TransactionUtil.sendVirtualCoins(sender, receiver, transferAmount, new KeyPair(publicKey, privateKey));

        verify(blockchain, times(1)).receiveMessage(any(Message.class));
    }

    @Test
    void testSendVirtualCoinsInsufficientBalance() {
        int transferAmount = 200;

        when(blockchain.getVirtualCoinsAmount(sender.getMinerId())).thenReturn(50);

        TransactionUtil.sendVirtualCoins(sender, receiver, transferAmount, new KeyPair(publicKey, privateKey));

        verify(blockchain, never()).receiveMessage(any(Message.class));
    }

    @Test
    void testSendVirtualCoinsNegativeBalance() {
        int transferAmount = 200;

        when(blockchain.getVirtualCoinsAmount(sender.getMinerId())).thenReturn(-250);

        TransactionUtil.sendVirtualCoins(sender, receiver, transferAmount, new KeyPair(publicKey, privateKey));

        verify(blockchain, never()).receiveMessage(any(Message.class));
    }

    @Test
    void testSendVirtualCoinsExceedsAllowed() {
        int transferAmount = 500;

        when(blockchain.getVirtualCoinsAmount(sender.getMinerId())).thenReturn(50);

        TransactionUtil.sendVirtualCoins(sender, receiver, transferAmount, new KeyPair(publicKey, privateKey));

        verify(blockchain, never()).receiveMessage(any(Message.class));
    }

    @Test
    void testSendVirtualCoinInvalidSignature() {
        int transferAmount = 50;
        try (MockedStatic<SignatureUtil> mockedStatic = mockStatic(SignatureUtil.class)) {
            mockedStatic.when(() -> SignatureUtil.signMessage(any(Message.class), any(PrivateKey.class)))
                    .thenThrow(NoSuchAlgorithmException.class);
            when(blockchain.getVirtualCoinsAmount(sender.getMinerId())).thenReturn(50);


            assertThrows(RuntimeException.class, () ->
                    TransactionUtil.sendVirtualCoins(sender, receiver, transferAmount, new KeyPair(publicKey, privateKey)));

            verify(blockchain, never()).receiveMessage(any(Message.class));
        }
    }
}