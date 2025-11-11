# Spring Cloud + Kubernetes 기반 E-Commerce MSA 프로젝트

## 1. 프로젝트 개요
- 모놀리식 구조를 8개의 마이크로서비스로 분리한 E-Commerce 시스템 구현
- Saga 패턴을 통한 분산 트랜잭션 처리 및 보상 트랜잭션 구현
- Redis 분산 락을 활용한 동시성 제어 및 재고 관리
- Apache Kafka 기반 Event-Driven Architecture로 서비스 간 비동기 통신
- Spring Cloud Gateway를 사용하여 서비스 라우팅 및 API Gateway 패턴 구현
- Resilience4j를 통한 Circuit Breaker/Fallback 적용으로 장애 대응
- Micrometer Tracing + Zipkin으로 분산 추적 환경 구축, 서비스 간 호출 흐름 시각화
- OpenFeign을 통한 마이크로서비스 간 동기 통신 구현
- Docker 컨테이너화 및 Docker Compose를 통한 로컬 개발 환경 구축
- Kubernetes 배포를 위한 클라우드 네이티브 아키텍처 설계

## 2. 아키텍처 다이어그램

### 서비스 구성
```
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway (8080)                         │
│                  (Spring Cloud Gateway)                          │
└────────────────────────┬────────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│User Service  │  │Order Service │  │Product Svc   │
│   (8081)     │◄─│   (8082)     │◄─│   (8087)     │
└──────────────┘  └──────┬───────┘  └──────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│Inventory Svc │  │Payment Svc   │  │Delivery Svc  │
│   (8084)     │  │   (8085)     │  │   (8086)     │
└──────────────┘  └──────────────┘  └──────────────┘
                         │
                         ▼
                  ┌──────────────┐
                  │Notification  │
                  │Service (8088)│
                  └──────────────┘

        ┌────────────────────────────────┐
        │   Apache Kafka (9092)          │
        │   - order-created              │
        │   - inventory-reserved         │
        │   - payment-completed          │
        │   - delivery-started           │
        └────────────────────────────────┘

        ┌────────────────────────────────┐
        │   Redis (6379)                 │
        │   - 분산 락 (재고 동시성 제어)  │
        └────────────────────────────────┘
```

### 주요 특징
- **동기 통신 (OpenFeign)**: Order → User, Order → Product (가격 검증)
- **비동기 통신 (Kafka)**: Saga 패턴 기반 이벤트 체인
- **분산 락 (Redis)**: 재고 차감 시 동시성 제어
- **Circuit Breaker**: User Service 장애 시 fallback 동작
- **분산 추적 (Zipkin)**: 서비스 간 호출 흐름 시각화

## 3. 기술 스택

### Backend
- **Java 21**, **Spring Boot 3.1.5**
- **Spring Cloud 2022.0.4**: Gateway, OpenFeign
- **Apache Kafka**: 비동기 메시징 (Event-Driven Architecture)
- **Redis**: 분산 락 (Redisson)
- **Resilience4j**: Circuit Breaker, Fallback
- **Micrometer Tracing + Brave**: 분산 추적
- **Zipkin**: 트레이싱 서버
- **H2 / MySQL**: Database per Service 패턴

### Infra
- **Docker**: 컨테이너화
- **Docker Compose**: 로컬 개발 환경
- **Kubernetes**: 프로덕션 배포 (예정)

## 4. 마이크로서비스 구성

### 4.1 User Service (8081)
- 사용자 정보 관리
- 사용자 검증 API 제공
- H2 in-memory database

### 4.2 Order Service (8082)
- 주문 생성 및 관리
- Saga 오케스트레이터 역할
- User/Product Service 호출 (OpenFeign)
- Kafka 이벤트 발행: order-created

### 4.3 Product Service (8087) ⭐ 신규
- 상품 정보 관리 (카탈로그)
- 가격 정보 제공 (서버 측 가격 검증)
- 카테고리별 상품 조회
- 주문 시점 스냅샷 데이터 제공

### 4.4 Inventory Service (8084)
- 재고 관리 및 확보
- Redis 분산 락 기반 동시성 제어
- Saga 보상 트랜잭션 구현 (재고 복구)
- Kafka 이벤트 처리: order-created → inventory-reserved

### 4.5 Payment Service (8085)
- 결제 처리 (시뮬레이션)
- Kafka 이벤트 처리: inventory-reserved → payment-completed

### 4.6 Delivery Service (8086)
- 배송 시작 처리
- Kafka 이벤트 처리: payment-completed → delivery-started

### 4.7 Notification Service (8088)
- 알림 발송 (이메일, SMS 시뮬레이션)
- Kafka 이벤트 구독: order-created, delivery-started, delivery-completed

### 4.8 API Gateway (8080)
- 단일 진입점 (Single Entry Point)
- 라우팅 및 로드 밸런싱
- 인증/인가 (예정)

## 5. 주요 기능 및 패턴 구현

### 1) Saga 패턴 (분산 트랜잭션) ⭐ 핵심
E-Commerce의 주문 프로세스를 Choreography 기반 Saga로 구현

**정상 플로우:**
```
Order Created → Inventory Reserved → Payment Completed → Delivery Started → Completed
```

**보상 트랜잭션 플로우 (결제 실패 시):**
```
Order Created → Inventory Reserved → Payment Failed
                      ↓                    ↓
                재고 복구 (Release)    Order Cancelled
```

**구현 상세:**
- Kafka 이벤트 체인으로 각 단계 연결
- `PaymentFailedEvent`에 `productId`, `quantity` 포함하여 재고 복구 가능
- Inventory Service가 `payment-failed` 토픽을 구독하여 자동 롤백

### 2) Redis 분산 락 (동시성 제어) ⭐ 핵심
재고 차감 시 Race Condition 방지

**문제 시나리오:**
- 100명이 마지막 1개 재고 동시 주문 시 음수 재고 발생

**해결 방법:**
- Redisson 기반 분산 락 구현
- `@DistributedLock` 커스텀 어노테이션 + AOP
- SpEL 표현식으로 상품별 동적 락 키 생성 (`inventory:lock:#productId`)
- Lock 타임아웃: 5초, Lease Time: 3초 (데드락 방지)

**효과:**
- 동시 요청 시 1개만 성공, 나머지는 "재고 부족" 응답
- 재고 음수 발생 방지

### 3) 서버 측 가격 검증 (보안) ⭐ 핵심
클라이언트가 가격을 조작할 수 없도록 서버에서 가격 계산

**Before (취약점):**
```json
POST /orders
{
  "userId": 1,
  "productName": "MacBook Pro",
  "price": 1,  // 클라이언트가 1원으로 조작 가능!
  "quantity": 1
}
```

**After (보안 강화):**
```json
POST /orders
{
  "userId": 1,
  "productId": 1,  // 상품 ID만 전달
  "quantity": 1
}
// 서버가 Product Service에서 실제 가격(3,500,000원)을 조회하여 계산
```

### 4) 마이크로서비스 아키텍처
- 8개 독립 서비스로 분리 (User, Order, Product, Inventory, Payment, Delivery, Notification, Gateway)
- Database per Service 패턴 (각 서비스 독립 DB)
- RESTful API 설계

### 5) API Gateway (Spring Cloud Gateway)
- 단일 진입점을 통한 라우팅
- 서비스별 로드 밸런싱
- 인증/인가 처리 (예정)

### 6) 서비스 간 통신
- **동기 통신 (OpenFeign)**: 즉시 응답 필요 (사용자 검증, 가격 조회)
- **비동기 통신 (Kafka)**: 이벤트 기반 처리 (알림, Saga 플로우)
- **Service Discovery**: Kubernetes Service DNS (운영), URL 직접 지정 (로컬)

### 7) 장애 대응 (Resilience4j)
- **Circuit Breaker**: 장애 전파 차단
- **Fallback**: User Service 장애 시 기본값 반환
- 설정: 10번 중 50% 실패 시 Circuit Open

### 8) 분산 추적 (Micrometer + Zipkin)
- **Trace ID/Span ID**: 서비스 간 요청 흐름 추적
- **B3 Propagation**: OpenFeign 호출 시 trace context 자동 전파
- **Zipkin UI**: 서비스 의존성 그래프 및 성능 시각화

### 9) 가격 스냅샷 패턴
주문 시점의 가격을 저장하여 이후 가격 변동에 영향받지 않도록 처리

**Order Entity:**
- `productId`: Product Service 참조 (현재 정보 조회 가능)
- `productName`: 주문 시점 스냅샷 (상품명 변경 시에도 유지)
- `unitPrice`: 주문 시점 단가 (BigDecimal)
- `totalPrice`: 서버 계산 총액 (단가 × 수량)

## 6. 개발 타임라인

### Phase 1: 기본 MSA 구성
- User/Order 서비스 분리
- Spring Cloud Gateway 구축
- Circuit Breaker 적용
- Micrometer + Zipkin 분산 추적

### Phase 2: E-Commerce 확장 (현재)
- **Product Service 신규 구축**: 서버 측 가격 검증 보안 강화
- **Inventory Service 리팩토링**: productName → productId 기반으로 전환
- **Payment/Delivery Service 추가**: 전체 주문 프로세스 구현
- **Saga 패턴 구현**: Choreography 방식의 분산 트랜잭션
- **Redis 분산 락**: 동시성 제어 및 재고 무결성 보장
- **Notification Service**: 비동기 알림 발송

### Phase 3: 프로덕션 준비 (예정)
- Kubernetes 배포
- Outbox Pattern (트랜잭셔널 메시징)
- 멱등성 처리 (Idempotency)
- API 인증/인가 (JWT, OAuth2)
- 모니터링 (Prometheus, Grafana)
- CI/CD 파이프라인

## 7. 실행 방법

### 7.1 로컬 개발 환경 (Docker Compose)

```bash
# 1. 전체 빌드
./gradlew clean build -x test

# 2. Docker Compose로 전체 시스템 시작
docker-compose up -d --build

# 3. 서비스 상태 확인
docker-compose ps

# 4. 특정 서비스 로그 확인
docker-compose logs -f order-service
docker-compose logs -f inventory-service

# 5. 종료
docker-compose down
```

### 7.2 접속 URL
- **API Gateway**: http://localhost:8080
- **User Service**: http://localhost:8081
- **Order Service**: http://localhost:8082
- **Inventory Service**: http://localhost:8084
- **Payment Service**: http://localhost:8085
- **Delivery Service**: http://localhost:8086
- **Product Service**: http://localhost:8087
- **Notification Service**: http://localhost:8088
- **Zipkin UI**: http://localhost:9411
- **Kafka UI**: http://localhost:9000 (Kafdrop)

### 7.3 E-Commerce 주문 플로우 테스트

```bash
# 1. 상품 조회
curl http://localhost:8087/products | jq

# 2. 특정 상품 상세 정보
curl http://localhost:8087/products/1 | jq
# Response: {"id":1,"name":"MacBook Pro 16","price":3500000,...}

# 3. 주문 생성 (productId만 전달, 가격은 서버에서 계산!)
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "productId": 1,
    "quantity": 1
  }' | jq

# 4. 사용자별 주문 조회
curl "http://localhost:8082/orders?userId=1" | jq

# 5. 재고 확인
curl http://localhost:8084/inventory/1 | jq
```

### 7.4 Saga 플로우 확인 (터미널 여러 개 사용)

```bash
# Terminal 1: Order Service 로그
docker-compose logs -f order-service

# Terminal 2: Inventory Service 로그
docker-compose logs -f inventory-service

# Terminal 3: Payment Service 로그
docker-compose logs -f payment-service

# Terminal 4: Delivery Service 로그
docker-compose logs -f delivery-service

# Terminal 5: Notification Service 로그
docker-compose logs -f notification-service

# Terminal 6: 주문 생성
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "productId": 1, "quantity": 1}'

# 로그에서 Saga 이벤트 체인 확인:
# Order → order-created 발행
# Inventory → 재고 확보 → inventory-reserved 발행
# Payment → 결제 처리 → payment-completed 발행
# Delivery → 배송 시작 → delivery-started 발행
# Notification → 각 단계마다 알림 발송
```

### 7.5 동시성 테스트 (분산 락 검증)

```bash
# 시나리오: 100명이 마지막 1개 재고 동시 주문

# 1. 재고 확인
curl http://localhost:8084/inventory/1 | jq
# {"productId":1,"quantity":1}

# 2. 100개 동시 요청 발생
for i in {1..100}; do
  curl -X POST http://localhost:8082/orders \
    -H "Content-Type: application/json" \
    -d '{"userId":1,"productId":1,"quantity":1}' &
done
wait

# 3. 결과 확인
# - 1개 주문만 성공 (200 OK)
# - 99개 주문 실패 (재고 부족)
# - 재고: 0개 (음수 아님!)

# 4. Redis 락 상태 확인
docker exec -it redis redis-cli
> KEYS inventory:lock:*
> TTL inventory:lock:1  # 락이 자동으로 해제되었는지 확인
```

### 7.6 보상 트랜잭션 테스트

```bash
# 시나리오: 재고 부족으로 주문 실패 시 정상 롤백

# 1. 재고 확인
curl http://localhost:8084/inventory/1 | jq
# {"productId":1,"quantity":5}

# 2. 재고보다 많은 수량 주문
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productId":1,"quantity":999}' | jq

# 3. 로그 확인
docker-compose logs inventory-service | grep "재고 부족"
# ⚠️ 재고 부족 - productId: 1, 요청: 999, 현재: 5
# 📤 inventory-failed 이벤트 발행

docker-compose logs order-service | grep "주문 취소"
# ❌ 주문 취소 처리 - orderId: X

# 4. 재고 재확인 (변동 없어야 함)
curl http://localhost:8084/inventory/1 | jq
# {"productId":1,"quantity":5}  ← 원상태 유지 ✅
```

### 7.7 Zipkin 분산 추적 확인

```bash
# 1. 주문 생성
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productId":1,"quantity":1}'

# 2. Zipkin UI 접속
open http://localhost:9411

# 3. 확인 사항:
# - Service Name: order-service 선택
# - Find Traces 클릭
# - 서비스 간 호출 흐름: Order → User → Product → Inventory
# - 각 서비스의 응답 시간 확인
# - 동일 Trace ID로 연결된 Span 확인
```

## 8. 학습 포인트 및 기술적 도전

### 8.1 실무 수준의 보안 구현
- **문제**: 클라이언트가 가격을 조작하여 1원에 상품 구매 가능
- **해결**: Product Service 신규 구축 + 서버 측 가격 검증
- **학습**: 금액 관련 데이터는 절대 클라이언트 입력을 믿으면 안 됨

### 8.2 분산 시스템의 동시성 제어
- **문제**: 100명이 동시 주문 시 재고 음수 발생
- **해결**: Redis 분산 락 (Redisson) + AOP 기반 커스텀 어노테이션
- **학습**: 단일 서버 락(@Synchronized)은 MSA에서 무용지물, 분산 락 필수

### 8.3 Saga 패턴의 보상 트랜잭션
- **문제**: 결제 실패 시 이미 차감된 재고 복구 방법 부재
- **해결**: Kafka 이벤트에 productId/quantity 포함 + Compensating Transaction
- **학습**: 분산 트랜잭션은 2PC가 아닌 Saga 패턴으로 해결

### 8.4 데이터 일관성 보장
- **문제**: 문자열 기반 productName으로 오타 발생 (재고 조회 실패)
- **해결**: productId 기반 아키텍처 전환 (Product Service와 1:1 매칭)
- **학습**: 외래키 대신 ID 참조로 서비스 간 느슨한 결합 유지

### 8.5 Spring Boot 3.x 분산 추적 이슈
- **문제**: OpenFeign 호출 시 trace context가 자동 전파되지 않음
- **해결**: `feign-micrometer` 의존성 추가 + B3 Propagation 설정
- **학습**: Spring Boot 3.x는 Sleuth가 제거되어 Micrometer Tracing으로 전환

### 8.6 Database per Service 패턴
- **장점**: 서비스 독립성, 기술 스택 자유도
- **단점**: JOIN 불가, 데이터 중복 (스냅샷 패턴으로 해결)
- **학습**: 주문 시점의 상품명/가격을 Order 테이블에 저장 (가격 변동 영향 없음)

## 9. 관련 문서
- [E-Commerce 실무 수준 아키텍처 개선 가이드](docs/E-Commerce-Production-Improvements.md) ⭐ 필독
- [Circuit Breaker 가이드](docs/resilience4j-patterns.md)
- [Circuit Breaker Q&A](docs/Circuit-Breaker-QNA.md)
- [Zipkin 분산 추적 가이드](docs/Zipkin-Distributed-Tracing.md)

## 10. 프로젝트 구조
```
MSA-SpringCloud-Kubernetes/
├── common/                      # 공통 이벤트 클래스 (Kafka)
│   └── src/main/java/com/example/common/event/
│       ├── OrderCreatedEvent.java
│       ├── InventoryReservedEvent.java
│       ├── PaymentCompletedEvent.java
│       └── DeliveryStartedEvent.java
├── user-service/                # 사용자 관리 (8081)
├── order-service/               # 주문 관리 (8082)
│   ├── client/
│   │   ├── UserClient.java      # OpenFeign
│   │   └── ProductClient.java   # OpenFeign
│   └── kafka/
│       └── SagaEventConsumer.java
├── product-service/             # 상품 관리 (8087) ⭐ 신규
│   ├── entity/Product.java
│   ├── dto/ProductResponse.java
│   └── controller/ProductController.java
├── inventory-service/           # 재고 관리 (8084)
│   ├── annotation/
│   │   └── DistributedLock.java # 커스텀 어노테이션
│   ├── aop/
│   │   └── DistributedLockAop.java
│   ├── config/
│   │   └── RedisConfig.java
│   └── service/
│       └── InventoryService.java
├── payment-service/             # 결제 처리 (8085)
├── delivery-service/            # 배송 관리 (8086)
├── notification-service/        # 알림 발송 (8088)
├── gateway/                     # API Gateway (8080)
├── docs/                        # 프로젝트 문서
│   ├── E-Commerce-Production-Improvements.md
│   ├── resilience4j-patterns.md
│   ├── Circuit-Breaker-QNA.md
│   └── Zipkin-Distributed-Tracing.md
├── docker-compose.yml           # 로컬 개발 환경
└── README.md
```

## 11. 기여 및 피드백
- 이슈 제기: GitHub Issues
- 개선 제안: Pull Request
- 질문: Discussions
