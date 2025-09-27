package com.anhngo.wso2.fido.service;

import com.anhngo.wso2.fido.dto.NativeAuthChallengeRequest;
import com.anhngo.wso2.fido.dto.NativeAuthCredentials;
import com.anhngo.wso2.fido.dto.NativeAuthInitRequest;
import com.anhngo.wso2.fido.dto.NativeAuthVerifyRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Base64;

@Service
public class NativeAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(NativeAuthService.class);
    
    private final Wso2Service wso2Service;
    private final ObjectMapper objectMapper;
    
    
    public NativeAuthService(Wso2Service wso2Service) {
        this.wso2Service = wso2Service;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Initialize native authentication flow
     */
    public JsonNode initNativeAuth(NativeAuthInitRequest request) {
        logger.info("Initializing native authentication flow for client: {}", request.getClientId());
        
        try {
            JsonNode result = wso2Service.initNativeAuth(
                request.getClientId(),
                request.getRedirectUri(),
                request.getScope(),
                request.getResponseType(),
                request.getResponseMode()
            );
            
            
            logger.info("Successfully initialized native authentication flow");
            return result;
        } catch (Exception error) {
            logger.error("Error initializing native authentication flow", error);
            throw error;
        }
    }
    
    
    /**
     * Get challenge for passkey authentication
     */
    public JsonNode getPasskeyChallenge(NativeAuthChallengeRequest request) {
        logger.info("Getting passkey challenge for flow: {}", request.getFlowId());
        
        try {
            JsonNode result = wso2Service.getPasskeyChallenge(
                request.getFlowId(),
                request.getAuthenticatorId()
            );
            
            logger.info("Successfully retrieved passkey challenge");
            return result;
        } catch (Exception error) {
            logger.error("Error getting passkey challenge", error);
            throw error;
        }
    }
    
    /**
     * Verify passkey authentication credentials
     */
    public JsonNode verifyPasskeyAuth(NativeAuthVerifyRequest request) {
        logger.info("Verifying passkey authentication for flow: {}", request.getFlowId());
        
        try {
            NativeAuthCredentials credentials = request.getCredentials();
            JsonNode result = wso2Service.verifyPasskeyAuth(
                request.getFlowId(),
                request.getAuthenticatorId(),
                credentials.getClientDataJSON(),
                credentials.getAuthenticatorData(),
                credentials.getSignature(),
                credentials.getUserHandle(),
                request.getRequestId(),
                credentials.getCredentialId()
            );
            
            logger.info("Successfully verified passkey authentication");
            return result;
        } catch (Exception error) {
            logger.error("Error verifying passkey authentication", error);
            throw error;
        }
    }
}
