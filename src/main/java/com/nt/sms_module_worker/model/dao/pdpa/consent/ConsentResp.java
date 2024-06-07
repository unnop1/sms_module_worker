package com.nt.sms_module_worker.model.dao.pdpa.consent;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsentResp {
    /*
    {
        "code": 200,
        "statusCode": 200,
        "haveConsent": true,
        "massage": "success"
    } 
    */
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("statusCode")
    private Integer statusCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("haveConsent")
    private Boolean haveConsent;

}
