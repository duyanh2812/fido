package com.anhngo.wso2.fido.dto;

import jakarta.validation.constraints.NotBlank;

public class NativeAuthInitRequest {
    
    @NotBlank(message = "Client ID is required")
    private String clientId;
    
    @NotBlank(message = "Redirect URI is required")
    private String redirectUri;
    
    private String scope = "openid profile";
    private String responseType = "code";
    private String responseMode = "direct";
    
    public NativeAuthInitRequest() {}
    
    public NativeAuthInitRequest(String clientId, String redirectUri) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getRedirectUri() {
        return redirectUri;
    }
    
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public String getResponseType() {
        return responseType;
    }
    
    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }
    
    public String getResponseMode() {
        return responseMode;
    }
    
    public void setResponseMode(String responseMode) {
        this.responseMode = responseMode;
    }
}
