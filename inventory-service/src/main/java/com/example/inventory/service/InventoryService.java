package com.example.inventory.service;

import com.example.inventory.annotation.DistributedLock;
import com.example.inventory.exception.InventoryNotFoundException;
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
     * - Redis 분산 락 적용으로 동시성 제어
     * @return 성공 여부
     */
    @DistributedLock(key = "inventory:lock:#productId", waitTime = 5, leaseTime = 3)
    @Transactional
    public boolean reserveInventory(Long productId, Integer quantity) {
        log.info("[Inventory Service] 재고 확보 요청 - productId: {}, quantity: {}",
                productId, quantity);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        boolean success = inventory.reserve(quantity);

        if (success) {
            inventoryRepository.save(inventory);
            log.info("✅ [Inventory Service] 재고 확보 성공 - productId: {}, 남은 재고: {}",
                    productId, inventory.getQuantity());
        } else {
            log.warn("⚠️ [Inventory Service] 재고 부족 - productId: {}, 요청: {}, 현재: {}",
                    productId, quantity, inventory.getQuantity());
        }

        return success;
    }

    /**
     * 재고 복구 (보상 트랜잭션)
     */
    @Transactional
    public void releaseInventory(Long productId, Integer quantity) {
        log.info("🔄 [Inventory Service] 재고 복구 (보상 트랜잭션) - productId: {}, quantity: {}",
                productId, quantity);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        inventory.release(quantity);
        inventoryRepository.save(inventory);

        log.info("✅ [Inventory Service] 재고 복구 완료 - productId: {}, 현재 재고: {}",
                productId, inventory.getQuantity());
    }

    /**
     * 재고 조회
     */
    @Transactional(readOnly = true)
    public Integer getAvailableQuantity(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .map(Inventory::getQuantity)
                .orElse(0);
    }
}
