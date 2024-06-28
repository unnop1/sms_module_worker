package com.nt.sms_module_worker.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.sms_module_worker.model.dao.pdpa.login.RefreshTokenResp;
import com.nt.sms_module_worker.log.LogFile;
import com.nt.sms_module_worker.model.dao.pdpa.consent.ConsentResp;

public class PDPAClient {
    private String host;

    public PDPAClient(String host){
        this.host = host;
    }

    public RefreshTokenResp refreshToken(String refreshToken)throws Exception {
        URL url = new URL(String.format("https://%s/portal/sdk/token", host));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        // Create JSON body
        String jsonInputString = "{\"refreshToken\": \"" + refreshToken + "\"}";

        // Write JSON input to the connection's output stream
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();

        RefreshTokenResp respData = null;

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                // Parse JSON response into MetricsResp object using ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();
                String responseStr = response.toString();

                LogFile.logMessageTest("KafkaConsumerService", "debug_pdpa", responseStr);
                if (!responseStr.isBlank()) {
                    respData = objectMapper.readValue(responseStr, RefreshTokenResp.class);
                }
            }
        } else {
            // Handle non-OK response code
            LogFile.logMessageTest("KafkaConsumerService", "error_pdpa", "HTTP error code: " + responseCode);
        }

        connection.disconnect();
        return respData;
    }


    public ConsentResp GetConsentPDPAByPhoneNumber(String consentID,String purposeID , String phoneNumber, String token){
        ConsentResp respData = new ConsentResp();
        HttpURLConnection connection = null;

        try {
            URL url = new URL(String.format(
                    "https://%s/portal/sdk/%s/datasubject?identify=%s&Checkconsent=True&purposeID=%s",
                    host,
                    consentID,
                    phoneNumber,
                    purposeID
            ));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Set the Authorization header
            connection.setRequestProperty("Authorization", token);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    // Parse JSON response into ConsentResp object using ObjectMapper
                    ObjectMapper objectMapper = new ObjectMapper();
                    String responseStr = response.toString();
                    if (!responseStr.isBlank()) {
                        respData = objectMapper.readValue(responseStr, ConsentResp.class);
                    }
                }
            } else {
                // Handle non-OK response code
                LogFile.logMessageTest("ConsentService", "error_pdpa", "HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogFile.logMessageTest("ConsentService", "error_pdpa", "Exception: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return respData;
    }
}
