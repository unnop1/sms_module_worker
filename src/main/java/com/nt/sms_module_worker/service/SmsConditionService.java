package com.nt.sms_module_worker.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
                if (!checkAndCondition(conf, jsonData)){
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
                if (!checkOrCondition(conf, jsonData)){ 
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


public boolean doCondition(JSONObject jsonData, String conditionKey, JSONObject orValueConfig){
    // check condition
    String configValueType = Condition.checkFieldType(orValueConfig, "value");
    System.out.println("checkFieldType:"+configValueType);
    String operation_type = orValueConfig.getString("operation_type").toLowerCase();
    System.out.println("operation_type:"+operation_type);
    switch (configValueType) {
        case "Integer":
            Integer dataInt = jsonData.getInt(conditionKey);
            Integer conditionInt = orValueConfig.getInt("value");
            return Condition.doNumberOperation(operation_type, dataInt, conditionInt);
        case "Boolean":
            Boolean dataBool = jsonData.getBoolean(conditionKey);
            Boolean conditionBool = orValueConfig.getBoolean("value");
            return Condition.doBooleanOperation(operation_type, dataBool, conditionBool);
        case "String":
            String dataStr = jsonData.getString(conditionKey);
            String conditionStr = orValueConfig.getString("value");
            return Condition.doStringOperation(operation_type, dataStr, conditionStr);
        case "JSONArray":
            JSONArray conditionArray = orValueConfig.getJSONArray("value");
            return Condition.doArrayOperation(operation_type, conditionKey, jsonData, conditionArray);
        default:
            return false;
    }
}


public boolean checkOrCondition(JSONObject orConf, JSONObject jsonData){

    // check or
    boolean isMatchCondition = false;
    for (String conditionKey : orConf.keySet()){
        String orConfType = Condition.checkFieldType(orConf, conditionKey);
        String dataType = Condition.checkFieldType(jsonData, conditionKey);
        System.out.println(conditionKey+ " have conf type "+ orConfType +" and data type "+ dataType);
        if (dataType == "NotFound"){
            continue;
        }else if(orConfType == "ValueConfig"){
            // String dataValue = jsonData.getString(key);
            JSONObject orValueConfig = orConf.getJSONObject(conditionKey);
            // String operation_type = orConfValue.getString("operation_type");
            isMatchCondition = doCondition(jsonData, conditionKey, orValueConfig);
            // System.out.println(conditionKey+ " have conf type "+ orConfType +" and data type "+ dataType + " isMatchCondition :"+ isMatchCondition );
            if (isMatchCondition){
                return true;
            }
        }else{
            // Sub Object
            if (dataType == "JSONArray"){
                JSONArray jsonArray = jsonData.getJSONArray(conditionKey);
                // hard code if array will use first element
                if(jsonArray.length() > 0){
                    JSONObject dataJson = jsonArray.getJSONObject(0); // first element only
                    // System.out.println("next array data==> "+dataJson.toString());
                    JSONObject orConditionFound = orConf.optJSONObject(conditionKey);
                    // System.out.println("next andConditionFound data==> "+orConditionFound.toString());
                    if(orConditionFound == null || dataJson == null){
                        continue;
                    }
                    // System.out.println("next conditionKey==> "+conditionKey);
                    isMatchCondition = checkOrCondition(orConf.getJSONObject(conditionKey), dataJson);
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
        String andConfType = Condition.checkFieldType(andConf, conditionKey);
        String dataType = Condition.checkFieldType(jsonData, conditionKey);
        System.out.println("root==> "+conditionKey+ " have conf type "+ andConfType +" and data type "+ dataType);
        if (dataType == "NotFound"){
            continue;
        }else if(andConfType == "ValueConfig"){
            // String dataValue = jsonData.getString(key);
            JSONObject orValueConfig = andConf.getJSONObject(conditionKey);
            // String operation_type = orConfValue.getString("operation_type");
            isMatchCondition = doCondition(jsonData, conditionKey, orValueConfig);
            System.out.println(conditionKey+ " have conf type "+ andConfType +" and data type "+ dataType + " isMatchCondition :"+ isMatchCondition);
            if (!isMatchCondition){
                return false;
            }
        }else{
            // Sub Object
            if (dataType == "JSONArray"){
                JSONArray jsonArray = jsonData.getJSONArray(conditionKey);
                // hard code if array will use first element
                if(jsonArray.length() > 0){
                    JSONObject dataJson = jsonArray.getJSONObject(0); // first element only
                    System.out.println("next array data==> "+dataJson.toString());
                    JSONObject andConditionFound = andConf.optJSONObject(conditionKey);
                    System.out.println("next andConditionFound data==> "+andConditionFound.toString());
                    if(andConditionFound == null || dataJson == null){
                        continue;
                    }
                    System.out.println("next conditionKey==> "+conditionKey);
                    isMatchCondition = checkAndCondition(andConf.getJSONObject(conditionKey), dataJson);
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