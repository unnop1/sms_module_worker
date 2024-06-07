package com.nt.sms_module_worker.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

public class MapString {

    public static final String interString(JSONObject object, String key){
        String objectType = Condition.checkFieldType(object, key);
        String output = "";
        switch (objectType) {
            case "Integer":
                output = ""+object.getInt(key);
                break;
            case "Boolean":
                output = ""+object.getBoolean(key);
                break;
            case "String":
                output = object.getString(key);
                break;
        }
        return output;
    }

    public static final String getValueMessageDataModel(JSONObject conditionJson, String key){
        // JSONObject jsonObject = new JSONObject(conditionJson);
        String output = " ";
        // Split the key into parts using underscore as delimiter
        String[] parts = key.split("_");

        // Navigate through the JSONObject to access the desired value
        JSONObject currentObject = conditionJson;
        for (String part : parts) {
            if (currentObject.has(part)) {
                Object obj = currentObject.get(part);
                if (obj instanceof JSONArray) {
                    // If the value is a JSONArray, access the first element
                    JSONArray jsonArray = (JSONArray) obj;
                    if (jsonArray.length() > 0) {
                        obj = jsonArray.get(0);
                        if (obj instanceof JSONObject) {
                            currentObject = (JSONObject) obj;
                        } else {
                            // If the value is not a JSONObject, it should be the desired value
                            System.out.println("Output: " + obj);
                            output = interString(currentObject, part);
                            break;
                        }
                    } else {
                        // If the array is empty, print an error message
                        System.out.println("Error: Array is empty");
                        break;
                    }
                } else if (obj instanceof JSONObject) {
                    currentObject = (JSONObject) obj;
                } else {
                    // If the value is not a JSONObject or JSONArray, it should be the desired value
                    System.out.println("Output: " + obj);
                    output = interString(currentObject, part);
                    break;
                }
            } else {
                // If any part of the key is not found, print an error message
                System.out.println("Error: Key not found");
                break;
            }
        }
        return output;
    }


    public static final List<String> SplitKeyMessageDataModel(String message){
        // Define the regular expression pattern
        Pattern pattern = Pattern.compile("\\{([^}]*)\\}");
        
        // Create a matcher object
        Matcher matcher = pattern.matcher(message);

        // List to store extracted placeholders
        List<String> placeholders = new ArrayList<>();

        // Find and extract all matches
        while (matcher.find()) {
            // Get the matched placeholder and add it to the list
            placeholders.add(matcher.group(1));
        }
        return placeholders;
    }


    public static final String mapPatternToSmsMessage(String message, JSONObject conditionJson){
        List<String> placeholders = SplitKeyMessageDataModel(message);
        List<String> outputValues = new ArrayList<String>();
        System.out.println("placeholders:"+placeholders.size());
        for (String placeholder : placeholders){
            System.out.println("placeholder:"+placeholder);
            String valueMessage = getValueMessageDataModel(conditionJson, placeholder);
            outputValues.add(valueMessage);
        }

        // Replace each placeholder in the message with its corresponding value
        for (int i = 0; i < placeholders.size(); i++) {
            message = message.replace("{" + placeholders.get(i) + "}", outputValues.get(i));
        }

        return message;

    }

    public static String removeLeadingZero(String input) {
        if (input != null && input.startsWith("0")) {
            return input.substring(1);
        }
        return input;
    }


}
