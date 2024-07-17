package com.nt.sms_module_worker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nt.sms_module_worker.client.PDPAClient;
import com.nt.sms_module_worker.entity.ConfigConditionsEntity;
import com.nt.sms_module_worker.log.LogFile;
import com.nt.sms_module_worker.model.dao.pdpa.consent.ConsentResp;
import com.nt.sms_module_worker.model.dao.pdpa.login.RefreshTokenResp;
import com.nt.sms_module_worker.util.DateTime;

import lombok.Getter;

@Component
@Getter
public class PDPAService {

    @Value("${pdpa.purpose_id}")
    private String purposeID;

    @Value("${pdpa.consent_id}")
    private String consentID;

    @Value("${pdpa.refresh_token}")
    private String refreshToken;

    @Value("${pdpa.is-skip-pdpa}")
    private boolean isSkipCheckPDPA;

    private PDPAClient client;

    public PDPAService(@Value("${pdpa.host}") String host) {
        client = new PDPAClient(host);
    }
    
    public boolean mustCheckPDPA(ConfigConditionsEntity condition){
        if (condition.getIs_pdpa()!= null){
            if(condition.getIs_pdpa().equals(0) || isSkipCheckPDPA){
                return false; // Skip check PDPA
            }else{
                return true;
            }
            // else{
            //     if (condition.getIs_period_time()!= null){
            //         if (condition.getIs_period_time().equals(1)){
            //             if (DateTime.isCurrentTimeInRange(condition.getTime_Start(), condition.getTime_End())){
            //                 return true;
            //             }
            //         }else{
            //             return true;
            //         }
            //     }
            // }
        }
        return false; // Skip check PDPA
    }

    public boolean mustInRangeTime(ConfigConditionsEntity condition){
        if (condition.getIs_period_time()!= null){
            if (condition.getIs_period_time().equals(1)){
                // System.out.println("check range time:"+DateTime.isCurrentTimeInRange(condition.getTime_Start(), condition.getTime_End()));
                return DateTime.isCurrentTimeInRange(condition.getTime_Start(), condition.getTime_End());
            }
        }
        return true;
    }

    public ConsentResp getPDPAConsent(String phoneNumber) throws Exception{
        ConsentResp resp= null;
        RefreshTokenResp tokenResp = client.refreshToken(refreshToken);
        // LogFile.logMessageTest("KafkaConsumerService", "debug_pdpa", tokenResp.getMessage());
        if (tokenResp != null){
            resp = client.GetConsentPDPAByPhoneNumber(consentID, purposeID, phoneNumber, tokenResp.getData().getAccessToken());
        }
        return resp;
    }
    
}
