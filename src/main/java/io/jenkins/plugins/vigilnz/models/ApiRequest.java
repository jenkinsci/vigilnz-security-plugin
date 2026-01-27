package io.jenkins.plugins.vigilnz.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiRequest {

    private String repoUrl;
    private String projectName;
    private ScanContext scanContext;
    private ContainerScanContext containerScanContext;
    private List<String> scanTypes;

    public ApiRequest() {}

    // getters and setters
    public List<String> getScanTypes() {
        return scanTypes;
    }

    public void setScanTypes(List<String> scanTypes) {
        this.scanTypes = scanTypes;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public ScanContext getScanContext() {
        return scanContext;
    }

    @DataBoundSetter
    public void setScanContext(ScanContext scanContext) {
        this.scanContext = scanContext;
    }

    public ContainerScanContext getContainerScanContext() {
        return containerScanContext;
    }

    @DataBoundSetter
    public void setContainerScanContext(ContainerScanContext containerScanContext) {
        this.containerScanContext = containerScanContext;
    }

    // Nested classes
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScanContext {
        private String targetUrl;
        private String dastScanType;

        @DataBoundConstructor
        public ScanContext(String targetUrl, String dastScanType) {
            this.targetUrl = targetUrl;
            this.dastScanType = dastScanType;
        }

        public String getTargetUrl() {
            return targetUrl;
        }

        @DataBoundSetter
        public void setTargetUrl(String targetUrl) {
            this.targetUrl = targetUrl;
        }

        public String getDastScanType() {
            return dastScanType;
        }

        @DataBoundSetter
        public void setDastScanType(String dastScanType) {
            this.dastScanType = dastScanType;
        }
    }

    // Nested classes
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContainerScanContext {
        private String imageName;
        private String registryProvider;
        private String registrySubType;
        private String customRegistryUrl;
        private String authMethod;
        private JSONObject credentials;

        @DataBoundConstructor
        public ContainerScanContext(String imageName) {
            this.imageName = imageName;
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

        public String getRegistrySubType() {
            return registrySubType;
        }

        @DataBoundSetter
        public void setRegistrySubType(String registrySubType) {
            this.registrySubType = registrySubType;
        }

        public String getAuthMethod() {
            return authMethod;
        }

        @DataBoundSetter
        public void setAuthMethod(String authMethod) {
            this.authMethod = authMethod;
        }

        public String getCustomRegistryUrl() {
            return customRegistryUrl;
        }

        @DataBoundSetter
        public void setCustomRegistryUrl(String customRegistryUrl) {
            this.customRegistryUrl = customRegistryUrl;
        }

        public JSONObject getCredentials() {
            return credentials;
        }

        @DataBoundSetter
        public void setCredentials(JSONObject credentials) {
            this.credentials = credentials;
        }
    }
}
