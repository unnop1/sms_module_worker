package com.nt.sms_module_worker.model.dto;


import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SmsGatewayData {
    private Long GID = null;
    private Long config_conditions_ID = null;
    private String SMSMessage= null;
    private String Message_Raw= null;
    private Long order_type_MainID = null;
    private String OrderType = null;
    private String PhoneNumber = null;
    private String PayloadMQ = null;
    private String PayloadGW = null;
    private Integer Is_Status = 0;
    private String remark = null;
    private Timestamp Created_Date = null;
    private Timestamp receive_date = null;
    private Timestamp send_Date = null;
    private String transaction_id = null;
}
