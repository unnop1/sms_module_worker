package com.nt.sms_module_worker.model.dto.distribute;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendSmsGatewayData {
    @JsonProperty("bulkRef")
    private String bulkRef;

    @JsonProperty("messages")
    private List<DataSmsMessage> messages;
}