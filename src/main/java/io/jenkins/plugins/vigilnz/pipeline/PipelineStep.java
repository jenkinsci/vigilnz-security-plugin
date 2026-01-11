package io.jenkins.plugins.vigilnz.pipeline;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import jakarta.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class PipelineStep extends Step {

    private final String credentialsId;
    private final List<String> scanTypes;
    private String projectName; // Optional parameter

    @DataBoundConstructor
    public PipelineStep(String credentialsId, String scanTypes) {
        this.credentialsId = credentialsId;

        // Split comma-separated string into a list
        if (scanTypes != null && !scanTypes.trim().isEmpty()) {
            List<String> scanTypeList = Arrays.asList(scanTypes.split("\\s*,\\s*"));
            this.scanTypes = scanTypeList.stream()
                    .map(s -> s.equalsIgnoreCase("sca") ? "cve" : s)
                    .collect(Collectors.toList());
        } else {
            this.scanTypes = List.of();
        }
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

    public List<String> getScanTypes() {
        return scanTypes != null ? scanTypes : List.of();
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new PipelineStepExecution(this, context);
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

        //        @Override
        //        public Set<? extends Class<?>> getRequiredContext() {
        //            return Set.of(TaskListener.class);
        //        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of(TaskListener.class, Run.class, FilePath.class);
        }
    }
}
