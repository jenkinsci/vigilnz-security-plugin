package io.jenkins.plugins.vigilnz.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerScanContext {

    private final String image;
    private final String provider;
    private Registry registry;
    private Auth auth;

    @DataBoundConstructor
    public ContainerScanContext(String image, String provider) {
        this.image = image;
        this.provider = provider;
    }

    public String getImage() {
        return image;
    }

    public String getProvider() {
        return provider;
    }

    public Registry getRegistry() {
        return registry;
    }

    @DataBoundSetter
    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Auth getAuth() {
        return auth;
    }

    @DataBoundSetter
    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Registry {

        private final String type;
        private String url;

        @DataBoundConstructor
        public Registry(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }

        @DataBoundSetter
        public void setUrl(String url) {
            this.url = url;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Auth {

        private final String type;
        private String token;
        private String username;
        private String password;

        @DataBoundConstructor
        public Auth(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public String getToken() {
            return token;
        }

        @DataBoundSetter
        public void setToken(String token) {
            this.token = token;
        }

        public String getUsername() {
            return username;
        }

        @DataBoundSetter
        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        @DataBoundSetter
        public void setPassword(String password) {
            this.password = password;
        }
    }
}
