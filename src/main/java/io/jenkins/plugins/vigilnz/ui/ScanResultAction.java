package io.jenkins.plugins.vigilnz.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.Action;
import io.jenkins.plugins.vigilnz.models.ApiResponse;

public class ScanResultAction implements Action {
    private final ApiResponse response;

    public ScanResultAction(String scanSummary) throws JsonProcessingException {

        // Convert JSON string to ApiResponse
        ObjectMapper mapper = new ObjectMapper();
        ApiResponse apiResponse;
        apiResponse = mapper.readValue(scanSummary, ApiResponse.class);
        this.response = apiResponse;
    }

    @Override
    public String getIconFileName() {
        //        return "clipboard.png"; // or a custom icon
        return "symbol-reader-outline plugin-ionicons-api"; // or a custom icon
    }

    @Override
    public String getDisplayName() {
        return "Vigilnz Scan Results";
    }

    @Override
    public String getUrlName() {
        return "vigilnzResult"; // URL path under build/job
    }

    public ApiResponse getResponse() {
        return response;
    }
}
