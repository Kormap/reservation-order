# Reservation Order

선착순 예약·주문 시스템을 단계적으로 고도화하기 위한 Spring Boot 포트폴리오 프로젝트입니다.

## 1단계 MVP

- Java 21, Spring Boot 3.5.16, Gradle Groovy DSL
- PostgreSQL, Spring Data JPA, Flyway
- Spring Security, Spring Session, Redis 세션
- Springdoc OpenAPI/Swagger UI
- 회원 가입/로그인, 상품, 재고, 예약 주문, 주문 취소 API
- 주문 시 재고 즉시 차감, 취소 시 재고 복구
- Testcontainers 기반 PostgreSQL 통합 테스트

현재 재고 차감은 의도적으로 락이나 원자적 UPDATE를 적용하지 않은 기준 구현입니다. 동시 요청에서는 lost update가 발생할 수 있으며, 2단계에서 낙관적 락과 비관적 락을 비교합니다.

## 실행

```bash
docker compose up -d
./gradlew bootRun
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health check: http://localhost:8080/actuator/health

로그인하면 Redis에 저장되는 HTTP 세션이 생성됩니다. 같은 브라우저의 Swagger UI 요청은 `SESSION` 쿠키로 인증됩니다. 상태 변경 API는 CSRF 토큰이 필요합니다.

## 주요 API

| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/v1/auth/signup` | 회원 가입 |
| POST | `/api/v1/auth/login` | 로그인 및 Redis 세션 생성 |
| POST | `/api/v1/auth/logout` | 세션 무효화 |
| GET | `/api/v1/members/me` | 내 회원 정보 조회 |
| POST | `/api/v1/products` | 상품과 초기 재고 생성 |
| GET | `/api/v1/products` | 상품 목록 조회 |
| GET/PATCH | `/api/v1/inventories/{productId}` | 재고 조회/수정 |
| POST | `/api/v1/orders` | 예약 주문 및 재고 차감 |
| GET | `/api/v1/orders` | 내 예약 주문 목록 |
| POST | `/api/v1/orders/{orderId}/cancel` | 주문 취소 및 재고 복구 |

## 환경 변수

| 이름 | 기본값 |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/reservation_order` |
| `DB_USERNAME` | `reservation` |
| `DB_PASSWORD` | `reservation` |
| `REDIS_HOST` | `localhost` |
| `REDIS_PORT` | `6379` |

운영 환경에서는 Redis를 고가용성 구성으로 운영하고, 세션 TTL과 장애 시 로그인 영향도를 함께 점검해야 합니다.

## 다음 단계

1. 무잠금 동시 예약에서 lost update 재현
2. 낙관적 락 + 제한된 재시도와 비관적 락의 처리량/지연 비교
3. Idempotency-Key와 중복 주문 방지
4. Redis 세션 TTL, 동시 로그인, 세션 고정 공격 방어 정책
5. 결제 mock, Transactional Outbox, retry/DLQ
