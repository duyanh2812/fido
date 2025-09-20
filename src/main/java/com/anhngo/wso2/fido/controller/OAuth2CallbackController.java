package com.anhngo.wso2.fido.controller;

import com.anhngo.wso2.fido.dto.ApiResponse;
import com.anhngo.wso2.fido.service.OAuth2Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/oauth2/code")
public class OAuth2CallbackController {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2CallbackController.class);

    @Autowired
    private OAuth2Service oauth2Service;

    /**
     * Handle OAuth2 callback from WSO2 IS
     */
    @GetMapping("/wso2")
    public ResponseEntity<ApiResponse> handleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_description) {
        
        logger.info("=== OAuth2 Callback Received ===");
        logger.info("Code: {}", code);
        logger.info("State: {}", state);
        logger.info("Error: {}", error);
        logger.info("Error Description: {}", error_description);

        try {
            if (error != null) {
                logger.error("OAuth2 callback error: {} - {}", error, error_description);
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("OAuth2 callback error: " + error + " - " + error_description));
            }

            if (code == null) {
                logger.error("No authorization code received");
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No authorization code received"));
            }

            // Exchange authorization code for tokens
            Map<String, Object> tokenResponse = oauth2Service.exchangeCodeForTokens(code);
            
            logger.info("Token exchange successful: {}", tokenResponse);
            
            return ResponseEntity.ok(ApiResponse.success("Authentication successful", tokenResponse));

        } catch (Exception e) {
            logger.error("Error handling OAuth2 callback", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to handle OAuth2 callback: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/wso2/health")
    public ResponseEntity<ApiResponse> health() {
        return ResponseEntity.ok(ApiResponse.success("OAuth2 callback controller is healthy"));
    }
}