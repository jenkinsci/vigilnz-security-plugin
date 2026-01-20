package io.jenkins.plugins.vigilnz.utils;

public class VigilnzConfig {

    private static String devBaseUrl = "http://localhost:1337";
    //    private static String devBaseUrl = "https://devapi.vigilnz.com";
    private static String prodBaseUrl = "https://api.vigilnz.com";
    private static String demoBaseUrl = "https://demoapi.vigilnz.com";
    private static String baseUrl = "https://api.vigilnz.com";

    private static String DEV_VIGILNZ_URL = "https://devplatform.vigilnz.com";
    private static String PROD_VIGILNZ_URL = "https://platform.vigilnz.com";
    private static String DEMO_VIGILNZ_URL = "https://demoplatform.vigilnz.com";
    private static String baseSiteUrl = DEV_VIGILNZ_URL;

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static void setBaseUrl(String newBaseUrl) {
        if (newBaseUrl.equalsIgnoreCase("dev")) {
            baseUrl = devBaseUrl;
            baseSiteUrl = DEV_VIGILNZ_URL;
        } else if (newBaseUrl.equalsIgnoreCase("demo")) {
            baseUrl = demoBaseUrl;
            baseSiteUrl = DEMO_VIGILNZ_URL;
        } else {
            baseUrl = prodBaseUrl;
            baseSiteUrl = PROD_VIGILNZ_URL;
        }
    }

    public static String getAuthUrl() {
        return baseUrl + "/auth/api-key";
    }

    public static String getBaseSiteUrl() {
        return baseSiteUrl;
    }

    public static String getScanUrl() {
        return baseUrl + "/scan-targets/multi-scan";
    }
}
