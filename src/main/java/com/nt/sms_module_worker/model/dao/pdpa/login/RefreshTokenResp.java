package com.nt.sms_module_worker.model.dao.pdpa.login;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenResp {
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("statusCode")
    private Integer statusCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private AccessTokenData data;

}
