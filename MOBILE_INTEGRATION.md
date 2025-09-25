# 📱 FIDO Mobile App Integration Guide

This guide explains how to integrate mobile applications (iOS/Android) with the FIDO Spring Boot application.

## 🚀 Quick Start

1. **Start the services:**
   ```bash
   ./rebuild-mobile-integration.sh
   ```

2. **Test the endpoints:**
   ```bash
   # Test Apple App Site Association
   curl -k https://localhost:8080/.well-known/apple-app-site-association
   
   # Test Android App Links
   curl -k https://localhost:8080/.well-known/assetlinks.json
   
   # Test FIDO Mobile API
   curl -k https://localhost:8080/.well-known/fido/mobile/health
   ```

## 📋 Available Endpoints

### Apple App Site Association
- **URL:** `https://localhost:8080/.well-known/apple-app-site-association`
- **Purpose:** iOS app integration with WebAuthn
- **Response:**
  ```json
  {
    "webcredentials": {
      "apps": ["EYP853985W.com.example.passkeyapp"]
    }
  }
  ```

### Android App Links
- **URL:** `https://localhost:8080/.well-known/assetlinks.json`
- **Purpose:** Android app integration with WebAuthn
- **Response:**
  ```json
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "com.example.passkeyapp",
      "sha256_cert_fingerprints": ["14:6D:E9:83:C5:73:06:50:D8:EE:B9:95:2F:34:FC:64:16:A0:83:42:E6:1D:BE:A8:8A:04:96:B2:3F:CF:44:E5"]
    }
  }
  ```

### FIDO Mobile API

#### Health Check
- **URL:** `GET https://localhost:8080/.well-known/fido/mobile/health`
- **Response:**
  ```json
  {
    "status": "UP",
    "service": "FIDO Mobile API",
    "version": "1.0.0",
    "timestamp": 1695468000000
  }
  ```

#### Configuration
- **URL:** `GET https://localhost:8080/.well-known/fido/mobile/config`
- **Response:**
  ```json
  {
    "appName": "FIDO Demo Mobile",
    "version": "1.0.0",
    "supportedAlgorithms": ["ES256", "RS256"],
    "supportedAttestation": ["direct", "indirect"],
    "timeout": 60000,
    "rpId": "localhost"
  }
  ```

#### Registration Options
- **URL:** `GET https://localhost:8080/.well-known/fido/mobile/registration-options`
- **Parameters:**
  - `userId` (optional): User ID for registration
  - `userName` (optional): Username for registration
- **Response:**
  ```json
  {
    "challenge": "base64-encoded-challenge",
    "rp": {
      "id": "localhost",
      "name": "FIDO Demo Application"
    },
    "user": {
      "id": "mobile-user-123",
      "name": "mobile@example.com",
      "displayName": "Mobile User"
    },
    "pubKeyCredParams": [
      {"type": "public-key", "alg": -7},
      {"type": "public-key", "alg": -257}
    ],
    "authenticatorSelection": {
      "authenticatorAttachment": "platform",
      "userVerification": "required"
    },
    "timeout": 60000,
    "attestation": "direct"
  }
  ```

#### Authentication Options
- **URL:** `GET https://localhost:8080/.well-known/fido/mobile/authentication-options`
- **Parameters:**
  - `userId` (optional): User ID for authentication
- **Response:**
  ```json
  {
    "challenge": "base64-encoded-challenge",
    "timeout": 60000,
    "rpId": "localhost",
    "allowCredentials": [
      {
        "type": "public-key",
        "id": "mobile-credential-id-123"
      }
    ],
    "userVerification": "required"
  }
  ```

#### Register Credential
- **URL:** `POST https://localhost:8080/.well-known/fido/mobile/register`
- **Body:** FIDO registration response from mobile app
- **Response:**
  ```json
  {
    "success": true,
    "message": "Registration successful",
    "credentialId": "credential-id-from-response"
  }
  ```

#### Authenticate
- **URL:** `POST https://localhost:8080/.well-known/fido/mobile/authenticate`
- **Body:** FIDO authentication response from mobile app
- **Response:**
  ```json
  {
    "success": true,
    "message": "Authentication successful",
    "userId": "mobile-user-123"
  }
  ```

## 🍎 iOS Integration

### 1. Configure Associated Domains
Add to your iOS app's `Info.plist`:
```xml
<key>com.apple.developer.associated-domains</key>
<array>
    <string>webcredentials:localhost</string>
</array>
```

### 2. Handle App Site Association
```swift
import AuthenticationServices

// Check if the domain supports WebAuthn
func checkWebAuthnSupport() {
    let domain = "localhost"
    let service = ASWebAuthenticationSession.getWebAuthnSupport(for: domain) { result in
        switch result {
        case .supported:
            print("WebAuthn supported")
        case .notSupported:
            print("WebAuthn not supported")
        case .unknown:
            print("WebAuthn support unknown")
        }
    }
}
```

### 3. Use FIDO Endpoints
```swift
// Get registration options
func getRegistrationOptions() async throws -> RegistrationOptions {
    let url = URL(string: "https://localhost:8080/.well-known/fido/mobile/registration-options")!
    let (data, _) = try await URLSession.shared.data(from: url)
    return try JSONDecoder().decode(RegistrationOptions.self, from: data)
}
```

## 🤖 Android Integration

### 1. Configure App Links
Add to your Android app's `AndroidManifest.xml`:
```xml
<activity android:name=".MainActivity">
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https"
              android:host="localhost" />
    </intent-filter>
</activity>
```

### 2. Handle App Links
```kotlin
// Check if the domain supports WebAuthn
fun checkWebAuthnSupport() {
    val domain = "localhost"
    // Use Android's WebAuthn API
    val webAuthnManager = WebAuthnManager()
    // Implementation depends on your WebAuthn library
}
```

### 3. Use FIDO Endpoints
```kotlin
// Get registration options
suspend fun getRegistrationOptions(): RegistrationOptions {
    val url = "https://localhost:8080/.well-known/fido/mobile/registration-options"
    val response = httpClient.get(url)
    return Json.decodeFromString<RegistrationOptions>(response.bodyAsText())
}
```

## 🔧 CORS Configuration

The application is configured to allow CORS requests from mobile apps:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

## 🧪 Testing

### Test Apple App Site Association
```bash
curl -k https://localhost:8080/.well-known/apple-app-site-association
```

### Test Android App Links
```bash
curl -k https://localhost:8080/.well-known/assetlinks.json
```

### Test FIDO Mobile API
```bash
# Health check
curl -k https://localhost:8080/.well-known/fido/mobile/health

# Get config
curl -k https://localhost:8080/.well-known/fido/mobile/config

# Get registration options
curl -k "https://localhost:8080/.well-known/fido/mobile/registration-options?userId=test&userName=test@example.com"

# Get authentication options
curl -k "https://localhost:8080/.well-known/fido/mobile/authentication-options?userId=test"
```

## 🚨 Security Considerations

1. **HTTPS Required:** All endpoints require HTTPS
2. **Certificate Validation:** Mobile apps should validate server certificates
3. **Origin Validation:** Server validates request origins
4. **Credential Storage:** Store credentials securely in mobile app
5. **User Verification:** Require user verification for sensitive operations

## 📚 Additional Resources

- [WebAuthn Specification](https://www.w3.org/TR/webauthn-2/)
- [Apple App Site Association](https://developer.apple.com/documentation/safariservices/supporting_associated_domains)
- [Android App Links](https://developer.android.com/training/app-links)
- [FIDO Alliance](https://fidoalliance.org/)

## 🆘 Troubleshooting

### Common Issues

1. **CORS Errors:** Ensure mobile app includes proper headers
2. **Certificate Errors:** Use `-k` flag for testing, implement proper certificate validation in production
3. **Network Errors:** Ensure mobile app can reach `localhost:8080`
4. **WebAuthn Errors:** Check browser/WebView support for WebAuthn

### Debug Commands

```bash
# Check service status
docker-compose ps

# View logs
docker-compose logs -f fido-app

# Test endpoints
curl -k https://localhost:8080/.well-known/fido/mobile/health
```
