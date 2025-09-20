package com.anhngo.wso2.fido.dto;

import jakarta.validation.constraints.NotBlank;

public class FidoAuthenticationOptionsRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
} 