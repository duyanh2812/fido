package com.anhngo.wso2.fido.service;

import com.anhngo.wso2.fido.dto.FidoAuthenticationOptionsRequest;
import com.anhngo.wso2.fido.dto.FidoAuthenticationRequest;
import com.anhngo.wso2.fido.dto.FidoRegistrationOptionsRequest;
import com.anhngo.wso2.fido.dto.FidoRegistrationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FidoService {
    
    private static final Logger logger = LoggerFactory.getLogger(FidoService.class);
    
    private final Wso2Service wso2Service;
    
    public FidoService(Wso2Service wso2Service) {
        this.wso2Service = wso2Service;
    }
    
    public JsonNode getRegistrationOptions(FidoRegistrationOptionsRequest request) {
        return getRegistrationOptions(request, null);
    }
    
    public JsonNode getRegistrationOptions(FidoRegistrationOptionsRequest request, String userAccessToken) {
        logger.info("Getting FIDO registration options for user: {}", request.getUsername());
        
        try {
            JsonNode result = wso2Service.getFidoRegistrationOptions(request.getUsername(), request.getDisplayName(), userAccessToken);
            logger.info("Successfully retrieved registration options from WSO2 for user: {}", request.getUsername());
            return result;
        } catch (Exception error) {
            logger.error("Error getting registration options for user: {}", request.getUsername(), error);
            throw error;
        }
    }
    
    public JsonNode registerFidoCredential(FidoRegistrationRequest request) {
        return registerFidoCredential(request, null);
    }
    
    public JsonNode registerFidoCredential(FidoRegistrationRequest request, String userAccessToken) {
        logger.info("Registering FIDO credential for user: {}", request.getUsername());
        
        try {
            JsonNode result = wso2Service.registerFidoCredential(
                request.getUsername(),
                request.getDisplayName(),
                request.getRequestId(),
                request.getAttestationObject(),
                request.getClientDataJSON(),
                request.getRawId(),
                userAccessToken
            );
            logger.info("Successfully registered FIDO credential with WSO2 for user: {}", request.getUsername());
            return result;
        } catch (Exception error) {
            logger.error("Error registering FIDO credential for user: {}", request.getUsername(), error);
            throw error;
        }
    }
    
    public JsonNode getAuthenticationOptions(FidoAuthenticationOptionsRequest request) {
        return getAuthenticationOptions(request, null);
    }
    
    public JsonNode getAuthenticationOptions(FidoAuthenticationOptionsRequest request, String userAccessToken) {
        logger.info("Getting FIDO authentication options for user: {}", request.getUsername());
        
        try {
            JsonNode result = wso2Service.getFidoAuthenticationOptions(request.getUsername(), userAccessToken);
            logger.info("Successfully retrieved authentication options from WSO2 for user: {}", request.getUsername());
            return result;
        } catch (Exception error) {
            logger.error("Error getting authentication options for user: {}", request.getUsername(), error);
            throw error;
        }
    }
    
    public JsonNode authenticateFido(FidoAuthenticationRequest request) {
        return authenticateFido(request, null);
    }
    
    public JsonNode authenticateFido(FidoAuthenticationRequest request, String userAccessToken) {
        logger.info("Authenticating FIDO for user: {}", request.getUsername());
        
        try {
            JsonNode result = wso2Service.authenticateFido(
                request.getUsername(),
                request.getAssertionObject(),
                request.getClientDataJSON(),
                request.getRawId(),
                userAccessToken
            );
            logger.info("Successfully authenticated FIDO with WSO2 for user: {}", request.getUsername());
            return result;
        } catch (Exception error) {
            logger.error("Error authenticating FIDO for user: {}", request.getUsername(), error);
            throw error;
        }
    }
    
    public void deregisterFidoCredential(String credentialId, String userAccessToken) {
        logger.info("Deregistering FIDO credential: {}", credentialId);
        
        try {
            wso2Service.deregisterFidoCredential(credentialId, userAccessToken);
            logger.info("Successfully deregistered FIDO credential: {}", credentialId);
        } catch (Exception error) {
            logger.error("Error deregistering FIDO credential: {}", credentialId, error);
            throw error;
        }
    }
} 