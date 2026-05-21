package com.ansj.delivery.payment.pg;

public record PgResult(boolean success, String pgTransactionId, String failReason) {
    public static PgResult success(String pgTransactionId) {
        return new PgResult(true, pgTransactionId, null);
    }

    public static PgResult fail(String reason) {
        return new PgResult(false, null, reason);
    }
}
