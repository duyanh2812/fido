package com.anhngo.wso2.fido.controller;

import com.anhngo.wso2.fido.dto.*;
import com.anhngo.wso2.fido.service.FidoService;
import com.anhngo.wso2.fido.service.Wso2Service;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fido")
@CrossOrigin(origins = "*")
public class FidoController {
    
    private static final Logger logger = LoggerFactory.getLogger(FidoController.class);
    
    private final FidoService fidoService;
    private final Wso2Service wso2Service;
    
    public FidoController(FidoService fidoService, Wso2Service wso2Service) {
        this.fidoService = fidoService;
        this.wso2Service = wso2Service;
    }
    
    @PostMapping("/registration-options")
    public ResponseEntity<JsonNode> getRegistrationOptions(
            @Valid @RequestBody FidoRegistrationOptionsRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "Content-Type", required = false) String contentType) {
        
        logger.info("Received registration options request for user: {}", request.getUsername());
        logger.info("Request headers - Authorization: {}", authorization != null ? "Present" : "Not present");
        logger.info("Request headers - Content-Type: {}", contentType);
        
        try {
            // Extract user access token from Authorization header
            String userAccessToken = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                userAccessToken = authorization.substring(7);
                logger.info("Using user access token for FIDO registration options");
            }
            
            JsonNode options = fidoService.getRegistrationOptions(request, userAccessToken);
            return ResponseEntity.ok(options);
        } catch (Exception error) {
            logger.error("Error getting registration options", error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<JsonNode>> registerFidoCredential(
            @Valid @RequestBody FidoRegistrationRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        logger.info("Received FIDO registration request for user: {}", request.getUsername());
        
        try {
            // Extract user access token from Authorization header
            String userAccessToken = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                userAccessToken = authorization.substring(7);
                logger.info("Using user access token for FIDO credential registration");
            }
            
            JsonNode result = fidoService.registerFidoCredential(request, userAccessToken);
            return ResponseEntity.ok(
                ApiResponse.success("FIDO credential registered successfully", result)
            );
        } catch (Exception error) {
            logger.error("Error registering FIDO credential", error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to register FIDO credential: " + error.getMessage()));
        }
    }
    
    @PostMapping("/authentication-options")
    public ResponseEntity<ApiResponse<JsonNode>> getAuthenticationOptions(
            @Valid @RequestBody FidoAuthenticationOptionsRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        logger.info("Received authentication options request for user: {}", request.getUsername());
        
        try {
            // Extract user access token from Authorization header
            String userAccessToken = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                userAccessToken = authorization.substring(7);
                logger.info("Using user access token for FIDO authentication options");
            }
            
            JsonNode options = fidoService.getAuthenticationOptions(request, userAccessToken);
            return ResponseEntity.ok(
                ApiResponse.success("Authentication options retrieved successfully", options)
            );
        } catch (Exception error) {
            logger.error("Error getting authentication options", error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get authentication options: " + error.getMessage()));
        }
    }
    
    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<JsonNode>> authenticateFido(
            @Valid @RequestBody FidoAuthenticationRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        logger.info("Received FIDO authentication request for user: {}", request.getUsername());
        
        try {
            // Extract user access token from Authorization header
            String userAccessToken = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                userAccessToken = authorization.substring(7);
                logger.info("Using user access token for FIDO authentication");
            }
            
            JsonNode result = fidoService.authenticateFido(request, userAccessToken);
            return ResponseEntity.ok(
                ApiResponse.success("FIDO authentication successful", result)
            );
        } catch (Exception error) {
            logger.error("Error authenticating with FIDO", error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to authenticate with FIDO: " + error.getMessage()));
        }
    }
    
    @DeleteMapping("/deregister/{credentialId}")
    public ResponseEntity<ApiResponse<String>> deregisterFidoCredential(
            @PathVariable String credentialId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        logger.info("Received FIDO deregistration request for credential: {}", credentialId);
        
        try {
            // Extract user access token from Authorization header
            String userAccessToken = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                userAccessToken = authorization.substring(7);
                logger.info("Using user access token for FIDO credential deregistration");
            }
            
            fidoService.deregisterFidoCredential(credentialId, userAccessToken);
            return ResponseEntity.ok(
                ApiResponse.success("FIDO credential deregistered successfully")
            );
        } catch (Exception error) {
            logger.error("Error deregistering FIDO credential", error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to deregister FIDO credential: " + error.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("FIDO service is running"));
    }
    
    @GetMapping("/oauth2/code/wso2")
    public ResponseEntity<ApiResponse<Map<String, Object>>> oauth2Callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam(value = "error", required = false) String error) {
        
        logger.info("Received OAuth2 callback - code: {}, state: {}, error: {}", code, state, error);
        
        if (error != null) {
            logger.error("OAuth2 error: {}", error);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("OAuth2 authentication failed: " + error));
        }
        
        try {
            // Exchange authorization code for access token
            Map<String, Object> tokenData = wso2Service.exchangeCodeForToken(code);
            
            logger.info("Successfully exchanged code for token");
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "OAuth2 authentication successful");
            response.put("token_data", tokenData);
            response.put("state", state);
            
            return ResponseEntity.ok(ApiResponse.success("OAuth2 authentication successful", response));
            
        } catch (Exception e) {
            logger.error("Error exchanging code for token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to complete OAuth2 authentication: " + e.getMessage()));
        }
    }
    

} 