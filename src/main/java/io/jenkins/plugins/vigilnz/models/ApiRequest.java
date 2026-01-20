package io.jenkins.plugins.vigilnz.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiRequest {

    private String repoUrl;
    private String projectName;
    private ScanContext scanContext;

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

    // Nested classes
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScanContext implements Describable<ScanContext> {
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

        @Extension
        public static class DescriptorImpl extends Descriptor<ScanContext> {
            @Nonnull
            @Override
            public String getDisplayName() {
                return "DAST Scan Context";
            }
        }
    }
}
