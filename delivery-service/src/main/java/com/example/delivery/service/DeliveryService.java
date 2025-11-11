package com.example.delivery.service;

import com.example.delivery.entity.Delivery;
import com.example.delivery.exception.DeliveryNotFoundException;
import com.example.delivery.kafka.DeliveryEventProducer;
import com.example.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryEventProducer deliveryEventProducer;

    /**
     * 배송 준비 (결제 완료 시 호출)
     * 동기 처리 - DB 저장 완료 후 리턴
     */
    @Transactional
    public Delivery prepareDelivery(Long orderId) {
        log.info("[Delivery Service] 배송 준비 - orderId: {}", orderId);

        String deliveryId = "DEL-" + UUID.randomUUID().toString().substring(0, 8);
        String carrier = selectCarrier(); // 택배사 선택 (CJ대한통운, 한진택배 등)
        String address = "서울시 강남구 테헤란로 123"; // 실제로는 Order/User Service에서 조회

        Delivery delivery = new Delivery(orderId, deliveryId, address, carrier);
        deliveryRepository.save(delivery);

        log.info("✅ [Delivery Service] 배송 준비 완료 - deliveryId: {}, carrier: {}",
                deliveryId, carrier);

        // 배송 시작 스케줄링 (비동기 - 물류센터 출고 시뮬레이션)
        startDeliveryAsync(delivery.getId());

        return delivery;
    }

    /**
     * 배송 시작 (비동기 - 3초 후 자동 시작)
     * @Async를 사용하여 별도 스레드에서 실행
     */
    @Async
    @Transactional
    public void startDeliveryAsync(Long deliveryId) {
        try {
            Thread.sleep(3000); // 3초 대기 (물류센터 출고 시뮬레이션)

            Delivery delivery = deliveryRepository.findById(deliveryId)
                    .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));

            delivery.start();
            deliveryRepository.save(delivery);

            log.info("🚚 [Delivery Service] 배송 시작 (물류센터 출고) - deliveryId: {}", delivery.getDeliveryId());

            // 트랜잭션 커밋 후 이벤트 발행
            deliveryEventProducer.publishDeliveryStarted(delivery);

            // 배송 완료 (비동기 - 5초 후 자동 완료)
            completeDeliveryAsync(deliveryId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ [Delivery Service] 배송 시작 중단됨", e);
            throw new RuntimeException("배송 시작 실패", e);
        } catch (Exception e) {
            log.error("❌ [Delivery Service] 배송 시작 실패", e);
            throw e; // 예외를 다시 던져서 트랜잭션 롤백
        }
    }

    /**
     * 배송 완료 (비동기 - 5초 후 자동 완료)
     */
    @Async
    @Transactional
    public void completeDeliveryAsync(Long deliveryId) {
        try {
            Thread.sleep(5000); // 5초 대기 (배송 중 시뮬레이션)

            Delivery delivery = deliveryRepository.findById(deliveryId)
                    .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));

            // 5% 확률로 배송 실패 (수령 거부, 주소 오류 등)
            if (Math.random() < 0.05) {
                delivery.fail("수령 거부");
                deliveryRepository.save(delivery);
                log.warn("❌ [Delivery Service] 배송 실패 - deliveryId: {}, reason: 수령 거부",
                        delivery.getDeliveryId());

                // 트랜잭션 커밋 후 실패 이벤트 발행
                deliveryEventProducer.publishDeliveryFailed(delivery);
            } else {
                delivery.complete();
                deliveryRepository.save(delivery);
                log.info("✅ [Delivery Service] 배송 완료 - deliveryId: {}",
                        delivery.getDeliveryId());

                // 트랜잭션 커밋 후 완료 이벤트 발행
                deliveryEventProducer.publishDeliveryCompleted(delivery);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ [Delivery Service] 배송 완료 처리 중단됨", e);
            throw new RuntimeException("배송 완료 실패", e);
        } catch (Exception e) {
            log.error("❌ [Delivery Service] 배송 완료 처리 실패", e);
            throw e; // 예외를 다시 던져서 트랜잭션 롤백
        }
    }

    /**
     * 택배사 선택 (랜덤)
     */
    private String selectCarrier() {
        String[] carriers = {"CJ대한통운", "한진택배", "로젠택배", "우체국택배"};
        return carriers[(int) (Math.random() * carriers.length)];
    }
}
