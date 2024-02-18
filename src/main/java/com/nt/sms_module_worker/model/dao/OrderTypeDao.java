package com.nt.sms_module_worker.model.dao;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderTypeDao {
    private Long TYPEID = null;
    private Long MainID = null;
    private String OrderTypeName = null;
    private Integer IsEnable = 1;
    private Integer IsDelete = 0;
    private Integer TotleMsg = 0;
    private Integer TotleSend = 0;
    private Timestamp CreatedDate = null;
    private Long CreatedBy_UserID = null;
    private Timestamp UpdatedDate = null;
    private Long UpdatedBy_UserID = null;
}

