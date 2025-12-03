package io.jenkins.plugins;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class TokenCredentials extends BaseStandardCredentials {

    private final Secret token;
    private final String tokenId;
    private final String tokenDescription;

    @DataBoundConstructor
    public TokenCredentials(
            CredentialsScope scope,
            String tokenId,
            String tokenDescription,
            Secret token
    ) {
        super(scope, tokenId, tokenDescription);
        // If tokenId is null or empty, use empty string (Jenkins will handle ID generation)
        // This prevents errors when updating credentials that were created without an ID
        String idToUse = (tokenId == null || tokenId.trim().isEmpty()) ? "" : tokenId;
        this.token = token;
        this.tokenId = idToUse;
        this.tokenDescription = tokenDescription;
    }

    public Secret getToken() {
        return token;
    }

    public String getTokenId() {
        // If tokenId was never set by user, return the actual Jenkins-generated ID
        if (tokenId == null || tokenId.trim().isEmpty()) {
            return getId();
        }
        return tokenId;
    }

    public String getTokenDescription() {
        return tokenDescription;
    }

    // Descriptor for Jenkins UI
    @Symbol("vigilnzToken")
    @Extension
    public static class DescriptorImpl extends CredentialsDescriptor {

        @Override
        public String getDisplayName() {
            return "Vigilnz Security Token";
        }

        public FormValidation doCheckToken(@QueryParameter Secret token) {
            if (token == null || Secret.toString(token).isEmpty()) {
                return FormValidation.error("Field is required");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckTokenId(@QueryParameter String tokenId) {
            if (tokenId != null && !tokenId.trim().isEmpty()) {
                // Check for spaces
                if (tokenId.contains(" ")) {
                    return FormValidation.error("ID must not contain spaces.");
                }
                // Optional: only allow letters, numbers, dash and underscore
                if (!tokenId.matches("^[a-zA-Z0-9_-]+$")) {
                    return FormValidation.error("ID can only contain letters, numbers, - and _");
                }
            }
            return FormValidation.ok();
        }
    }
}
