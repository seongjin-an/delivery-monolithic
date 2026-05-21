# 배달 플랫폼 설계 문서

## 주문 상태머신

```
[고객 주문]
    │
    ▼
PENDING_PAYMENT      ← 주문 생성, 결제 대기
    │ 결제 완료
    ▼
PAYMENT_COMPLETED    ← 결제 완료, 가게에 접수 요청
    │ 가게 수락
    ▼
ACCEPTED             ← 가게 접수, 조리 시작
    │ 조리 완료
    ▼
READY_FOR_PICKUP     ← 픽업 대기, 라이더 배정 시작
    │ 라이더 픽업
    ▼
PICKED_UP            ← 배달 중
    │ 배달 완료
    ▼
DELIVERED            ← 완료

취소 경로:
  PENDING_PAYMENT    → CANCELLED  (결제 실패 or 고객 취소)
  PAYMENT_COMPLETED  → CANCELLED  (가게 거절 → 자동 환불)
  ACCEPTED           → CANCELLED  (가게 취소 → 자동 환불)
```

---

## ERD

```mermaid
erDiagram

  users {
    uuid    id PK
    string  email UK
    string  password_hash
    string  name
    string  phone
    enum    role "CUSTOMER | OWNER | RIDER | ADMIN"
    timestamp created_at
    timestamp updated_at
  }

  addresses {
    bigint  id PK
    uuid    user_id FK
    string  alias "집, 회사 등"
    string  address
    string  detail_address
    decimal latitude
    decimal longitude
    boolean is_default
  }

  restaurants {
    bigint  id PK
    uuid    owner_id FK
    string  name
    string  phone
    string  address
    decimal latitude
    decimal longitude
    enum    category "KOREAN | CHINESE | JAPANESE | PIZZA | BURGER | CHICKEN | CAFE"
    enum    status "OPEN | CLOSED | PREPARING"
    int     min_order_amount
    int     delivery_fee
    int     estimated_delivery_minutes
    timestamp created_at
    timestamp updated_at
  }

  menus {
    bigint  id PK
    bigint  restaurant_id FK
    string  name
    string  description
    int     price
    string  image_url
    boolean is_available
    string  category_name
    int     sort_order
  }

  menu_options {
    bigint  id PK
    bigint  menu_id FK
    string  name "사이즈 선택, 토핑 추가 등"
    boolean is_required
    int     max_select_count
  }

  menu_option_items {
    bigint  id PK
    bigint  menu_option_id FK
    string  name "Large, 치즈 추가 등"
    int     extra_price
  }

  orders {
    uuid    id PK
    uuid    customer_id FK
    bigint  restaurant_id FK
    enum    status "PENDING_PAYMENT | PAYMENT_COMPLETED | ACCEPTED | READY_FOR_PICKUP | PICKED_UP | DELIVERED | CANCELLED"
    int     total_amount
    int     delivery_fee
    string  delivery_address
    decimal delivery_latitude
    decimal delivery_longitude
    string  request_note
    string  cancel_reason
    timestamp created_at
    timestamp updated_at
  }

  order_items {
    bigint  id PK
    uuid    order_id FK
    bigint  menu_id FK
    string  menu_name "주문 시점 스냅샷"
    int     quantity
    int     unit_price "주문 시점 스냅샷"
    int     total_price
  }

  order_item_options {
    bigint  id PK
    bigint  order_item_id FK
    string  option_name
    string  option_item_name
    int     extra_price
  }

  payments {
    uuid    id PK
    uuid    order_id FK "UNIQUE"
    uuid    customer_id FK
    int     amount
    enum    method "CARD | KAKAO_PAY | NAVER_PAY | TOSS"
    enum    status "PENDING | COMPLETED | FAILED | REFUNDED"
    string  pg_transaction_id
    timestamp paid_at
    timestamp refunded_at
    timestamp created_at
  }

  deliveries {
    bigint  id PK
    uuid    order_id FK "UNIQUE"
    uuid    rider_id FK
    enum    status "ASSIGNED | PICKED_UP | DELIVERED"
    timestamp assigned_at
    timestamp picked_up_at
    timestamp delivered_at
  }

  rider_locations {
    bigint  id PK
    uuid    rider_id FK
    decimal latitude
    decimal longitude
    timestamp recorded_at
  }

  reviews {
    bigint  id PK
    uuid    order_id FK "UNIQUE"
    uuid    customer_id FK
    bigint  restaurant_id FK
    int     rating "1~5"
    string  content
    timestamp created_at
  }

  notifications {
    bigint  id PK
    uuid    user_id FK
    string  type "ORDER_STATUS_CHANGED | DELIVERY_ASSIGNED | PAYMENT_COMPLETED 등"
    string  title
    string  content
    boolean is_read
    timestamp created_at
  }

  users        ||--o{ addresses          : "has"
  users        ||--o{ restaurants        : "owns"
  users        ||--o{ orders             : "places"
  users        ||--o{ payments           : "pays"
  users        ||--o{ deliveries         : "rides"
  users        ||--o{ rider_locations    : "tracks"
  users        ||--o{ reviews            : "writes"
  users        ||--o{ notifications      : "receives"

  restaurants  ||--o{ menus              : "has"
  restaurants  ||--o{ orders             : "receives"
  restaurants  ||--o{ reviews            : "receives"

  menus        ||--o{ menu_options       : "has"
  menu_options ||--o{ menu_option_items  : "has"
  menus        ||--o{ order_items        : "referenced by"

  orders       ||--o{ order_items        : "contains"
  orders       ||--|| payments           : "paid by"
  orders       ||--|| deliveries         : "delivered by"
  orders       ||--o| reviews            : "reviewed as"

  order_items  ||--o{ order_item_options : "has"
```

---

## 도메인 모듈 구조 (모놀리식 Phase 1)

```
delivery/
├── src/main/java/com/example/delivery/
│   ├── user/
│   │   ├── domain/
│   │   ├── repository/
│   │   ├── service/
│   │   └── controller/
│   ├── restaurant/
│   ├── order/
│   ├── payment/
│   ├── delivery/
│   └── notification/
```

---

## 진행 로드맵

| Phase | 내용 | 핵심 기술 |
|-------|------|-----------|
| 1 | 모놀리식 + 모듈화, 전체 도메인 구현 | Spring Boot, JPA, JWT |
| 2 | 스트레스 테스트 → 병목 시각화 | k6, OTel, Grafana |
| 3 | 병목 서비스 MSA 분리, 이벤트 기반 전환 | Kafka, Saga 패턴 |
| 4 | 실시간 배달 추적, 알림, 운영 완성 | WebSocket, SSE |
