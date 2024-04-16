package com.nt.sms_module_worker.config;

import java.util.HashMap; 
import java.util.Map; 
import org.apache.kafka.clients.consumer.ConsumerConfig; 
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SecurityConfig;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.security.auth.SecurityProviderCreator;
import org.apache.kafka.common.security.scram.*;
import org.apache.kafka.common.serialization.StringDeserializer; 
import org.springframework.context.annotation.Bean; 
import org.springframework.context.annotation.Configuration; 
import org.springframework.kafka.annotation.EnableKafka; 
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory; 
import org.springframework.kafka.core.ConsumerFactory; 
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableKafka
public class KafkaConfig {
    
   
    // @Value("${spring.kafka.bootstrap-servers}")
    // private String bootstrapServers;

    // @Value("${spring.kafka.enable.auto-commit}")
    // private String enableAutoCommitConfig;

    // @Value("${spring.kafka.consumer.auto-offset-reset}")
    // private String autoOffsetResetConfig;
    @Bean
    public ConsumerFactory<String, String> consumerFactory() 
    { 
        Map<String, Object> properties = new HashMap<>();
        // properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        
        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    concurrentKafkaListenerContainerFactory() 
    { 
        ConcurrentKafkaListenerContainerFactory< 
            String, String> factory 
            = new ConcurrentKafkaListenerContainerFactory<>(); 
        factory.setConsumerFactory(consumerFactory()); 
        return factory; 
    }
}

