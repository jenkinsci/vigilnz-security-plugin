package io.jenkins.plugins.vigilnz.pipeline;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.vigilnz.api.ApiService;
import io.jenkins.plugins.vigilnz.credentials.TokenCredentials;
import io.jenkins.plugins.vigilnz.models.ApiResponse;
import io.jenkins.plugins.vigilnz.ui.ScanResultAction;
import io.jenkins.plugins.vigilnz.utils.VigilnzConfig;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

public class PipelineStepExecution extends StepExecution {
    private static final long serialVersionUID = 1L;

    private final transient PipelineStep step;

    public PipelineStepExecution(PipelineStep step, StepContext context) {
        super(context);
        this.step = step;
    }

    public static void getGitDetails(File workspaceDir, EnvVars env, TaskListener listener) {
        try {
            File gitDir = new File(workspaceDir, ".git");
            if (!gitDir.exists()) {
                listener.getLogger().println("No .git folder found");
                return;
            }

            // Read HEAD
            String head = Files.readString(Paths.get(gitDir.getAbsolutePath(), "HEAD"), StandardCharsets.UTF_8)
                    .trim();
            String branch = null;
            String commit = null;

            if (head.startsWith("ref:")) {
                branch = head.substring("ref: refs/heads/".length());
                Path refPath = Paths.get(gitDir.getAbsolutePath(), head.substring("ref: ".length()));
                if (Files.exists(refPath)) {
                    commit = Files.readString(refPath, StandardCharsets.UTF_8).trim();
                }
            } else {
                commit = head; // detached HEAD
            }

            // Read remote URL from config
            Path configPath = Paths.get(gitDir.getAbsolutePath(), "config");
            String repoUrl = null;
            if (Files.exists(configPath)) {
                for (String line : Files.readAllLines(configPath, StandardCharsets.UTF_8)) {
                    if (line.trim().startsWith("url =")) {
                        repoUrl = line.split("=")[1].trim();
                        break;
                    }
                }
            }
            // Inject into environment
            if (branch != null) env.put("GIT_BRANCH", branch);
            if (commit != null) env.put("GIT_COMMIT", commit);
            if (repoUrl != null) env.put("GIT_URL", repoUrl);

        } catch (Exception e) {
            listener.getLogger().println("Error reading git info: " + e.getMessage());
        }
    }

    @Override
    public boolean start() throws Exception {

        TaskListener listener = getContext().get(TaskListener.class);
        Run<?, ?> run = getContext().get(Run.class);

        String credentialsId = step.getCredentialsId();

        // Validate credentials ID is provided
        if (credentialsId == null || credentialsId.trim().isEmpty()) {
            listener.error("Error: Credentials ID is required. Please provide a credential ID in the pipeline step.");
            attachResult(run, buildErrorResponse("Credentials ID is required."));
            getContext().onFailure(new AbortException("Credentials ID is required"));
            return false;
        }

        TokenCredentials creds = CredentialsProvider.findCredentialById(credentialsId, TokenCredentials.class, run);

        listener.getLogger().println("------ Pipeline Method ------");

        if (creds != null) {
            // listener.getLogger().println("Token ID: " + creds.getTokenId());
            // listener.getLogger().println("Description: " + creds.getTokenDescription());
            EnvVars env = getContext().get(EnvVars.class);

            FilePath ws = getContext().get(FilePath.class);
            if (ws != null) {
                File workspaceDir = new File(ws.getRemote());
                getGitDetails(workspaceDir, env, listener);
            }

            String token = creds.getToken().getPlainText();

            // Set base URL based on environment selection
            VigilnzConfig.setBaseUrl(creds.getEnvironment());

            List<String> scanTypes = step.getScanTypes();

            // Validate at least one scan type is selected
            if (scanTypes == null || scanTypes.isEmpty()) {
                listener.error("Error: At least one scan type must be selected.");
                attachResult(run, buildErrorResponse("At least one scan type must be selected."));
                getContext().onFailure(new AbortException("At least one scan type must be selected"));
                return false;
            }

            listener.getLogger().println("Selected Scan Types: " + String.join(", ", scanTypes));

            String result;
            try {
                result = ApiService.triggerScan(token, step.getTargetFile(), scanTypes, env, listener);
                if (result != null && !result.isEmpty()) {
                    run.addAction(new ScanResultAction(result));
                } else {
                    listener.getLogger().println("API call failed, no action added.");
                    return false;
                }
            } catch (Exception e) {
                listener.error("Scan failed");
                attachResult(run, buildErrorResponse("Scan failed: " + e.getMessage()));
                getContext().onFailure(new AbortException("Scan failed"));
                return false;
            }

        } else {
            listener.error("Error: Vigilnz Token credential not found with ID: " + credentialsId);
            attachResult(run, buildErrorResponse("Vigilnz Token credential not found with ID: " + credentialsId));
            getContext().onFailure(new AbortException("No Vigilnz Token credential found with ID: " + credentialsId));
            return false;
        }

        getContext().onSuccess(null);
        return true;
    }

    private void attachResult(Run<?, ?> run, String json) {
        try {
            run.addAction(new ScanResultAction(json));
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
}
