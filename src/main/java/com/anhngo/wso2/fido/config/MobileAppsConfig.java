package com.anhngo.wso2.fido.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "mobile")
@Component
public class MobileAppsConfig {
    
    private Apps apps = new Apps();
    
    public Apps getApps() {
        return apps;
    }
    
    public void setApps(Apps apps) {
        this.apps = apps;
    }
    
    public static class Apps {
        private List<IosApp> ios;
        private List<AndroidApp> android;
        
        public List<IosApp> getIos() {
            return ios;
        }
        
        public void setIos(List<IosApp> ios) {
            this.ios = ios;
        }
        
        public List<AndroidApp> getAndroid() {
            return android;
        }
        
        public void setAndroid(List<AndroidApp> android) {
            this.android = android;
        }
    }
    
    public static class IosApp {
        private String appId;
        private String packageName;
        private String description;
        
        public String getAppId() {
            return appId;
        }
        
        public void setAppId(String appId) {
            this.appId = appId;
        }
        
        public String getPackageName() {
            return packageName;
        }
        
        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getFullAppId() {
            return appId + "." + packageName;
        }
    }
    
    public static class AndroidApp {
        private String packageName;
        private List<String> sha256Fingerprints;
        private String description;
        
        public String getPackageName() {
            return packageName;
        }
        
        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }
        
        public List<String> getSha256Fingerprints() {
            return sha256Fingerprints;
        }
        
        public void setSha256Fingerprints(List<String> sha256Fingerprints) {
            this.sha256Fingerprints = sha256Fingerprints;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
}
