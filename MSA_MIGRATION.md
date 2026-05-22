# MSA 전환 계획

## 분리할 서비스

```
delivery-monolithic (현재)
│
├── user-service        회원가입, 로그인, JWT 발급
├── restaurant-service  식당 및 메뉴 관리
├── order-service       주문 생성 및 상태 관리
├── payment-service     결제 처리 (PG 연동)
├── delivery-service    배달 배정 및 상태 관리
├── notification-service 알림 발송
│
└── [인프라]
    ├── api-gateway     단일 진입점, JWT 검증, 라우팅
    ├── eureka-server   서비스 디스커버리
    └── kafka           비동기 이벤트 브로커
```

---

## 서비스 간 통신 전략

```
동기 (REST)      →  조회성 요청, 즉시 응답이 필요한 경우
비동기 (Kafka)   →  상태 변경 이벤트, 알림 발송
```

| 시나리오 | 방식 | 예시 |
|---|---|---|
| 식당 정보 조회 | REST | order-service → restaurant-service |
| 주문 생성 후 알림 | Kafka | order-service → notification-service |
| 결제 완료 후 주문 상태 변경 | Kafka | payment-service → order-service |
| 배달 완료 후 알림 | Kafka | delivery-service → notification-service |

---

## 현재 모놀리식의 직접 의존 관계 (끊어야 할 것들)

```java
// OrderService 가 NotificationService 를 직접 주입 → Kafka 이벤트로 교체
private final NotificationService notificationService;

// DeliveryService 가 OrderRepository 를 직접 사용 → REST 또는 Kafka 로 교체
private final OrderRepository orderRepository;

// PaymentService 가 OrderRepository 를 직접 사용 → REST 로 교체
private final OrderRepository orderRepository;
```

---

## 분산 트랜잭션 처리 — Saga 패턴

결제 흐름처럼 여러 서비스에 걸친 트랜잭션은 Saga 패턴으로 처리한다.

```
[Choreography-based Saga] — 이벤트 기반 자율 협력

1. order-service     → ORDER_CREATED 발행
2. payment-service   → 이벤트 수신 → 결제 시도 → PAYMENT_COMPLETED 발행
3. order-service     → 이벤트 수신 → 주문 상태 PAYMENT_COMPLETED 로 변경
4. notification-service → 이벤트 수신 → 알림 발송

실패 시 보상 트랜잭션:
payment-service 결제 실패 → PAYMENT_FAILED 발행
order-service   → 이벤트 수신 → 주문 CANCELLED 처리
```

---

## 기술 스택

| 항목 | 기술 | 비고 |
|---|---|---|
| API Gateway | Spring Cloud Gateway | JWT 검증, 라우팅, Rate Limiting |
| Service Discovery | Netflix Eureka | 서비스 자동 등록/탐색 |
| Message Broker | Apache Kafka | 비동기 이벤트 |
| 서비스 간 REST | OpenFeign | 선언적 HTTP 클라이언트 |
| DB | 서비스별 독립 PostgreSQL | Database per Service 패턴 |
| Config | Spring Cloud Config | 중앙 설정 관리 |
| Container | Docker + Docker Compose | 로컬 개발 환경 |

---

## 단계별 전환 계획 (Strangler Fig Pattern)

기존 모놀리식을 유지하면서 서비스를 하나씩 떼어낸다.

### Phase 1 — 인프라 구성
- [ ] Eureka Server 구성
- [ ] Spring Cloud Gateway 구성 (JWT 검증 필터 포함)
- [ ] Kafka + Zookeeper Docker Compose 구성
- [ ] 각 서비스 프로젝트 뼈대 생성 (Spring Initializr)

### Phase 2 — User Service 분리 (가장 독립적)
- [ ] user-service 로 회원/인증 로직 이전
- [ ] JWT 발급을 user-service 에서 담당
- [ ] Gateway 에서 JWT 검증 → userId/role 헤더로 전달
- [ ] 다른 서비스는 DB 조인 대신 헤더의 userId 사용

### Phase 3 — Restaurant Service 분리
- [ ] restaurant-service 로 식당/메뉴 로직 이전
- [ ] order-service 가 메뉴 정보 필요 시 → OpenFeign 으로 조회
- [ ] 주문 시점 메뉴 스냅샷은 order-service DB 에 저장 (현재와 동일)

### Phase 4 — Order + Payment Service 분리
- [ ] order-service, payment-service 분리
- [ ] 결제 흐름을 Kafka Saga 로 재구성
  - ORDER_CREATED → PAYMENT_REQUESTED → PAYMENT_COMPLETED → ORDER_CONFIRMED
- [ ] 보상 트랜잭션 (PAYMENT_FAILED → ORDER_CANCELLED) 구현

### Phase 5 — Delivery + Notification Service 분리
- [ ] delivery-service 분리
  - order-service 로부터 READY_FOR_PICKUP 이벤트 수신
  - 배달 완료 시 ORDER_DELIVERED 이벤트 발행
- [ ] notification-service 분리
  - 모든 이벤트를 구독해서 알림 발송

---

## Kafka 이벤트 목록

| Topic | Producer | Consumer | 시점 |
|---|---|---|---|
| `order.created` | order-service | payment-service, notification-service | 주문 생성 |
| `payment.completed` | payment-service | order-service, notification-service | 결제 완료 |
| `payment.failed` | payment-service | order-service | 결제 실패 |
| `order.accepted` | order-service | notification-service | 주문 수락 |
| `order.ready` | order-service | delivery-service | 조리 완료 |
| `delivery.assigned` | delivery-service | notification-service | 배달 배정 |
| `delivery.picked_up` | delivery-service | order-service, notification-service | 픽업 완료 |
| `delivery.completed` | delivery-service | order-service, notification-service | 배달 완료 |

---

## 현재 코드에서 달라지는 핵심 포인트

### 1. JWT 검증 위치 변경
```
현재: 각 서비스의 JwtAuthenticationFilter
변경: API Gateway 에서 한 번만 검증 → userId, role 을 헤더로 전달

// Gateway 통과 후 각 서비스는 헤더만 읽으면 됨
String userId = request.getHeader("X-User-Id");
String role   = request.getHeader("X-User-Role");
```

### 2. 직접 Repository 참조 → REST or Event
```
현재: deliveryService.orderRepository.findById()
변경: orderServiceClient.getOrder(orderId)  // OpenFeign
  또는 Kafka 이벤트로 필요한 데이터 수신
```

### 3. 동기 알림 → 비동기 이벤트
```
현재: notificationService.send() 직접 호출 → 실패 시 주문 롤백
변경: kafkaTemplate.send("order.created", event) → 알림 서비스가 별도 처리
```

---

## 예상 난이도

| 작업 | 난이도 | 이유 |
|---|---|---|
| Eureka + Gateway 구성 | ★★☆ | Spring Cloud 설정 |
| User Service 분리 | ★★☆ | 의존성 적음 |
| Restaurant Service 분리 | ★★☆ | 의존성 적음 |
| Order + Payment Saga | ★★★★☆ | 분산 트랜잭션 복잡 |
| Delivery Service 분리 | ★★★☆ | Order 상태 연동 |
| Kafka 이벤트 설계 | ★★★☆ | 이벤트 스키마, 멱등성 |
| 통합 테스트 | ★★★★☆ | 서비스 간 계약 테스트 |
