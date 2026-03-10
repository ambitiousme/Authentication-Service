package com.auth.Auth.Service.Kafka;

import com.auth.Auth.Service.Kafka.Events.PasswordResetEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, PasswordResetEvent> kafkaTemplate;

    public void publishPasswordResetEvent(PasswordResetEvent event) {

        kafkaTemplate.send("password-reset-topic", event);
    }
}
