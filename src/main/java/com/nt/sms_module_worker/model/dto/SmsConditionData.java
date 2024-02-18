package com.nt.sms_module_worker.model.dto;

import java.sql.Date;
import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression.DateTime;


@Getter
@Setter
public class SmsConditionData {
    private Long SMSID = null;
    private Long order_type_MainID = null;
    private String OrderType = null;
    private String Message = null;
    private String Chanel = null;
    private String Frequency = null;
    private Integer serviceType = null;
    private String by_offeringId = null;
    private Date DateStart = null;
    private Date DateEnd = null;
    private Boolean IsEnable = true;
    private Boolean IsDelete = false;
    private Timestamp CreatedDate = null;
    private Long CreatedBy_UserID = null;
    private Timestamp UpdatedDate = null;
    private Long UpdatedBy_UserID = null;
}
