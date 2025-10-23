package com.example.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 재고 엔티티
 */
@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 상품명 (unique)
     */
    @Column(nullable = false, unique = true)
    private String productName;

    /**
     * 재고 수량
     */
    @Column(nullable = false)
    private Integer quantity;

    public Inventory(String productName, Integer quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }

    /**
     * 재고 차감
     * @param amount 차감할 수량
     * @return 성공 여부
     */
    public boolean reserve(Integer amount) {
        if (this.quantity >= amount) {
            this.quantity -= amount;
            return true;
        }
        return false;
    }

    /**
     * 재고 복구 (보상 트랜잭션)
     * @param amount 복구할 수량
     */
    public void release(Integer amount) {
        this.quantity += amount;
    }
}
