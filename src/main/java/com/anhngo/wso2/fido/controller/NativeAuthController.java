package com.anhngo.wso2.fido.controller;

import com.anhngo.wso2.fido.dto.ApiResponse;
import com.anhngo.wso2.fido.dto.NativeAuthChallengeRequest;
import com.anhngo.wso2.fido.dto.NativeAuthInitRequest;
import com.anhngo.wso2.fido.dto.NativeAuthVerifyRequest;
import com.anhngo.wso2.fido.service.NativeAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/native-auth")
@CrossOrigin(origins = "*")
public class NativeAuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(NativeAuthController.class);
    
    private final NativeAuthService nativeAuthService;
    
    public NativeAuthController(NativeAuthService nativeAuthService) {
        this.nativeAuthService = nativeAuthService;
    }
    
    /**
     * Initialize native authentication flow
     */
    @PostMapping("/init")
    public ResponseEntity<ApiResponse<JsonNode>> initNativeAuth(
            @Valid @RequestBody NativeAuthInitRequest request) {
        
        logger.info("Received native auth init request for client: {}", request.getClientId());
        
        try {
            JsonNode result = nativeAuthService.initNativeAuth(request);
            return ResponseEntity.ok(
                ApiResponse.success("Native authentication flow initialized successfully", result)
            );
        } catch (Exception error) {
            logger.error("Error initializing native authentication flow", error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to initialize native authentication flow: " + error.getMessage()));
        }
    }
    
    /**
     * Get challenge for passkey authentication
     */
    @PostMapping("/challenge")
    public ResponseEntity<ApiResponse<JsonNode>> getPasskeyChallenge(
            @Valid @RequestBody NativeAuthChallengeRequest request) {
        
        logger.info("Received passkey challenge request for flow: {}", request.getFlowId());
        
        try {
            JsonNode result = nativeAuthService.getPasskeyChallenge(request);
            return ResponseEntity.ok(
                ApiResponse.success("Passkey challenge retrieved successfully", result)
            );
        } catch (Exception error) {
            logger.error("Error getting passkey challenge", error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get passkey challenge: " + error.getMessage()));
        }
    }
    
    /**
     * Verify passkey authentication credentials
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<JsonNode>> verifyPasskeyAuth(
            @Valid @RequestBody NativeAuthVerifyRequest request) {
        
        logger.info("Received passkey verify request for flow: {}", request.getFlowId());
        
        try {
            JsonNode result = nativeAuthService.verifyPasskeyAuth(request);
            return ResponseEntity.ok(
                ApiResponse.success("Passkey authentication verified successfully", result)
            );
        } catch (Exception error) {
            logger.error("Error verifying passkey authentication", error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to verify passkey authentication: " + error.getMessage()));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Native authentication service is running"));
    }
}
