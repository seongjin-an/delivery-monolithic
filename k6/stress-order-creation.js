import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const MAX_VUS = 100;

// 커스텀 메트릭 (Trend = 응답시간 분포 추적)
const orderCreateDuration = new Trend('order_create_duration', true);
const orderListDuration   = new Trend('order_list_duration',   true);
const orderCreateFailed   = new Rate('order_create_failed');
const orderListFailed     = new Rate('order_list_failed');

/*
  ramping-vus executor: 시간에 따라 VU 수를 증가시키며 지속 부하 테스트
  목적: 부하 증가에 따른 응답시간 변화 + N+1 쿼리 병목 + DB 커넥션 풀 고갈 시점 확인

  [워밍업 30s] → [부하 증가 1m] → [최대 유지 2m] → [종료 30s]
       10명            50명             100명
*/
export let options = {
  scenarios: {
    order_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 10  }, // 워밍업
        { duration: '1m',  target: 50  }, // 부하 증가
        { duration: '2m',  target: 100 }, // 최대 부하 유지 ← N+1 + 커넥션 풀 터지는 구간
        { duration: '30s', target: 0   }, // 종료
      ],
    },
  },
  thresholds: {
    'order_create_duration': ['p(95)<2000'],  // 주문 생성 95%가 2초 이내
    'order_list_duration':   ['p(95)<1000'],  // 목록 조회 95%가 1초 이내
    'order_create_failed':   ['rate<0.01'],   // 주문 생성 에러율 1% 미만
    'http_req_failed':       ['rate<0.01'],
  },
};

// ── setup: 고객 100명 + 식당 1개 준비 ──────────────────────────────────────
export function setup() {
  const headers = { 'Content-Type': 'application/json' };

  // 오너 생성
  http.post(`${BASE_URL}/api/auth/signup`, JSON.stringify({
    email: 'load_owner@test.com', password: 'password123',
    name: '부하테스트오너', role: 'OWNER',
  }), { headers });

  const ownerToken = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    email: 'load_owner@test.com', password: 'password123',
  }), { headers }).json('data.accessToken');

  const ownerHeaders = { ...headers, Authorization: `Bearer ${ownerToken}` };

  // 식당 생성 → 영업 시작
  const restaurantId = http.post(`${BASE_URL}/api/owner/restaurants`, JSON.stringify({
    name: '부하테스트식당', address: '서울시 강남구 테헤란로 1',
    category: 'KOREAN', minOrderAmount: 10000,
    deliveryFee: 3000, estimatedDeliveryMinutes: 30,
  }), { headers: ownerHeaders }).json('data.id');

  http.patch(`${BASE_URL}/api/owner/restaurants/${restaurantId}/open`, null, { headers: ownerHeaders });

  // 메뉴 3개 등록 (옵션 포함 — N+1 심화용)
  const menuIds = [];
  const menuData = [
    { name: '된장찌개',  price: 12000 },
    { name: '김치찌개',  price: 11000 },
    { name: '순두부찌개', price: 11000 },
  ];

  for (const m of menuData) {
    const menuId = http.post(
      `${BASE_URL}/api/owner/restaurants/${restaurantId}/menus`,
      JSON.stringify({
        name: m.name, price: m.price, sortOrder: menuIds.length + 1,
        options: [
          {
            name: '맵기 선택', isRequired: true, maxSelectCount: 1,
            items: [
              { name: '순한맛', extraPrice: 0 },
              { name: '보통맛', extraPrice: 0 },
              { name: '매운맛', extraPrice: 0 },
            ],
          },
        ],
      }),
      { headers: ownerHeaders },
    ).json('data.id');
    menuIds.push(menuId);
  }

  // 고객 MAX_VUS명 생성 (각 VU가 자기 계정으로 독립적으로 주문)
  console.log(`\n고객 ${MAX_VUS}명 생성 중...`);
  const customerTokens = [];
  for (let i = 0; i < MAX_VUS; i++) {
    http.post(`${BASE_URL}/api/auth/signup`, JSON.stringify({
      email: `load_customer_${i}@test.com`, password: 'password123',
      name: `부하고객${i}`, role: 'CUSTOMER',
    }), { headers });

    const token = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
      email: `load_customer_${i}@test.com`, password: 'password123',
    }), { headers }).json('data.accessToken');

    customerTokens.push(token);
  }

  console.log(`\n✅ 준비 완료`);
  console.log(`   - restaurantId : ${restaurantId}`);
  console.log(`   - menuIds      : [${menuIds}]`);
  console.log(`   - 고객 수       : ${customerTokens.length}명\n`);

  return { restaurantId, menuIds, customerTokens };
}

// ── default: 주문 생성 → 목록 조회 (N+1 발생 지점) ────────────────────────
export default function (data) {
  const { restaurantId, menuIds, customerTokens } = data;

  // VU 번호로 고객 토큰 선택 (100명 초과 시 순환)
  const myToken = customerTokens[(__VU - 1) % customerTokens.length];
  const authHeaders = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${myToken}`,
  };

  // ── 1. 주문 생성 ──────────────────────────────────────────────
  const menuId = menuIds[Math.floor(Math.random() * menuIds.length)];

  const orderStart = Date.now();
  const orderRes = http.post(`${BASE_URL}/api/orders`, JSON.stringify({
    restaurantId,
    deliveryAddress: '서울시 마포구 테스트로 1',
    items: [{ menuId, quantity: 1, selectedOptionItemIds: [] }],
  }), { headers: authHeaders });

  orderCreateDuration.add(Date.now() - orderStart);
  orderCreateFailed.add(orderRes.status !== 201);

  check(orderRes, {
    '주문 생성 201': (r) => r.status === 201,
    '주문 생성 500 없음': (r) => r.status !== 500,
  });

  if (orderRes.status !== 201) {
    console.error(`❌ 주문 생성 실패 VU=${__VU} status=${orderRes.status}: ${orderRes.body}`);
    return;
  }

  //sleep(0.1); // 실제 사용자처럼 약간의 텀

  // ── 2. 내 주문 목록 조회 (N+1 쿼리 발생 지점) ────────────────
  //    주문 수가 쌓일수록 응답시간이 선형으로 증가하는지 확인
  const listStart = Date.now();
  const listRes = http.get(`${BASE_URL}/api/orders`, { headers: authHeaders });

  orderListDuration.add(Date.now() - listStart);
  orderListFailed.add(listRes.status !== 200);

  check(listRes, {
    '목록 조회 200': (r) => r.status === 200,
  });

  //sleep(0.1); // iteration 간 think time
}

// ── teardown: 결과 해석 가이드 출력 ──────────────────────────────────────
export function teardown() {
  console.log(`
═══════════════════════════════════════════════════════
                   주문 생성 부하 테스트 결과 해석
═══════════════════════════════════════════════════════

▶ order_create_duration (주문 생성 응답시간)
  - p(95) < 2000ms  → 정상
  - p(95) > 2000ms  → DB 커넥션 풀 고갈 또는 락 대기 의심

▶ order_list_duration (목록 조회 응답시간)
  - VU 증가에 따라 응답시간이 선형으로 오르면 → N+1 쿼리 문제
  - 갑자기 수직 상승하면 → DB 커넥션 풀 고갈

▶ 확인할 지표
  - hikari.connections.pending 이 올라가면 → DB 커넥션 부족
  - http_req_failed rate 가 올라가는 VU 수 → 서비스 한계 TPS

▶ 예상 병목 순서
  1. order_list_duration 상승 (N+1 쿼리)
  2. hikari connection pending 증가 (커넥션 풀 고갈)
  3. 주문 생성 자체 timeout 발생
═══════════════════════════════════════════════════════
  `);
}
