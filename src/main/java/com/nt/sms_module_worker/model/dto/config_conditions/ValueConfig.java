package com.nt.sms_module_worker.model.dto.config_conditions;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValueConfig {

    @JsonProperty("value")
    private String value;
    
    @JsonProperty("operation_type")
    private String operation_type;
}
