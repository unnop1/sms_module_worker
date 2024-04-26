package com.nt.sms_module_worker.model.dto.distribute;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceivedData {
    @JsonProperty("triggerDate")
    private String triggerDate;
    @JsonProperty("orderType")
    private String orderType;
    @JsonProperty("msisdn")
    private String msisdn;
    @JsonProperty("publishChannel")
    private String publishChannel;
    @JsonProperty("eventData")
    private Object eventData;
}