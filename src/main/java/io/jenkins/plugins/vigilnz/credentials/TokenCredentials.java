package io.jenkins.plugins.vigilnz.credentials;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import hudson.model.Item;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

public class TokenCredentials extends BaseStandardCredentials {

    /** API token - stored securely using Jenkins Secret (encrypted when serialized) */
    private final Secret token;
    
    /** Credential identifier (not sensitive - just a label/ID, not a password) */
    // lgtm[jenkins/password-in-field]
    private final String tokenId;
    
    /** Credential description (not sensitive - just metadata, not a password) */
    // lgtm[jenkins/password-in-field]
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

        if (token == null || Secret.toString(token).isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
        if (tokenId != null && tokenId.contains(" ")) {
            throw new IllegalArgumentException("Token ID must not contain spaces");
        }
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

        @POST
        public FormValidation doCheckToken(@AncestorInPath Item item, @QueryParameter String token) {
            // Security: Check if user has permission to configure this item (project/folder)
            // If item is provided, check item permission; otherwise check global admin permission
            if (item != null) {
                if (!item.hasPermission(Item.CONFIGURE)) {
                    return FormValidation.error("No permission to configure this item");
                }
            } else {
                // Global credential creation/editing requires admin permission
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            }
            
            if (token == null || token.trim().isEmpty()) {
                return FormValidation.error("Field is required");
            }
            return FormValidation.ok();
        }

        @POST
        public FormValidation doCheckTokenId(@AncestorInPath Item item, @QueryParameter String tokenId) {
            // Security: Check if user has permission to configure this item (project/folder)
            // If item is provided, check item permission; otherwise check global admin permission
            if (item != null) {
                if (!item.hasPermission(Item.CONFIGURE)) {
                    return FormValidation.error("No permission to configure this item");
                }
            } else {
                // Global credential creation/editing requires admin permission
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            }
            
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
