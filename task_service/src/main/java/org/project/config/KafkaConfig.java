package org.project.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.project.dto.kafka.NotificationPayload;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaConfig {

    @Value("${kafka.partitions}")
    private int partitions;

    private NewTopic createTopic(String name){
        return TopicBuilder.name(name).partitions(partitions).build();
    }

    @Bean
    public NewTopic messageUserTopic(){
        return createTopic("notifications-user-task");
    }

    @Bean
    public NewTopic messageUserFroGroup(){
        return createTopic("notifications-user-group-task");
    }

    @Bean
    public ProducerFactory<Long, NotificationPayload> groupProducerFactory(
            @Value("${spring.kafka.producer.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ProducerFactory<UUID, String> userProducerFactory(
            @Value("${spring.kafka.producer.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    @Qualifier("messageToAllUserInGroup")
    public KafkaTemplate<Long, NotificationPayload> messageToAllUserInGroup(
            ProducerFactory<Long, NotificationPayload> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @Qualifier("messageToUser")
    public KafkaTemplate<UUID, String> messageToUser(
            ProducerFactory<UUID, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
