package com.example.common.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * 공통 Kafka Producer 설정
 * 모든 마이크로서비스에서 재사용 가능
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Producer 성능 및 신뢰성 설정
        config.put(ProducerConfig.ACKS_CONFIG, "1");  // 리더 파티션 ACK (성능과 신뢰성 균형)
        config.put(ProducerConfig.RETRIES_CONFIG, 3);  // 전송 실패 시 재시도
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");  // 압축
        config.put(ProducerConfig.LINGER_MS_CONFIG, 10);  // 배치 대기 시간
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);  // 배치 크기

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
