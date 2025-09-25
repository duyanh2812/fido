package com.anhngo.wso2.fido.controller;

import com.anhngo.wso2.fido.config.MobileAppsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mobile App Integration Controller
 * Provides .well-known endpoints for mobile app integration
 */
@RestController
@RequestMapping("/.well-known")
public class MobileController {

    @Autowired
    private MobileAppsConfig mobileAppsConfig;

    /**
     * Apple App Site Association endpoint
     * Required for iOS app integration with WebAuthn
     */
    @GetMapping(value = "/apple-app-site-association", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAppleAppSiteAssociation() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get iOS apps from configuration
            List<MobileAppsConfig.IosApp> iosApps = mobileAppsConfig.getApps().getIos();
            List<String> appIds;
            
            if (iosApps != null && !iosApps.isEmpty()) {
                appIds = iosApps.stream()
                        .map(MobileAppsConfig.IosApp::getFullAppId)
                        .collect(Collectors.toList());
            } else {
                // Fallback to default app if config is not loaded
                appIds = List.of("C0289CB5B6.com.tymex.authentication.fido");
            }
            
            // Web credentials configuration for iOS
            Map<String, Object> webCredentials = new HashMap<>();
            webCredentials.put("apps", appIds);
            
            response.put("webcredentials", webCredentials);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            // Fallback response if config fails
            Map<String, Object> fallbackResponse = new HashMap<>();
            Map<String, Object> webCredentials = new HashMap<>();
            webCredentials.put("apps", new String[]{"C0289CB5B6.com.tymex.authentication.fido"});
            fallbackResponse.put("webcredentials", webCredentials);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(fallbackResponse);
        }
    }

    /**
     * Android App Links endpoint
     * Required for Android app integration with WebAuthn
     */
    @GetMapping(value = "/assetlinks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getAndroidAppLinks() {
        try {
            // Get Android apps from configuration
            List<MobileAppsConfig.AndroidApp> androidApps = mobileAppsConfig.getApps().getAndroid();
            List<Map<String, Object>> appLinks;
            
            if (androidApps != null && !androidApps.isEmpty()) {
                appLinks = androidApps.stream()
                        .map(androidApp -> {
                            Map<String, Object> appLink = new HashMap<>();
                            appLink.put("relation", new String[]{"delegate_permission/common.handle_all_urls"});
                            
                            Map<String, Object> target = new HashMap<>();
                            target.put("namespace", "android_app");
                            target.put("package_name", androidApp.getPackageName());
                            target.put("sha256_cert_fingerprints", androidApp.getSha256Fingerprints());
                            
                            appLink.put("target", target);
                            return appLink;
                        })
                        .collect(Collectors.toList());
            } else {
                // Fallback to default app if config is not loaded
                Map<String, Object> fallbackAppLink = new HashMap<>();
                fallbackAppLink.put("relation", new String[]{"delegate_permission/common.handle_all_urls"});
                
                Map<String, Object> target = new HashMap<>();
                target.put("namespace", "android_app");
                target.put("package_name", "com.tymex.authentication.fido");
                target.put("sha256_cert_fingerprints", new String[]{
                    "14:6D:E9:83:C5:73:06:50:D8:EE:B9:95:2F:34:FC:64:16:A0:83:42:E6:1D:BE:A8:8A:04:96:B2:3F:CF:44:E5"
                });
                
                fallbackAppLink.put("target", target);
                appLinks = List.of(fallbackAppLink);
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(appLinks);
        } catch (Exception e) {
            // Fallback response if config fails
            Map<String, Object> fallbackAppLink = new HashMap<>();
            fallbackAppLink.put("relation", new String[]{"delegate_permission/common.handle_all_urls"});
            
            Map<String, Object> target = new HashMap<>();
            target.put("namespace", "android_app");
            target.put("package_name", "com.tymex.authentication.fido");
            target.put("sha256_cert_fingerprints", new String[]{
                "14:6D:E9:83:C5:73:06:50:D8:EE:B9:95:2F:34:FC:64:16:A0:83:42:E6:1D:BE:A8:8A:04:96:B2:3F:CF:44:E5"
            });
            
            fallbackAppLink.put("target", target);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(List.of(fallbackAppLink));
        }
    }

    /**
     * Get configured mobile apps
     */
    @GetMapping(value = "/fido/mobile/apps", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getMobileApps() {
        Map<String, Object> response = new HashMap<>();
        response.put("ios", mobileAppsConfig.getApps().getIos());
        response.put("android", mobileAppsConfig.getApps().getAndroid());
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    /**
     * Get iOS apps for Apple App Site Association
     */
    @GetMapping(value = "/fido/mobile/ios-apps", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getIosApps() {
        List<Map<String, Object>> iosApps = mobileAppsConfig.getApps().getIos().stream()
                .map(app -> {
                    Map<String, Object> appInfo = new HashMap<>();
                    appInfo.put("appId", app.getAppId());
                    appInfo.put("packageName", app.getPackageName());
                    appInfo.put("fullAppId", app.getFullAppId());
                    appInfo.put("description", app.getDescription());
                    return appInfo;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(iosApps);
    }

    /**
     * Get Android apps for App Links
     */
    @GetMapping(value = "/fido/mobile/android-apps", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getAndroidApps() {
        List<Map<String, Object>> androidApps = mobileAppsConfig.getApps().getAndroid().stream()
                .map(app -> {
                    Map<String, Object> appInfo = new HashMap<>();
                    appInfo.put("packageName", app.getPackageName());
                    appInfo.put("sha256Fingerprints", app.getSha256Fingerprints());
                    appInfo.put("description", app.getDescription());
                    return appInfo;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(androidApps);
    }
}
