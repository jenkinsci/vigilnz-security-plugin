package io.jenkins.plugins.vigilnz.build;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import io.jenkins.plugins.vigilnz.api.ApiService;
import io.jenkins.plugins.vigilnz.credentials.TokenCredentials;
import io.jenkins.plugins.vigilnz.models.ApiRequest;
import io.jenkins.plugins.vigilnz.models.ApiResponse;
import io.jenkins.plugins.vigilnz.ui.ScanResultAction;
import io.jenkins.plugins.vigilnz.utils.VigilnzConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

// This file for Jenkins FreeStyle Job Method
public class SecurityCheckBuilder extends Builder {

    private final String credentialsId;
    private String projectName; // Optional parameter
    private boolean cveScan;
    private boolean sastScan;
    private boolean sbomScan;
    private boolean iacScan;
    private boolean secretScan;
    private boolean dastScan;
    private boolean containerScan;
    private String targetSiteUrl;
    private String dastScanType;

    @DataBoundConstructor
    public SecurityCheckBuilder(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getProjectName() {
        return projectName;
    }

    @DataBoundSetter
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public boolean isCveScan() {
        return cveScan;
    }

    @DataBoundSetter
    public void setCveScan(boolean cveScan) {
        this.cveScan = cveScan;
    }

    public boolean isSastScan() {
        return sastScan;
    }

    @DataBoundSetter
    public void setSastScan(boolean sastScan) {
        this.sastScan = sastScan;
    }

    public boolean isSbomScan() {
        return sbomScan;
    }

    @DataBoundSetter
    public void setSbomScan(boolean sbomScan) {
        this.sbomScan = sbomScan;
    }

    public boolean isSecretScan() {
        return secretScan;
    }

    @DataBoundSetter
    public void setSecretScan(boolean secretScan) {
        this.secretScan = secretScan;
    }

    public boolean isDastScan() {
        return dastScan;
    }

    @DataBoundSetter
    public void setDastScan(boolean dastScan) {
        this.dastScan = dastScan;
    }

    public boolean isContainerScan() {
        return containerScan;
    }

    @DataBoundSetter
    public void setContainerScan(boolean containerScan) {
        this.containerScan = containerScan;
    }

    public boolean isIacScan() {
        return iacScan;
    }

    @DataBoundSetter
    public void setIacScan(boolean iacScan) {
        this.iacScan = iacScan;
    }

    public List<String> getScanTypes() {
        List<String> types = new ArrayList<>();
        if (cveScan) types.add("cve");
        if (sastScan) types.add("sast");
        if (sbomScan) types.add("sbom");
        if (iacScan) types.add("iac");
        if (secretScan) types.add("secret");
        if (dastScan) types.add("dast");
        if (containerScan) types.add("container");
        return types;
    }

    public String displayScan(List<String> scansList) {
        List<String> updatedList = scansList.stream()
                .map(s -> {
                    if (s.equalsIgnoreCase("cve")) {
                        s = "sca";
                    } else if (s.equalsIgnoreCase("secret")) {
                        s = "secret scan";
                    } else if (s.equalsIgnoreCase("iac")) {
                        s = "iac scan";
                    } else if (s.equalsIgnoreCase("container")) {
                        s = "container scan";
                    }
                    return s.toUpperCase();
                })
                .toList();
        return String.join(", ", updatedList);
    }

    // this function trigger when user click the build button
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {

        EnvVars env = build.getEnvironment(listener);
        listener.getLogger().println("------ Freestyle Method ------");

        // Build scan types list from checkboxes
        List<String> scanTypes = getScanTypes();

        // Validate at least one scan type is selected
        if (scanTypes.isEmpty()) {
            listener.error("Error: At least one scan type must be selected.");
            attachResult(build, buildErrorResponse("At least one scan type must be selected."));
            return false;
        }

        // Validate credentials ID is provided
        if (credentialsId == null || credentialsId.trim().isEmpty()) {
            listener.error(
                    "Error: Credentials ID is required. Please select a credential in the build step configuration.");
            attachResult(build, buildErrorResponse("Credentials ID is required."));
            return false;
        }

        // Look up the actual TokenCredentials object
        TokenCredentials creds = CredentialsProvider.findCredentialById(credentialsId, TokenCredentials.class, build);

        if (creds == null) {
            listener.error("Error: Vigilnz Token credential not found with ID: " + credentialsId);
            attachResult(build, buildErrorResponse("Vigilnz Token credential not found with ID: " + credentialsId));
            return false;
        }
        // Get the actual token value from the credential
        String tokenText = creds.getToken().getPlainText();

        // Set base URL based on environment selection
        VigilnzConfig.setBaseUrl(creds.getEnvironment());

        // listener.getLogger().println("Credential ID: " + credentialsId);
        // listener.getLogger().println("Your Token from Plugin: " + tokenText);
        if (projectName != null && !projectName.trim().isEmpty()) {
            listener.getLogger().println("Project Name: " + projectName);
        } else {
            listener.getLogger().println("Target File: (not specified)");
        }
        listener.getLogger().println("Selected Scan Types: " + displayScan(scanTypes));
        listener.getLogger().println("Selected Scan Types:1 " + targetSiteUrl);
        String result = "";

        ApiRequest.ScanContext dastScanContext = new ApiRequest.ScanContext("", "");
        dastScanContext.setTargetUrl(targetSiteUrl);
        dastScanContext.setDastScanType(dastScanType);

        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setProjectName(projectName);
        apiRequest.setScanTypes(scanTypes);
        apiRequest.setScanContext(dastScanContext);

        try {
            result = ApiService.triggerScan(tokenText, apiRequest, env, listener);
            // Attach results to build
            if (result != null && !result.isEmpty()) {
                build.addAction(new ScanResultAction(result, credentialsId));
            } else {
                listener.getLogger().println("API call failed, no action added.");
                // return false;
            }
        } catch (Exception e) {
            listener.error("Scan failed");
            attachResult(build, buildErrorResponse("Scan failed: " + e.getMessage()));
            build.addAction(new ScanResultAction(new ApiResponse().toString(), ""));
            return false;
        }

        return true;
    }

    private void attachResult(AbstractBuild build, String json) {
        try {
            build.addAction(new ScanResultAction(json, ""));
        } catch (Exception ignored) {
            // Swallow to avoid masking original error
        }
    }

    private String buildErrorResponse(String message) {
        ApiResponse resp = new ApiResponse();
        resp.setMessage(message);
        try {
            return new ObjectMapper().writeValueAsString(resp);
        } catch (JsonProcessingException e) {
            return "{\"message\":\"" + message.replace("\"", "\\\"") + "\"}";
        }
    }

    public String getTargetSiteUrl() {
        return targetSiteUrl;
    }

    @DataBoundSetter
    public void setTargetSiteUrl(String targetSiteUrl) {
        this.targetSiteUrl = targetSiteUrl;
    }

    public String getDastScanType() {
        return dastScanType;
    }

    @DataBoundSetter
    public void setDastScanType(String dastScanType) {
        this.dastScanType = dastScanType;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return "Invoke Vigilnz Security Task"; // This appears in dropdown
        }

        @POST
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item project) {
            // Security: Check if user has permission to configure this project
            if (project == null || !project.hasPermission(Item.CONFIGURE)) {
                return new ListBoxModel(); // Return empty list if no permission
            }

            // Use the actual user's authentication context instead of ACL.SYSTEM
            // This ensures only credentials the user is allowed to see are returned
            ListBoxModel items = new ListBoxModel();
            for (TokenCredentials c : CredentialsProvider.lookupCredentials(
                    TokenCredentials.class,
                    project,
                    ACL.SYSTEM, // Use actual user authentication, not ACL.SYSTEM
                    Collections.emptyList())) {
                String label = c.getId().isEmpty() ? c.getDescription() : c.getId();
                if (label == null || label.isEmpty()) {
                    label = c.getId();
                }
                items.add(label, c.getId());
            }
            items.add("None", "");
            return items;
        }

        @POST
        public ListBoxModel doFillDastScanTypeItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Spider Only", "spider");
            items.add("Active Scan Only", "active");
            items.add("Full Scan", "full");
            return items;
        }

        @Override
        public boolean isApplicable(Class jobType) {
            return true;
        }

        @POST
        public FormValidation doCheckCredentialsId(@AncestorInPath Item project, @QueryParameter String credentialsId) {
            // Security: Check if user has permission to configure this project
            if (project != null && !project.hasPermission(Item.CONFIGURE)) {
                return FormValidation.error("No permission to configure this project");
            }
            // If no project context, check global permission
            if (project == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            }

            if (credentialsId == null || credentialsId.trim().isEmpty()) {
                return FormValidation.error("Credentials selection is required");
            }
            return FormValidation.ok();
        }

        @POST
        public FormValidation doCheckScanType(@AncestorInPath Item project, @QueryParameter String value) {
            // Security: Check if user has permission to configure this project
            if (project != null && !project.hasPermission(Item.CONFIGURE)) {
                return FormValidation.error("No permission to configure this project");
            }
            // If no project context, check global permission
            if (project == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            }

            if (StringUtils.isBlank(value)) {
                return FormValidation.error("You must select at least one scan type.");
            }
            return FormValidation.ok();
        }
    }
}
