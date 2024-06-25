package com.nt.sms_module_worker.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.sms_module_worker.model.dao.pdpa.login.RefreshTokenResp;
import com.nt.sms_module_worker.model.dao.pdpa.consent.ConsentResp;

public class PDPAClient {
    private String host;

    public PDPAClient(String host){
        this.host = host;
    }

    public RefreshTokenResp refreshToken(String refreshToken){
        RefreshTokenResp respData = null;
        try {
            URL url = new URL(String.format(
                    "https://%s/portal/sdk/token",
                    host
                )
            );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            // Create JSON body
            String jsonInputString = "{\"refreshToken\": \"" + refreshToken + "\"}";

            // Write JSON input to the connection's output stream
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse JSON response into MetricsResp object using ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();
                // System.out.println("offerings: \"" + response.toString()+"\"");
                if (response.toString().isBlank()){
                    return null;
                }
                respData = objectMapper.readValue(response.toString(), RefreshTokenResp.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respData;
    }


    public ConsentResp GetConsentPDPAByPhoneNumber(String consentID,String purposeID , String phoneNumber, String token){
        ConsentResp respData = null;
        try {
            URL url = new URL(String.format(
                    "https://%s/portal/sdk/%s/datasubject?identify=%s&Checkconsent=True&purposeID=%s",
                    host,
                    consentID,
                    phoneNumber,
                    purposeID
                )
            );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Set the Authorization header
            connection.setRequestProperty("Authorization", token);

            int responseCode = connection.getResponseCode();
            

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse JSON response into MetricsResp object using ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();
                // System.out.println("offerings: \"" + response.toString()+"\"");
                if (response.toString().isBlank()){
                    return null;
                }
                respData = objectMapper.readValue(response.toString(), ConsentResp.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respData;
    }
}
