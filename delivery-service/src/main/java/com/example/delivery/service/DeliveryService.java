package com.example.delivery.service;

import com.example.delivery.entity.Delivery;
import com.example.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    /**
     * 배송 준비 (결제 완료 시 호출)
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

        // 배송 시작 (비동기 - 실제로는 물류센터 출고 후)
        CompletableFuture.runAsync(() -> startDeliveryAsync(delivery.getId()));

        return delivery;
    }

    /**
     * 배송 시작 (비동기 - 3초 후 자동 시작)
     */
    private void startDeliveryAsync(Long deliveryId) {
        try {
            Thread.sleep(3000); // 3초 대기 (물류센터 출고 시뮬레이션)

            Delivery delivery = deliveryRepository.findById(deliveryId)
                    .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다"));

            delivery.start();
            deliveryRepository.save(delivery);

            log.info("🚚 [Delivery Service] 배송 시작 - deliveryId: {}", delivery.getDeliveryId());

            // 배송 완료 (비동기 - 5초 후 자동 완료)
            completeDeliveryAsync(deliveryId);

        } catch (Exception e) {
            log.error("❌ [Delivery Service] 배송 시작 실패", e);
        }
    }

    /**
     * 배송 완료 (비동기 - 5초 후 자동 완료)
     */
    private void completeDeliveryAsync(Long deliveryId) {
        try {
            Thread.sleep(5000); // 5초 대기 (배송 중 시뮬레이션)

            Delivery delivery = deliveryRepository.findById(deliveryId)
                    .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다"));

            // 5% 확률로 배송 실패 (수령 거부, 주소 오류 등)
            if (Math.random() < 0.05) {
                delivery.fail("수령 거부");
                deliveryRepository.save(delivery);
                log.warn("❌ [Delivery Service] 배송 실패 - deliveryId: {}, reason: 수령 거부",
                        delivery.getDeliveryId());
            } else {
                delivery.complete();
                deliveryRepository.save(delivery);
                log.info("✅ [Delivery Service] 배송 완료 - deliveryId: {}",
                        delivery.getDeliveryId());
            }

        } catch (Exception e) {
            log.error("❌ [Delivery Service] 배송 완료 처리 실패", e);
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
