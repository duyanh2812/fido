package com.anhngo.wso2.fido.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "fido")
public class FidoConfig {
    
    private Trusted trusted;
    private RelyingParty relyingParty;
    
    public static class Trusted {
        private List<String> origins;
        
        public List<String> getOrigins() {
            return origins;
        }
        
        public void setOrigins(List<String> origins) {
            this.origins = origins;
        }
    }
    
    public static class RelyingParty {
        private String id;
        private String name;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public Trusted getTrusted() {
        return trusted;
    }
    
    public void setTrusted(Trusted trusted) {
        this.trusted = trusted;
    }
    
    public RelyingParty getRelyingParty() {
        return relyingParty;
    }
    
    public void setRelyingParty(RelyingParty relyingParty) {
        this.relyingParty = relyingParty;
    }
} 