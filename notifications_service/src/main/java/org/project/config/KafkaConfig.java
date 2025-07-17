package org.project.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.project.dto.kafka.NotificationPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaConfig {

    @Autowired
    private Environment env;

    @Bean
    public ConsumerFactory<UUID, String> uuidConsumerFactory() {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                env.getProperty("spring.kafka.consumer.bootstrap-servers"));
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                UUIDDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class);
        cfg.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                StringDeserializer.class);
        cfg.put(JsonDeserializer.TRUSTED_PACKAGES,
                env.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"));
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG,
                env.getProperty("spring.kafka.consumer.group-id"));
        return new DefaultKafkaConsumerFactory<>(cfg);
    }

    @Bean
    public ConsumerFactory<Long, NotificationPayload> longConsumerFactory() {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                env.getProperty("spring.kafka.consumer.bootstrap-servers"));
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                LongDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class);
        cfg.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class);
        cfg.put(JsonDeserializer.TRUSTED_PACKAGES,
                env.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"));
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG,
                env.getProperty("spring.kafka.consumer.group-id"));
        return new DefaultKafkaConsumerFactory<>(cfg);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<UUID, String> uuidKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<UUID, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(uuidConsumerFactory());
        factory.setConcurrency(2);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, NotificationPayload> longKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<Long, NotificationPayload> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(longConsumerFactory());
        factory.setConcurrency(2);
        return factory;
    }
}
