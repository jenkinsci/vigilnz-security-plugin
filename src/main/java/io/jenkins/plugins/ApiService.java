package io.jenkins.plugins;

import hudson.EnvVars;
import hudson.model.TaskListener;
import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ApiService {

    public static boolean triggerScan(String token, String targetFile, List<String> scanTypes, EnvVars env, TaskListener listener) {
        try {
            URL url = new URL("http://localhost:8000/scan-targets/multi-scan");

            String branch = env.get("GIT_BRANCH");
            String repoUrl = env.get("GIT_URL");
            String commit = env.get("GIT_COMMIT");

            listener.getLogger().println("Branch: " + branch);
            listener.getLogger().println("Repo URL: " + repoUrl);
            listener.getLogger().println("Commit: " + commit);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Cookie", token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Validate scan types
            if (scanTypes == null || scanTypes.isEmpty()) {
                listener.error("No scan types selected. At least one scan type is required.");
                return false;
            }

            JSONObject json = new JSONObject();
            // Send scan types as array
            json.put("scanTypes", scanTypes);
            json.put("project", repoUrl);
            json.put("gitRepoUrl", repoUrl);
            if (targetFile != null) json.put("targetFile", targetFile);

            String body = json.toString();

            listener.getLogger().println("Request Body: " + body);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
            }

            int responseCode = conn.getResponseCode();
            listener.getLogger().println("API Response Code: " + responseCode);

            // Print the response to output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream()))) {

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                listener.getLogger().println("API Response Body: " + response);
            }

            return responseCode == 200;

        } catch (Exception e) {
            listener.getLogger().println("API Error: " + e.getMessage());
            return false;
        }
    }

}
