package com.nt.sms_module_worker.model.dto.config_conditions;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventItem {
    @JsonProperty("offer")
    private Offer offer;
}
