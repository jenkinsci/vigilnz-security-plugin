package io.jenkins.plugins.vigilnz.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
    private String message;
    private String repositoryId;
    private String gitHubUrl;
    private List<String> scanTypes;
    private int totalRequested;
    private int successfulResults;
    private int failedResults;
    private List<Result> results;

    public ApiResponse() {}

    // getters and setters

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getGitHubUrl() {
        return gitHubUrl;
    }

    public void setGitHubUrl(String gitHubUrl) {
        this.gitHubUrl = gitHubUrl;
    }

    public List<String> getScanTypes() {
        return scanTypes;
    }

    public void setScanTypes(List<String> scanTypes) {
        this.scanTypes = scanTypes;
    }

    public int getTotalRequested() {
        return totalRequested;
    }

    public void setTotalRequested(int totalRequested) {
        this.totalRequested = totalRequested;
    }

    public int getSuccessfulResults() {
        return successfulResults;
    }

    public void setSuccessfulResults(int successfulResults) {
        this.successfulResults = successfulResults;
    }

    public int getFailedResults() {
        return failedResults;
    }

    public void setFailedResults(int failedResults) {
        this.failedResults = failedResults;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    // Nested classes
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String scanType;
        private String projectName;
        private String repositoryName;
        private String totalPackages;
        private String vulnerabilities;
        private String language;
        private String status;
        private String message;

        public Result() {}

        public String getScanType() {
            return scanType;
        }

        public void setScanType(String scanType) {
            this.scanType = scanType;
        }

        public String getVulnerabilities() {
            return vulnerabilities;
        }

        public void setVulnerabilities(String vulnerabilities) {
            this.vulnerabilities = vulnerabilities;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public String getRepositoryName() {
            return repositoryName;
        }

        public void setRepositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
        }

        public String getTotalPackages() {
            return totalPackages;
        }

        public void setTotalPackages(String totalPackages) {
            this.totalPackages = totalPackages;
        }
    }
}
