package com.anhngo.wso2.fido.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NativeAuthVerifyRequest {
    
    @NotBlank(message = "Flow ID is required")
    private String flowId;
    
    @NotBlank(message = "Authenticator ID is required")
    private String authenticatorId;
    
    @NotNull(message = "Credentials are required")
    @Valid
    private NativeAuthCredentials credentials;
    
    @NotBlank(message = "Request ID is required")
    private String requestId;
    
    public NativeAuthVerifyRequest() {}
    
    public NativeAuthVerifyRequest(String flowId, String authenticatorId, NativeAuthCredentials credentials, String requestId) {
        this.flowId = flowId;
        this.authenticatorId = authenticatorId;
        this.credentials = credentials;
        this.requestId = requestId;
    }
    
    public String getFlowId() {
        return flowId;
    }
    
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }
    
    public String getAuthenticatorId() {
        return authenticatorId;
    }
    
    public void setAuthenticatorId(String authenticatorId) {
        this.authenticatorId = authenticatorId;
    }
    
    public NativeAuthCredentials getCredentials() {
        return credentials;
    }
    
    public void setCredentials(NativeAuthCredentials credentials) {
        this.credentials = credentials;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
