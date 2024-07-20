package com.nt.sms_module_worker.service;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;

import org.apache.kafka.common.protocol.types.Field.Bool;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nt.sms_module_worker.entity.ConfigConditionsEntity;
import com.nt.sms_module_worker.log.LogFile;
import com.nt.sms_module_worker.model.dto.distribute.SendSmsGatewayData;
import com.nt.sms_module_worker.repo.SmsConditionRepo;
import com.nt.sms_module_worker.util.Condition;
import com.nt.sms_module_worker.util.DateTime;

@Component
public class SmsConditionService {

  @Autowired
  private SmsConditionRepo smsConditionRepo;

  private final RabbitTemplate rabbitTemplate;

  public SmsConditionService(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publish(String exchangeName, String routingKey,SendSmsGatewayData bulkMessage) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    String message = objectMapper.writeValueAsString(bulkMessage);
    // String message = "{\"messages\":[{\"systemCode\":\"REDS\",\"target\":\"66893054171\",\"source\":\"my\",\"message\":\"ทดสอบการส่งข้อความ\",\"systemTransRef\":\"f52888f8-82c5-11ee-b962-0242ac120099\",\"requestDate\":\"2023-06-22 17:31:20\"}],\"bulkRef\":\"BulkTest-e9bfae24-82c5-11ee-b962-0242ac120002\"}";

    // Set the headers
    Map<String, Object> headers = new HashMap<>();
    headers.put("__TypeId__", "cat.shot.smsc.dto.BulkMessage");
    headers.put("content_encoding", "UTF-8");
    headers.put("content_type", "application/json");

    // Create message properties and set headers
    MessageProperties messageProperties = new MessageProperties();
    headers.forEach(messageProperties::setHeader);

    // Create and send the message
    Message rabbitMessage = new Message(message.getBytes(StandardCharsets.UTF_8), messageProperties);
    rabbitTemplate.convertAndSend(exchangeName, routingKey, rabbitMessage);
  }

    public List<ConfigConditionsEntity> getListSmsCondition(String orderType) throws SQLException {
        Timestamp currentTime = DateTime.getTimeStampNow();
        List<ConfigConditionsEntity> conditionDataList = smsConditionRepo.findSmsCondition(orderType.toUpperCase(), currentTime, currentTime);
        return conditionDataList;
    }


    public boolean checkSendSms(ConfigConditionsEntity smsCondition , JSONObject jsonData) throws JsonMappingException, JsonProcessingException {
    System.out.println("smsCondition.getConditions_and:"+smsCondition.getConditions_or());
    System.out.println("smsCondition.getConditions_or:"+smsCondition.getConditions_and());
        try{
            if(smsCondition.getConditions_and() != null){
                // System.out.println("smsCondition.getConditions_and:"+smsCondition.getConditions_and());
                JSONArray jsonSmsAndCon = new JSONArray(smsCondition.getConditions_and());
                
                for (int i = 0; i < jsonSmsAndCon.length(); i++) {
                    JSONObject conf = jsonSmsAndCon.getJSONObject(i);
                    if (!Condition.checkAndCondition(conf, jsonData)){
                        System.out.println("return not match and");
                        return false;
                    }
                }
            }

            if(smsCondition.getConditions_or()!= null){
                Boolean isMatch = false;
                // System.out.println("smsCondition.getConditions_or:"+smsCondition.getConditions_or());
                JSONArray jsonSmsOrCon = new JSONArray(smsCondition.getConditions_or());
                
                for (int i = 0; i < jsonSmsOrCon.length(); i++) {
                    JSONObject conf = jsonSmsOrCon.getJSONObject(i);
                    if (Condition.checkOrCondition(conf, jsonData)){
                        isMatch = true;
                        break;
                    }
                    
                }
                return isMatch;
            }

            
            return true;    
        }catch(Exception e){
            System.out.println("error checking condition: " + e.getMessage());
            return false;
        }
    }



}