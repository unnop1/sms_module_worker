package com.nt.sms_module_worker.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.kafka.common.protocol.types.Field.Bool;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.sms_module_worker.model.dto.SmsConditionData;
import com.nt.sms_module_worker.model.dto.distribute.ReceivedData;
import com.nt.sms_module_worker.util.Condition;
import com.nt.sms_module_worker.util.DateTime;

@Component
public class SmsConditionService {

  private final RabbitTemplate rabbitTemplate;

  public SmsConditionService(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publish(String exchangeName, String routingKey,String message) throws Exception {
    // System.out.println("Sending message...");
    rabbitTemplate.convertAndSend("", routingKey, message);
    // receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
  }

  public String getQueryOrderTypeSmsCondition(ReceivedData receivedData) {
    // Get the current date
    Timestamp currentTime = DateTime.getTimeStampNow();

    String tableName = "config_conditions";

    String query = "SELECT * FROM "+ tableName +
                  " WHERE is_enable = 1"+
                  " AND ( orderType ='" + receivedData.getOrderType() + "'" + " OR orderType IS NULL )" +
                  " AND ( date_Start <= TO_TIMESTAMP('" + currentTime + "', 'YYYY-MM-DD HH24:MI:SS.FF') OR date_Start IS NULL )" +
                  " AND ( date_End >= TO_TIMESTAMP('" + currentTime + "', 'YYYY-MM-DD HH24:MI:SS.FF') OR date_End IS NULL )";         
    // System.out.println("query condition : "+query);
    return query;
    
  }

    public List<SmsConditionData> getListSmsCondition(String query) throws SQLException {
        List<SmsConditionData> conditionDataList = new ArrayList<>();

        try (
            Connection con = JdbcDatabaseService.getConnection();
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet rs = statement.executeQuery()
        ) {
            while (rs.next()) {
                SmsConditionData smsConditionData = new SmsConditionData();
                smsConditionData.setConditions_ID(rs.getLong("conditions_ID"));
                smsConditionData.setOrderType(rs.getString("orderType"));
                smsConditionData.setMessage(rs.getString("Message"));
                smsConditionData.setOrder_type_MainID(rs.getLong("order_type_MainID"));
                smsConditionData.setConditions_or(rs.getString("conditions_or"));
                smsConditionData.setConditions_and(rs.getString("conditions_and"));
                smsConditionData.setDate_Start(rs.getDate("Date_Start"));
                smsConditionData.setDate_End(rs.getDate("Date_End"));
                smsConditionData.setCreated_Date(rs.getTimestamp("created_Date"));
                smsConditionData.setUpdated_Date(rs.getTimestamp("updated_Date"));
                smsConditionData.setCreated_By(rs.getString("created_By"));
                smsConditionData.setUpdated_By(rs.getString("updated_By"));
                smsConditionData.setIs_delete(rs.getInt("is_delete"));
                smsConditionData.setIs_delete_By(rs.getString("is_delete_By"));
                smsConditionData.setIs_delete_Date(rs.getTimestamp("is_delete_Date"));
                
                conditionDataList.add(smsConditionData);
            }
        } catch (SQLException e) {
            System.out.println("Error getListSmsCondition: " + e.getMessage());
            throw e; // Rethrow the exception to propagate it
        }

        return conditionDataList;
    }


    public boolean checkSendSms(SmsConditionData smsCondition , JSONObject jsonData) throws JsonMappingException, JsonProcessingException {
    // System.out.println("smsCondition.getConditions_or:"+smsCondition.getConditions_or());
    // System.out.println("smsCondition.getConditions_and:"+smsCondition.getConditions_and());
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
                // System.out.println("smsCondition.getConditions_or:"+smsCondition.getConditions_or());
                JSONArray jsonSmsOrCon = new JSONArray(smsCondition.getConditions_or());
                
                for (int i = 0; i < jsonSmsOrCon.length(); i++) {
                    JSONObject conf = jsonSmsOrCon.getJSONObject(i);
                    if (!Condition.checkOrCondition(conf, jsonData)){ 
                        System.out.println("return not match or");
                        return false;
                    }
                }
            }

            
            return true;    
        }catch(Exception e){
            System.out.println("error checking condition: " + e.getMessage());
            return false;
        }
    }



}