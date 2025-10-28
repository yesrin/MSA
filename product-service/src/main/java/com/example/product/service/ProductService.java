package com.example.product.service;

import com.example.product.entity.Category;
import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 상품 생성
     */
    @Transactional
    public Product createProduct(CreateProductRequest request) {
        log.info("[Product Service] 상품 생성 - name: {}, price: {}",
                request.getName(), request.getPrice());

        Product product = new Product(
            request.getName(),
            request.getDescription(),
            request.getPrice(),
            request.getCategory(),
            request.getBrand(),
            request.getImageUrl()
        );

        Product savedProduct = productRepository.save(product);
        log.info("[Product Service] 상품 생성 완료 - productId: {}", savedProduct.getId());

        return savedProduct;
    }

    /**
     * 전체 상품 조회 (활성화된 것만)
     */
    @Transactional(readOnly = true)
    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    /**
     * 상품 ID로 조회
     */
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));
    }

    /**
     * 카테고리별 상품 조회
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(Category category) {
        return productRepository.findByCategoryAndActiveTrue(category);
    }

    /**
     * 브랜드별 상품 조회
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrandAndActiveTrue(brand);
    }

    /**
     * 상품명 검색
     */
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingAndActiveTrue(name);
    }

    /**
     * 상품 가격 변경
     */
    @Transactional
    public Product updatePrice(Long id, BigDecimal newPrice) {
        log.info("[Product Service] 가격 변경 - productId: {}, newPrice: {}", id, newPrice);

        Product product = getProductById(id);
        product.updatePrice(newPrice);

        return productRepository.save(product);
    }

    /**
     * 상품 비활성화 (판매 중단)
     */
    @Transactional
    public void deactivateProduct(Long id) {
        log.info("[Product Service] 상품 비활성화 - productId: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));

        product.deactivate();
        productRepository.save(product);
    }

    /**
     * 상품 생성 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateProductRequest {
        private String name;
        private String description;
        private BigDecimal price;
        private Category category;
        private String brand;
        private String imageUrl;

        public CreateProductRequest(String name, String description, BigDecimal price,
                                    Category category, String brand, String imageUrl) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.category = category;
            this.brand = brand;
            this.imageUrl = imageUrl;
        }
    }
}
