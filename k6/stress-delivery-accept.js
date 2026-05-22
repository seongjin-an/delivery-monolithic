import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// 커스텀 메트릭
const successCount = new Counter('delivery_accept_success');
const conflictCount = new Counter('delivery_accept_conflict');
const errorCount = new Counter('delivery_accept_error');

// default 함수가 몇 번 실행되느냐는 executor 설정이 결정한다.
/*
  전체 흐름 요약

  k6 실행
    │
    ├─ setup() 1회 ──────────────── 라이더 50명 생성, 주문 1개 READY_FOR_PICKUP 준비
    │   └─ { orderId, riderTokens } 반환
    │
    ├─ default() 50회 동시 실행 ─── VU 1~50이 동시에 같은 orderId로 수락 시도
    │   ├─ VU 1  → riderTokens[0] 으로 POST /accept
    │   ├─ VU 2  → riderTokens[1] 으로 POST /accept
    │   ├─ ...
    │   └─ VU 50 → riderTokens[49] 으로 POST /accept
    │
    └─ teardown() 1회 ──────────── 결과 출력

  한 줄 요약: VU = 동시 접속 유저, __VU = 그 유저의 번호표, executor = 언제 몇 번 실행할지 전략.
  s
executor 종류마다 "언제, 얼마나" 실행할지 전략이 다름
  'shared-iterations'  // 총 N번을 VU들이 나눠서 → 동시성 테스트에 적합
  'constant-vus'       // N명이 duration 동안 계속 반복 → 지속 부하 테스트
  'ramping-vus'        // 시간에 따라 VU 수를 늘리거나 줄임 → 부하 증가 테스트
  'per-vu-iterations'  // VU 각자가 N번씩 → 총 VU*N 번 실행
  'constant-arrival-rate' // 초당 N번 요청 유지 → TPS 고정 테스트

  저번에 제안했던 주문 생성 시나리오에서 stages를 썼던 것도 ramping-vus executor를 쓰는 방식이었습니다.

  stages: [
    { duration: '30s', target: 10 },  // 30초 동안 10명으로 증가
    { duration: '1m',  target: 100 }, // 1분 동안 100명으로 증가
  ]
 */
export let options = {
  scenarios: {
    concurrent_accept: {
      executor: 'shared-iterations', // 50번의 실행 횟수를 50명의 VU 가 나눠서 처리, VU 50명이 각자 1번씩 default 를 실행하고, 이게 동시에 터진다. 그래서 같은 주문에 50명이 동시에 수락을 시도하는 시나리오가 만들어진다.
      vus: 50,        // 라이더 50명이, 가상 유저
      iterations: 50, // 동시에 같은 주문 1개를 수락 시도, 총 싫행 횟수
      maxDuration: '30s',
    },
  },
  thresholds: {
    // 모든 응답이 201(성공) 또는 409(중복) 이어야 함 — 500이 나오면 실패
    'delivery_accept_error': ['count==0'],
  },
};

// ── setup: 테스트 데이터 준비 (VU 실행 전 1회만 실행) ──────────────────────
export function setup() {
  const headers = { 'Content-Type': 'application/json' };

  // 1. 오너 계정
  http.post(`${BASE_URL}/api/auth/signup`, JSON.stringify({
    email: 'stress_owner@test.com', password: 'password123',
    name: '스트레스오너', role: 'OWNER',
  }), { headers });

  const ownerToken = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    email: 'stress_owner@test.com', password: 'password123',
  }), { headers }).json('data.accessToken');

  // 2. 고객 계정
  http.post(`${BASE_URL}/api/auth/signup`, JSON.stringify({
    email: 'stress_customer@test.com', password: 'password123',
    name: '스트레스고객', role: 'CUSTOMER',
  }), { headers });

  const customerToken = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    email: 'stress_customer@test.com', password: 'password123',
  }), { headers }).json('data.accessToken');

  // 3. 라이더 50명 생성
  const riderTokens = [];
  for (let i = 0; i < 50; i++) {
    http.post(`${BASE_URL}/api/auth/signup`, JSON.stringify({
      email: `stress_rider_${i}@test.com`, password: 'password123',
      name: `스트레스라이더${i}`, role: 'RIDER',
    }), { headers });

    const token = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
      email: `stress_rider_${i}@test.com`, password: 'password123',
    }), { headers }).json('data.accessToken');

    riderTokens.push(token);
  }

  // 4. 식당 생성 → 영업 시작
  const ownerHeaders = { ...headers, Authorization: `Bearer ${ownerToken}` };

  const restaurantId = http.post(`${BASE_URL}/api/owner/restaurants`, JSON.stringify({
    name: '스트레스테스트식당', address: '서울시 강남구 테헤란로 1',
    category: 'KOREAN', minOrderAmount: 10000,
    deliveryFee: 3000, estimatedDeliveryMinutes: 30,
  }), { headers: ownerHeaders }).json('data.id');

  http.patch(`${BASE_URL}/api/owner/restaurants/${restaurantId}/open`, null, { headers: ownerHeaders });

  // 5. 메뉴 등록
  const menuId = http.post(
    `${BASE_URL}/api/owner/restaurants/${restaurantId}/menus`,
    JSON.stringify({ name: '테스트메뉴', price: 15000, sortOrder: 1 }),
    { headers: ownerHeaders },
  ).json('data.id');

  // 6. 주문 생성 → 결제 → 수락 → 조리완료 (READY_FOR_PICKUP)
  const customerHeaders = { ...headers, Authorization: `Bearer ${customerToken}` };

  const orderId = http.post(`${BASE_URL}/api/orders`, JSON.stringify({
    restaurantId,
    deliveryAddress: '서울시 마포구 테스트로 1',
    items: [{ menuId, quantity: 1, selectedOptionItemIds: [] }],
  }), { headers: customerHeaders }).json('data.id');

  http.post(`${BASE_URL}/api/payments`, JSON.stringify({
    orderId, method: 'CARD',
  }), { headers: customerHeaders });

  http.patch(`${BASE_URL}/api/owner/orders/${orderId}/accept`, null, { headers: ownerHeaders });
  http.patch(`${BASE_URL}/api/owner/orders/${orderId}/ready`, null, { headers: ownerHeaders });

  console.log(`\n✅ 준비 완료`);
  console.log(`   - orderId  : ${orderId}`);
  console.log(`   - 라이더 수 : ${riderTokens.length}명\n`);

  return { orderId, riderTokens };
}

// ── default: 50명 동시 수락 시도 ──────────────────────────────────────────
export default function (data) {
  const { orderId, riderTokens } = data;
  const myToken = riderTokens[__VU - 1]; // VU는 1부터 시작

  const res = http.post(
    `${BASE_URL}/api/rider/deliveries/${orderId}/accept`,
    null,
    { headers: { Authorization: `Bearer ${myToken}` } },
  );

  // 결과 분류
  if (res.status === 201) {
    successCount.add(1);
    console.log(`✅ VU ${__VU} (라이더 ${__VU - 1}번) → 배달 수락 성공`);
  } else if (res.status === 409) {
    conflictCount.add(1);
  } else {
    errorCount.add(1);
    console.error(`❌ VU ${__VU} → 예상치 못한 응답 ${res.status}: ${res.body}`);
  }

  check(res, {
    '201 또는 409만 나와야 함': (r) => r.status === 201 || r.status === 409,
    '500 서버 에러 없음':       (r) => r.status !== 500,
  });
}

// ── teardown: 결과 요약 ───────────────────────────────────────────────────
export function teardown(data) {
  console.log('\n═══════════════════════════════════');
  console.log('         동시 배달 수락 결과');
  console.log('═══════════════════════════════════');
  console.log(`orderId: ${data.orderId}`);
  console.log('위 메트릭에서 아래를 확인하세요:');
  console.log('  delivery_accept_success → 반드시 1이어야 함');
  console.log('  delivery_accept_conflict → 49여야 함');
  console.log('  delivery_accept_error   → 반드시 0이어야 함');
  console.log('═══════════════════════════════════\n');
}
