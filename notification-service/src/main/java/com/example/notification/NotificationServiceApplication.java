package com.example.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Notification Service
 * - Kafka에서 주문 이벤트를 구독하여 알림 발송
 * - DB 없음 (stateless service)
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableKafka
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

}
