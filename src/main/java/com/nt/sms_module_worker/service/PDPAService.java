package com.nt.sms_module_worker.service;

import org.springframework.stereotype.Component;

import com.nt.sms_module_worker.model.dto.SmsConditionData;
import com.nt.sms_module_worker.util.DateTime;

@Component
public class PDPAService {
    public boolean isPDPASendSMS(SmsConditionData condition){

        if (condition.getIs_pdpa().equals(0)){
            return true;
        }else{
            if (condition.getIs_period_time().equals(1)){
                return DateTime.isCurrentTimeInRange(condition.getTime_Start(), condition.getTime_End());
            }else{
                return true;
            }
        }
    }
    
}
