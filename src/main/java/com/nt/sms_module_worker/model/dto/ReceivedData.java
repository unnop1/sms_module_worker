package com.nt.sms_module_worker.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceivedData {
    @JsonProperty("orderType")
    private String Order_type;
    @JsonProperty("channel")
    private String channel;
    @JsonProperty("frequency")
    private String frequency;
    @JsonProperty("phoneNumber")
    private String phoneNumber;
    @JsonProperty("serviceType")
    private Integer serviceType;
    @JsonProperty("offeringId")
    private String offeringId;
}