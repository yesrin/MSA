package com.example.inventory.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 분산 락 어노테이션
 * - Redis 기반 분산 락을 AOP로 적용
 *
 * 사용 예시:
 * @DistributedLock(key = "inventory:lock:#productId")
 * public boolean reserveInventory(Long productId, Integer quantity) { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 락 키 (SpEL 지원)
     * 예: "inventory:lock:#productId"
     */
    String key();

    /**
     * 락 대기 시간 (기본: 5초)
     */
    long waitTime() default 5L;

    /**
     * 락 점유 시간 (기본: 3초)
     */
    long leaseTime() default 3L;

    /**
     * 시간 단위 (기본: 초)
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
