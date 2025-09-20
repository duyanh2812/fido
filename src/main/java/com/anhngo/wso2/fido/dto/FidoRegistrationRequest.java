package com.anhngo.wso2.fido.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class FidoRegistrationRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Display name is required")
    @JsonProperty("displayName")
    private String displayName;
    
    @NotBlank(message = "Request ID is required")
    @JsonProperty("requestId")
    private String requestId;
    
    @NotBlank(message = "Attestation object is required")
    @JsonProperty("attestationObject")
    private String attestationObject;
    
    @NotBlank(message = "Client data JSON is required")
    @JsonProperty("clientDataJSON")
    private String clientDataJSON;
    
    @NotBlank(message = "Raw ID is required")
    @JsonProperty("rawId")
    private String rawId;
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getAttestationObject() {
        return attestationObject;
    }
    
    public void setAttestationObject(String attestationObject) {
        this.attestationObject = attestationObject;
    }
    
    public String getClientDataJSON() {
        return clientDataJSON;
    }
    
    public void setClientDataJSON(String clientDataJSON) {
        this.clientDataJSON = clientDataJSON;
    }
    
    public String getRawId() {
        return rawId;
    }
    
    public void setRawId(String rawId) {
        this.rawId = rawId;
    }
} 