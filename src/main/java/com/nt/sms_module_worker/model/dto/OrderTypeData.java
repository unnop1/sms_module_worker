package com.nt.sms_module_worker.model.dto;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderTypeData {
    private Long TYPEID = null;
    private Long MainID = null;
    private String OrderType_Name = null;
    private Boolean Is_Enable = false;
    private Boolean Is_Delete = false;
    private Timestamp Created_Date = null;
    private Long Created_By = null;
    private Timestamp Updated_Date = null ;
    private Long Updated_By = null;
}
