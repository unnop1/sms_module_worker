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
                return dataValue != orConfValue;
            case "=":
                return dataValue == orConfValue;
            default:
                return false;
        }
    }

    public static final boolean doStringOperation(String operator,String dataValue, String orConfValue){
        switch (operator) {
            case "!=":
                return !(dataValue.equals(orConfValue));
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
                return false;
        }
    }


    public static final boolean doArrayOperation(String operation_type, String conditionKey,JSONObject jsonData, JSONArray conditionArray){
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
                        Timestamp dataTimestamp = DateTime.convertTimeStampDataModel(dataStr);
                        Timestamp startTime = Timestamp.valueOf(dataStr);
                        Timestamp endTime = Timestamp.valueOf(dataStr);
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
                    case "wherein":
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
            default:
                return false;
        }
    }
}
