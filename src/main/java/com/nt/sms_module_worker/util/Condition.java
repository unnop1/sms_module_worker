package com.nt.sms_module_worker.util;

import java.sql.Timestamp;

import org.json.JSONArray;
import org.json.JSONObject;

public class Condition {
    public static final String checkFieldType(JSONObject jObj, String fieldName) {
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


    public static final boolean doBooleanOperation(String operator,Boolean dataValue, Boolean orConfValue){
        switch (operator) {
            case "!=":
                return !dataValue.equals(orConfValue);
            case "=":
                return dataValue.equals(orConfValue);
            default:
                return false;
        }
    }

    public static final boolean doStringOperation(String operator,String dataValue, String orConfValue){
        switch (operator) {
            case "!=":
                return !dataValue.equals(orConfValue);
            case "=":
                return dataValue.equals(orConfValue);
            case "like":
                return dataValue.contains(orConfValue);
            default:
                return false;
        }
    }



    public static final boolean doTimeStampOperation(String operator,Timestamp timestamp1, Timestamp timestamp2){
        switch (operator) {
            case ">":
                return timestamp1.compareTo(timestamp2) < 0;
            case "<":
                return timestamp1.compareTo(timestamp2) > 0;
            case "<=":
                return timestamp1.compareTo(timestamp2) <= 0;
            case ">=":
                return timestamp1.compareTo(timestamp2) >= 0;
            case "=":
                return timestamp1.compareTo(timestamp2) == 0;
            default:
                return false;
        }
    }

    public static final boolean doNumberOperation(String operator,Integer dataNumber, Integer orConfNumber){
        // int dataNumber = Integer.parseInt(dataValue);
        // int orConfNumber = Integer.parseInt(orConfValue);
        System.out.println(operator+" , dataNumber: "+dataNumber+ " , orConfNumber:"+ orConfNumber + " ==> "+(!dataNumber.equals(orConfNumber)));
        switch (operator) {
            case "!=":
                return !dataNumber.equals(orConfNumber);
            case "=":
                return dataNumber.equals(orConfNumber);
            case "<":
                return dataNumber < orConfNumber;
            case ">":
                return dataNumber > orConfNumber;    
            case "<=":
                return dataNumber <= orConfNumber; 
            case ">=":
                return dataNumber >= orConfNumber; 
            default:
                return false;
        }
    }


    public static final boolean doArrayOperation(String condition_type,String operation_type, String conditionKey,JSONObject jsonData, JSONArray conditionArray){
        String dataType = Condition.checkFieldType(jsonData, conditionKey);
        // System.out.println(" doArrayOperation dataType: " + dataType);
        Boolean found = false;
        
        switch (dataType) {
            case "String":
                String dataStr = jsonData.getString(conditionKey);
                switch ( operation_type) {
                    case "where in":
                        for (int i = 0; i < conditionArray.length(); i++) {
                            String conditionStr = conditionArray.getString(i);
                            if (doStringOperation("=", dataStr, conditionStr)) {
                                found = true;
                                break;
                            }
                        }
                        break;
                    case "between":
                        // Date Between
                        System.out.println("dataStr:"+dataStr);
                        Timestamp dataTimestamp = DateTime.convertTimeStampDataModel(dataStr);
                        String startTimeStr = conditionArray.getString(0);
                        String endTimeStr = conditionArray.getString(1);
                        System.out.println("startTimeStr:"+startTimeStr);
                        System.out.println("endTimeStr:"+endTimeStr);
                        Timestamp startTime = Timestamp.valueOf(startTimeStr);
                        Timestamp endTime = Timestamp.valueOf(endTimeStr);

                        if (doTimeStampOperation(">=", dataTimestamp, startTime)&&
                            doTimeStampOperation("<=", dataTimestamp, endTime)) {
                            found = true;
                        }
                        break;
                    default:
                        break;
                }
                
                return found;
            case "Integer":
                Integer dataInt = jsonData.getInt(conditionKey);
                switch ( operation_type) {
                    case "where in":
                        for (int i = 0; i < conditionArray.length(); i++) {
                            Integer conditionInt = conditionArray.getInt(i);
                            if (doNumberOperation("=", dataInt, conditionInt)) {
                                found = true;
                                break;
                            }
                        }
                        break;
                    case "between":
                        // Number Between
                        Integer conditionStartInt = conditionArray.getInt(0);
                        Integer conditionEndInt = conditionArray.getInt(1);
                        if (doNumberOperation(">=", dataInt, conditionStartInt) &&
                            doNumberOperation("<=", dataInt, conditionEndInt)
                        ) {
                            found = true;
                        }
                        break;
                    default:
                        break;
                }

                return found;
            case "ValueConfig":
                for (int i = 0; i < conditionArray.length();i++){
                    if (condition_type.equals("or")){
                        found = checkOrCondition(conditionArray.getJSONObject(i), jsonData);
                    }else{
                        
                    }
                }
                return found;
            default:
                return false;
        }
    }

    public static final boolean doCondition(String condition_type, JSONObject jsonData, String conditionKey, JSONObject orValueConfig){
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
                return Condition.doArrayOperation(condition_type, operation_type, conditionKey, jsonData, conditionArray);
            default:
                return false;
        }
    }


    public static boolean checkOrConditionValueConfigArray(String conditionKey, JSONObject jsonData, JSONObject orConf){
        Boolean isMatchArrayCondition = true;
        JSONArray jsonArray = jsonData.getJSONArray(conditionKey);
        if(jsonArray.length() > 0){
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject dataJson = jsonArray.getJSONObject(i); // first element only
                // System.out.println("next array data==> "+dataJson.toString());
                JSONObject orConditionFound = orConf.optJSONObject(conditionKey);
                // System.out.println("next andConditionFound data==> "+orConditionFound.toString());
                if(orConditionFound == null || dataJson == null){
                    continue;
                }
                // System.out.println("next conditionKey==> "+conditionKey);
                isMatchArrayCondition = checkOrCondition(orConf.getJSONObject(conditionKey), dataJson);
                if (!isMatchArrayCondition){
                    break;
                }
            }
        }
        return isMatchArrayCondition;
    }

    public static boolean checkAndConditionValueConfigArray(String conditionKey, JSONObject jsonData, JSONObject andConf){
        Boolean isMatchArrayCondition = true;
        JSONArray jsonArray = jsonData.getJSONArray(conditionKey);
        // All element in arrays is AND conditions
        if(jsonArray.length() > 0){
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject dataJson = jsonArray.getJSONObject(i); // first element only
                System.out.println("next array data==> "+dataJson.toString());
                JSONObject andConditionFound = andConf.optJSONObject(conditionKey);
                System.out.println("next andConditionFound data==> "+andConditionFound.toString());
                if(andConditionFound == null || dataJson == null){
                    continue;
                }
                System.out.println("next conditionKey==> "+conditionKey);
                isMatchArrayCondition = checkAndCondition(andConf.getJSONObject(conditionKey), dataJson);
                if (!isMatchArrayCondition){
                    break;
                }
            }
        }
        return isMatchArrayCondition;
    }
    
    public static boolean checkOrCondition(JSONObject orConf, JSONObject jsonData){
    
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
                
                // String operation_type = orConfValue.getString("operation_type");
                if (dataType == "JSONArray"){
                    boolean isMatchArrayCondition = checkOrConditionValueConfigArray(conditionKey, jsonData, orConf);
                    if (isMatchArrayCondition){
                        return true;
                    }
                }else{
                    JSONObject orValueConfig = orConf.getJSONObject(conditionKey);
                    isMatchCondition = doCondition("or",jsonData, conditionKey, orValueConfig);
                }
                // System.out.println(conditionKey+ " have conf type "+ orConfType +" and data type "+ dataType + " isMatchCondition :"+ isMatchCondition );
                if (isMatchCondition){
                    return true;
                }
            }else{
                // Sub Object
                if (dataType == "JSONArray"){
                    boolean isMatchArrayCondition = checkOrConditionValueConfigArray(conditionKey, jsonData, orConf);
                    if (isMatchArrayCondition){
                        return true;
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
    
    public static final boolean checkAndCondition(JSONObject andConf, JSONObject jsonData){
    
        // check or
        boolean isMatchCondition = true;
        for (String conditionKey : andConf.keySet()){
            String andConfType = Condition.checkFieldType(andConf, conditionKey);
            String dataType = Condition.checkFieldType(jsonData, conditionKey);
            System.out.println("root==> "+conditionKey+ " have conf type "+ andConfType +" and data type "+ dataType);
            if (dataType == "NotFound"){
                continue;
            }else if(andConfType == "ValueConfig"){
                if (dataType == "JSONArray"){
                    boolean isMatchArrayCondition = checkAndConditionValueConfigArray(conditionKey, jsonData, andConf);
                    if (!isMatchArrayCondition){
                        return false;
                    }
                }else{
                    // String dataValue = jsonData.getString(key);
                    JSONObject orValueConfig = andConf.getJSONObject(conditionKey);
                    // String operation_type = orConfValue.getString("operation_type");
                    isMatchCondition = doCondition("and", jsonData, conditionKey, orValueConfig);
                    System.out.println(conditionKey+ " have conf type "+ andConfType +" and data type "+ dataType + " isMatchCondition :"+ isMatchCondition);
                }

                if (!isMatchCondition){
                    return false;
                }
            }else{
                // Sub Object
                if (dataType == "JSONArray"){
                    boolean isMatchArrayCondition = checkAndConditionValueConfigArray(conditionKey, jsonData, andConf);
                    if (!isMatchArrayCondition){
                        return false;
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
