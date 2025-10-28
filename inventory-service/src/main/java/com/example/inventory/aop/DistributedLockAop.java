package com.example.inventory.aop;

import com.example.inventory.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 분산 락 AOP
 * - @DistributedLock 어노테이션이 붙은 메서드에 Redis 분산 락 적용
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAop {

    private final RedissonClient redissonClient;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(com.example.inventory.annotation.DistributedLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        // SpEL 표현식으로 락 키 생성
        String lockKey = generateKey(distributedLock.key(), method, joinPoint.getArgs());
        RLock lock = redissonClient.getLock(lockKey);

        log.info("🔒 [Distributed Lock] 락 획득 시도 - key: {}", lockKey);

        boolean acquired = false;
        try {
            // 락 획득 시도 (waitTime, leaseTime, timeUnit)
            acquired = lock.tryLock(
                    distributedLock.waitTime(),
                    distributedLock.leaseTime(),
                    distributedLock.timeUnit()
            );

            if (!acquired) {
                log.warn("⏰ [Distributed Lock] 락 획득 실패 (타임아웃) - key: {}", lockKey);
                throw new IllegalStateException("락 획득 실패: 다른 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.");
            }

            log.info("✅ [Distributed Lock] 락 획득 성공 - key: {}", lockKey);
            return joinPoint.proceed();

        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("🔓 [Distributed Lock] 락 해제 완료 - key: {}", lockKey);
            }
        }
    }

    /**
     * SpEL 표현식 파싱하여 락 키 생성
     * 예: "inventory:lock:#productId" → "inventory:lock:1"
     */
    private String generateKey(String keyExpression, Method method, Object[] args) {
        String[] parameterNames = nameDiscoverer.getParameterNames(method);
        if (parameterNames == null) {
            return keyExpression;
        }

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        Expression expression = parser.parseExpression(keyExpression);
        return expression.getValue(context, String.class);
    }
}
