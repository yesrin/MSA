package com.example.inventory.service;

import com.example.inventory.entity.Inventory;
import com.example.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * 재고 확보 (차감)
     * @return 성공 여부
     */
    @Transactional
    public boolean reserveInventory(String productName, Integer quantity) {
        log.info("[Inventory Service] 재고 확보 요청 - product: {}, quantity: {}",
                productName, quantity);

        Inventory inventory = inventoryRepository.findByProductName(productName)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productName));

        boolean success = inventory.reserve(quantity);

        if (success) {
            inventoryRepository.save(inventory);
            log.info("✅ [Inventory Service] 재고 확보 성공 - product: {}, 남은 재고: {}",
                    productName, inventory.getQuantity());
        } else {
            log.warn("⚠️ [Inventory Service] 재고 부족 - product: {}, 요청: {}, 현재: {}",
                    productName, quantity, inventory.getQuantity());
        }

        return success;
    }

    /**
     * 재고 복구 (보상 트랜잭션)
     */
    @Transactional
    public void releaseInventory(String productName, Integer quantity) {
        log.info("🔄 [Inventory Service] 재고 복구 (보상 트랜잭션) - product: {}, quantity: {}",
                productName, quantity);

        Inventory inventory = inventoryRepository.findByProductName(productName)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productName));

        inventory.release(quantity);
        inventoryRepository.save(inventory);

        log.info("✅ [Inventory Service] 재고 복구 완료 - product: {}, 현재 재고: {}",
                productName, inventory.getQuantity());
    }

    /**
     * 재고 조회
     */
    @Transactional(readOnly = true)
    public Integer getAvailableQuantity(String productName) {
        return inventoryRepository.findByProductName(productName)
                .map(Inventory::getQuantity)
                .orElse(0);
    }
}
