package com.nt.sms_module_worker.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
import com.nt.sms_module_worker.util.DateTime;

import ch.qos.logback.classic.pattern.Util;

@Component
public class SmsConditionService {

  @Autowired
  private JdbcDatabaseService jbdcDB;

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
      // String query = "SELECT * FROM conditions";
      List<SmsConditionData> conditionDataList = new ArrayList<>();
      String databaseName = "admin_red_sms";
      Connection con = jbdcDB.getConnection();

      PreparedStatement statement = con.prepareStatement(query);
      ResultSet rs = statement.executeQuery();

      try{          
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
              smsConditionData.setIs_delete_Date(rs.getTimestamp("is_delete_Date")); //
              
              conditionDataList.add(smsConditionData);
          }
          
      } catch (SQLException e) {
          System.out.println("Error getListSmsCondition: " + e.getMessage());
          
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
    // System.out.println("smsCondition.getConditions_or:"+smsCondition.getConditions_or());
    // System.out.println("smsCondition.getConditions_and:"+smsCondition.getConditions_and());
    // try{
        if(smsCondition.getConditions_and() != null){
            JSONArray jsonSmsAndCon = new JSONArray(smsCondition.getConditions_and());
            
            for (int i = 0; i < jsonSmsAndCon.length(); i++) {
                JSONObject conf = jsonSmsAndCon.getJSONObject(i);
                if (!checkAndCondition(conf, jsonData)){
                    return false;
                }
            }
        }

        if(smsCondition.getConditions_or()!= null){
            JSONArray jsonSmsOrCon = new JSONArray(smsCondition.getConditions_or());
            
            for (int i = 0; i < jsonSmsOrCon.length(); i++) {
                JSONObject conf = jsonSmsOrCon.getJSONObject(i);
                if (!checkOrCondition(conf, jsonData)){ 
                    return false;
                }
            }
        }

        
        return true;    
    // }catch(Exception e){
    //     System.out.println("error checking condition: " + e.getMessage());
    //     return false;
    // }
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
        case "like":
            return dataValue.contains(orConfValue);
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
            return true;
    }
}

public boolean doCondition(JSONObject jsonData, String conditionKey, JSONObject orValueConfig){
    // check condition
    String configValueType = checkFieldType(orValueConfig, "value");
    // System.out.println("checkFieldType:"+configValueType);
    String operation_type = orValueConfig.getString("operation_type").toLowerCase();
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
    boolean isMatchCondition = false;
    for (String conditionKey : orConf.keySet()){
        String orConfType = checkFieldType(orConf, conditionKey);
        String dataType = checkFieldType(jsonData, conditionKey);
        System.out.println(conditionKey+ " have conf type "+ orConfType +" and data type "+ dataType);
        if(orConfType == "ValueConfig"){
            // String dataValue = jsonData.getString(key);
            JSONObject orValueConfig = orConf.getJSONObject(conditionKey);
            // String operation_type = orConfValue.getString("operation_type");
            isMatchCondition = doCondition(jsonData, conditionKey, orValueConfig);
            System.out.println(conditionKey+ " have conf type "+ orConfType +" and data type "+ dataType );
            if (isMatchCondition){
                return true;
            }
        }else{
            // Sub Object
            String subConfType = checkFieldType(jsonData, conditionKey);
            if (subConfType == "JSONArray"){
                JSONArray jsonArray = jsonData.getJSONArray(conditionKey);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject dataJson = jsonArray.getJSONObject(i);
                    JSONObject orConditionFound = orConf.optJSONObject(conditionKey);
                    JSONObject dataJsonFound = dataJson.optJSONObject(conditionKey);
                    if(orConditionFound == null || dataJsonFound == null){
                        continue;
                    }
                    isMatchCondition = checkOrCondition(orConf.getJSONObject(conditionKey), dataJson.getJSONObject(conditionKey));
                    if (isMatchCondition){
                        return true;
                    }
                }
            }else{
                isMatchCondition = checkOrCondition(orConf.getJSONObject(conditionKey), jsonData.getJSONObject(conditionKey));
                if (isMatchCondition){
                    return true;
                }
            }
        }
    }

    return isMatchCondition;
}

public boolean checkAndCondition(JSONObject andConf, JSONObject jsonData){

    // check or
    boolean isMatchCondition = true;
    for (String conditionKey : andConf.keySet()){
        String andConfType = checkFieldType(andConf, conditionKey);
        String dataType = checkFieldType(jsonData, conditionKey);
        // System.out.println(conditionKey+ " have conf type "+ andConfType +" and data type "+ dataType);
        if(andConfType == "ValueConfig"){
            // String dataValue = jsonData.getString(key);
            JSONObject orValueConfig = andConf.getJSONObject(conditionKey);
            // String operation_type = orConfValue.getString("operation_type");
            isMatchCondition = doCondition(jsonData, conditionKey, orValueConfig);
            // System.out.println(conditionKey+ " have conf type "+ andConfType +" and data type "+ dataType + ", isCondition is "+isCondition);
            if (!isMatchCondition){
                return false;
            }
        }else{
            // Sub Object
            String subConfType = checkFieldType(jsonData, conditionKey);
            if (subConfType == "JSONArray"){
                JSONArray jsonArray = jsonData.getJSONArray(conditionKey);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject dataJson = jsonArray.getJSONObject(i);
                    JSONObject andConditionFound = andConf.optJSONObject(conditionKey);
                    JSONObject dataJsonFound = dataJson.optJSONObject(conditionKey);
                    if(andConditionFound == null || dataJsonFound == null){
                        continue;
                    }
                    isMatchCondition = checkAndCondition(andConf.getJSONObject(conditionKey), dataJson.getJSONObject(conditionKey));
                    if (!isMatchCondition){
                        return false;
                    }
                }
            }else{
                isMatchCondition = checkAndCondition(andConf.getJSONObject(conditionKey), jsonData.getJSONObject(conditionKey));
                if (!isMatchCondition){
                    return false;
                }
            }

            
        }
    }

    return isMatchCondition;
}
}