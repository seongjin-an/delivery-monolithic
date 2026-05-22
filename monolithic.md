```
  모놀리식의 한계                     MSA로 해결

  배포 시 전체 서비스 중단        →   payment-service만 배포 가능
  한 모듈 버그가 전체 다운         →   delivery-service 장애가 order에 무관
  Order팀/Payment팀 코드 충돌     →   저장소 자체가 분리
  DB 스키마를 모두가 공유          →   각 서비스가 자기 DB 소유
  트래픽 몰리는 서비스만 스케일 불가 →  order-service만 10대로 늘리기 가능
```