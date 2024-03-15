package com.nt.sms_module_worker.model.dto.config_conditions;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Offer {
    @JsonProperty("offeringType")
    private String offeringType;
    
    @JsonProperty("ServiceType")
    private Integer ServiceType;

    @JsonProperty("rcAmount")
    private Integer rcAmount;
}
