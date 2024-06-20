package com.nt.sms_module_worker.model.dto.distribute;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataSmsMessage {

    // Client's system name 
    @JsonProperty("systemCode")
    private String systemCode="REDS";

    // mobile number destination 11 Digits start with 66
    @JsonProperty("target")
    private String target;

    // SMS sender name
    @JsonProperty("source")
    private String source="my";

    // message reference transaction id (for transaction tracking)
    @JsonProperty("systemTransRef")
    private String systemTransRef;

    // Client's request date time (YYYY-MM-DD HH24:MI:SS)
    @JsonProperty("requestDate")
    private LocalDateTime requestDate;
    
    // message text
    @JsonProperty("message")
    private String message;
}
