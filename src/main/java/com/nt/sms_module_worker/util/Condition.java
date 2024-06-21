package com.nt.sms_module_worker.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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
        } else if (checkTypeField instanceof Long) {
            return "Long";
        } else if (checkTypeField instanceof Double) {
            return "Double";
        } else if (checkTypeField instanceof Float) {
            return "Float";
        } else if (checkTypeField instanceof Boolean) {
            return "Boolean";
        } else if (checkTypeField instanceof LocalDateTime) {
            return "LocalDateTime";
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


    public static final boolean isDateFormatData(String dateCheck){
        if(DateTime.validateDate(dateCheck, "yyyy-MM-dd")){
            return true;
        }else if(DateTime.validateDate(dateCheck, "yyyy-MM-dd HH:mm:ss")){
            return true;
        }
        return false;
    }

    public static final boolean doStringOperation(String operator,String dataValue, String orConfValue){
        // Date String operations
        if(isDateFormatData(orConfValue)){
            try{
                LocalDateTime dataDateTime = DateTime.convertDateTime(dataValue);
                LocalDateTime conditionDateTime = DateTime.convertDateTime(orConfValue);
                return doDateTimeOperation(operator, dataDateTime, conditionDateTime);
            }catch(Exception e){
                System.out.println("error converting date time : " + e.getMessage());
            }
        }

        // Default String operations
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

    public static final boolean doFloatOperation(String operator,Float dataNumber, Float orConfNumber){
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

    public static final boolean doDoubleOperation(String operator,Double dataNumber, Double orConfNumber){
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

    public static final boolean doBigDecimalOperation(String operator,BigDecimal number1, BigDecimal number2){
        switch (operator) {
            case ">":
                return number1.compareTo(number2) < 0;
            case "<":
                return number1.compareTo(number2) > 0;
            case "<=":
                return number1.compareTo(number2) <= 0;
            case ">=":
                return number1.compareTo(number2) >= 0;
            case "=":
                return number1.compareTo(number2) == 0;
            default:
                return false;
        }
    }

    public static final boolean doDateTimeOperation(String operator,LocalDateTime datetime1, LocalDateTime datetime2){
        switch (operator) {
            case ">":
                return datetime1.compareTo(datetime2) < 0;
            case "<":
                return datetime1.compareTo(datetime2) > 0;
            case "<=":
                return datetime1.compareTo(datetime2) <= 0;
            case ">=":
                return datetime1.compareTo(datetime2) >= 0;
            case "=":
                return datetime1.compareTo(datetime2) == 0;
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
        System.out.println(operator+" , dataNumber: "+dataNumber+ " , orConfNumber:"+ orConfNumber);
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

    public static final String checkTypeObject(Object obj, String fieldName){
        if (obj instanceof JSONObject) {
            System.out.println("checkTypeObject json data:"+obj.toString());
            System.out.println("checkTypeObject fieldName:"+fieldName);
            JSONObject checkJsonObj = new JSONObject(obj.toString());
            JSONObject checkJsonKey = checkJsonObj.optJSONObject(fieldName);
            if (checkJsonKey != null){
                JSONObject jsonObj = checkJsonObj.getJSONObject(fieldName);
                Integer jsonSize = jsonObj.length();
                System.out.println("jsonSize:"+jsonSize);
                
                if (jsonSize == 2){
                    if (jsonObj.has("value") && jsonObj.has("operation_type")){
                        return "ValueConfig";
                    }
                }
            }
        } else if (obj instanceof Integer) {
            return "Integer";
        } else if (obj instanceof String) {
            return "String";
        } else if (obj instanceof Double) {
            return "Double";
        }
        
        return "Unknown";
    }


    public static final boolean doArrayOperation(String operation_type, String conditionKey,JSONObject jsonData, JSONArray conditionArray){
        String dataType = Condition.checkFieldType(jsonData, conditionKey);
        System.out.println("================================ doArrayOperation ===========================================");
        System.out.println(" conditionKey : " + conditionKey);
        System.out.println(" operation_type : " + operation_type);
        System.out.println(" dataType: " + dataType);
        System.out.println("=============================================================================================");
        
        Boolean found = false;
        
        switch (dataType) {
            case "String":
                String dataStr = jsonData.getString(conditionKey);
                switch ( operation_type) {
                    case "where in":
                        for (int i = 0; i < conditionArray.length(); i++) {
                            Object conditionObject = conditionArray.get(i);
                            String elementType = checkTypeObject(conditionObject, conditionKey);
                            // System.out.println("conditionKey: "+conditionKey+" ,  elementType : " + elementType);
                            switch ( elementType ) {
                                case "String":
                                    String conditionStr = conditionArray.getString(i);
                                    if (doStringOperation("=", dataStr, conditionStr)) {
                                        found = true;
                                    }
                                    break;
                                case "ValueConfig":
                                    JSONObject subCondition = conditionArray.getJSONObject(i);
                                    // System.out.println("==> subCondition : " + subCondition);
                                    boolean isMatchCondition = checkAndCondition(subCondition, jsonData);
                                    if (isMatchCondition){
                                        found = true;
                                    }
                                    break;
                                default:
                                    found = true;
                                    break;
                            }
                            if(found){
                                return found;
                            }
                        }
                        break;
                    case "between":
                        // Date Between
                        // System.out.println("dataStr:"+dataStr);
                        LocalDateTime dataTimestamp = DateTime.convertDateTime(dataStr);
                        String startTimeStr = conditionArray.getString(0);
                        String endTimeStr = conditionArray.getString(1);
                        // System.out.println("startTimeStr:"+startTimeStr);
                        // System.out.println("endTimeStr:"+endTimeStr);
                        LocalDateTime startTime = DateTime.convertDateTime(startTimeStr);
                        LocalDateTime endTime = DateTime.convertDateTime(endTimeStr);

                        if (doDateTimeOperation(">=", dataTimestamp, startTime)&&
                            doDateTimeOperation("<=", dataTimestamp, endTime)) {
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
                        Object conditionObject = conditionArray.get(i);
                        String elementType = checkTypeObject(conditionObject, conditionKey);
                        System.out.println("conditionKey: "+conditionKey+" ,  elementType : " + elementType);
                        switch ( elementType ) {
                            case "Integer":
                                Integer conditionStr = conditionArray.getInt(i);
                                if (doNumberOperation("=", dataInt, conditionStr)) {
                                    found = true;
                                }
                                break;
                            case "ValueConfig":
                                JSONObject subCondition = conditionArray.getJSONObject(i);
                                System.out.println("==> subCondition : " + subCondition);
                                if (checkAndCondition(subCondition, jsonData)){
                                    found = true;
                                }
                                break;
                            default:
                                found = true;
                                break;
                        }
                        if(found){
                            return found;
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

    public static final Boolean doCondition(JSONObject jsonData, String conditionKey, JSONObject orValueConfig){
        // check condition
        String configValueType = Condition.checkFieldType(orValueConfig, "value");
        System.out.println("================================doCondition================================");
        System.out.println("ValueConfig:"+orValueConfig.toString());
        System.out.println("checkFieldType:"+configValueType);
        System.out.println("jsonData:"+jsonData.toString());
        String operation_type = orValueConfig.getString("operation_type").toLowerCase();
        System.out.println("operation_type:"+operation_type);
        System.out.println("===========================================================================");
        switch (configValueType) {
            case "Integer":
                Integer dataInt = jsonData.getInt(conditionKey);
                Integer conditionInt = orValueConfig.getInt("value");
                return Condition.doNumberOperation(operation_type, dataInt, conditionInt);
            case "Boolean":
                Boolean dataBool = jsonData.getBoolean(conditionKey);
                Boolean conditionBool = orValueConfig.getBoolean("value");
                return Condition.doBooleanOperation(operation_type, dataBool, conditionBool);
            case "String", "LocalDateTime":
                String dataStr = jsonData.getString(conditionKey);
                String conditionStr = orValueConfig.getString("value");
                return Condition.doStringOperation(operation_type, dataStr, conditionStr);
            case "BigDecimal":
                BigDecimal dataBigDecimal = jsonData.getBigDecimal(conditionKey);
                BigDecimal conditionBigDecimal = orValueConfig.getBigDecimal("value");
                return Condition.doBigDecimalOperation(operation_type, dataBigDecimal, conditionBigDecimal);
            case "Float":
                Float dataFloat = jsonData.getFloat(conditionKey);
                Float conditionFloat = orValueConfig.getFloat("value");
                return Condition.doFloatOperation(operation_type, dataFloat, conditionFloat);
            case "Double":
                Double dataDouble = jsonData.getDouble(conditionKey);
                Double conditionDouble = orValueConfig.getDouble("value");
                return Condition.doDoubleOperation(operation_type, dataDouble, conditionDouble);
            case "JSONArray":
                JSONArray conditionArray = orValueConfig.getJSONArray("value");
                return Condition.doArrayOperation(operation_type, conditionKey, jsonData, conditionArray);
            default:
                return false;
        }
    }

    public static boolean checkOrConditionValueConfigArray(String conditionKey, JSONObject jsonData, JSONObject andConf){
        Boolean isMatchArrayCondition = false;
        Boolean isMatchAllData = true;
        JSONArray jsonArray = jsonData.getJSONArray(conditionKey);
        // All element in arrays is AND conditions
        if(jsonArray.length() > 0){
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject dataJson = jsonArray.getJSONObject(i); // first element only
                // System.out.println("next array data==> "+dataJson.toString());
                JSONObject orConditionFound = andConf.optJSONObject(conditionKey);
                System.out.println("next orConditionFound data==> "+orConditionFound.toString());
                if(orConditionFound == null || dataJson == null){
                    continue;
                }
                
                JSONObject valueConfig = andConf.getJSONObject(conditionKey);
                String operationType = valueConfig.getString("operation_type");
                if (operationType.equals("WHERE NOT IN")){
                    isMatchAllData = false;
                }
                JSONArray values = valueConfig.getJSONArray("value");
                for (int j = 0; j < values.length(); j++){
                    JSONObject subCondition = values.getJSONObject(j);
                    // System.out.println("next array data==> "+dataJson.toString());
                    System.out.println(" conditionKey==> "+conditionKey);
                    System.out.println("next subCondition==> "+subCondition.toString() );
                    if (!isMatchAllData){
                        isMatchArrayCondition = checkAndCondition(subCondition, dataJson);
                        System.out.println("isMatchArrayCondition==> "+isMatchArrayCondition);
                        if (isMatchArrayCondition){
                            return true;
                        }
                    }else{
                        isMatchArrayCondition = checkOrCondition(subCondition, dataJson);
                        System.out.println("isMatchArrayCondition==> "+isMatchArrayCondition);
                        if (!isMatchArrayCondition){
                            return false;
                        }
                    }
                    
                }
            }
        }
        return isMatchArrayCondition;
    }

    public static boolean checkAndConditionValueConfigArray(String conditionKey, JSONObject jsonData, JSONObject andConf){
        Boolean isMatchArrayCondition = false;
        Boolean isMatchAllData = false;
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
                
                JSONObject valueConfig = andConf.getJSONObject(conditionKey);
                String operationType = valueConfig.getString("operation_type");
                if (operationType.equals("WHERE NOT IN")){
                    isMatchAllData = true;
                }
                JSONArray values = valueConfig.getJSONArray("value");
                for (int j = 0; j < values.length(); j++){
                    JSONObject subCondition = values.getJSONObject(j);
                    // System.out.println("next array data==> "+dataJson.toString());
                    System.out.println(" conditionKey==> "+conditionKey);
                    System.out.println("next subCondition==> "+subCondition.toString() );
                    if (!isMatchAllData){
                        isMatchArrayCondition = checkAndCondition(subCondition, dataJson);
                        System.out.println("isMatchArrayCondition==> "+isMatchArrayCondition);
                        if (isMatchArrayCondition){
                            return true;
                        }
                    }else{
                        isMatchArrayCondition = checkOrCondition(subCondition, dataJson);
                        System.out.println("isMatchArrayCondition==> "+isMatchArrayCondition);
                        if (!isMatchArrayCondition){
                            return false;
                        }
                    }
                    
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
            System.out.println(conditionKey+ " have orconf type "+ orConfType +" and data type "+ dataType);
            if (dataType == "NotFound"){
                return true;
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
                    isMatchCondition = doCondition(jsonData, conditionKey, orValueConfig);
                }
                System.out.println(conditionKey+ " have orconf type "+ orConfType +" and data type "+ dataType + " isMatchCondition :"+ isMatchCondition );
                if (isMatchCondition){
                    return true;
                }
            }else{
                // Sub Object
                if (dataType == "JSONArray"){
                    System.out.println("OR Condition array");
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
            System.out.println("jsonData:"+jsonData.toString());
            if (dataType == "NotFound"){
                return false;
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
                    isMatchCondition = doCondition( jsonData, conditionKey, orValueConfig);
                    System.out.println(conditionKey+ " have andconf type "+ andConfType +" and data type "+ dataType + " isMatchCondition :"+ isMatchCondition);
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
