package com.anhngo.wso2.fido.dto;

import jakarta.validation.constraints.NotBlank;

public class NativeAuthChallengeRequest {
    
    @NotBlank(message = "Flow ID is required")
    private String flowId;
    
    @NotBlank(message = "Authenticator ID is required")
    private String authenticatorId;
    
    public NativeAuthChallengeRequest() {}
    
    public NativeAuthChallengeRequest(String flowId, String authenticatorId) {
        this.flowId = flowId;
        this.authenticatorId = authenticatorId;
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
}
