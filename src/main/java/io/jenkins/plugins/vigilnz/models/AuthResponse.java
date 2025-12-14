package io.jenkins.plugins.vigilnz.models;

/**
 * Authentication response model.
 * NOTE: This class is NOT serialized to disk - it's only used in-memory during API calls.
 * Tokens are cleared from memory after use and never persisted.
 * This class does NOT implement Serializable, so fields are never written to disk.
 */
public class AuthResponse {

    /** Access token - sensitive but only in-memory, never persisted (class not serializable) */
    // lgtm[jenkins/password-in-field]
    private final String accessToken;
    
    /** Refresh token - sensitive but only in-memory, never persisted (class not serializable) */
    // lgtm[jenkins/password-in-field]
    private final String refreshToken;
    
    private final long expiresIn;
    
    /** Token type (e.g., "Bearer") - not sensitive, just metadata */
    // lgtm[jenkins/password-in-field]
    private final String tokenType;

    public AuthResponse(String accessToken, String refreshToken, long expiresIn, String tokenType) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }
}
