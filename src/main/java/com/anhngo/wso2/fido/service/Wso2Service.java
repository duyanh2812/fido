package com.anhngo.wso2.fido.service;

import com.anhngo.wso2.fido.config.Wso2Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class Wso2Service {
    
    private static final Logger logger = LoggerFactory.getLogger(Wso2Service.class);
    
    private final RestTemplate restTemplate;
    private final Wso2Config wso2Config;
    private final ObjectMapper objectMapper;
    
    // Cache để lưu admin session
    private final Map<String, String> adminSessionCache = new ConcurrentHashMap<>();
    private static final String ADMIN_SESSION_KEY = "admin_session";
    
    public Wso2Service(RestTemplate restTemplate, Wso2Config wso2Config, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.wso2Config = wso2Config;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Lấy admin session từ cache hoặc tạo mới
     */
    public String getAdminSession() {
        String session = adminSessionCache.get(ADMIN_SESSION_KEY);
        if (session == null) {
            session = createAdminSession();
            adminSessionCache.put(ADMIN_SESSION_KEY, session);
        }
        return session;
    }
    
    /**
     * Tạo admin session mới
     */
    private String createAdminSession() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("username", wso2Config.getAdminUsername());
            loginRequest.put("password", wso2Config.getAdminPassword());
            loginRequest.put("grant_type", "password");
            loginRequest.put("scope", "openid");
            
            // Sử dụng OAuth client credentials để authenticate admin
            String credentials = wso2Config.getOauth().getClientKey() + ":" + wso2Config.getOauth().getClientSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            headers.setBasicAuth(encodedCredentials);
            
            HttpEntity<String> request = new HttpEntity<>(buildFormData(loginRequest), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                wso2Config.getBaseUrl() + "/oauth2/token",
                request,
                String.class
            );
            
            logger.info("Admin session creation response: {}", response.getBody());
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            JsonNode accessTokenNode = jsonNode.get("access_token");
            
            if (accessTokenNode == null) {
                logger.error("No access_token found in admin session response. Full response: {}", jsonNode.toString());
                throw new RuntimeException("No access_token found in admin session response");
            }
            
            return accessTokenNode.asText();
            
        } catch (Exception e) {
            logger.error("Error creating admin session", e);
            throw new RuntimeException("Failed to create admin session", e);
        }
    }
    
    /**
     * Refresh admin session nếu cần
     */
    public void refreshAdminSession() {
        adminSessionCache.remove(ADMIN_SESSION_KEY);
        getAdminSession();
    }
    
    public String getAccessToken() {
        try {
            // Log OAuth credentials for debugging
            String clientKey = wso2Config.getOauth().getClientKey();
            String clientSecret = wso2Config.getOauth().getClientSecret();
            
            logger.info("OAuth Client Key: {}", clientKey);
            logger.info("OAuth Client Secret: {}", clientSecret != null ? "***" + clientSecret.substring(Math.max(0, clientSecret.length() - 4)) : "null");
            
            if (clientKey == null || clientSecret == null) {
                throw new RuntimeException("OAuth credentials are null. Client Key: " + clientKey + ", Client Secret: " + (clientSecret != null ? "not null" : "null"));
            }
            
            // Use OAuth client credentials
            String credentials = clientKey + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(encodedCredentials);
            
            Map<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("grant_type", "client_credentials");
            tokenRequest.put("scope", "openid");
            
            HttpEntity<String> request = new HttpEntity<>(buildFormData(tokenRequest), headers);
            
            String url = wso2Config.getBaseUrl() + "/oauth2/token";
            logger.info("=== WSO2 IS OAuth2 Token Request ===");
            logger.info("URL: {}", url);
            logger.info("Headers: {}", headers);
            logger.info("Payload: {}", buildFormData(tokenRequest));
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            logger.info("=== WSO2 IS OAuth2 Token Response ===");
            logger.info("Status: {}", response.getStatusCode());
            logger.info("Response Headers: {}", response.getHeaders());
            logger.info("Response Body: {}", response.getBody());
            
            logger.info("OAuth response status: {}", response.getStatusCode());
            logger.info("OAuth response body: {}", response.getBody());
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            logger.info("OAuth response: {}", jsonNode.toString());
            
            JsonNode accessTokenNode = jsonNode.get("access_token");
            if (accessTokenNode == null) {
                logger.error("No access_token found in response. Full response: {}", jsonNode.toString());
                throw new RuntimeException("No access_token found in OAuth response");
            }
            
            return accessTokenNode.asText();
            
        } catch (Exception e) {
            logger.error("Error getting access token", e);
            throw new RuntimeException("Failed to get access token", e);
        }
    }
    
    public JsonNode getFidoRegistrationOptions(String username, String displayName) {
        return getFidoRegistrationOptions(username, displayName, null);
    }
    
    public JsonNode getFidoRegistrationOptions(String username, String displayName, String userAccessToken) {
        try {
            // Phải sử dụng user access token từ frontend
            if (userAccessToken == null || userAccessToken.trim().isEmpty()) {
                throw new RuntimeException("User access token is required for FIDO registration");
            }
            
            logger.info("Using user access token for FIDO registration options");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(userAccessToken);

            // Tạo form data như WSO2 IS yêu cầu
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("appId", "https://localhost:9443");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            String url = wso2Config.getBaseUrl() + "/t/" + wso2Config.getTenantDomain() + "/api/users/v2/me/webauthn/start-usernameless-registration";
            logger.info("=== WSO2 IS FIDO Registration Options Request ===");
            logger.info("URL: {}", url);
            logger.info("Headers: {}", headers);
            logger.info("Payload: {}", formData);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            logger.info("=== WSO2 IS FIDO Registration Options Response ===");
            logger.info("Status: {}", response.getStatusCode());
            logger.info("Response Headers: {}", response.getHeaders());
            logger.info("Response Body: {}", response.getBody());
            
            // Debug start-usernameless-registration response
            try {
                JsonNode responseNode = objectMapper.readTree(response.getBody());
                if (responseNode.has("publicKeyCredentialCreationOptions")) {
                    JsonNode options = responseNode.get("publicKeyCredentialCreationOptions");
                    if (options.has("challenge")) {
                        String challenge = options.get("challenge").asText();
                        logger.info("Challenge from start-usernameless-registration: {}", challenge);
                    }
                }
            } catch (Exception e) {
                logger.error("Error parsing start-usernameless-registration response: {}", e.getMessage());
            }
            
            return objectMapper.readTree(response.getBody());

        } catch (Exception e) {
            logger.error("Error getting FIDO registration options", e);
            throw new RuntimeException("Failed to get registration options", e);
        }
    }
    
    public JsonNode registerFidoCredential(String username, String displayName, 
                                          String requestId, String attestationObject, String clientDataJSON, String rawId, String userAccessToken) {
        try {
            // Phải sử dụng user access token từ frontend
            if (userAccessToken == null || userAccessToken.trim().isEmpty()) {
                throw new RuntimeException("User access token is required for FIDO registration");
            }
            
            logger.info("Using user access token for FIDO credential registration");
            logger.info("Using requestId from frontend: {}", requestId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(userAccessToken);

            // Thử sử dụng format JSON đơn giản như curl example
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Extract user info from JWT token
            String actualUsername = "admin";
            String actualDisplayName = "admin";
            
            try {
                // Decode JWT token to get user info
                String[] parts = userAccessToken.split("\\.");
                if (parts.length == 3) {
                    String payload = parts[1];
                    // Add padding if needed
                    while (payload.length() % 4 != 0) {
                        payload += "=";
                    }
                    String decodedPayload = new String(java.util.Base64.getUrlDecoder().decode(payload));
                    JsonNode tokenNode = objectMapper.readTree(decodedPayload);
                    
                    if (tokenNode.has("name")) {
                        actualDisplayName = tokenNode.get("name").asText();
                    }
                    if (tokenNode.has("username")) {
                        actualUsername = tokenNode.get("username").asText();
                    } else if (tokenNode.has("sub")) {
                        // Nếu sub là UUID, sử dụng "admin" thay thế
                        String sub = tokenNode.get("sub").asText();
                        if (sub.contains("-")) { // UUID format
                            actualUsername = "admin";
                        } else {
                            actualUsername = sub;
                        }
                    }
                    
                    logger.info("Extracted from JWT - Username: {}, DisplayName: {}", actualUsername, actualDisplayName);
                }
            } catch (Exception e) {
                logger.warn("Could not decode JWT token, using default values: {}", e.getMessage());
            }
            
            // Tạo JSON object thực sự thay vì string
            ObjectNode jsonPayload = objectMapper.createObjectNode();
            jsonPayload.put("requestId", requestId);
            
            ObjectNode credential = objectMapper.createObjectNode();
            credential.put("id", rawId);
            
            ObjectNode credentialResponse = objectMapper.createObjectNode();
            credentialResponse.put("attestationObject", attestationObject);
            credentialResponse.put("clientDataJSON", clientDataJSON);
            
            credential.set("response", credentialResponse);
            credential.set("clientExtensionResults", objectMapper.createObjectNode());
            credential.put("type", "public-key");
            
            jsonPayload.set("credential", credential);

            String jsonBody = objectMapper.writeValueAsString(jsonPayload);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
            logger.info("JSON payload being sent: {}", jsonBody);
            logger.info("Content-Type: {}", headers.getContentType());
            logger.info("RequestId: {}", requestId);
            logger.info("RawId: {}", rawId);
            logger.info("AttestationObject length: {}", attestationObject.length());
            logger.info("ClientDataJSON: {}", clientDataJSON);
            
            // Debug challenge verification - KHÔNG sửa clientDataJSON
            try {
                String clientDataJsonDecoded = new String(java.util.Base64.getUrlDecoder().decode(clientDataJSON));
                logger.info("ClientDataJSON (decoded): {}", clientDataJsonDecoded);
                
                // Parse JSON to extract challenge
                JsonNode clientDataNode = objectMapper.readTree(clientDataJsonDecoded);
                String challenge = clientDataNode.get("challenge").asText();
                String origin = clientDataNode.get("origin").asText();
                logger.info("Challenge from ClientDataJSON: {}", challenge);
                logger.info("Origin from ClientDataJSON: {}", origin);
                
            } catch (Exception e) {
                logger.error("Error parsing ClientDataJSON: {}", e.getMessage());
            }

            String endpointUrl = wso2Config.getBaseUrl() + "/api/users/v2/me/webauthn/finish-registration";
            logger.info("=== WSO2 IS FIDO Registration Finish Request ===");
            logger.info("URL: {}", endpointUrl);
            logger.info("Headers: {}", headers);
            logger.info("Payload: {}", jsonBody);

            ResponseEntity<String> response = restTemplate.postForEntity(
                endpointUrl,
                request,
                String.class
            );

            logger.info("=== WSO2 IS FIDO Registration Finish Response ===");
            logger.info("Status: {}", response.getStatusCode());
            logger.info("Response Headers: {}", response.getHeaders());
            logger.info("Response Body: {}", response.getBody());

            logger.info("FIDO registration response status: {}", response.getStatusCode());
            logger.info("FIDO registration response body: {}", response.getBody());
            return objectMapper.readTree(response.getBody());

        } catch (Exception e) {
            logger.error("Error registering FIDO credential", e);
            throw new RuntimeException("Failed to register FIDO credential", e);
        }
    }
    
    public JsonNode getFidoAuthenticationOptions(String username) {
        return getFidoAuthenticationOptions(username, null);
    }
    
    public JsonNode getFidoAuthenticationOptions(String username, String userAccessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Nếu có user access token, sử dụng nó. Nếu không, sử dụng admin token
            if (userAccessToken != null && !userAccessToken.trim().isEmpty()) {
                headers.setBearerAuth(userAccessToken);
                logger.info("Using user access token for FIDO authentication options");
            } else {
                // Sử dụng OAuth client credentials để lấy admin access token
                String token = getAccessToken();
                headers.setBearerAuth(token);
                logger.info("Using admin access token for FIDO authentication options");
            }

            // Tạo form data như WSO2 IS yêu cầu
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("appId", "https://localhost:9443");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            String url = wso2Config.getBaseUrl() + "/t/" + wso2Config.getTenantDomain() + "/api/users/v2/me/webauthn/start-authentication";
            logger.info("=== WSO2 IS FIDO Authentication Start Request ===");
            logger.info("URL: {}", url);
            logger.info("Headers: {}", headers);
            logger.info("Payload: {}", formData);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            logger.info("=== WSO2 IS FIDO Authentication Start Response ===");
            logger.info("Status: {}", response.getStatusCode());
            logger.info("Response Headers: {}", response.getHeaders());
            logger.info("Response Body: {}", response.getBody());

            logger.info("FIDO authentication options response: {}", response.getBody());
            return objectMapper.readTree(response.getBody());

        } catch (Exception e) {
            logger.error("Error getting FIDO authentication options", e);
            throw new RuntimeException("Failed to get authentication options", e);
        }
    }
    
    public JsonNode authenticateFido(String username, String assertionObject, 
                                    String clientDataJSON, String rawId, String userAccessToken) {
        try {
            // Phải sử dụng user access token từ frontend
            if (userAccessToken == null || userAccessToken.trim().isEmpty()) {
                throw new RuntimeException("User access token is required for FIDO authentication");
            }
            
            logger.info("Using user access token for FIDO authentication");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(userAccessToken);

            // Tạo JSON payload theo format string như trong curl example
            String jsonBody = String.format(
                "\"{requestId=%s, credential={id=%s, response={assertionObject=%s, clientDataJSON=%s}, clientExtensionResults={}, type=public-key}}\"",
                "request-id", rawId, assertionObject, clientDataJSON
            );
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            String url = wso2Config.getBaseUrl() + "/t/" + wso2Config.getTenantDomain() + "/api/users/v2/me/webauthn/finish-authentication";
            logger.info("=== WSO2 IS FIDO Authentication Finish Request ===");
            logger.info("URL: {}", url);
            logger.info("Headers: {}", headers);
            logger.info("Payload: {}", jsonBody);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            logger.info("=== WSO2 IS FIDO Authentication Finish Response ===");
            logger.info("Status: {}", response.getStatusCode());
            logger.info("Response Headers: {}", response.getHeaders());
            logger.info("Response Body: {}", response.getBody());

            logger.info("FIDO authentication response: {}", response.getBody());
            return objectMapper.readTree(response.getBody());

        } catch (Exception e) {
            logger.error("Error authenticating FIDO", e);
            throw new RuntimeException("Failed to authenticate FIDO", e);
        }
    }
    
    /**
     * User login để lấy session ID
     */
    public String userLogin(String username, String password) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("username", username);
            loginRequest.put("password", password);
            loginRequest.put("grant_type", "password");
            loginRequest.put("scope", "openid");
            
            // Sử dụng OAuth client credentials để authenticate user
            String credentials = wso2Config.getOauth().getClientKey() + ":" + wso2Config.getOauth().getClientSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            headers.setBasicAuth(encodedCredentials);
            
            HttpEntity<String> request = new HttpEntity<>(buildFormData(loginRequest), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                wso2Config.getBaseUrl() + "/oauth2/token",
                request,
                String.class
            );
            
            logger.info("User login response: {}", response.getBody());
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            JsonNode accessTokenNode = jsonNode.get("access_token");
            
            if (accessTokenNode == null) {
                logger.error("No access_token found in user login response. Full response: {}", jsonNode.toString());
                throw new RuntimeException("No access_token found in user login response");
            }
            
            return accessTokenNode.asText();
            
        } catch (Exception e) {
            logger.error("Error during user login", e);
            throw new RuntimeException("Failed to login user", e);
        }
    }
    
    private String buildFormData(Map<String, String> data) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (result.length() > 0) {
                result.append("&");
            }
            result.append(entry.getKey()).append("=").append(java.net.URLEncoder.encode(entry.getValue(), java.nio.charset.StandardCharsets.UTF_8));
        }
        return result.toString();
    }
    
    /**
     * Exchange authorization code for access token
     */
        public Map<String, Object> exchangeCodeForToken(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            Map<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("grant_type", "authorization_code");
            tokenRequest.put("code", code);
            tokenRequest.put("redirect_uri", "https://localhost:8080/oauth2/code/wso2");

            // Use OAuth client credentials
            String credentials = wso2Config.getOauth().getClientKey() + ":" + wso2Config.getOauth().getClientSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            headers.setBasicAuth(encodedCredentials);

            HttpEntity<String> request = new HttpEntity<>(buildFormData(tokenRequest), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                wso2Config.getBaseUrl() + "/oauth2/token",
                request,
                String.class
            );

            logger.info("Token exchange response: {}", response.getBody());

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("access_token", jsonNode.get("access_token").asText());
            tokenData.put("refresh_token", jsonNode.get("refresh_token").asText());
            tokenData.put("token_type", jsonNode.get("token_type").asText());
            tokenData.put("expires_in", jsonNode.get("expires_in").asInt());

            return tokenData;

        } catch (Exception e) {
            logger.error("Error exchanging code for token", e);
            throw new RuntimeException("Failed to exchange code for token", e);
        }
    }

    
    /**
     * Initialize native authentication flow - call /oauth2/authorize/ endpoint
     */
    public JsonNode initNativeAuth(String clientId, String redirectUri, String scope, String responseType, String responseMode) {
        try {
            // Call /oauth2/authorize/ endpoint directly as per WSO2 IS documentation
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // Add Basic Authentication with client credentials
            String credentials = clientId + ":" + wso2Config.getOauth().getClientSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            headers.setBasicAuth(encodedCredentials);

            // Build form data for OAuth2 authorize request
            Map<String, String> formData = new HashMap<>();
            formData.put("client_id", clientId);
            formData.put("redirect_uri", redirectUri);
            formData.put("scope", scope != null ? scope : "openid profile");
            formData.put("response_type", responseType != null ? responseType : "code");
            formData.put("response_mode", responseMode != null ? responseMode : "direct");

            HttpEntity<String> request = new HttpEntity<>(buildFormData(formData), headers);

            String url = wso2Config.getBaseUrl() + "/oauth2/authorize/";
            logger.info("=== WSO2 IS Native Auth Init Request ===");
            logger.info("URL: {}", url);
            logger.info("Headers: {}", headers);
            logger.info("Payload: {}", buildFormData(formData));

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            logger.info("=== WSO2 IS Native Auth Init Response ===");
            logger.info("Status: {}", response.getStatusCode());
            logger.info("Response Headers: {}", response.getHeaders());
            logger.info("Response Body: {}", response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("WSO2 IS returned status: " + response.getStatusCode());
            }

            return objectMapper.readTree(response.getBody());

        } catch (Exception e) {
            logger.error("Error calling WSO2 IS native auth init", e);
            throw new RuntimeException("Failed to call WSO2 IS native auth init", e);
        }
    }
    
    /**
     * Get challenge for passkey authentication - call /oauth2/authn/ endpoint
     */
    public JsonNode getPasskeyChallenge(String flowId, String authenticatorId) {
        try {
            // Call /oauth2/authn/ endpoint as per WSO2 IS documentation
            logger.info("Getting passkey challenge for flow: {} with authenticator: {}", flowId, authenticatorId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // Add Basic Authentication with client credentials
            String credentials = wso2Config.getOauth().getClientKey() + ":" + wso2Config.getOauth().getClientSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            headers.setBasicAuth(encodedCredentials);

            // Create request body for /oauth2/authn/ endpoint
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("flowId", flowId);
            
            ObjectNode selectedAuthenticator = objectMapper.createObjectNode();
            selectedAuthenticator.put("authenticatorId", authenticatorId);
            requestBody.set("selectedAuthenticator", selectedAuthenticator);

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            String url = wso2Config.getBaseUrl() + "/oauth2/authn/";
            logger.info("=== WSO2 IS Passkey Challenge Request ===");
            logger.info("URL: {}", url);
            logger.info("Headers: {}", headers);
            logger.info("Payload: {}", objectMapper.writeValueAsString(requestBody));

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            logger.info("=== WSO2 IS Passkey Challenge Response ===");
            logger.info("Status: {}", response.getStatusCode());
            logger.info("Response Headers: {}", response.getHeaders());
            logger.info("Response Body: {}", response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("WSO2 IS returned status: " + response.getStatusCode());
            }

            return objectMapper.readTree(response.getBody());

        } catch (Exception e) {
            logger.error("Error calling WSO2 IS passkey challenge", e);
            throw new RuntimeException("Failed to call WSO2 IS passkey challenge", e);
        }
    }
    
    /**
     * Verify passkey authentication credentials - call /oauth2/authn/ endpoint
     */
    public JsonNode verifyPasskeyAuth(String flowId, String authenticatorId, 
                                     String clientDataJSON, String authenticatorData, 
                                     String signature, String userHandle, String requestId, String credentialId) {
        try {
            // Call /oauth2/authn/ endpoint as per WSO2 IS documentation
            logger.info("Verifying passkey authentication for flow: {} with authenticator: {}", flowId, authenticatorId);
            
            // Debug: Decode and check clientDataJSON
            try {
                String decodedClientData = new String(java.util.Base64.getDecoder().decode(clientDataJSON));
                logger.info("🔍 ClientDataJSON (decoded): {}", decodedClientData);
                JsonNode clientDataObj = objectMapper.readTree(decodedClientData);
                logger.info("🔍 ClientDataJSON challenge: {}", clientDataObj.get("challenge"));
                logger.info("🔍 ClientDataJSON origin: {}", clientDataObj.get("origin"));
            } catch (Exception e) {
                logger.error("❌ Failed to decode clientDataJSON: {}", e.getMessage());
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // Add Basic Authentication with client credentials
            String credentials = wso2Config.getOauth().getClientKey() + ":" + wso2Config.getOauth().getClientSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            headers.setBasicAuth(encodedCredentials);

            // Create WebAuthn credential response according to WSO2 IS documentation
            // Format should be: { "requestId": "...", "credential": { ... } }
            // Use the requestId from challenge data and credentialId from WebAuthn response
            logger.info("🔍 Using requestId from challenge data: {}", requestId);
            logger.info("🔍 Using credentialId from WebAuthn response: {}", credentialId);
            
            // Create the main response object with requestId and credential
            ObjectNode responseObject = objectMapper.createObjectNode();
            responseObject.put("requestId", requestId);
            
            // Create credential object
            ObjectNode credential = objectMapper.createObjectNode();
            credential.put("id", credentialId);
            logger.info("🔍 Using provided credentialId: {}", credentialId);
            
            ObjectNode response = objectMapper.createObjectNode();
            response.put("authenticatorData", authenticatorData);
            response.put("clientDataJSON", clientDataJSON);
            response.put("signature", signature);
            if (userHandle != null) {
                response.put("userHandle", userHandle);
            }
            
            credential.set("response", response);
            credential.set("clientExtensionResults", objectMapper.createObjectNode());
            credential.put("type", "public-key");
            
            // Set credential in the main response object
            responseObject.set("credential", credential);
            
            // Base64 encode the complete response object
            String responseJson = objectMapper.writeValueAsString(responseObject);
            String tokenResponse = java.util.Base64.getEncoder().encodeToString(responseJson.getBytes());
            
            logger.info("Response JSON: {}", responseJson);
            logger.info("TokenResponse (base64): {}", tokenResponse);
            
            // Test if the base64 encoded JSON can be decoded and parsed back
            try {
                String decodedJson = new String(java.util.Base64.getDecoder().decode(tokenResponse));
                logger.info("Decoded JSON from base64: {}", decodedJson);
                
                // Try to parse the decoded JSON to ensure it's valid
                JsonNode parsedNode = objectMapper.readTree(decodedJson);
                logger.info("Successfully parsed decoded JSON: {}", parsedNode);
                
                // Check if all required fields are present
                if (parsedNode.has("requestId") && parsedNode.has("credential")) {
                    logger.info("✅ All required fields present in decoded JSON");
                    JsonNode credentialNode = parsedNode.get("credential");
                    if (credentialNode.has("id") && credentialNode.has("response") && credentialNode.has("type")) {
                        logger.info("✅ Credential object has all required fields");
                    } else {
                        logger.warn("⚠️ Credential object missing required fields");
                    }
                } else {
                    logger.warn("⚠️ Missing required fields in decoded JSON");
                }
                
            } catch (Exception e) {
                logger.error("❌ Failed to decode/parse base64 JSON: {}", e.getMessage());
            }
            
            // Create request body for /oauth2/authn/ endpoint
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("flowId", flowId);
            
            ObjectNode selectedAuthenticator = objectMapper.createObjectNode();
            selectedAuthenticator.put("authenticatorId", authenticatorId);
            
            ObjectNode params = objectMapper.createObjectNode();
            params.put("tokenResponse", tokenResponse);
            selectedAuthenticator.set("params", params);
            
            requestBody.set("selectedAuthenticator", selectedAuthenticator);

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            String url = wso2Config.getBaseUrl() + "/oauth2/authn/";
            logger.info("=== WSO2 IS Passkey Verify Request ===");
            logger.info("URL: {}", url);
            logger.info("Headers: {}", headers);
            logger.info("Payload: {}", objectMapper.writeValueAsString(requestBody));
            
            // Additional logging for debugging
            logger.info("Request body structure:");
            logger.info("- flowId: {}", requestBody.get("flowId"));
            logger.info("- selectedAuthenticator.authenticatorId: {}", requestBody.get("selectedAuthenticator").get("authenticatorId"));
            logger.info("- selectedAuthenticator.params.tokenResponse length: {}", 
                requestBody.get("selectedAuthenticator").get("params").get("tokenResponse").asText().length());

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);

            logger.info("=== WSO2 IS Passkey Verify Response ===");
            logger.info("Status: {}", responseEntity.getStatusCode());
            logger.info("Response Headers: {}", responseEntity.getHeaders());
            logger.info("Response Body: {}", responseEntity.getBody());

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("WSO2 IS returned status: " + responseEntity.getStatusCode());
            }

            return objectMapper.readTree(responseEntity.getBody());

        } catch (Exception e) {
            logger.error("Error calling WSO2 IS passkey verify", e);
            throw new RuntimeException("Failed to call WSO2 IS passkey verify", e);
        }
    }
    
    /**
     * Deregister FIDO credential from WSO2 IS
     */
    public void deregisterFidoCredential(String credentialId, String userAccessToken) {
        try {
            logger.info("Deregistering FIDO credential: {} from WSO2 IS", credentialId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            // Add user access token authentication
            if (userAccessToken != null && !userAccessToken.trim().isEmpty()) {
                headers.setBearerAuth(userAccessToken);
                logger.info("Using user access token for FIDO credential deregistration");
            } else {
                // Fallback to admin credentials if no user token
                String credentials = wso2Config.getOauth().getClientKey() + ":" + wso2Config.getOauth().getClientSecret();
                String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
                headers.setBasicAuth(encodedCredentials);
                logger.info("Using admin credentials for FIDO credential deregistration");
            }
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            String url = wso2Config.getBaseUrl() + "/t/" + wso2Config.getTenantDomain() + "/api/users/v2/me/webauthn/" + credentialId;
            logger.info("Calling WSO2 IS deregister FIDO credential: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.DELETE,
                request,
                String.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("WSO2 IS returned status: " + response.getStatusCode());
            }
            
            logger.info("Successfully deregistered FIDO credential: {} from WSO2 IS", credentialId);
            
        } catch (Exception e) {
            logger.error("Error deregistering FIDO credential: {} from WSO2 IS", credentialId, e);
            throw new RuntimeException("Failed to deregister FIDO credential from WSO2 IS", e);
        }
    }
} 