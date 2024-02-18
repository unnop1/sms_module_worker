package com.nt.sms_module_worker.model.dao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsConditionDao {
    private Long SMSID = null;
    private Long order_type_MainID = null;
    private String OrderType = null;
    private String Message = null;
    private String Chanel = null;
    private String Frequency = null;
    private Integer serviceType = null;
    private String ref_OfferingId = null;
    private String DateStart = null;
    private String DateEnd = null;
    private Boolean IsEnable = true;
    private Boolean IsDelete = false;
    private String CreatedDate = null;
    private Long CreatedBy_UserID = null;
    private String UpdatedDate = null;
    private Long UpdatedBy_UserID = null;
}
