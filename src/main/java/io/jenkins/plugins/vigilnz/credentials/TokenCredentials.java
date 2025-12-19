package io.jenkins.plugins.vigilnz.credentials;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;

public class TokenCredentials extends BaseStandardCredentials implements StringCredentials {

    private final Secret token;

    @DataBoundConstructor
    public TokenCredentials(CredentialsScope scope, String id, String description, Secret token) {
        super(scope, id, description);

        if (token == null || Secret.toString(token).isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
        //        if (id != null && id.contains(" ")) {
        //            throw new IllegalArgumentException("Token ID must not contain spaces");
        //        }
        this.token = token;
    }

    public Secret getToken() {
        return token;
    }

    @NonNull
    @Override
    public Secret getSecret() {
        return token;
    }

    // Descriptor for Jenkins UI
    @Symbol("vigilnzToken")
    @Extension
    public static class DescriptorImpl extends CredentialsDescriptor {

        @Override
        public String getDisplayName() {
            return "Vigilnz Security Token";
        }

        //        @POST
        //        public FormValidation doCheckToken(@AncestorInPath Item item, @QueryParameter String token) {
        //             Security: Check if user has permission to configure this item (project/folder)
        //             If item is provided, check item permission; otherwise check global admin permission
        //            if (item != null) {
        //                if (!item.hasPermission(Item.CONFIGURE)) {
        //                    return FormValidation.error("No permission to configure this item");
        //                }
        //            } else {
        //                // Global credential creation/editing requires admin permission
        //                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        //            }
        //
        //            if (token == null || token.trim().isEmpty()) {
        //                return FormValidation.error("Field is required");
        //            }
        //            return FormValidation.ok();
        //        }

        //        @POST
        //        public FormValidation doCheckTokenId(@AncestorInPath Item item, @QueryParameter String tokenId) {
        //            // Security: Check if user has permission to configure this item (project/folder)
        //            // If item is provided, check item permission; otherwise check global admin permission
        //            if (item != null) {
        //                if (!item.hasPermission(Item.CONFIGURE)) {
        //                    return FormValidation.error("No permission to configure this item");
        //                }
        //            } else {
        //                // Global credential creation/editing requires admin permission
        //                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        //            }
        //
        //            if (tokenId != null && !tokenId.trim().isEmpty()) {
        //                // Check for spaces
        //                if (tokenId.contains(" ")) {
        //                    return FormValidation.error("ID must not contain spaces.");
        //                }
        //                // Optional: only allow letters, numbers, dash and underscore
        //                if (!tokenId.matches("^[a-zA-Z0-9_-]+$")) {
        //                    return FormValidation.error("ID can only contain letters, numbers, - and _");
        //                }
        //            }
        //            return FormValidation.ok();
        //        }
    }
}
