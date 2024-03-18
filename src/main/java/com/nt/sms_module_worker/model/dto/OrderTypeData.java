package com.nt.sms_module_worker.model.dto;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderTypeData {
    private Long TYPEID = null;
    private Long MainID = null;
    private String OrderTypeName = null;
    private Boolean IsEnable = false;
    private Boolean IsDelete = false;
    private Timestamp CreatedDate = null;
    private Long CreatedBy_UserID = null;
    private Timestamp UpdatedDate = null ;
    private Long UpdatedBy_UserID = null;
}
