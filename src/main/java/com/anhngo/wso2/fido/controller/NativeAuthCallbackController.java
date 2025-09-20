package com.anhngo.wso2.fido.controller;

import com.anhngo.wso2.fido.dto.ApiResponse;
import com.anhngo.wso2.fido.service.Wso2Service;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/native-auth")
@CrossOrigin(origins = "*")
public class NativeAuthCallbackController {
    
    private static final Logger logger = LoggerFactory.getLogger(NativeAuthCallbackController.class);
    
    private final Wso2Service wso2Service;
    
    public NativeAuthCallbackController(Wso2Service wso2Service) {
        this.wso2Service = wso2Service;
    }
    
    /**
     * Handle callback from WSO2 IS after native authentication
     */
    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            @RequestParam(value = "state", required = false) String state) {
        
        logger.info("Received native auth callback - code: {}, error: {}, state: {}", code, error, state);
        
        if (error != null) {
            logger.error("Native auth error: {} - {}", error, errorDescription);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Native authentication failed: " + error + " - " + errorDescription));
        }
        
        if (code == null) {
            logger.error("No authorization code received");
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("No authorization code received"));
        }
        
        try {
            // Exchange authorization code for access token
            Map<String, Object> tokenData = wso2Service.exchangeCodeForToken(code);
            
            logger.info("Successfully exchanged code for token");
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Native authentication successful");
            response.put("token_data", tokenData);
            response.put("state", state);
            
            return ResponseEntity.ok(ApiResponse.success("Native authentication successful", response));
            
        } catch (Exception e) {
            logger.error("Error exchanging code for token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to complete native authentication: " + e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/callback/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Native authentication callback service is running"));
    }
}
