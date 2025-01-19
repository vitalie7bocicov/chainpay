package com.vitaliebocicov.chainpay.security;

import com.vitaliebocicov.chainpay.model.Message;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.Signature;


public final class SignatureUtil {

    private SignatureUtil() { }

    public static byte[] signMessage(Message message, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature rsa = Signature.getInstance("SHA256withRSA");
        rsa.initSign(privateKey);
        rsa.update(message.getData());
        return rsa.sign();
    }

    public static boolean isSignatureValid(Message message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(message.getPublicKey());
        sig.update(message.getData());
        return sig.verify(message.getSignature());
    }
}
