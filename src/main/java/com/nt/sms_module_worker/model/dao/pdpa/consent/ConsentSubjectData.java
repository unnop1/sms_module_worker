package com.nt.sms_module_worker.model.dao.pdpa.consent;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConsentSubjectData {
    @JsonProperty("identify")
    private String identify;

    @JsonProperty("purposeID")
    private String purposeID;

    @JsonProperty("status")
    private String status;

    @JsonProperty("statusboolean")
    private Boolean statusboolean;

    @JsonProperty("publicDt")
    private String publicDt;
}
