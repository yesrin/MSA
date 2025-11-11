package com.example.order.client;

import com.example.order.dto.ProductResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Product Service Feign Client
 */
@FeignClient(name = "product-service", url = "${product.service.url:http://localhost:8087}")
public interface ProductClient {

    /**
     * 상품 ID로 조회
     */
    @GetMapping("/products/{id}")
    ProductResponse getProductById(@PathVariable Long id);
}
