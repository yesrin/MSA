package com.example.product.entity;

import com.example.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 상품 엔티티
 */
@Entity
@Table(name = "products",
        indexes = {
            @Index(name = "idx_category", columnList = "category"),
            @Index(name = "idx_brand", columnList = "brand")
        })
@Getter
@Setter
@NoArgsConstructor
public class Product extends BaseEntity {

    /**
     * 상품명
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * 상품 설명
     */
    @Column(length = 2000)
    private String description;

    /**
     * 가격
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * 카테고리
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Category category;

    /**
     * 브랜드
     */
    @Column(length = 100)
    private String brand;

    /**
     * 상품 이미지 URL (대표 이미지)
     */
    @Column(length = 500)
    private String imageUrl;

    /**
     * 상품 활성화 여부 (판매 중단 시 false)
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * 생성자
     */
    public Product(String name, String description, BigDecimal price,
                   Category category, String brand, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.brand = brand;
        this.imageUrl = imageUrl;
        this.active = true;
    }

    /**
     * 상품 비활성화 (판매 중단)
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * 상품 활성화 (판매 재개)
     */
    public void activate() {
        this.active = true;
    }

    /**
     * 가격 변경
     */
    public void updatePrice(BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다");
        }
        this.price = newPrice;
    }
}
