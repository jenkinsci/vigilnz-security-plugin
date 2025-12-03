package io.jenkins.plugins;

import com.cloudbees.plugins.credentials.CredentialsProvider;
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
import hudson.util.Secret;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

// This file for Jenkins FreeStyle Job Method
public class SecurityCheckBuilder extends Builder {

    private final Secret token;
    private final String targetFile;
    private boolean cveScan;
    private boolean sastScan;
    private boolean sbomScan;

    @DataBoundConstructor
    public SecurityCheckBuilder(Secret token, String targetFile) {
        this.token = token;
        this.targetFile = targetFile;
    }

    public Secret getToken() {
        return token;
    }

    public String getTargetFile() {
        return targetFile;
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

    public List<String> getScanTypes() {
        List<String> types = new java.util.ArrayList<>();
        if (cveScan) types.add("cve");
        if (sastScan) types.add("sast");
        if (sbomScan) types.add("sbom");
        return types;
    }

    // this function trigger when user click the build button
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        EnvVars env = build.getEnvironment(listener);
        listener.getLogger().println("------ Freestyle Method ------");

        // Build scan types list from checkboxes
        List<String> scanTypes = getScanTypes();

        // Validate at least one scan type is selected
        if (scanTypes.isEmpty()) {
            listener.error("Error: At least one scan type must be selected.");
            return false;
        }

        // Get the credential ID from the Secret
        String credentialId = token.getPlainText();
        
        // Look up the actual TokenCredentials object
        TokenCredentials creds = CredentialsProvider.findCredentialById(
            credentialId,
            TokenCredentials.class,
            build
        );

        if (creds == null) {
            listener.error("Error: Vigilnz Token credential not found with ID: " + credentialId);
            return false;
        }

        // Get the actual token value from the credential
        String tokenText = creds.getToken().getPlainText();
        
        listener.getLogger().println("Credential ID: " + credentialId);
        listener.getLogger().println("Your Token from Plugin: " + tokenText);
        listener.getLogger().println("Your Target File : " + targetFile);
        listener.getLogger().println("Selected Scan Types: " + String.join(", ", scanTypes));

        boolean result = ApiService.triggerScan(tokenText, targetFile, scanTypes, env, listener);

        if (!result) {
            listener.error("Scan failed");
            return false;
        }

        return true;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return "Invoke Vigilnz Security Task";   // This appears in dropdown
        }

        public ListBoxModel doFillTokenItems(@AncestorInPath Item project) {
            ListBoxModel items = new ListBoxModel();

            for (TokenCredentials c : CredentialsProvider.lookupCredentials(TokenCredentials.class, project, ACL.SYSTEM, Collections.emptyList())) {
                String label = c.getTokenId().isEmpty() ? c.getTokenDescription() : c.getTokenId();
                if (label == null || label.isEmpty()) {
                    label = c.getId();
                }
                items.add(label, c.getId());
            }
            return items;
        }


        @Override
        public boolean isApplicable(Class jobType) {
            return true;
        }

        public FormValidation doCheckToken(@QueryParameter Secret token) {
            if (token == null || Secret.toString(token).isEmpty()) {
                return FormValidation.error("Token is required");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckScanTypes(@QueryParameter boolean cveScan,
                                               @QueryParameter boolean sastScan,
                                               @QueryParameter boolean sbomScan) {
            if (!cveScan && !sastScan && !sbomScan) {
                return FormValidation.error("You must select at least one scan type.");
            }
            return FormValidation.ok();
        }
    }

}
