package com.nt.sms_module_worker.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PDPAClient {
    private String host;
    private String port;
    private String accessToken="";

    public PDPAClient(String host, String port){
        this.host = host;
        this.port = port;
    }

    public String Login(String username, String password) {
        try {
            // URL for the request
            URL url = new URL(String.format(
                    "http://%s:%s/auth/login",
                    host,
                    port
                )
            );
            
            // Creating the JSON request body using String.format
            String jsonInputString = String.format("{\"username\": \"%s\", \"password\": \"%s\", \"device\": \"%s\", \"system\": \"%s\", \"browser\": \"%s\"}",
                                                    username, password);

            // Establishing the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            // Sending the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Reading the response
            int responseCode = connection.getResponseCode();
            System.out.println("responseCode login:"+responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    // Parse the response JSON to get access_token
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(response.toString());
                    accessToken = jsonNode.get("access_token").asText();

                    System.out.println("Access Token: " + accessToken);
                }
            } else {
                System.out.println("POST request not worked. Response code: " + responseCode);
            }
            return accessToken;
        } catch (Exception e) {
            e.printStackTrace();
            return accessToken;
        }
        
    }

    public JSONObject ListMetrics(){
        JSONObject metricsResp = null;
        try {
            URL url = new URL(String.format(
                    "http://%s:%s/manage_system/metrics?draw=11&order[0][dir]=DESC&order[0][name]=UPDATED_DATE",
                    host,
                    port
                )
            );
            System.out.println("metric accessToken:"+accessToken);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
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
                metricsResp = objectMapper.readValue(response.toString(), JSONObject.class);
            } else {
                System.out.println("GET request failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return metricsResp;
    }
}
