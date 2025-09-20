package com.anhngo.wso2.fido.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;

@Configuration
@ConfigurationProperties(prefix = "wso2.is")
public class Wso2Config {
    
    private String baseUrl;
    
    @JsonProperty("admin-username")
    private String adminUsername;
    
    @JsonProperty("admin-password")
    private String adminPassword;
    
    private OAuth oauth = new OAuth();
    
    @JsonProperty("service-provider-name")
    private String serviceProviderName;
    
    @JsonProperty("tenant-domain")
    private String tenantDomain;
    
    public static class OAuth {
        @JsonProperty("client-key")
        private String clientKey;
        
        @JsonProperty("client-secret")
        private String clientSecret;
        
        @JsonProperty("redirect-uri")
        private String redirectUri;
        
        public String getClientKey() {
            return clientKey;
        }
        
        public void setClientKey(String clientKey) {
            this.clientKey = clientKey;
        }
        
        public String getClientSecret() {
            return clientSecret;
        }
        
        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
        
        public String getRedirectUri() {
            return redirectUri;
        }
        
        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }
    }
    
    // Getters and Setters
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getAdminUsername() {
        return adminUsername;
    }
    
    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }
    
    public String getAdminPassword() {
        return adminPassword;
    }
    
    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
    
    public OAuth getOauth() {
        return oauth;
    }
    
    public void setOauth(OAuth oauth) {
        this.oauth = oauth;
    }
    
    public String getServiceProviderName() {
        return serviceProviderName;
    }
    
    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }
    
    public String getTenantDomain() {
        return tenantDomain;
    }
    
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }
} 