package com.ansj.delivery.payment.pg;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FakePgClient implements PgClient {

    @Override
    public PgResult pay(int amount, String method) {
        // 실제 PG 연동 전 가짜 구현 — 항상 성공하며 UUID로 거래 ID 생성
        return PgResult.success("FAKE-" + UUID.randomUUID());
    }

    @Override
    public void refund(String pgTransactionId) {
        // 실제 PG 연동 전 가짜 구현 — 아무것도 하지 않음
    }
}
