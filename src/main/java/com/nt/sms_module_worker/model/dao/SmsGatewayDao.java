package com.nt.sms_module_worker.model.dao;

import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;
@Getter
@Setter
public class SmsGatewayDao {
    private Long GID = null;
    private Long sms_condition_SMSID = null;
    private String SMSMessage= null;
    private Long order_type_MainID = null;
    private String OrderType = null;
    private String PhoneNumber = null;
    private Integer serviceType = null;
    private String Frequency = null;
    private String Chanel = null;
    private String OfferingId = null;
    private String PayloadMQ = null;
    private Integer IsStatus = 0;
    private Timestamp CreatedDate = null;
}
