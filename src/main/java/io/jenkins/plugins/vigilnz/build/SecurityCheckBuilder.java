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
import io.jenkins.plugins.vigilnz.models.ApiResponse;
import io.jenkins.plugins.vigilnz.ui.ScanResultAction;
import java.io.IOException;
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
    private String targetFile; // Optional parameter
    private boolean cveScan;
    private boolean sastScan;
    private boolean sbomScan;

    @DataBoundConstructor
    public SecurityCheckBuilder(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getTargetFile() {
        return targetFile;
    }

    @DataBoundSetter
    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
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

    public List<String> getScanTypes() {
        List<String> types = new java.util.ArrayList<>();
        if (cveScan) types.add("cve");
        if (sastScan) types.add("sast");
        if (sbomScan) types.add("sbom");
        return types;
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

        //        listener.getLogger().println("Credential ID: " + credentialsId);
        //        listener.getLogger().println("Your Token from Plugin: " + tokenText);
        if (targetFile != null && !targetFile.trim().isEmpty()) {
            listener.getLogger().println("Target File: " + targetFile);
        } else {
            listener.getLogger().println("Target File: (not specified)");
        }
        listener.getLogger().println("Selected Scan Types: " + String.join(", ", scanTypes));
        String result = "";

        try {

            result = ApiService.triggerScan(tokenText, targetFile, scanTypes, env, listener);
            // Attach results to build
            if (result != null && !result.isEmpty()) {
                build.addAction(new ScanResultAction(result));
            } else {
                listener.getLogger().println("API call failed, no action added.");
            }
        } catch (Exception e) {
            listener.error("Scan failed");
            attachResult(build, buildErrorResponse("Scan failed: " + e.getMessage()));
            build.addAction(new ScanResultAction(new ApiResponse().toString()));
            return false;
        }

        return true;
    }

    private void attachResult(AbstractBuild build, String json) {
        try {
            build.addAction(new ScanResultAction(json));
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
