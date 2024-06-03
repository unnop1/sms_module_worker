package com.nt.sms_module_worker.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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

    public static final Timestamp getRequestDateUtcNow(){
        // Get the current instant
        Instant now = Instant.now();

        // Convert to UTC time zone
        LocalDateTime utcDateTime = LocalDateTime.ofInstant(now, ZoneId.of("UTC"));

        // Define the desired format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Format the UTC time to the desired format
        String formattedDateTime = utcDateTime.format(formatter);

        // Parse the string to a LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.parse(formattedDateTime, formatter);

        // Convert LocalDateTime to java.sql.Timestamp
        Timestamp sqlTimestamp = Timestamp.valueOf(localDateTime);

        return sqlTimestamp;
    }

    public static final Timestamp convertTimeStampDataModel(String input){

        String simpleFormat = "yyyy-MM-dd";
        String timeStampFormat = "yyyy-MM-dd HH:mm:ss";
        boolean isSimpleFormat = validateDate(input, simpleFormat);
        if (isSimpleFormat){
            input = String.format("%s %s", input, "00:00:00");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeStampFormat);
        LocalDateTime dateTime = LocalDateTime.parse(input, formatter);
        Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
        Timestamp dataTime = Timestamp.from(instant);
        return dataTime;
    }

    public static boolean validateDate(String inputDate, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false); // Disable lenient parsing (strict parsing)

        try {
            sdf.parse(inputDate); // Try to parse the input date
            return true; // If parsing succeeds, return true
        } catch (ParseException e) {
            return false; // If parsing fails, return false
        }
    }

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
