package com.example.delivery.repository;

import com.example.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    /**
     * 주문 ID로 배송 조회
     */
    Optional<Delivery> findByOrderId(Long orderId);
}
