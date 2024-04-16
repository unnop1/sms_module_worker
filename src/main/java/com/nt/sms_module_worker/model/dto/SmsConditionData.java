package com.nt.sms_module_worker.model.dto;

import java.sql.Date;
import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression.DateTime;


@Getter
@Setter
public class SmsConditionData {
    private Long conditions_ID = null;
    private Long order_type_MainID = null;
    private String orderType = null;
    private String refID = null;
    private Date date_Start = null;
    private Date date_End = null;
    private String message = null;
    private String conditions_or = null;
    private String conditions_and = null;
    private Timestamp created_Date = null;
    private String created_By = null;
    private Timestamp updated_Date = null;
    private String updated_By = null;
    private Integer is_delete=0;
    private String is_delete_By = null;
    private Timestamp is_delete_Date = null;

}
