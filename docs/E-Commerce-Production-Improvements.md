# E-Commerce 실무 수준 아키텍처 개선 가이드

> 구현 가이드 문서 | 학습용 프로토타입 → 프로덕션 레벨 시스템 전환

## 목차
1. [개선 배경](#1-개선-배경)
2. [Product Service 구축](#2-product-service-구축)
3. [재고 관리 리팩토링](#3-재고-관리-리팩토링)
4. [Saga 보상 트랜잭션](#4-saga-보상-트랜잭션)
5. [Redis 분산 락](#5-redis-분산-락)
6. [테스트 가이드](#6-테스트-가이드)
7. [아키텍처 비교](#7-아키텍처-비교)

---

## 1. 개선 배경

### 기존 시스템의 문제점

#### 1.1 보안 취약점 (Critical)
```json
// ❌ 기존: 클라이언트가 가격을 직접 입력
POST /orders
{
  "userId": 1,
  "productName": "MacBook Pro",
  "price": 1,           // 악의적인 사용자가 1원으로 조작!
  "quantity": 1
}
```

**문제:**
- 클라이언트가 가격을 조작할 수 있음
- 3,500,000원짜리 맥북을 1원에 구매 가능
- 실제 서비스에서는 절대 있어서는 안 되는 구조

#### 1.2 데이터 일관성 문제
```java
// ❌ 기존: 문자열 기반 상품명
@Entity
public class Inventory {
    private String productName;  // "맥북 프로" vs "MacBook Pro" 오타 발생
    private Integer quantity;
}

// 문제 발생 시나리오
Order: productName = "MacBook Pro"
Inventory: productName = "맥북프로"  // 띄어쓰기 차이로 재고 조회 실패!
```

#### 1.3 동시성 제어 부재
```
100명이 동시에 마지막 1개 재고 주문 시:

Thread 1: 재고 조회(1) → 차감 → 저장(0) ✅
Thread 2: 재고 조회(1) → 차감 → 저장(0) ✅
Thread 3: 재고 조회(1) → 차감 → 저장(0) ✅
...
Thread 100: 재고 조회(1) → 차감 → 저장(0) ✅

결과: 100개 주문 모두 성공, 재고 -99개 ❌
```

#### 1.4 불완전한 Saga 보상
```
주문 생성 → 재고 확보 → 결제 실패
    ↓            ↓           ↓
 PENDING    RESERVED    PaymentFailedEvent
                            ↓
              재고 복구 방법이 없음! ❌
              (productId, quantity 정보 부재)
```

---

## 2. Product Service 구축

### 2.1 아키텍처 구조

```
┌─────────────────────────────────────────────────────────┐
│                    Client (Web/App)                     │
└────────────────────────┬────────────────────────────────┘
                         │ POST /orders
                         │ {userId: 1, productId: 1, quantity: 1}
                         ↓
┌─────────────────────────────────────────────────────────┐
│                   Order Service                          │
├─────────────────────────────────────────────────────────┤
│  1. User 검증 (OpenFeign)                                │
│     └─> UserClient.getUserById(userId)                  │
│                                                          │
│  2. Product 정보 조회 및 가격 검증 (OpenFeign) ⭐        │
│     └─> ProductClient.getProductById(productId)         │
│         └─> 가격: 3,500,000원 (서버에서 가져옴)          │
│                                                          │
│  3. 서버 측 총액 계산 ⭐                                  │
│     totalPrice = price × quantity                       │
│     = 3,500,000 × 1 = 3,500,000원                       │
│                                                          │
│  4. 주문 생성 (가격 스냅샷 저장) ⭐                       │
│     Order {                                             │
│       productId: 1,                                     │
│       productName: "MacBook Pro 16",  // 스냅샷         │
│       unitPrice: 3,500,000,            // 스냅샷         │
│       totalPrice: 3,500,000            // 서버 계산      │
│     }                                                    │
└────────────────┬────────────────────────┬───────────────┘
                 │                        │
                 ↓                        ↓
┌────────────────────────┐  ┌───────────────────────────┐
│    User Service        │  │   Product Service ⭐ NEW  │
├────────────────────────┤  ├───────────────────────────┤
│ GET /api/users/{id}    │  │ GET /products/{id}        │
│                        │  │                           │
│ UserResponse {         │  │ ProductResponse {         │
│   id: 1,               │  │   id: 1,                  │
│   name: "홍길동",      │  │   name: "MacBook Pro 16", │
│   email: "hong@..."    │  │   price: 3500000,         │
│ }                      │  │   category: "ELECTRONICS",│
│                        │  │   brand: "Apple"          │
│                        │  │ }                         │
└────────────────────────┘  └───────────────────────────┘
```

### 2.2 Entity 설계

**Product Entity**
```java
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    // 가격은 BigDecimal 사용 (정확한 금액 계산)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Category category;

    @Column(length = 100)
    private String brand;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean active = true;  // 논리 삭제
}
```

**Category Enum**
```java
public enum Category {
    ELECTRONICS,      // 전자기기
    FASHION,          // 패션
    FOOD,             // 식품
    BOOKS,            // 도서
    SPORTS,           // 스포츠
    HOME_LIVING,      // 홈/리빙
    BEAUTY,           // 뷰티
    OTHERS            // 기타
}
```

### 2.3 Order Entity 개선

**Before vs After**

```java
// ❌ Before
@Entity
public class Order {
    private Long userId;
    private String productName;  // 문자열
    private Integer quantity;
    private Integer price;       // 클라이언트 입력
}

// ✅ After
@Entity
public class Order {
    private Long userId;

    // Product Service 참조
    @Column(nullable = false)
    private Long productId;

    // 주문 시점 스냅샷 (가격 변경 영향 없음)
    @Column(nullable = false)
    private String productName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;    // 주문 시점 단가

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;   // 서버 계산 총액

    @Column(nullable = false)
    private Integer quantity;
}
```

### 2.4 서비스 로직 개선

**OrderService.java**
```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserClient userClient;
    private final ProductClient productClient;  // ⭐ 추가
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        // 1. 사용자 검증
        UserResponse user = userClient.getUserById(request.getUserId());
        log.info("✅ 사용자 검증 완료: {}", user.getName());

        // 2. 상품 정보 조회 및 가격 검증 ⭐ 핵심 보안 로직
        ProductResponse product = productClient.getProductById(request.getProductId());
        log.info("✅ 상품 정보 조회: {} - {}원", product.getName(), product.getPrice());

        // 3. 서버 측 가격 계산 (클라이언트 입력 무시!)
        BigDecimal totalPrice = product.getPrice()
            .multiply(new BigDecimal(request.getQuantity()));

        // 4. 주문 생성 (가격 스냅샷 저장)
        Order order = new Order(
            request.getUserId(),
            product.getId(),
            product.getName(),      // 주문 시점 상품명
            request.getQuantity(),
            product.getPrice(),     // 주문 시점 단가
            totalPrice              // 서버 계산 총액
        );

        orderRepository.save(order);

        // 5. Kafka 이벤트 발행
        OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(order.getId())
            .userId(order.getUserId())
            .productId(order.getProductId())      // ⭐ productId 추가
            .productName(order.getProductName())
            .quantity(order.getQuantity())
            .unitPrice(order.getUnitPrice().intValue())
            .totalPrice(order.getTotalPrice().intValue())
            .createdAt(LocalDateTime.now())
            .build();

        kafkaTemplate.send("order-created", event);

        return order;
    }
}
```

### 2.5 OpenFeign Client

**ProductClient.java**
```java
@FeignClient(
    name = "product-service",
    url = "${product.service.url:http://localhost:8087}"
)
public interface ProductClient {

    @GetMapping("/products/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id);

    @GetMapping("/products")
    List<ProductResponse> getAllProducts();

    @GetMapping("/products")
    List<ProductResponse> getProductsByCategory(
        @RequestParam("category") String category
    );
}
```

### 2.6 DTO 패턴 적용

**왜 DTO를 사용하는가?**
- Entity 직접 노출 방지 (JPA 지연 로딩 이슈 회피)
- API 응답 형식 제어
- 민감 정보 제외 가능

```java
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Category category;
    private String brand;
    private String imageUrl;

    // Entity → DTO 변환
    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .category(product.category())
            .brand(product.getBrand())
            .imageUrl(product.getImageUrl())
            .build();
    }
}
```

---

## 3. 재고 관리 리팩토링

### 3.1 문제 상황

```
Order Service: productName = "MacBook Pro"
Inventory Service: productName = "맥북프로"

재고 조회 시도:
SELECT * FROM inventory WHERE product_name = 'MacBook Pro'
→ 결과 없음! ❌

실제로는 재고가 있지만 문자열 불일치로 "재고 부족" 에러 발생
```

### 3.2 해결책: productId 기반 아키텍처

**Inventory Entity 리팩토링**

```java
// ❌ Before
@Entity
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;  // 문자열 기반

    private Integer quantity;
}

// ✅ After
@Entity
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long productId;  // Product Service의 ID와 1:1 매칭

    @Column(nullable = false)
    private Integer quantity;

    public Inventory(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // 재고 확보 (비즈니스 로직)
    public boolean reserve(Integer quantity) {
        if (this.quantity < quantity) {
            return false;  // 재고 부족
        }
        this.quantity -= quantity;
        return true;
    }

    // 재고 복구 (보상 트랜잭션)
    public void release(Integer quantity) {
        this.quantity += quantity;
    }
}
```

**Repository 변경**

```java
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    // ❌ Before
    // Optional<Inventory> findByProductName(String productName);

    // ✅ After
    Optional<Inventory> findByProductId(Long productId);
}
```

### 3.3 이벤트 클래스 업데이트

**모든 Kafka 이벤트에 productId 추가**

```java
// OrderCreatedEvent
@Getter
@Builder
public class OrderCreatedEvent implements Serializable {
    private Long orderId;
    private Long userId;
    private Long productId;      // ⭐ 추가
    private String productName;  // 스냅샷 (표시용)
    private Integer quantity;
    private Integer unitPrice;   // ⭐ 추가
    private Integer totalPrice;  // ⭐ 추가
    private LocalDateTime createdAt;
}

// InventoryReservedEvent
@Getter
@Builder
public class InventoryReservedEvent implements Serializable {
    private Long orderId;
    private Long productId;      // ⭐ 추가
    private String productName;  // 스냅샷
    private Integer quantity;
    private LocalDateTime reservedAt;
}

// PaymentFailedEvent
@Getter
@Builder
public class PaymentFailedEvent implements Serializable {
    private Long orderId;
    private Long productId;      // ⭐ 재고 복구용
    private Integer quantity;    // ⭐ 재고 복구용
    private String reason;
    private LocalDateTime failedAt;
}
```

---

## 4. Saga 보상 트랜잭션

### 4.1 Saga 패턴 플로우

```
정상 플로우:
┌──────────┐    ┌───────────┐    ┌──────────┐    ┌──────────┐
│  Order   │───>│ Inventory │───>│ Payment  │───>│ Delivery │
│ Created  │    │ Reserved  │    │ Approved │    │ Started  │
└──────────┘    └───────────┘    └──────────┘    └──────────┘
   PENDING         RESERVED       CONFIRMED         SHIPPED

보상 트랜잭션 플로우 (결제 실패):
┌──────────┐    ┌───────────┐    ┌──────────┐
│  Order   │───>│ Inventory │───>│ Payment  │
│ Created  │    │ Reserved  │    │  Failed  │
└──────────┘    └───────────┘    └─────┬────┘
   PENDING         RESERVED              │
      ↑                ↑                 │ PaymentFailedEvent
      │                │                 │ {productId: 1, quantity: 1}
      │                │                 ↓
      │            ┌───────────────────────┐
      │            │ Inventory Service가   │
      │            │ 재고 복구 (+1)        │
      │            └───────────────────────┘
      │                    │
      └────────────────────┘
         OrderCancelledEvent
```

### 4.2 보상 트랜잭션 구현

**InventoryEventConsumer.java**

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final InventoryService inventoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 주문 생성 이벤트 처리
    @KafkaListener(topics = "order-created", groupId = "inventory-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("📦 [Kafka Consumer] 주문 생성 이벤트 수신 - orderId: {}, productId: {}",
            event.getOrderId(), event.getProductId());

        // 재고 확보 시도
        boolean success = inventoryService.reserveInventory(
            event.getProductId(),
            event.getQuantity()
        );

        if (success) {
            // 재고 확보 성공 → Payment Service로 전달
            InventoryReservedEvent reservedEvent = InventoryReservedEvent.builder()
                .orderId(event.getOrderId())
                .productId(event.getProductId())
                .productName(event.getProductName())
                .quantity(event.getQuantity())
                .reservedAt(LocalDateTime.now())
                .build();

            kafkaTemplate.send("inventory-reserved", reservedEvent);
        } else {
            // 재고 부족 → Order Service로 실패 알림
            InventoryFailedEvent failedEvent = InventoryFailedEvent.builder()
                .orderId(event.getOrderId())
                .productId(event.getProductId())
                .reason("재고 부족")
                .failedAt(LocalDateTime.now())
                .build();

            kafkaTemplate.send("inventory-failed", failedEvent);
        }
    }

    // ⭐ 결제 실패 이벤트 처리 (보상 트랜잭션)
    @KafkaListener(topics = "payment-failed", groupId = "inventory-service-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("🔄 [Kafka Consumer] 결제 실패 이벤트 수신 - orderId: {}", event.getOrderId());
        log.info("재고 복구 시작 - productId: {}, quantity: {}",
            event.getProductId(), event.getQuantity());

        // 재고 복구 (보상 트랜잭션)
        inventoryService.releaseInventory(event.getProductId(), event.getQuantity());

        log.info("✅ 재고 복구 완료 - productId: {}", event.getProductId());
    }
}
```

### 4.3 보상 트랜잭션 테스트

**시나리오: 결제 실패 시 재고 자동 복구**

```bash
# 1. 초기 재고 확인
curl http://localhost:8084/inventory/1
# 응답: {"productId": 1, "quantity": 10}

# 2. 주문 생성
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "productId": 1, "quantity": 1}'

# 3. 로그 확인
docker-compose logs -f inventory-service
# 📦 주문 생성 이벤트 수신 - productId: 1
# ✅ 재고 확보 성공 - 남은 재고: 9
# 🔄 결제 실패 이벤트 수신 (시뮬레이션)
# ✅ 재고 복구 완료 - 현재 재고: 10

# 4. 재고 재확인
curl http://localhost:8084/inventory/1
# 응답: {"productId": 1, "quantity": 10}  ← 원상복구!
```

---

## 5. Redis 분산 락

### 5.1 동시성 문제 재현

**문제 상황 시뮬레이션**

```bash
# 재고 1개 상태에서 100명이 동시 주문
for i in {1..100}; do
  curl -X POST http://localhost:8082/orders \
    -H "Content-Type: application/json" \
    -d '{"userId":1,"productId":1,"quantity":1}' &
done

# ❌ 분산 락 없을 때:
# - 100개 주문 모두 성공
# - 재고: -99개 (음수!)

# ✅ 분산 락 적용 후:
# - 1개 주문만 성공
# - 99개 주문 실패 ("재고 부족")
# - 재고: 0개 (정상)
```

### 5.2 Redis 분산 락 아키텍처

```
Client 1                Client 2                Client 3
    │                       │                       │
    ├─ POST /orders ───────┼─ POST /orders ────────┼─ POST /orders
    ↓                       ↓                       ↓
┌───────────────────────────────────────────────────────────┐
│              Inventory Service (3 instances)              │
├───────────────────────────────────────────────────────────┤
│                                                           │
│  @DistributedLock(key = "inventory:lock:#productId")     │
│  public boolean reserveInventory(Long productId, ...)    │
│                                                           │
└────────────────────────┬──────────────────────────────────┘
                         │
                         ↓ tryLock()
           ┌─────────────────────────┐
           │     Redis Cluster       │
           ├─────────────────────────┤
           │ inventory:lock:1 = UUID │  ← 분산 락
           │ TTL: 3초                │
           └─────────────────────────┘

실행 순서:
1. Client 1 → Lock 획득 성공 ✅ → 재고 차감 진행
2. Client 2 → Lock 획득 대기 (최대 5초)
3. Client 3 → Lock 획득 대기 (최대 5초)
4. Client 1 → 작업 완료 → Lock 해제
5. Client 2 → Lock 획득 성공 ✅ → 재고 부족으로 실패
6. Client 3 → Lock 획득 성공 ✅ → 재고 부족으로 실패
```

### 5.3 커스텀 어노테이션 구현

**@DistributedLock.java**

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 락 키 (SpEL 지원)
     * 예: "inventory:lock:#productId"
     */
    String key();

    /**
     * 락 획득 대기 시간 (초)
     * 이 시간 동안 락 획득 시도
     */
    long waitTime() default 5L;

    /**
     * 락 점유 시간 (초)
     * 이 시간이 지나면 자동으로 락 해제
     */
    long leaseTime() default 3L;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
```

### 5.4 AOP 구현

**DistributedLockAop.java**

```java
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAop {

    private final RedissonClient redissonClient;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(com.example.inventory.annotation.DistributedLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        // SpEL 표현식으로 동적 락 키 생성
        String lockKey = generateKey(distributedLock.key(), method, joinPoint.getArgs());
        RLock lock = redissonClient.getLock(lockKey);

        log.debug("🔒 [Lock] 락 획득 시도: {}", lockKey);

        // 락 획득 시도
        boolean acquired = false;
        try {
            acquired = lock.tryLock(
                distributedLock.waitTime(),
                distributedLock.leaseTime(),
                distributedLock.timeUnit()
            );

            if (!acquired) {
                log.warn("⚠️ [Lock] 락 획득 실패 (타임아웃): {}", lockKey);
                throw new IllegalStateException(
                    "락 획득 실패: 다른 요청이 처리 중입니다. 잠시 후 다시 시도해주세요."
                );
            }

            log.debug("✅ [Lock] 락 획득 성공: {}", lockKey);

            // 실제 비즈니스 로직 실행
            return joinPoint.proceed();

        } finally {
            // 락 해제
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("🔓 [Lock] 락 해제: {}", lockKey);
            }
        }
    }

    /**
     * SpEL 표현식을 파싱하여 실제 락 키 생성
     *
     * 예: "inventory:lock:#productId" → "inventory:lock:1"
     */
    private String generateKey(String keyExpression, Method method, Object[] args) {
        if (!keyExpression.contains("#")) {
            return keyExpression;  // SpEL 없으면 그대로 반환
        }

        StandardEvaluationContext context = new StandardEvaluationContext();

        // 메서드 파라미터를 SpEL 변수로 등록
        String[] paramNames = new DefaultParameterNameDiscoverer()
            .getParameterNames(method);

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        // SpEL 표현식 평가
        Expression expression = parser.parseExpression(keyExpression);
        return expression.getValue(context, String.class);
    }
}
```

### 5.5 서비스 적용

**InventoryService.java**

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * 재고 확보 (차감)
     * - Redis 분산 락 적용으로 동시성 제어
     *
     * @param productId 상품 ID
     * @param quantity 차감할 수량
     * @return 성공 여부
     */
    @DistributedLock(
        key = "inventory:lock:#productId",  // 상품별로 다른 락
        waitTime = 5,   // 5초 동안 락 획득 시도
        leaseTime = 3   // 3초 후 자동 해제 (데드락 방지)
    )
    @Transactional
    public boolean reserveInventory(Long productId, Integer quantity) {
        log.info("[Inventory Service] 재고 확보 요청 - productId: {}, quantity: {}",
                productId, quantity);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "상품 재고를 찾을 수 없습니다: " + productId
                ));

        // 재고 확보 시도 (Entity의 비즈니스 로직)
        boolean success = inventory.reserve(quantity);

        if (success) {
            inventoryRepository.save(inventory);
            log.info("✅ 재고 확보 성공 - productId: {}, 남은 재고: {}",
                    productId, inventory.getQuantity());
        } else {
            log.warn("⚠️ 재고 부족 - productId: {}, 요청: {}, 현재: {}",
                    productId, quantity, inventory.getQuantity());
        }

        return success;
    }

    /**
     * 재고 복구 (보상 트랜잭션)
     */
    @Transactional
    public void releaseInventory(Long productId, Integer quantity) {
        log.info("🔄 재고 복구 (보상 트랜잭션) - productId: {}, quantity: {}",
                productId, quantity);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "상품 재고를 찾을 수 없습니다: " + productId
                ));

        inventory.release(quantity);
        inventoryRepository.save(inventory);

        log.info("✅ 재고 복구 완료 - productId: {}, 현재 재고: {}",
                productId, inventory.getQuantity());
    }
}
```

### 5.6 Redis 설정

**RedisConfig.java**

```java
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
            .setAddress("redis://" + redisHost + ":" + redisPort)
            .setConnectionPoolSize(50)
            .setConnectionMinimumIdleSize(10)
            .setConnectTimeout(3000)
            .setTimeout(3000);

        return Redisson.create(config);
    }
}
```

**application.yml**

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
```

**docker-compose.yml**

```yaml
redis:
  image: redis:7-alpine
  container_name: redis
  ports:
    - "6379:6379"
  networks:
    - msa-network
  command: redis-server --appendonly yes
  healthcheck:
    test: ["CMD", "redis-cli", "ping"]
    interval: 10s
    timeout: 5s
    retries: 5
```

---

## 6. 테스트 가이드

### 6.1 전체 시스템 시작

```bash
# 1. 프로젝트 빌드
./gradlew clean build -x test

# 2. Docker Compose로 전체 시스템 시작
docker-compose up -d --build

# 3. 서비스 상태 확인
docker-compose ps

# 4. 로그 확인
docker-compose logs -f order-service
docker-compose logs -f inventory-service
docker-compose logs -f product-service
```

### 6.2 Product Service 테스트

```bash
# 전체 상품 조회
curl http://localhost:8087/products | jq

# 특정 상품 조회
curl http://localhost:8087/products/1 | jq

# 응답 예시:
# {
#   "id": 1,
#   "name": "MacBook Pro 16",
#   "description": "Apple M3 Max 칩 탑재",
#   "price": 3500000,
#   "category": "ELECTRONICS",
#   "brand": "Apple",
#   "imageUrl": "https://..."
# }

# 카테고리별 조회
curl "http://localhost:8087/products?category=ELECTRONICS" | jq
```

### 6.3 실무 주문 플로우 테스트

```bash
# 주문 생성 (productId만 전달, 가격은 서버에서 계산!)
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "productId": 1,
    "quantity": 1
  }' | jq

# 응답 예시:
# {
#   "id": 1,
#   "userId": 1,
#   "productId": 1,
#   "productName": "MacBook Pro 16",  # 주문 시점 스냅샷
#   "quantity": 1,
#   "unitPrice": 3500000,             # 주문 시점 단가
#   "totalPrice": 3500000,            # 서버 계산 총액
#   "status": "PENDING"
# }

# 사용자별 주문 조회
curl "http://localhost:8082/orders?userId=1" | jq
```

### 6.4 Saga 플로우 확인

```bash
# 1. 주문 생성
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "productId": 2, "quantity": 1}'

# 2. 각 서비스 로그 확인 (별도 터미널에서)
docker-compose logs -f order-service       # 주문 생성
docker-compose logs -f inventory-service   # 재고 확보
docker-compose logs -f payment-service     # 결제 처리
docker-compose logs -f delivery-service    # 배송 준비
docker-compose logs -f notification-service # 알림 발송

# 3. Zipkin에서 분산 추적 확인
open http://localhost:9411
# → 서비스 간 호출 흐름 시각화
```

### 6.5 동시성 테스트

```bash
# 시나리오: 100명이 동시에 마지막 1개 재고 주문

# 1. 재고 확인
curl http://localhost:8084/inventory/1 | jq
# {"productId": 1, "quantity": 1}

# 2. 100개 동시 요청
for i in {1..100}; do
  curl -X POST http://localhost:8082/orders \
    -H "Content-Type: application/json" \
    -d '{"userId":1,"productId":1,"quantity":1}' &
done

# 3. 결과 확인
# - 1개 주문만 성공 ✅
# - 99개 주문 "재고 부족" 응답
# - 재고: 0개 (정상)

# 4. Redis 락 상태 확인
docker exec -it redis redis-cli
> KEYS inventory:lock:*
# 락이 정상적으로 해제되었는지 확인
```

### 6.6 보상 트랜잭션 테스트

```bash
# 시나리오: 재고 부족으로 주문 취소

# 1. 초기 재고 확인
curl http://localhost:8084/inventory/1 | jq
# {"productId": 1, "quantity": 5}

# 2. 재고보다 많은 수량 주문
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "productId": 1, "quantity": 999}' | jq

# 3. 로그 확인
docker-compose logs inventory-service | tail -20
# ⚠️ 재고 부족 - productId: 1, 요청: 999, 현재: 5
# 📤 Kafka 발행: inventory-failed

docker-compose logs order-service | tail -20
# 📥 Kafka 수신: inventory-failed
# ❌ 주문 취소 처리 - orderId: X

# 4. 재고 재확인 (변동 없어야 함)
curl http://localhost:8084/inventory/1 | jq
# {"productId": 1, "quantity": 5}  ← 원상태 유지
```

---

## 7. 아키텍처 비교

### 7.1 Before vs After

| 항목 | Before (학습용) | After (프로덕션) |
|------|----------------|-----------------|
| **보안** | 클라이언트가 가격 입력 ❌ | 서버 측 가격 검증 ✅ |
| **상품 관리** | 없음 | Product Service 신규 구축 ✅ |
| **재고 관리** | 문자열 기반 (productName) | ID 기반 (productId) ✅ |
| **동시성 제어** | 없음 (음수 재고 발생) | Redis 분산 락 ✅ |
| **Saga 보상** | 불완전 (재고 복구 안 됨) | 완전한 보상 트랜잭션 ✅ |
| **가격 변동** | 과거 주문에 영향 | 스냅샷으로 보호 ✅ |
| **이벤트 구조** | productName만 전달 | productId + 가격 정보 ✅ |
| **데이터 일관성** | 오타 발생 가능 | Product Service와 1:1 매칭 ✅ |

### 7.2 기술 스택 변화

**Before:**
```
- Spring Boot 3.1.5
- Spring Cloud Gateway
- Apache Kafka
- Resilience4j
- Micrometer + Zipkin
- H2 Database
- OpenFeign
```

**After (추가된 기술):**
```
- Redis 7-alpine      ← 분산 락
- Redisson 3.x        ← Redis 클라이언트
- Spring AOP          ← 횡단 관심사
- SpEL                ← 동적 락 키
- BigDecimal          ← 정확한 금액 계산
- DTO Pattern         ← Entity 노출 방지
```

### 7.3 서비스 아키텍처 변화

**Before (7개 서비스):**
```
User Service
Order Service
Inventory Service
Payment Service
Delivery Service
Notification Service
API Gateway
```

**After (8개 서비스):**
```
User Service
Order Service
Product Service        ← ⭐ 신규 추가
Inventory Service
Payment Service
Delivery Service
Notification Service
API Gateway
```

### 7.4 데이터베이스 스키마 변화

**Order 테이블**

```sql
-- Before
CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_name VARCHAR(255),    -- 문자열
    quantity INT NOT NULL,
    price INT NOT NULL,           -- 클라이언트 입력
    status VARCHAR(50)
);

-- After
CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,   -- Product Service 참조
    product_name VARCHAR(255),    -- 스냅샷
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2),     -- 스냅샷
    total_price DECIMAL(10,2),    -- 서버 계산
    status VARCHAR(50)
);
```

**Inventory 테이블**

```sql
-- Before
CREATE TABLE inventory (
    id BIGINT PRIMARY KEY,
    product_name VARCHAR(255),    -- 문자열
    quantity INT NOT NULL
);

-- After
CREATE TABLE inventory (
    id BIGINT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,  -- Product Service 참조
    quantity INT NOT NULL
);
```

**Product 테이블 (신규)**

```sql
CREATE TABLE products (
    id BIGINT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    brand VARCHAR(100),
    image_url VARCHAR(500),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

---

## 8. 참고 문서

- **[Circuit-Breaker-QNA.md](./Circuit-Breaker-QNA.md)** - Circuit Breaker 면접 대비 Q&A
- **[resilience4j-patterns.md](./resilience4j-patterns.md)** - Resilience4j 패턴 가이드
- **[Zipkin-Distributed-Tracing.md](./Zipkin-Distributed-Tracing.md)** - Zipkin 분산 추적 가이드
- [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign)
- [Redisson Documentation](https://github.com/redisson/redisson/wiki)
- [Apache Kafka](https://kafka.apache.org/documentation/)
