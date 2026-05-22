## 동시 배달 수락 테스트 실행 방법

1. 앱 + DB 실행 \
   docker compose up -d
   ./gradlew bootRun

2. k6 설치 \
   brew install k6

3. 테스트 실행 \
   k6 run k6/stress-delivery-accept.js

4. 재실행 시 데이터 초기화 \
   docker exec -i delivery-postgres psql -U delivery -d delivery < k6/cleanup.sql

## 주문 생성 주하 테스트 실행 방법
1. 재실행 전 초기화 \
docker exec -i delivery-postgres psql -U delivery -d delivery < k6/cleanup.sql

2. 테스트 실행 \
k6 run k6/stress-order-creation.js
