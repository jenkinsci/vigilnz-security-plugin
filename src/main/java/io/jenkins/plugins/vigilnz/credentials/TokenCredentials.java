package io.jenkins.plugins.vigilnz.credentials;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;

public class TokenCredentials extends BaseStandardCredentials implements StringCredentials {

    private final Secret token;
    private final String environment;

    @DataBoundConstructor
    public TokenCredentials(CredentialsScope scope, String id, String description, Secret token, String environment) {
        super(scope, id, description);

        if (token == null || Secret.toString(token).isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
        // if (id != null && id.contains(" ")) {
        // throw new IllegalArgumentException("Token ID must not contain spaces");
        // }
        this.token = token;
        this.environment = environment;
    }

    public Secret getToken() {
        return token;
    }

    public String getEnvironment() {
        return environment;
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

        // Populate Environment Dropdown
        public ListBoxModel doFillEnvironmentItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Development", "dev");
            items.add("Production", "prod");
            return items;
        }
    }
}
