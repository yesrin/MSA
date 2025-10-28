package com.example.inventory.config;

import com.example.inventory.entity.Inventory;
import com.example.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 초기 재고 데이터 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final InventoryRepository inventoryRepository;

    @Override
    public void run(String... args) {
        if (inventoryRepository.count() > 0) {
            log.info("재고 데이터가 이미 존재합니다. 초기화 스킵");
            return;
        }

        log.info("📦 초기 재고 데이터 생성 중...");

        // Product Service와 매칭되는 productId 사용
        inventoryRepository.save(new Inventory(1L, 10));  // MacBook Pro 16
        inventoryRepository.save(new Inventory(2L, 50));  // iPhone 15 Pro
        inventoryRepository.save(new Inventory(3L, 30));  // Galaxy S24 Ultra
        inventoryRepository.save(new Inventory(4L, 100)); // 나이키 에어맥스
        inventoryRepository.save(new Inventory(5L, 80));  // 리바이스 501 진
        inventoryRepository.save(new Inventory(6L, 40));  // 설화수 자음생 에센스
        inventoryRepository.save(new Inventory(7L, 150)); // 클린 코드
        inventoryRepository.save(new Inventory(8L, 120)); // 이펙티브 자바
        inventoryRepository.save(new Inventory(9L, 25));  // 윌슨 테니스 라켓
        inventoryRepository.save(new Inventory(10L, 15)); // 다이슨 무선 청소기

        log.info("✅ 초기 재고 데이터 생성 완료 (Product Service와 매칭)");
    }
}
