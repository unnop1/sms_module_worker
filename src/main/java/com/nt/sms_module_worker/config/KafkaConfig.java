package com.nt.sms_module_worker.config;

import java.util.HashMap; 
import java.util.Map; 
import org.apache.kafka.clients.consumer.ConsumerConfig; 
import org.apache.kafka.common.serialization.StringDeserializer; 
import org.springframework.context.annotation.Bean; 
import org.springframework.context.annotation.Configuration; 
import org.springframework.kafka.annotation.EnableKafka; 
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory; 
import org.springframework.kafka.core.ConsumerFactory; 
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Bean
    public ConsumerFactory<String, String> consumerFactory() 
    { 
        Map<String, Object> properties = new HashMap<>();
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

