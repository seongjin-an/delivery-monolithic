package com.ansj.delivery.payment.pg;

public interface PgClient {
    PgResult pay(int amount, String method);
    void refund(String pgTransactionId);
}
