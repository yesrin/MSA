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
        log.info("📦 초기 재고 데이터 생성 중...");

        inventoryRepository.save(new Inventory("Laptop", 100));
        inventoryRepository.save(new Inventory("Mouse", 500));
        inventoryRepository.save(new Inventory("Keyboard", 300));
        inventoryRepository.save(new Inventory("Monitor", 50));

        log.info("✅ 초기 재고 데이터 생성 완료");
    }
}
