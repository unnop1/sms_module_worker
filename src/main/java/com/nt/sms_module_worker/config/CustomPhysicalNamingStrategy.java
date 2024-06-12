package com.nt.sms_module_worker.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomPhysicalNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {
    
    @Value("${spring.jpa.schema}")
    private String schemaName;
    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        if (identifier != null) {
            String name = identifier.getText();
            if (name.toLowerCase().equals("${replace_schema}")) {
                String schema = schemaName.toUpperCase();
                return super.toPhysicalSchemaName(new Identifier(schema, identifier.isQuoted()), jdbcEnvironment);
            }
        }
        return super.toPhysicalSchemaName(identifier, jdbcEnvironment);
    }
}