package com.nt.sms_module_worker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nt.sms_module_worker.entity.ConfigConditionsEntity;
import com.nt.sms_module_worker.model.dto.SmsConditionData;
import com.nt.sms_module_worker.util.DateTime;

import lombok.Getter;

@Component
@Getter
public class PDPAService {

    @Value("${pdpa.is-skip-pdpa}")
    private Boolean isSkipPDPA = false;

    public boolean isPDPASendSMS(ConfigConditionsEntity condition){
        if(isSkipPDPA){
            return true;
        } 
        else if (condition.getIs_pdpa().equals(0)){
            return true;
        }
        else{
            if (condition.getIs_period_time().equals(1)){
                return DateTime.isCurrentTimeInRange(condition.getTime_Start(), condition.getTime_End());
            }else{
                return true;
            }
        }
    }
    
}
