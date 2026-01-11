package io.jenkins.plugins.vigilnz.ui;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.vigilnz.api.ApiService;
import io.jenkins.plugins.vigilnz.credentials.TokenCredentials;
import io.jenkins.plugins.vigilnz.models.ApiResponse;
import io.jenkins.plugins.vigilnz.utils.VigilnzConfig;
import java.io.IOException;
import java.util.List;
import jenkins.model.RunAction2;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class ScanResultAction implements RunAction2 {
    private ApiResponse response;
    private transient Run<?, ?> run;

    public ScanResultAction(String scanSummary, String apiKeyId) throws JsonProcessingException {
        // Convert JSON string to ApiResponse
        ObjectMapper mapper = new ObjectMapper();
        ApiResponse apiResponse;
        apiResponse = mapper.readValue(scanSummary, ApiResponse.class);
        apiResponse.setApiKey(apiKeyId);
        setResponse(apiResponse);
        this.response = apiResponse;
    }

    public boolean getIsScanCompleted() {
        try {
            JSONObject payload = new JSONObject();
            payload.put("scanDetails", response.getScanInfo());
            payload.put("resultMethod", true);

            TokenCredentials creds =
                    CredentialsProvider.findCredentialById(response.getApiKey(), TokenCredentials.class, run);

            if (creds == null) {
                return false;
            }
            VigilnzConfig.setBaseUrl(creds.getEnvironment());
            String apiResult =
                    ApiService.fetchScanResults(creds.getToken().getPlainText(), payload, TaskListener.NULL, true);

            ObjectMapper mapper = new ObjectMapper();
            ApiResponse apiResponse;
            apiResponse = mapper.readValue(apiResult, ApiResponse.class);
            apiResponse.setApiKey(response.getApiKey());
            // Update response first
            setResponse(apiResponse);
            // Checking if the result is completed or not
            boolean allCompleted = isAllCompleted();
            apiResponse.setAllScanCompleted(allCompleted);

            return allCompleted;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isAllCompleted() {
        List<ApiResponse.ScanResults> scanResultsList = response.getScanResults();
        List<ApiResponse.ScanInfo> scanInfoList = response.getScanInfo();

        if (scanResultsList == null || scanResultsList.isEmpty()) {
            return false; // nothing to check
        }

        for (ApiResponse.ScanInfo scanInfo : scanInfoList) {
            for (ApiResponse.ScanResults scanResults : scanResultsList) {
                if (scanInfo.getScanTargetId().equals(scanResults.getScanTargetId())
                        && scanInfo.getScanType().equalsIgnoreCase(scanResults.getScanType())) {

                    if (!"COMPLETED".equalsIgnoreCase(scanResults.getStatus())) {
                        return false; // found one not completed → immediately return false
                    }
                }
            }
        }
        return true; // only reached if all matched results were completed
    }

    public String getTotalFindingsBySeverity(String severity) {
        if (response.getScanResults() == null) return "0";

        int value = response.getScanResults().stream()
                .mapToInt(res -> switch (severity.toLowerCase()) {
                    case "critical" -> res.getDetails().getCriticalFindings();
                    case "high" -> res.getDetails().getHighFindings();
                    case "medium" -> res.getDetails().getMediumFindings();
                    case "low" -> res.getDetails().getLowFindings();
                    default -> 0;
                })
                .sum();
        return String.valueOf(value);
    }

    @Override
    public String getIconFileName() {
        //        return "clipboard.png"; // or a custom icon
        return "symbol-reader-outline plugin-ionicons-api"; // or a custom icon
    }

    public String getSiteUrl(String res) {
        String siteUrl = VigilnzConfig.getBaseSiteUrl();
        return switch (res.toLowerCase()) {
            case "secret" -> siteUrl + "/secret-scan";
            case "iac" -> siteUrl + "/iac-scan";
            case "cve", "sca" -> siteUrl + "/sca";
            default -> siteUrl + "/" + res.toLowerCase();
        };
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

    public void setResponse(ApiResponse response) {
        this.response = response;
    }

    public Run<?, ?> getRun() {
        return run;
    }

    @Override
    public void onAttached(Run<?, ?> r) {
        this.run = r;
    }

    @Override
    public void onLoad(Run<?, ?> r) {
        this.run = r;
    }
}
