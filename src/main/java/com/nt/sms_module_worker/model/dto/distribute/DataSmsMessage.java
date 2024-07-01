package com.nt.sms_module_worker.model.dto.distribute;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("requestDate")
    private LocalDateTime requestDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("deliveryDateTime")
    private LocalDateTime deliveryDateTime;
    
    // message text
    @JsonProperty("message")
    private String message;
}
