package com.sofka.tagoKoder.backend.audit.infra.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers}") 
  private String bootstrap;

  @Value("${kafka.topics.dlt:audit.dlt}")     
  private String dltTopic;

  // Producer (solo si este micro necesita publicar al DLT u otros tópicos)
  @Bean
  public ProducerFactory<String, String> producerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    return new DefaultKafkaProducerFactory<>(props);
  }

  @Bean
  public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> pf) {
    return new KafkaTemplate<>(pf);
  }

  // Usa el ConsumerFactory auto-configurado por Spring Boot (a partir de spring.kafka.*)
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
      ConsumerFactory<String, String> consumerFactory,
      KafkaTemplate<String, String> template) {

    var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
    factory.setConsumerFactory(consumerFactory);

    var recoverer = new DeadLetterPublishingRecoverer(
        template,
        (rec, ex) -> new org.apache.kafka.common.TopicPartition(dltTopic, rec.partition())
    );

    var errorHandler = new SeekToCurrentErrorHandler(
        recoverer,
        new FixedBackOff(1000L, 3) // 3 reintentos, 1s entre intentos
    );

    errorHandler.addNotRetryableExceptions(
        org.apache.kafka.common.errors.SerializationException.class,
        org.springframework.kafka.support.converter.ConversionException.class,
        org.springframework.kafka.KafkaException.class,
        IllegalArgumentException.class
    );

    factory.setErrorHandler(errorHandler);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    factory.setRecordInterceptor(r -> r);
    return factory;
  }

  public static Map<String, Object> toHeaderMap(ConsumerRecord<?, ?> rec) {
    Map<String, Object> map = new HashMap<>();
    for (Header h : rec.headers()) {
      map.put(h.key(), h.value() == null ? null : new String(h.value(), StandardCharsets.UTF_8));
    }
    return map;
  }
}
