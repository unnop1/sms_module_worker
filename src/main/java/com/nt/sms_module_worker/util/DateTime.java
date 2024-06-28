package com.nt.sms_module_worker.util;

import java.util.regex.Pattern;

import com.nt.sms_module_worker.log.LogFile;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTime {

    public static final Timestamp getTimeStampNow(){
        Instant instant = Instant.now();
        return Timestamp.from(instant);
    }

    public static final String getDayStr(Integer d){
        String dayStr = ""+d;
        if (d < 10){
            dayStr = "0"+d;
        }
        return dayStr;
    }

    public static final String GetMonthStr(Integer m){
        String monthStr = ""+m;
        if (m < 10){
            monthStr = "0"+m;
        }
        return monthStr;
    }

    public static final String transactionDateStr(Timestamp timestamp){
        ZonedDateTime zonedDateTime = timestamp.toInstant().atZone(ZoneId.of("UTC"));
        String year = ""+zonedDateTime.getYear();
        String month = GetMonthStr(zonedDateTime.getMonthValue());
        String day = GetMonthStr(zonedDateTime.getDayOfMonth());
        return year+month+day;
    }

    public static final LocalDateTime getRequestDateUtcNow(){
        return LocalDateTime.now();
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.ofInstant(Instant.parse(dateTimeStr), ZoneId.systemDefault());
        }
    }

    public static final LocalDateTime convertDateTime(String input) throws Exception {
        String dateFormat = "yyyy-MM-dd";
        boolean isSimpleFormat = validateDate(input, dateFormat);

        if (isSimpleFormat) {
            // input = String.format("%s %s", input, "00:00:00");
            LocalDate date = LocalDate.parse(input);
            LocalDateTime dateTime = date.atStartOfDay();
            return dateTime;
        } else if (isIsoDateTime(input)) {
            return LocalDateTime.parse(input, DateTimeFormatter.ISO_DATE_TIME);
        } else {
            throw new DateTimeParseException("Input string does not match expected formats", input, 0);
        }
    }

    
    public static final LocalDateTime convertDateTime(String input, boolean isBeginOfDay) throws Exception {
        String dateFormat = "yyyy-MM-dd";
        boolean isSimpleFormat = validateDate(input, dateFormat);
        LogFile.logMessageTest("Condition", "debug_condition","isSimpleFormat:"+isSimpleFormat);
        if (isSimpleFormat) {
            LocalDate date = LocalDate.parse(input);
            
            if (isBeginOfDay) {
                LocalDateTime dateTime = date.atStartOfDay();
                return dateTime;
            } else {
                LocalDateTime dateTime = date.atTime(LocalTime.MAX);
                return dateTime;
            }
        } else if (isIsoDateTime(input)) {
            LogFile.logMessageTest("Condition", "debug_condition","isIsoDateTime:"+isIsoDateTime(input));
            return LocalDateTime.parse(input, DateTimeFormatter.ISO_DATE_TIME);
        } else {
            LogFile.logMessageTest("Condition", "debug_condition","Input string does not match expected formats");
            throw new DateTimeParseException("Input string does not match expected formats", input, 0);
        }
    }
    
    // Placeholder method to validate the date format
    public static boolean validateDate(String dateStr, String dateFormat) {
        // Implement validation logic here
        return dateStr.matches("\\d{4}-\\d{2}-\\d{2}");
    }
    
    // Placeholder method to check if the input is in ISO date-time format
    private static boolean isIsoDateTime(String dateTimeStr) {
        try {
            Instant.parse(dateTimeStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    // Placeholder method to convert ISO date-time to custom format
    public static String convertIsoToCustomFormat(String isoDateTimeStr) {
        // Implement conversion logic here
        return isoDateTimeStr.replace("T", " ");
    }

    // public static String convertIsoToCustomFormat(String isoDateTimeString) {
    //     try {
    //         ZonedDateTime zdt = ZonedDateTime.parse(isoDateTimeString, DateTimeFormatter.ISO_DATE_TIME);
    //         LocalDateTime ldt = zdt.toLocalDateTime();
    //         DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    //         return ldt.format(customFormatter);
    //     } catch (DateTimeParseException e) {
    //         throw new RuntimeException("Error parsing the ISO datetime string", e);
    //     }
    // }

    // public static boolean isIsoDateTime(String datetimeString) {
    //     String isoDateTimePattern = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z?$";
    //     return Pattern.matches(isoDateTimePattern, datetimeString);
    // }

    // public static boolean validateDate(String inputDate, String format) {
    //     SimpleDateFormat sdf = new SimpleDateFormat(format);
    //     sdf.setLenient(false); // Disable lenient parsing (strict parsing)
    //     try {
    //         sdf.parse(inputDate); // Try to parse the input date
    //         return true; // If parsing succeeds, return true
    //     } catch (ParseException e) {
    //         return false; // If parsing fails, return false
    //     }
    // }
    public static final Boolean isCurrentTimeInRange(String startInput, String endInput) {
        // Parse the start and end times
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime startTime = LocalTime.parse(startInput, formatter);
        LocalTime endTime = LocalTime.parse(endInput, formatter);

        // Get the current time
        LocalTime now = LocalTime.now();

        // Check if the current time is within the range
        if (startTime.isBefore(endTime)) {
            // If start time is before end time (normal case)
            return now.isAfter(startTime) && now.isBefore(endTime);
        } else {
            // If start time is after end time (range spans midnight)
            return now.isAfter(startTime) || now.isBefore(endTime);
        }
    }
}
