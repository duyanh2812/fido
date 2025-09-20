package com.anhngo.wso2.fido.dto;

import jakarta.validation.constraints.NotBlank;

public class FidoRegistrationOptionsRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Display name is required")
    private String displayName;
    
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
} 