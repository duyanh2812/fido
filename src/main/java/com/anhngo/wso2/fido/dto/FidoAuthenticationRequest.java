package com.anhngo.wso2.fido.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class FidoAuthenticationRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Assertion object is required")
    @JsonProperty("assertionObject")
    private String assertionObject;
    
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
    
    public String getAssertionObject() {
        return assertionObject;
    }
    
    public void setAssertionObject(String assertionObject) {
        this.assertionObject = assertionObject;
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