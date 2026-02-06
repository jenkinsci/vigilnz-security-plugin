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
import net.sf.json.JSONObject;
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
    // DAST Scan Fields
    private String targetSiteUrl;
    private String dastScanType;
    // Container Scan Fields
    private String imageName;
    private String registryProvider;
    private String authType;

    private String dockerUsername;
    private String registryType;
    private String dockerPassword;
    private String registryUrl;
    private String accessToken;

    private String googleRegistryUrl;
    private String dockerAuthMethod;
    private String awsRegistryType;
    private String ecrAccessToken;
    private String gitRegistryType;
    private String gitHubAccessToken;
    private String googleRegistryType;
    private String googleAuthenticateFields;
    private String googleAccessToken;

    private String quayAuthenticateFields;
    private String quayUsername;
    private String quayPassword;
    private String quayAccessToken;

    private String azureRegistryType;
    private String azureRegistryUrl;
    private String azureAuthenticateFields;
    private String azureUsername;
    private String azurePassword;
    private String azureAccessToken;

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

    public JSONObject getCredentialData() {

        JSONObject json = new JSONObject();
        // Send scan types as array
        if (authType.equalsIgnoreCase("token")) {
            if (registryProvider.equalsIgnoreCase("aws-ecr")) {
                json.put("token", ecrAccessToken);
            } else if (registryProvider.equalsIgnoreCase("github") || registryProvider.equalsIgnoreCase("gitlab")) {
                json.put("token", gitHubAccessToken);
            } else if (registryProvider.equalsIgnoreCase("google")) {
                json.put("token", googleAccessToken);
            } else if (registryProvider.equalsIgnoreCase("azure")) {
                json.put("token", accessToken);
            } else if (registryProvider.equalsIgnoreCase("quay")) {
                json.put("token", quayAccessToken);
            }
        } else if (authType.equalsIgnoreCase("username-password")) {
            if (registryProvider.equalsIgnoreCase("quay")) {
                json.put("username", quayUsername);
                json.put("password", quayPassword);
            } else {
                json.put("username", dockerUsername);
                json.put("password", dockerPassword);
            }
        }
        return json;
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
        //        listener.getLogger().println("Selected Scan Types:1 " + targetSiteUrl);
        String result = "";

        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setProjectName(projectName);
        apiRequest.setScanTypes(scanTypes);

        ApiRequest.ScanContext dastScanContext = new ApiRequest.ScanContext("", "");
        if (dastScan) {
            dastScanContext.setTargetUrl(targetSiteUrl);
            dastScanContext.setDastScanType(dastScanType);
            apiRequest.setScanContext(dastScanContext);
        }

        ApiRequest.ContainerScanContext containerScanInfo = new ApiRequest.ContainerScanContext("");
        if (containerScan) {
            containerScanInfo.setImageName(imageName);
            containerScanInfo.setRegistryProvider(registryProvider);

            if (registryProvider.equalsIgnoreCase("aws-ecr")
                    || registryProvider.equalsIgnoreCase("azure")
                    || registryProvider.equalsIgnoreCase("google")) {
                if (registryProvider.equalsIgnoreCase("google")) {
                    containerScanInfo.setCustomRegistryUrl(googleRegistryUrl);
                    containerScanInfo.setRegistrySubType(googleRegistryType);
                } else if (registryProvider.equalsIgnoreCase("aws-ecr")) {
                    containerScanInfo.setCustomRegistryUrl(registryUrl);
                    containerScanInfo.setRegistrySubType(awsRegistryType);
                } else if (registryProvider.equalsIgnoreCase("azure")) {
                    containerScanInfo.setCustomRegistryUrl(azureRegistryUrl);
                    containerScanInfo.setRegistrySubType(azureRegistryType);
                }
            }

            containerScanInfo.setCredentials(getCredentialData());
            if (authType.equalsIgnoreCase("ecr-public")) {
                containerScanInfo.setAuthMethod("none");
            } else {
                containerScanInfo.setAuthMethod(authType);
            }
            apiRequest.setContainerScanContext(containerScanInfo);
        }

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

    public String getImageName() {
        return imageName;
    }

    @DataBoundSetter
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getRegistryProvider() {
        return registryProvider;
    }

    @DataBoundSetter
    public void setRegistryProvider(String registryProvider) {
        this.registryProvider = registryProvider;
    }

    public String isAuthType() {
        return authType;
    }

    @DataBoundSetter
    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getDockerUsername() {
        return dockerUsername;
    }

    @DataBoundSetter
    public void setDockerUsername(String dockerUsername) {
        this.dockerUsername = dockerUsername;
    }

    public String getDockerPassword() {
        return dockerPassword;
    }

    @DataBoundSetter
    public void setDockerPassword(String dockerPassword) {
        this.dockerPassword = dockerPassword;
    }

    public String getRegistryUrl() {
        return registryUrl;
    }

    @DataBoundSetter
    public void setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @DataBoundSetter
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRegistryType() {
        return registryType;
    }

    @DataBoundSetter
    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public String getDockerAuthMethod() {
        return dockerAuthMethod;
    }

    @DataBoundSetter
    public void setDockerAuthMethod(String dockerAuthMethod) {
        this.dockerAuthMethod = dockerAuthMethod;
    }

    public String getAwsRegistryType() {
        return awsRegistryType;
    }

    @DataBoundSetter
    public void setAwsRegistryType(String awsRegistryType) {
        this.awsRegistryType = awsRegistryType;
    }

    public String getEcrAccessToken() {
        return ecrAccessToken;
    }

    @DataBoundSetter
    public void setEcrAccessToken(String ecrAccessToken) {
        this.ecrAccessToken = ecrAccessToken;
    }

    public String getGitRegistryType() {
        return gitRegistryType;
    }

    @DataBoundSetter
    public void setGitRegistryType(String gitRegistryType) {
        this.gitRegistryType = gitRegistryType;
    }

    public String getGitHubAccessToken() {
        return gitHubAccessToken;
    }

    @DataBoundSetter
    public void setGitHubAccessToken(String gitHubAccessToken) {
        this.gitHubAccessToken = gitHubAccessToken;
    }

    public String getGoogleRegistryType() {
        return googleRegistryType;
    }

    @DataBoundSetter
    public void setGoogleRegistryType(String googleRegistryType) {
        this.googleRegistryType = googleRegistryType;
    }

    public String getGoogleAuthenticateFields() {
        return googleAuthenticateFields;
    }

    @DataBoundSetter
    public void setGoogleAuthenticateFields(String googleAuthenticateFields) {
        this.googleAuthenticateFields = googleAuthenticateFields;
    }

    public String getGoogleAccessToken() {
        return googleAccessToken;
    }

    @DataBoundSetter
    public void setGoogleAccessToken(String googleAccessToken) {
        this.googleAccessToken = googleAccessToken;
    }

    public String getGoogleRegistryUrl() {
        return googleRegistryUrl;
    }

    @DataBoundSetter
    public void setGoogleRegistryUrl(String googleRegistryUrl) {
        this.googleRegistryUrl = googleRegistryUrl;
    }

    public String getQuayAuthenticateFields() {
        return quayAuthenticateFields;
    }

    @DataBoundSetter
    public void setQuayAuthenticateFields(String quayAuthenticateFields) {
        this.quayAuthenticateFields = quayAuthenticateFields;
    }

    public String getQuayUsername() {
        return quayUsername;
    }

    @DataBoundSetter
    public void setQuayUsername(String quayUsername) {
        this.quayUsername = quayUsername;
    }

    public String getQuayPassword() {
        return quayPassword;
    }

    @DataBoundSetter
    public void setQuayPassword(String quayPassword) {
        this.quayPassword = quayPassword;
    }

    public String getQuayAccessToken() {
        return quayAccessToken;
    }

    @DataBoundSetter
    public void setQuayAccessToken(String quayAccessToken) {
        this.quayAccessToken = quayAccessToken;
    }

    public String getAzureRegistryType() {
        return azureRegistryType;
    }

    @DataBoundSetter
    public void setAzureRegistryType(String azureRegistryType) {
        this.azureRegistryType = azureRegistryType;
    }

    public String getAzureRegistryUrl() {
        return azureRegistryUrl;
    }

    @DataBoundSetter
    public void setAzureRegistryUrl(String azureRegistryUrl) {
        this.azureRegistryUrl = azureRegistryUrl;
    }

    public String getAzureAuthenticateFields() {
        return azureAuthenticateFields;
    }

    @DataBoundSetter
    public void setAzureAuthenticateFields(String azureAuthenticateFields) {
        this.azureAuthenticateFields = azureAuthenticateFields;
    }

    public String getAzureUsername() {
        return azureUsername;
    }

    @DataBoundSetter
    public void setAzureUsername(String azureUsername) {
        this.azureUsername = azureUsername;
    }

    public String getAzurePassword() {
        return azurePassword;
    }

    @DataBoundSetter
    public void setAzurePassword(String azurePassword) {
        this.azurePassword = azurePassword;
    }

    public String getAzureAccessToken() {
        return azureAccessToken;
    }

    @DataBoundSetter
    public void setAzureAccessToken(String azureAccessToken) {
        this.azureAccessToken = azureAccessToken;
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

        @POST
        public ListBoxModel doFillAwsRegistryTypeItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("ECR Public", "ecr-public");
            items.add("ECR Private", "ecr-private");
            return items;
        }

        @POST
        public ListBoxModel doFillDockerAuthMethodItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("None (Public Images)", "none");
            items.add("Username/Password", "username-password");
            return items;
        }

        @POST
        public ListBoxModel doFillQuayAuthenticateFieldsItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("None (Public Images)", "none");
            items.add("Username/Password", "username-password");
            items.add("Access Token", "token");
            return items;
        }

        @POST
        public ListBoxModel doFillAzureAuthenticateFieldsItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Username/Password", "username-password");
            items.add("Access Token", "token");
            return items;
        }

        @POST
        public ListBoxModel doFillAzureRegistryTypeItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Microsoft Container Registry", "mcr");
            items.add("Private Azure CR", "acr-private");
            return items;
        }

        @POST
        public ListBoxModel doFillGitRegistryTypeItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("None (Public Images)", "none");
            items.add("Access Token", "token");
            return items;
        }

        @POST
        public ListBoxModel doFillGoogleRegistryTypeItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Container Registry", "gcr");
            items.add("Artifact Registry", "artifact-registry");
            return items;
        }

        @POST
        public ListBoxModel doFillGoogleAuthenticateFieldsItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("None (Public Images)", "none");
            items.add("Access Token", "token");
            return items;
        }

        @POST
        public ListBoxModel doFillRegistryProviderItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Docker Hub", "dockerhub");
            items.add("AWS ECR", "aws-ecr");
            items.add("GitHub Container Registry", "github");
            items.add("GitLab Container Registry", "gitlab");
            items.add("Google Container Registry", "google");
            items.add("Azure Container Registry", "azure");
            items.add("Quay.io", "quay");
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
        public FormValidation doCheckScanTypes(@AncestorInPath Item project, @QueryParameter String value) {
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

        public FormValidation doCheckImageName(@QueryParameter String imageName) {
            if (imageName == null || imageName.isEmpty()) {
                return FormValidation.error("Image name is required");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckTargetSiteUrl(@QueryParameter String targetSiteUrl) {
            if (targetSiteUrl == null || targetSiteUrl.isEmpty()) {
                return FormValidation.error("Target Url is required");
            }

            return FormValidation.ok();
        }
    }
}
