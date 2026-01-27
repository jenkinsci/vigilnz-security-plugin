package io.jenkins.plugins.vigilnz.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
    private String repositoryId;
    private String repoUrl;
    private String projectName;
    private String repoName;
    private String apiKey;
    private String message;
    private boolean isAllScanCompleted;
    private List<ScanInfo> scanInfo;
    private List<ScanResults> scanResults;
    private List<String> scanTypes;

    public ApiResponse() {}

    // getters and setters
    public boolean isAllScanCompleted() {
        return isAllScanCompleted;
    }

    public void setAllScanCompleted(boolean allScanCompleted) {
        isAllScanCompleted = allScanCompleted;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<String> getScanTypes() {
        return scanTypes;
    }

    public void setScanTypes(List<String> scanTypes) {
        this.scanTypes = scanTypes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public List<ScanInfo> getScanInfo() {
        return scanInfo;
    }

    public void setScanInfo(List<ScanInfo> scanInfo) {
        List<String> scanTypesList =
                scanInfo.stream().map(ScanInfo::getScanType).toList();
        setScanTypes(scanTypesList);
        this.scanInfo = scanInfo;
    }

    public List<ScanResults> getScanResults() {
        return scanResults;
    }

    public void setScanResults(List<ScanResults> scanResults) {
        this.scanResults = scanResults;
    }

    // Nested classes
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScanInfo {
        private String scanTargetId;
        private String scanType;

        public ScanInfo() {}

        public String getScanTargetId() {
            return scanTargetId;
        }

        public void setScanTargetId(String scanTargetId) {
            this.scanTargetId = scanTargetId;
        }

        public String getScanType() {
            return scanType;
        }

        public void setScanType(String scanType) {
            this.scanType = scanType;
        }
    }

    // Nested classes
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScanResults {
        private String scanTargetId;
        private String scanType;
        private String status;
        private int progress;
        private String branchName;
        private Details details;
        private String language;

        public ScanResults() {}

        public String getScanTargetId() {
            return scanTargetId;
        }

        public void setScanTargetId(String scanTargetId) {
            this.scanTargetId = scanTargetId;
        }

        public String getScanType() {
            return scanType;
        }

        public void setScanType(String scanType) {
            this.scanType = scanType;
        }

        public String getBranchName() {
            return branchName;
        }

        public void setBranchName(String branchName) {
            this.branchName = branchName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public Details getDetails() {
            return details;
        }

        public void setDetails(Details details) {
            this.details = details;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        // Nested classes
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Details {

            private int totalFindings;
            private int criticalFindings;
            private int highFindings;
            private int mediumFindings;
            private int lowFindings;
            private int riskScore;

            // DAST SCAN fields
            private String dastScanType;
            private String targetUrl;

            // CONTAINER SCAN fields
            private String imageName;
            private String registryProvider;
            private String imageType;
            private String totalSecrets;
            private String totalMisconfigurations;
            private String error;

            // SBOM SCAN fields
            private int totalDependencies;
            private int directDependencies;
            private int transitiveDependencies;
            private int uniquePackages;

            public Details() {}

            public int getTotalDependencies() {
                return totalDependencies;
            }

            public void setTotalDependencies(int totalDependencies) {
                this.totalDependencies = totalDependencies;
            }

            public int getDirectDependencies() {
                return directDependencies;
            }

            public void setDirectDependencies(int directDependencies) {
                this.directDependencies = directDependencies;
            }

            public int getTransitiveDependencies() {
                return transitiveDependencies;
            }

            public void setTransitiveDependencies(int transitiveDependencies) {
                this.transitiveDependencies = transitiveDependencies;
            }

            public int getUniquePackages() {
                return uniquePackages;
            }

            public void setUniquePackages(int uniquePackages) {
                this.uniquePackages = uniquePackages;
            }

            public int getTotalFindings() {
                return totalFindings;
            }

            public void setTotalFindings(int totalFindings) {
                this.totalFindings = totalFindings;
            }

            public int getCriticalFindings() {
                return criticalFindings;
            }

            public void setCriticalFindings(int criticalFindings) {
                this.criticalFindings = criticalFindings;
            }

            public int getHighFindings() {
                return highFindings;
            }

            public void setHighFindings(int highFindings) {
                this.highFindings = highFindings;
            }

            public int getMediumFindings() {
                return mediumFindings;
            }

            public void setMediumFindings(int mediumFindings) {
                this.mediumFindings = mediumFindings;
            }

            public int getLowFindings() {
                return lowFindings;
            }

            public void setLowFindings(int lowFindings) {
                this.lowFindings = lowFindings;
            }

            public int getRiskScore() {
                return riskScore;
            }

            public void setRiskScore(int riskScore) {
                this.riskScore = riskScore;
            }

            public String getDastScanType() {
                return dastScanType;
            }

            public void setDastScanType(String dastScanType) {
                this.dastScanType = dastScanType;
            }

            public String getTargetUrl() {
                return targetUrl;
            }

            public void setTargetUrl(String targetUrl) {
                this.targetUrl = targetUrl;
            }

            public String getImageName() {
                return imageName;
            }

            public void setImageName(String imageName) {
                this.imageName = imageName;
            }

            public String getRegistryProvider() {
                return registryProvider;
            }

            public void setRegistryProvider(String registryProvider) {
                this.registryProvider = registryProvider;
            }

            public String getImageType() {
                return imageType;
            }

            public void setImageType(String imageType) {
                this.imageType = imageType;
            }

            public String getTotalSecrets() {
                return totalSecrets;
            }

            public void setTotalSecrets(String totalSecrets) {
                this.totalSecrets = totalSecrets;
            }

            public String getTotalMisconfigurations() {
                return totalMisconfigurations;
            }

            public void setTotalMisconfigurations(String totalMisconfigurations) {
                this.totalMisconfigurations = totalMisconfigurations;
            }

            public String getError() {
                return error;
            }

            public void setError(String error) {
                this.error = error;
            }
        }
    }
}
