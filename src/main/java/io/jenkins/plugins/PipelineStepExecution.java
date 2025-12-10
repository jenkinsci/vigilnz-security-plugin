package io.jenkins.plugins;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PipelineStepExecution extends StepExecution {
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
            String head = Files.readString(Paths.get(gitDir.getAbsolutePath(), "HEAD"), StandardCharsets.UTF_8).trim();
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
        TokenCredentials creds =
                CredentialsProvider.findCredentialById(
                        step.getToken(),
                        TokenCredentials.class,
                        run
                );

        listener.getLogger().println("------ Pipeline Method ------");

        if (creds != null) {
//            listener.getLogger().println("Token ID: " + creds.getTokenId());
//            listener.getLogger().println("Description: " + creds.getTokenDescription());
            EnvVars env = getContext().get(EnvVars.class);

            FilePath ws = getContext().get(FilePath.class);
            if (ws != null) {
                File workspaceDir = new File(ws.getRemote());
                getGitDetails(workspaceDir, env, listener);
            }

            String token = creds.getToken().getPlainText();
            List<String> scanTypes = step.getScanTypes();

            // Validate at least one scan type is selected
            if (scanTypes == null || scanTypes.isEmpty()) {
                listener.error("Error: At least one scan type must be selected.");
                getContext().onFailure(new AbortException("At least one scan type must be selected"));
                return false;
            }

            listener.getLogger().println("Selected Scan Types: " + String.join(", ", scanTypes));
            String result = ApiService.triggerScan(token, step.getTargetFile(), scanTypes, env, listener);

            run.addAction(new ScanResultAction(result));

            if (result == null || result.isEmpty()) {
                listener.error("Scan failed");
                getContext().onFailure(new AbortException("Scan failed"));
                return false;
            }

        } else {
            listener.getLogger().println("No Vigilnz Token credential found");
            getContext().onFailure(new AbortException("No Vigilnz Token credential found"));
            return false;
        }

        getContext().onSuccess(null);
        return true;
    }
}