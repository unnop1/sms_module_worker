package com.nt.sms_module_worker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nt.sms_module_worker.client.PDPAClient;
import com.nt.sms_module_worker.entity.ConfigConditionsEntity;
import com.nt.sms_module_worker.model.dao.pdpa.consent.ConsentResp;
import com.nt.sms_module_worker.model.dto.SmsConditionData;
import com.nt.sms_module_worker.util.DateTime;

import lombok.Getter;

@Component
@Getter
public class PDPAService {

    @Autowired
    private PDPAClient client;

    @Value("${pdpa.consent_id}")
    private String consentID = "xxxxxxxxxxxxxxxxxxxxxxxx";
    
    public boolean mustCheckPDPA(ConfigConditionsEntity condition){
        if(condition.getIs_pdpa().equals(0)){
            return false; // Skip check PDPA
        }
        else{
            if (condition.getIs_period_time().equals(1)){
                if (DateTime.isCurrentTimeInRange(condition.getTime_Start(), condition.getTime_End())){
                    return true;
                }
            }else{
                return true;
            }
        }
        return false; // Skip check PDPA
    }

    public ConsentResp getPDPAConsent(String phoneNumber){
        ConsentResp resp = client.GetConsentPDPAByPhoneNumber(phoneNumber);
        return resp;
    }
    
}
