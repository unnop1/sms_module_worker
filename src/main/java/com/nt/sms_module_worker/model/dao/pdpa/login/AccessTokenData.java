package com.nt.sms_module_worker.model.dao.pdpa.login;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AccessTokenData {
    @JsonProperty("AccessToken")
    private String AccessToken;

    @JsonProperty("RefreshToken")
    private String RefreshToken;

    @JsonProperty("exp_in")
    private Integer expIn;
}
