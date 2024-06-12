package com.nt.sms_module_worker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SchemaConfiguration {
    @Value("${app.schema}")
    private String schema;

    public String getSchema() {
        return schema;
    }
}
