package com.nt.sms_module_worker.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
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

    public static final Timestamp convertTimeStampDataModel(String input){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(input, formatter);
        Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
        Timestamp dataTime = Timestamp.from(instant);
        return dataTime;
    }
}
