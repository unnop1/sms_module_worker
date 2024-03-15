package com.nt.sms_module_worker.model.dto;

import java.sql.Date;
import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression.DateTime;


@Getter
@Setter
public class SmsConditionData {
    private Long conditionsID = null;
    private Long order_type_MainID = null;
    private String orderType = null;
    private String refID = null;
    private Date dateStart = null;
    private Date dateEnd = null;
    private String message = null;
    private String messageRaw = null;
    private String conditions_or = null;
    private String conditions_and = null;
    private DateTime createdDate = null;
    private Long createdBy_userID = null;
    private DateTime updatedDate = null;
}
