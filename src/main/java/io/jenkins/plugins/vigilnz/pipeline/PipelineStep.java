package io.jenkins.plugins.vigilnz.pipeline;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.vigilnz.credentials.TokenCredentials;
import io.jenkins.plugins.vigilnz.models.ApiRequest;
import io.jenkins.plugins.vigilnz.models.ContainerScanContext;
import jakarta.annotation.Nonnull;
import java.util.*;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.verb.POST;

public class PipelineStep extends Step {

    private final String credentialsId;
    private final String scanTypes;
    private String projectName; // Optional parameter
    private ApiRequest.ScanContext dastScanContext = new ApiRequest.ScanContext("", ""); // Required for Dast Scan

    private ContainerScanContext containerScanContext = new ContainerScanContext("", ""); // Required for Container Scan

    private boolean cveScan;
    private boolean sastScan;
    private boolean sbomScan;
    private boolean iacScan;
    private boolean secretScan;

    @DataBoundConstructor
    public PipelineStep(String credentialsId, String scanTypes) {
        this.credentialsId = credentialsId;
        this.scanTypes = scanTypes;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getScanTypes() {
        if (scanTypes != null && !scanTypes.trim().isEmpty()) {
            return scanTypes;
        }
        List<String> types = new ArrayList<>();
        if (cveScan) types.add("SCA");
        if (sastScan) types.add("SAST");
        if (sbomScan) types.add("SBOM");
        if (iacScan) types.add("IAC");
        if (secretScan) types.add("SECRET");
        return String.join(",", types);
    }

    @DataBoundSetter
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new PipelineStepExecution(this, context);
    }

    public ApiRequest.ScanContext getDastScanContext() {
        return dastScanContext;
    }

    @DataBoundSetter
    public void setDastScanContext(ApiRequest.ScanContext dastScanContext) {
        this.dastScanContext = dastScanContext;
    }

    public boolean isCveScan() {
        return cveScan;
    }

    public void setCveScan(boolean cveScan) {
        this.cveScan = cveScan;
    }

    public boolean isSastScan() {
        return sastScan;
    }

    public void setSastScan(boolean sastScan) {
        this.sastScan = sastScan;
    }

    public boolean isSbomScan() {
        return sbomScan;
    }

    public void setSbomScan(boolean sbomScan) {
        this.sbomScan = sbomScan;
    }

    public boolean isIacScan() {
        return iacScan;
    }

    public void setIacScan(boolean iacScan) {
        this.iacScan = iacScan;
    }

    public boolean isSecretScan() {
        return secretScan;
    }

    public void setSecretScan(boolean secretScan) {
        this.secretScan = secretScan;
    }

    public ContainerScanContext getContainerScanContext() {
        return containerScanContext;
    }

    @DataBoundSetter
    public void setContainerScanContext(ContainerScanContext containerScanContext) {
        this.containerScanContext = containerScanContext;
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "vigilnzScan"; // This is the pipeline function name
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Run Vigilnz Security Scan";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of(TaskListener.class, Run.class, FilePath.class);
        }

        @POST
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item project) {

            ListBoxModel items = new ListBoxModel();
            for (TokenCredentials c : CredentialsProvider.lookupCredentials(
                    TokenCredentials.class,
                    project,
                    ACL.SYSTEM, // Use actual user authentication, not ACL.SYSTEM
                    Collections.emptyList())) {
                String label = c.getId().isEmpty() ? c.getDescription() : c.getId();
                if (label.isEmpty()) {
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
    }
}
