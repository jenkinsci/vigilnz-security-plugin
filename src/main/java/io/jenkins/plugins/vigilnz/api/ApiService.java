package io.jenkins.plugins.vigilnz.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.model.TaskListener;
import io.jenkins.plugins.vigilnz.models.ApiRequest;
import io.jenkins.plugins.vigilnz.models.ApiResponse;
import io.jenkins.plugins.vigilnz.models.AuthResponse;
import io.jenkins.plugins.vigilnz.utils.VigilnzConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.sf.json.JSONObject;

public class ApiService {

    /**
     * Authenticate with API key and get access token
     */
    public static AuthResponse authenticate(String apiKey, TaskListener listener) {
        try {
            URL url = new URL(VigilnzConfig.getAuthUrl());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            //            listener.getLogger().println("Triggering authentication API call " +
            // VigilnzConfig.getBaseUrl());

            JSONObject json = new JSONObject();
            json.put("apiKey", apiKey);
            String body = json.toString();

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                // Read error response
                try (BufferedReader reader =
                        new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    listener.error("Authentication failed: " + errorResponse);
                }
                return null;
            }

            // Read success response
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse response
            JSONObject responseJson = JSONObject.fromObject(response.toString());
            String accessToken = responseJson.getString("access_token");
            String refreshToken = responseJson.optString("refresh_token", "");
            long expiresIn = responseJson.optLong("expires_in", 3600);
            String tokenType = responseJson.optString("token_type", "Bearer");

            return new AuthResponse(accessToken, refreshToken, expiresIn, tokenType);

        } catch (Exception e) {
            listener.error("Authentication error: " + e.getMessage());
            return null;
        }
    }

    public static String fetchScanResults(String token, JSONObject scanInfo, TaskListener listener, boolean isApiKey) {
        try {

            if (isApiKey) {
                try {
                    // Step 1: Authenticate and get access token
                    AuthResponse authResponse = authenticate(token, listener);
                    if (authResponse == null || authResponse.getAccessToken() == null) {
                        listener.error("Failed to authenticate. Cannot proceed with scan.");
                        return null;
                    }
                    token = authResponse.getTokenType() + " " + authResponse.getAccessToken();
                } catch (Exception e) {
                    listener.getLogger().println("Credential Error:" + e);
                }
            }
            // Step 1: Call multi-scan API with resultMethod:true
            URL url = new URL(VigilnzConfig.getScanUrl());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String body = scanInfo.toString();
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            //            listener.getLogger().println("API Succeed  " + response);
            return response.toString();

        } catch (Exception e) {
            listener.getLogger().println("API Error (fetch results): " + e.getMessage());
            return null;
        }
    }

    public static String triggerScan(String token, ApiRequest requestData, EnvVars env, TaskListener listener) {
        try {
            // Step 1: Authenticate and get access token
            AuthResponse authResponse = authenticate(token, listener);
            if (authResponse == null || authResponse.getAccessToken() == null) {
                listener.error("Failed to authenticate. Cannot proceed with scan.");
                return null;
            }

            String accessToken = authResponse.getAccessToken();
            String tokenType = authResponse.getTokenType();

            String projectName = requestData.getProjectName();
            List<String> scanTypes = requestData.getScanTypes();

            listener.getLogger().println("Using access token for multi-scan API call...");

            // Step 2: Call multi-scan API with access token
            URL url = new URL(VigilnzConfig.getScanUrl());

            String branch = env.get("GIT_BRANCH");
            String repoUrl = env.get("GIT_URL");
            String commit = env.get("GIT_COMMIT");

            listener.getLogger().println("Branch: " + branch);
            listener.getLogger().println("Repo URL: " + repoUrl);
            listener.getLogger().println("Commit: " + commit);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            // Use Bearer token authentication
            conn.setRequestProperty("Authorization", tokenType + " " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            //            listener.getLogger().println("scanTypes -- : " + scanTypes);
            // Validate scan types
            if (scanTypes == null || scanTypes.isEmpty()) {
                listener.error("No scan types selected. At least one scan type is required.");
                return null;
            }

            JSONObject json = new JSONObject();
            // Send scan types as array
            json.put("scanTypes", scanTypes);
            json.put("gitRepoUrl", repoUrl);
            json.put("branch", branch);

            if (scanTypes.stream().anyMatch(s -> s.equalsIgnoreCase("dast"))) {
                if (requestData.getScanContext() != null) {
                    json.put("scanContext", requestData.getScanContext());
                } else {
                    listener.getLogger().println("No DAST context set, skipping...");
                }
            }

            if (scanTypes.stream().anyMatch(s -> s.equalsIgnoreCase("container"))) {
                if (requestData.getContainerScanContext() != null) {
                    json.put("containerScanContext", requestData.getContainerScanContext());
                } else {
                    listener.getLogger().println("No CONTAINER SCAN context set, skipping...");
                }
            }

            //            listener.getLogger().println("No ++" + requestData.getContainerScanContext());
            // Optional fields
            if (projectName != null && !projectName.trim().isEmpty()) {
                json.put("projectName", projectName);
            }

            String body = json.toString();

            //            listener.getLogger().println("Payload: " + body);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            StringBuilder response = new StringBuilder();
            // Print the response to output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                    //                    listener.getLogger().println("API Body: ===--- " + line);
                }
                //                listener.getLogger().println("API Response Body: " + response);
            }

            if (responseCode >= 400) {
                listener.error("API call failed with HTTP " + responseCode);
                throw new AbortException("Scan API failed: " + response);
            }

            String accessTokenValue = tokenType + " " + accessToken;

            // Convert JSON string to ApiResponse
            ObjectMapper mapper = new ObjectMapper();
            ApiResponse apiResponse;
            apiResponse = mapper.readValue(response.toString(), ApiResponse.class);
            ApiResponse scanResponse = apiResponse;

            JSONObject scanInfo = new JSONObject();
            scanInfo.put("scanDetails", scanResponse.getScanInfo());
            scanInfo.put("resultMethod", true);

            // return response.toString();
            return fetchScanResults(accessTokenValue, scanInfo, listener, false);

        } catch (IOException e) {
            listener.getLogger().println("API Error: " + e.getMessage());
            e.printStackTrace(listener.getLogger());
            return null;
        }
    }
}
