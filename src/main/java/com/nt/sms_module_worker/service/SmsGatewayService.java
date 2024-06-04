package com.nt.sms_module_worker.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nt.sms_module_worker.entity.ConfigConditionsEntity;
import com.nt.sms_module_worker.entity.SmsGatewayEntity;
import com.nt.sms_module_worker.model.dto.SmsGatewayData;
import com.nt.sms_module_worker.repo.SmsGatewayRepo;


@Component
class SmsGatewayService {

    @Autowired 
    private SmsGatewayRepo smsGatewayRepo;

    public Integer countAllToday() throws SQLException {
        Integer count = smsGatewayRepo.countTotalAllToday();
        return count;
    }

    public SmsGatewayEntity createConditionalMessage(SmsGatewayEntity smsGatewayData) throws SQLException {    
        smsGatewayRepo.save(smsGatewayData);
        return smsGatewayData;
    }


    public void updateConditionalMessageById(Long smsGatewayId, SmsGatewayEntity updates) throws SQLException {
        SmsGatewayEntity existingEntity = smsGatewayRepo.findById(smsGatewayId).orElse(null);
        if (existingEntity != null) {
            if (updates.getIs_Status() != null){
                existingEntity.setIs_Status(updates.getIs_Status());
            }

            if (updates.getSend_Date() != null){
                existingEntity.setSend_Date(updates.getSend_Date());
            }
            smsGatewayRepo.save(existingEntity);
        }
        
    }

}