package com.anhngo.wso2.fido.dto;

import jakarta.validation.constraints.NotBlank;

public class NativeAuthCredentials {
    
    @NotBlank(message = "Client Data JSON is required")
    private String clientDataJSON;
    
    @NotBlank(message = "Authenticator Data is required")
    private String authenticatorData;
    
    @NotBlank(message = "Signature is required")
    private String signature;
    
    private String userHandle;
    
    @NotBlank(message = "Credential ID is required")
    private String credentialId;
    
    public NativeAuthCredentials() {}
    
    public NativeAuthCredentials(String clientDataJSON, String authenticatorData, String signature, String credentialId) {
        this.clientDataJSON = clientDataJSON;
        this.authenticatorData = authenticatorData;
        this.signature = signature;
        this.credentialId = credentialId;
    }
    
    public String getClientDataJSON() {
        return clientDataJSON;
    }
    
    public void setClientDataJSON(String clientDataJSON) {
        this.clientDataJSON = clientDataJSON;
    }
    
    public String getAuthenticatorData() {
        return authenticatorData;
    }
    
    public void setAuthenticatorData(String authenticatorData) {
        this.authenticatorData = authenticatorData;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    public String getUserHandle() {
        return userHandle;
    }
    
    public void setUserHandle(String userHandle) {
        this.userHandle = userHandle;
    }
    
    public String getCredentialId() {
        return credentialId;
    }
    
    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }
}
