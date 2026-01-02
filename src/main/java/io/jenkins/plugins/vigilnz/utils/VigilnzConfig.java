package io.jenkins.plugins.vigilnz.utils;

public class VigilnzConfig {

    // Default URLs (for development)
    // public static final String DEFAULT_BASE_URL = "https://api.vigilnz.com";
    // public static final String DEFAULT_AUTH_URL = DEFAULT_BASE_URL +
    // "/auth/api-key";
    // public static final String DEFAULT_SCAN_URL = DEFAULT_BASE_URL +
    // "/scan-targets/multi-scan";

    private static String devBaseUrl = "https://devapi.vigilnz.com";
    private static String prodBaseUrl = "https://api.vigilnz.com";
    private static String demoBaseUrl = "https://demoapi.vigilnz.com";
    private static String baseUrl = "https://api.vigilnz.com";

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static void setBaseUrl(String newBaseUrl) {
        if (newBaseUrl.equalsIgnoreCase("dev")) {
            baseUrl = devBaseUrl;
            return;
        }else if(newBaseUrl.equalsIgnoreCase("demo")){
            baseUrl = demoBaseUrl;
            return;
        } else {
            baseUrl = prodBaseUrl;
        }
    }

    public static String getAuthUrl() {
        return baseUrl + "/auth/api-key";
    }

    public static String getScanUrl() {
        return baseUrl + "/scan-targets/multi-scan";
    }
}
