package com.anhngo.wso2.fido.service;

import com.anhngo.wso2.fido.config.Wso2Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OAuth2Service {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Service.class);

    private final RestTemplate restTemplate;
    private final Wso2Config wso2Config;
    private final ObjectMapper objectMapper;

    public OAuth2Service(RestTemplate restTemplate, Wso2Config wso2Config, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.wso2Config = wso2Config;
        this.objectMapper = objectMapper;
    }

    /**
     * Exchange authorization code để lấy access token (alias method)
     */
    public Map<String, Object> exchangeCodeForTokens(String authorizationCode) {
        return exchangeAuthorizationCode(authorizationCode);
    }

    /**
     * Exchange authorization code để lấy access token
     */
    public Map<String, Object> exchangeAuthorizationCode(String authorizationCode) {
        try {
            String tokenUrl = wso2Config.getBaseUrl() + "/oauth2/token";

            // Tạo Basic Auth header
            String credentials = wso2Config.getOauth().getClientKey() + ":" + wso2Config.getOauth().getClientSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

            // Tạo headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(encodedCredentials);

            // Tạo form data
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("code", authorizationCode);
            formData.add("redirect_uri", wso2Config.getOauth().getRedirectUri());
            formData.add("scope", "openid");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            logger.info("=== WSO2 IS OAuth2 Token Exchange Request ===");
            logger.info("URL: {}", tokenUrl);
            logger.info("Headers: {}", headers);
            logger.info("Payload: {}", formData);

            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

            logger.info("=== WSO2 IS OAuth2 Token Exchange Response ===");
            logger.info("Status: {}", response.getStatusCode());
            logger.info("Response Headers: {}", response.getHeaders());
            logger.info("Response Body: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                Map<String, Object> result = new HashMap<>();
                
                // Convert JsonNode to Map
                responseJson.fields().forEachRemaining(entry -> 
                    result.put(entry.getKey(), entry.getValue().asText())
                );

                logger.info("Successfully exchanged authorization code for tokens");
                return result;
            } else {
                logger.error("Failed to exchange authorization code. Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to exchange authorization code. Status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Error exchanging authorization code", e);
            throw new RuntimeException("Failed to exchange authorization code", e);
        }
    }

    /**
     * Build authorization URL cho OAuth2 Authorization Code flow
     */
    public String buildAuthorizationUrl(String redirectUri, String state) {
        try {
            String authUrl = UriComponentsBuilder
                .fromHttpUrl(wso2Config.getBaseUrl() + "/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", wso2Config.getOauth().getClientKey())
                .queryParam("redirect_uri", redirectUri != null ? redirectUri : wso2Config.getOauth().getRedirectUri())
                .queryParam("scope", "openid")
                .queryParam("state", state != null ? state : generateState())
                .build()
                .toUriString();

            logger.info("Built authorization URL: {}", authUrl);
            return authUrl;

        } catch (Exception e) {
            logger.error("Error building authorization URL", e);
            throw new RuntimeException("Failed to build authorization URL", e);
        }
    }

    /**
     * Refresh access token
     */
    public Map<String, Object> refreshAccessToken(String refreshToken) {
        try {
            String tokenUrl = wso2Config.getBaseUrl() + "/oauth2/token";

            // Tạo Basic Auth header
            String credentials = wso2Config.getOauth().getClientKey() + ":" + wso2Config.getOauth().getClientSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

            // Tạo headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(encodedCredentials);

            // Tạo form data
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "refresh_token");
            formData.add("refresh_token", refreshToken);
            formData.add("scope", "openid");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            logger.info("Refreshing access token at: {}", tokenUrl);

            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                Map<String, Object> result = new HashMap<>();
                
                // Convert JsonNode to Map
                responseJson.fields().forEachRemaining(entry -> 
                    result.put(entry.getKey(), entry.getValue().asText())
                );

                logger.info("Successfully refreshed access token");
                return result;
            } else {
                logger.error("Failed to refresh access token. Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to refresh access token. Status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Error refreshing access token", e);
            throw new RuntimeException("Failed to refresh access token", e);
        }
    }

    /**
     * Revoke token
     */
    public void revokeToken(String token, String tokenTypeHint) {
        try {
            String revokeUrl = wso2Config.getBaseUrl() + "/oauth2/revoke";

            // Tạo Basic Auth header
            String credentials = wso2Config.getOauth().getClientKey() + ":" + wso2Config.getOauth().getClientSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

            // Tạo headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(encodedCredentials);

            // Tạo form data
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("token", token);
            formData.add("token_type_hint", tokenTypeHint);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            logger.info("Revoking token at: {}", revokeUrl);

            ResponseEntity<String> response = restTemplate.postForEntity(revokeUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Successfully revoked token");
            } else {
                logger.error("Failed to revoke token. Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to revoke token. Status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Error revoking token", e);
            throw new RuntimeException("Failed to revoke token", e);
        }
    }

    /**
     * Introspect token
     */
    public Map<String, Object> introspectToken(String token) {
        try {
            String introspectUrl = wso2Config.getBaseUrl() + "/oauth2/introspect";

            // Tạo Basic Auth header
            String credentials = wso2Config.getOauth().getClientKey() + ":" + wso2Config.getOauth().getClientSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

            // Tạo headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(encodedCredentials);

            // Tạo form data
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("token", token);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            logger.info("Introspecting token at: {}", introspectUrl);

            ResponseEntity<String> response = restTemplate.postForEntity(introspectUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                Map<String, Object> result = new HashMap<>();
                
                // Convert JsonNode to Map
                responseJson.fields().forEachRemaining(entry -> {
                    if (entry.getValue().isBoolean()) {
                        result.put(entry.getKey(), entry.getValue().asBoolean());
                    } else if (entry.getValue().isNumber()) {
                        result.put(entry.getKey(), entry.getValue().asLong());
                    } else {
                        result.put(entry.getKey(), entry.getValue().asText());
                    }
                });

                logger.info("Successfully introspected token");
                return result;
            } else {
                logger.error("Failed to introspect token. Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to introspect token. Status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Error introspecting token", e);
            throw new RuntimeException("Failed to introspect token", e);
        }
    }

    /**
     * Generate random state parameter
     */
    private String generateState() {
        return UUID.randomUUID().toString();
    }
} 