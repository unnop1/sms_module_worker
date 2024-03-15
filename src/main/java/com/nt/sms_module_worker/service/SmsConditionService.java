package com.nt.sms_module_worker.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Date;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.sms_module_worker.model.dto.SmsConditionData;
import com.nt.sms_module_worker.model.dto.config_conditions.ConfigCondition;
import com.nt.sms_module_worker.model.dto.distribute.ReceivedData;

@Component
public class SmsConditionService {

  @Autowired
  private JdbcDatabaseService jbdcDB;

  private final RabbitTemplate rabbitTemplate;

  public SmsConditionService(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publish(String exchangeName, String routingKey,String message) throws Exception {
    System.out.println("Sending message...");
    rabbitTemplate.convertAndSend("", routingKey, message);
    // receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
  }

  public String getQueryOrderTypeSmsCondition(ReceivedData receivedData) {
    // Get the current date
    String currentDate = LocalDate.now().toString();

    String tableName = "config_conditions";

    String query = "SELECT * FROM "+ tableName +
                  " WHERE ( OrderType ='" + receivedData.getOrderType() + "'" + " OR OrderType IS NULL )" +
                  " AND ( DateStart <='" + currentDate + "'" + " OR DateStart IS NULL )" +
                  " AND ( DateEnd >='" + currentDate + "'" + " OR DateEnd IS NULL )";        
                  
    System.out.println("query condition : "+query);
    return query;
    
  }

  public List<SmsConditionData> getListSmsCondition(String query) throws SQLException {
      // String query = "SELECT * FROM conditions";
      List<SmsConditionData> conditionDataList = new ArrayList<>();
      String databaseName = "admin_red_sms";
      Connection con = jbdcDB.getConnection();

      PreparedStatement statement = con.prepareStatement(query);
      ResultSet rs = statement.executeQuery();

      try{          
          while (rs.next()) {
              SmsConditionData smsConditionData = new SmsConditionData();
              smsConditionData.setOrderType(rs.getString("orderType"));
              smsConditionData.setMessage(rs.getString("Message"));
              smsConditionData.setOrder_type_MainID(rs.getLong("order_type_MainID"));
              smsConditionData.setDateStart(rs.getDate("DateStart"));
              smsConditionData.setDateEnd(rs.getDate("DateEnd"));
              conditionDataList.add(smsConditionData);
          }
          
      } catch (SQLException e) {
          System.out.println("Error: " + e.getMessage());
          
      } finally {
        // Step 4: Close Connection
        try {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
              statement.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
      }
      return conditionDataList;
  }


  public boolean checkSendSms(SmsConditionData smsCondition , JSONObject jsonData) throws JsonMappingException, JsonProcessingException {
    JSONArray jsonSmsOrCon = new JSONArray(smsCondition.getConditions_or());
    JSONArray jsonSmsAndCon = new JSONArray(smsCondition.getConditions_and());
    
    for (int i = 0; i < jsonSmsOrCon.length(); i++) {
        JSONObject conf = jsonSmsOrCon.getJSONObject(i);
        if (checkOrCondition(conf, jsonData)){ // or = false
            return false;
        }
    }

    for (int i = 0; i < jsonSmsAndCon.length(); i++) {
        JSONObject conf = jsonSmsAndCon.getJSONObject(i);
        if (!checkAndCondition(conf, jsonData)){ // and = true
            return false;
        }
    }
    return true;    
  }
  public String checkFieldType(JSONObject jObj, String fieldName) {
    if (!jObj.has(fieldName)){
        return "NotFound";
    }
    Object checkTypeField = jObj.get(fieldName);
    if (checkTypeField instanceof Integer) {
        return "Integer";
    } else if (checkTypeField instanceof String) {
        return "String";
    } else if (checkTypeField instanceof Boolean) {
        return "Boolean";
    } else if (checkTypeField instanceof JSONObject) {
        JSONObject jsonObj = jObj.getJSONObject(fieldName);
        Integer jsonSize = jObj.getJSONObject(fieldName).length();
        if (jsonSize == 2){
            if (jsonObj.has("value") && jsonObj.has("operation_type")){
                return "ValueConfig";
            }
        }
        return "JSONObject";
    } else if (checkTypeField instanceof JSONArray) {
        return "JSONArray";
    } else {
        return "Unknown";
    }        
}

public boolean doBooleanOperation(String operator,Boolean dataValue, Boolean orConfValue){
    switch (operator) {
        case "!=":
            return dataValue != orConfValue;
        case "=":
            return dataValue == orConfValue;
        default:
            return true;
    }
}

public boolean doStringOperation(String operator,String dataValue, String orConfValue){
    switch (operator) {
        case "!=":
            return !(dataValue.equals(orConfValue));
        case "=":
            return dataValue.equals(orConfValue);
        default:
            return true;
    }
}

public boolean doNumberOperation(String operator,Integer dataNumber, Integer orConfNumber){
    // int dataNumber = Integer.parseInt(dataValue);
    // int orConfNumber = Integer.parseInt(orConfValue);
    switch (operator) {
        case "!=":
            return dataNumber != orConfNumber;
        case "=":
            return dataNumber == orConfNumber;
        case "<":
            return dataNumber < orConfNumber;
        case ">":
            return dataNumber > orConfNumber;    
        case "<=":
            return dataNumber <= orConfNumber; 
        case ">=":
            return dataNumber >= orConfNumber; 
        default:
            return true;
    }
}

public boolean doArrayOperation(String operation_type, String conditionKey,JSONObject jsonData, JSONArray conditionArray){
    String dataType = checkFieldType(jsonData, conditionKey);
    switch (dataType) {
        case "String":
            Boolean found = false;
            String dataStr = jsonData.getString(conditionKey);
            for (int i = 0; i < conditionArray.length(); i++) {
                String conditionStr = conditionArray.getString(i);
                if (doStringOperation(operation_type, dataStr, conditionStr)) {
                    found = true;
                    break;
                }
            }
            return found;
        default:
            return false;
    }
}

public boolean doCondition(JSONObject jsonData, String conditionKey, JSONObject orValueConfig){
    // check condition
    String configValueType = checkFieldType(orValueConfig, "value");
    // System.out.println("checkFieldType:"+configValueType);
    String operation_type = orValueConfig.getString("operation_type");
    // System.out.println("operation_type:"+operation_type);
    switch (configValueType) {
        case "Integer":
            Integer dataInt = jsonData.getInt(conditionKey);
            Integer conditionInt = orValueConfig.getInt("value");
            return doNumberOperation(operation_type, dataInt, conditionInt);
        case "Boolean":
            Boolean dataBool = jsonData.getBoolean(conditionKey);
            Boolean conditionBool = orValueConfig.getBoolean("value");
            return doBooleanOperation(operation_type, dataBool, conditionBool);
        case "String":
            String dataStr = jsonData.getString(conditionKey);
            String conditionStr = orValueConfig.getString("value");
            return doStringOperation(operation_type, dataStr, conditionStr);
        case "JSONArray":
            JSONArray conditionArray = orValueConfig.getJSONArray("value");
            return doArrayOperation(operation_type, conditionKey, jsonData, conditionArray);
        default:
            return true;
    }
}


public boolean checkOrCondition(JSONObject orConf, JSONObject jsonData){

    // check or
    for (String conditionKey : orConf.keySet()){
        boolean isNoCondition = true;
        String orConfType = checkFieldType(orConf, conditionKey);
        String dataType = checkFieldType(jsonData, conditionKey);
        System.out.println(conditionKey+ " have conf type "+ orConfType +" and data type "+ dataType);
        if(orConfType == "ValueConfig"){
            // String dataValue = jsonData.getString(key);
            JSONObject orValueConfig = orConf.getJSONObject(conditionKey);
            // String operation_type = orConfValue.getString("operation_type");
            isNoCondition = doCondition(jsonData, conditionKey, orValueConfig);
            System.out.println(conditionKey+ " have conf type "+ orConfType +" and data type "+ dataType + ", isNoCondition is "+isNoCondition);
            if (isNoCondition){
                return true;
            }
        }else{
            // Sub Object
            isNoCondition = checkOrCondition(orConf.getJSONObject(conditionKey), jsonData.getJSONObject(conditionKey));
            if (isNoCondition){
                return true;
            }
        }
    }
    return false;
}

public boolean checkAndCondition(JSONObject orConf, JSONObject jsonData){

    // check or
    for (String conditionKey : orConf.keySet()){
        boolean isCondition = false;
        String orConfType = checkFieldType(orConf, conditionKey);
        String dataType = checkFieldType(jsonData, conditionKey);
        System.out.println(conditionKey+ " have conf type "+ orConfType +" and data type "+ dataType);
        if(orConfType == "ValueConfig"){
            // String dataValue = jsonData.getString(key);
            JSONObject orValueConfig = orConf.getJSONObject(conditionKey);
            // String operation_type = orConfValue.getString("operation_type");
            isCondition = doCondition(jsonData, conditionKey, orValueConfig);
            System.out.println(conditionKey+ " have conf type "+ orConfType +" and data type "+ dataType + ", isCondition is "+isCondition);
            if (!isCondition){
                return false;
            }
        }else{
            // Sub Object
            isCondition = checkAndCondition(orConf.getJSONObject(conditionKey), jsonData.getJSONObject(conditionKey));
            if (!isCondition){
                return false;
            }
        }
    }
    return true;
}
}