package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.TaskListener;
import jakarta.annotation.Nonnull;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;
import java.util.Set;

public class PipelineStep extends Step {

    private final String token;
    private final String targetFile;
    private final List<String> scanTypes;

    @DataBoundConstructor
    public PipelineStep(String token, String targetFile, List<String> scanTypes) {
        this.token = token;
        this.targetFile = targetFile;
        this.scanTypes = scanTypes != null ? scanTypes : List.of();
    }

    public String getToken() {
        return token;
    }

    public String getTargetFile() {
        return targetFile;
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
            return "vigilnzScan";   // This is the pipeline function name
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Run Vigilnz Security Scan";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of(TaskListener.class);
        }
    }

}
