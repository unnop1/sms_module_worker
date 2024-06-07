package com.nt.sms_module_worker.model.dao.pdpa.consent;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConsentResp {
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private List<ConsentSubjectData> data;
}
