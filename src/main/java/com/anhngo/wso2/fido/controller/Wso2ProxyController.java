package com.anhngo.wso2.fido.controller;

import com.anhngo.wso2.fido.config.Wso2Config;
import com.anhngo.wso2.fido.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/wso2-proxy")
@CrossOrigin(origins = "*")
public class Wso2ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(Wso2ProxyController.class);

    private final RestTemplate restTemplate;
    private final Wso2Config wso2Config;

    public Wso2ProxyController(RestTemplate restTemplate, Wso2Config wso2Config) {
        this.restTemplate = restTemplate;
        this.wso2Config = wso2Config;
    }

    /**
     * Proxy endpoint để lấy access token từ WSO2 IS
     */
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAccessToken(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        logger.info("Getting access token for user: {}", username);

        try {
            String tokenUrl = wso2Config.getBaseUrl() + "/oauth2/token";

            // Tạo headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Tạo form data
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "password");
            formData.add("username", username);
            formData.add("password", password);
            formData.add("client_id", wso2Config.getOauth().getClientKey());
            formData.add("client_secret", wso2Config.getOauth().getClientSecret());
            formData.add("scope", "openid internal_login");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            logger.info("Making request to WSO2 IS token endpoint: {}", tokenUrl);

            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                // Parse JSON response
                org.springframework.http.converter.json.Jackson2ObjectMapperBuilder builder = 
                    new org.springframework.http.converter.json.Jackson2ObjectMapperBuilder();
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = builder.build();
                
                @SuppressWarnings("unchecked")
                Map<String, Object> tokenData = objectMapper.readValue(response.getBody(), Map.class);

                logger.info("Successfully obtained access token for user: {}", username);

                return ResponseEntity.ok(
                    ApiResponse.success("Access token obtained successfully", tokenData)
                );
            } else {
                logger.error("Failed to get access token. Status: {}", response.getStatusCode());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to get access token. Status: " + response.getStatusCode()));
            }

        } catch (Exception e) {
            logger.error("Error getting access token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get access token: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthData = Map.of(
            "status", "UP",
            "service", "WSO2 Proxy",
            "timestamp", System.currentTimeMillis()
        );

        return ResponseEntity.ok(
            ApiResponse.success("WSO2 Proxy is healthy", healthData)
        );
    }
} 