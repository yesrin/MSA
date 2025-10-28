package com.example.product.repository;

import com.example.product.entity.Category;
import com.example.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 활성화된 상품 목록 조회
     */
    List<Product> findByActiveTrue();

    /**
     * 카테고리별 상품 조회
     */
    List<Product> findByCategoryAndActiveTrue(Category category);

    /**
     * 브랜드별 상품 조회
     */
    List<Product> findByBrandAndActiveTrue(String brand);

    /**
     * 상품명으로 검색 (LIKE)
     */
    List<Product> findByNameContainingAndActiveTrue(String name);

    /**
     * 활성화된 상품 조회 (ID)
     */
    Optional<Product> findByIdAndActiveTrue(Long id);
}
