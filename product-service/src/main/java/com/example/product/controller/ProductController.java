package com.example.product.controller;

import com.example.product.dto.ProductResponse;
import com.example.product.entity.Category;
import com.example.product.entity.Product;
import com.example.product.service.ProductService;
import com.example.product.service.ProductService.CreateProductRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 생성
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody CreateProductRequest request) {
        log.info("[Product Controller] 상품 생성 API 호출");
        Product product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(product));
    }

    /**
     * 전체 상품 조회
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String search) {

        List<Product> products;

        if (category != null) {
            log.info("[Product Controller] 카테고리별 상품 조회 - category: {}", category);
            products = productService.getProductsByCategory(category);
        } else if (brand != null) {
            log.info("[Product Controller] 브랜드별 상품 조회 - brand: {}", brand);
            products = productService.getProductsByBrand(brand);
        } else if (search != null) {
            log.info("[Product Controller] 상품명 검색 - search: {}", search);
            products = productService.searchProductsByName(search);
        } else {
            log.info("[Product Controller] 전체 상품 조회");
            products = productService.getAllActiveProducts();
        }

        List<ProductResponse> response = products.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 상품 ID로 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        log.info("[Product Controller] 상품 조회 - productId: {}", id);
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    /**
     * 상품 비활성화 (판매 중단)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateProduct(@PathVariable Long id) {
        log.info("[Product Controller] 상품 비활성화 - productId: {}", id);
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }
}
