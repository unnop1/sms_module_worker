package com.nt.sms_module_worker.log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nt.sms_module_worker.model.dao.pdpa.consent.ConsentResp;

import java.util.logging.Level;


public class LogFile {

	public static String dateFolderName() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("MMyyyy");
        return df.format(date);
    }

	public static void logMessage(String className, String path, String phoneNumber, ConsentResp messageLog) {
        Logger logger = Logger.getLogger(className);

        try {
            
            // Use JBoss data directory
            String jbossDataDir = "/data/logs/pdpa/";
            
            String pathLog = jbossDataDir + "/" + path + "/";
            // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String fileName = phoneNumber + ".json";

            // Ensure directory exists, create if it doesn't
            File dir = new File(pathLog);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + pathLog);
                }
            }

            FileHandler fileHandler = new FileHandler(pathLog + "/" + fileName, true);
            fileHandler.setFormatter(new PlainTextFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Prevents logging to console
            logger.setLevel(Level.INFO);
            fileHandler.setLevel(Level.INFO);

            // Convert log entry to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Optional: for pretty print
            String logEntryJson = objectMapper.writeValueAsString(messageLog);

            // Append log entry to file
            File file = new File(pathLog + "/" + fileName);
            objectMapper.writeValue(file, logEntryJson);


            // Close the handler to ensure the log is written
            fileHandler.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error creating directory or file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("General error: " + e.getMessage());
        }
    }

    public static void logMessageTest(String className, String path, String messageLog) {
        Logger logger = Logger.getLogger(className);

        try {
            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("MMyyyy");
            
            // Use JBoss data directory
            String jbossDataDir = "data";
            
            String pathLog = jbossDataDir + "/" + path + "/";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String fileName = dateFormat.format(date) + ".text";

            // Ensure directory exists, create if it doesn't
            File dir = new File(pathLog);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + pathLog);
                }
            }

            // Configure FileHandler for log rotation
            // Here, we set a file size limit of 1MB (1 * 1024 * 1024 bytes) and a maximum of 5 log files.
            // System.out.println("save log to : " + pathLog+"/"+fileName);
            FileHandler fileHandler = new FileHandler(pathLog + "/" + fileName, 1024 * 1024, 5, true);
            fileHandler.setFormatter(new PlainTextFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Prevents logging to console
            logger.setLevel(Level.INFO);
            fileHandler.setLevel(Level.INFO);
            logger.info(messageLog);


            // Close the handler to ensure the log is written
            fileHandler.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error creating directory or file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("General error: " + e.getMessage());
        }
    }

}