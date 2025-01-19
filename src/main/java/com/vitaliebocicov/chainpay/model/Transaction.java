package com.vitaliebocicov.chainpay.model;

import com.vitaliebocicov.chainpay.util.StringUtil;

public record Transaction(String sender, String receiver, int virtualCoinsAmount) {
    public String getHashCode() {
        return StringUtil.applySha256(sender + receiver + virtualCoinsAmount);
    }
}
