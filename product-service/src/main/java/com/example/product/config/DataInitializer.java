package com.example.product.config;

import com.example.product.entity.Category;
import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 초기 데이터 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            log.info("상품 데이터가 이미 존재합니다. 초기화 스킵");
            return;
        }

        log.info("===== 테스트 상품 데이터 생성 시작 =====");

        // 전자제품
        productRepository.save(new Product(
                "MacBook Pro 16",
                "M3 Max 칩 탑재, 16인치 Liquid Retina XDR 디스플레이",
                new BigDecimal("3500000"),
                Category.ELECTRONICS,
                "Apple",
                "https://example.com/macbook-pro.jpg"
        ));

        productRepository.save(new Product(
                "iPhone 15 Pro",
                "A17 Pro 칩, 티타늄 디자인, 48MP 카메라",
                new BigDecimal("1550000"),
                Category.ELECTRONICS,
                "Apple",
                "https://example.com/iphone-15-pro.jpg"
        ));

        productRepository.save(new Product(
                "Galaxy S24 Ultra",
                "AI 기능 탑재, 200MP 카메라, S펜 지원",
                new BigDecimal("1690000"),
                Category.ELECTRONICS,
                "Samsung",
                "https://example.com/galaxy-s24.jpg"
        ));

        // 패션
        productRepository.save(new Product(
                "나이키 에어맥스",
                "최고의 쿠셔닝과 편안함을 제공하는 러닝화",
                new BigDecimal("159000"),
                Category.FASHION,
                "Nike",
                "https://example.com/airmax.jpg"
        ));

        productRepository.save(new Product(
                "리바이스 501 진",
                "클래식 스트레이트 핏 데님",
                new BigDecimal("129000"),
                Category.FASHION,
                "Levi's",
                "https://example.com/levis-501.jpg"
        ));

        // 뷰티
        productRepository.save(new Product(
                "설화수 자음생 에센스",
                "한방 발효 에센스, 피부 탄력 개선",
                new BigDecimal("230000"),
                Category.BEAUTY,
                "설화수",
                "https://example.com/sulwhasoo.jpg"
        ));

        // 도서
        productRepository.save(new Product(
                "클린 코드",
                "애자일 소프트웨어 장인 정신 - 로버트 C. 마틴",
                new BigDecimal("33000"),
                Category.BOOK,
                "인사이트",
                "https://example.com/clean-code.jpg"
        ));

        productRepository.save(new Product(
                "이펙티브 자바",
                "자바 프로그래밍의 바이블 - 조슈아 블로크",
                new BigDecimal("36000"),
                Category.BOOK,
                "인사이트",
                "https://example.com/effective-java.jpg"
        ));

        // 스포츠
        productRepository.save(new Product(
                "윌슨 테니스 라켓",
                "프로 스태프 시리즈, 카본 프레임",
                new BigDecimal("250000"),
                Category.SPORTS,
                "Wilson",
                "https://example.com/tennis-racket.jpg"
        ));

        // 홈/리빙
        productRepository.save(new Product(
                "다이슨 무선 청소기",
                "V15 Detect, 레이저 먼지 감지",
                new BigDecimal("899000"),
                Category.HOME,
                "Dyson",
                "https://example.com/dyson-v15.jpg"
        ));

        log.info("===== 테스트 상품 데이터 생성 완료 (총 10개) =====");
    }
}
