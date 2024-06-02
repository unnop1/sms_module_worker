package com.nt.sms_module_worker;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
public class SmsModuleWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmsModuleWorkerApplication.class, args);
    }

}
