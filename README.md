# FIDO Authentication with WSO2 IS

Spring Boot application để tích hợp FIDO authentication với WSO2 Identity Server 6.1.0.

## Cấu hình WSO2 IS

### 1. Cài đặt và khởi động WSO2 IS 6.1.0

```bash
# Download WSO2 IS 6.1.0
# Extract và chạy
./bin/wso2server.sh
```

### 2. Cấu hình Service Provider

1. Đăng nhập vào WSO2 IS Console: `https://localhost:9443/carbon`
2. Tạo Service Provider với tên: `SpringBootFIDOApp`
3. Cấu hình OAuth:
   - OAuth Client Key: `fG2qUiWLuNkM2lc4J6qpYDL51Dca`
   - OAuth Client Secret: `8sNV4rXOY8j2SIOgzk6Fd3TVBusa`
   - Callback URL: `https://localhost:8080/api/fido/callback`

### 3. Cấu hình FIDO

Trong file `repository/conf/identity/identity.xml`, thêm cấu hình:

```xml
[fido.trusted]
origins=["https://localhost:9443", "https://localhost:8080"]
```

## Cấu hình Spring Boot Application

### 1. Cập nhật application.properties

```properties
# WSO2 IS Configuration
wso2.is.base-url=https://localhost:9443
wso2.is.admin.username=admin
wso2.is.admin.password=admin
wso2.is.oauth.client.key=fG2qUiWLuNkM2lc4J6qpYDL51Dca
wso2.is.oauth.client.secret=8sNV4rXOY8j2SIOgzk6Fd3TVBusa
wso2.is.service.provider.name=SpringBootFIDOApp
```

### 2. Chạy ứng dụng

```bash
./gradlew bootRun
```

## API Endpoints

### 1. Health Check
```
GET /api/fido/health
```

### 2. FIDO Registration Flow

#### 2.1. Lấy Registration Options
```
POST /api/fido/registration-options
Content-Type: application/json

{
  "username": "user@example.com",
  "displayName": "John Doe"
}
```

#### 2.2. Đăng ký FIDO Credential
```
POST /api/fido/register
Content-Type: application/json

{
  "username": "user@example.com",
  "displayName": "John Doe",
  "attestationObject": "base64_encoded_attestation_object",
  "clientDataJSON": "base64_encoded_client_data",
  "rawId": "base64_encoded_raw_id"
}
```

### 3. FIDO Authentication Flow

#### 3.1. Lấy Authentication Options
```
POST /api/fido/authentication-options
Content-Type: application/json

{
  "username": "user@example.com"
}
```

#### 3.2. Xác thực FIDO
```
POST /api/fido/authenticate
Content-Type: application/json

{
  "username": "user@example.com",
  "assertionObject": "base64_encoded_assertion_object",
  "clientDataJSON": "base64_encoded_client_data",
  "rawId": "base64_encoded_raw_id"
}
```

## Response Format

Tất cả API responses đều có format:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    // Response data
  },
  "error": null
}
```

## iOS Integration

### 1. Sử dụng WebAuthn API

```swift
import AuthenticationServices

class FidoAuthenticator {
    
    func registerFidoCredential(username: String, displayName: String) {
        // 1. Lấy registration options từ server
        getRegistrationOptions(username: username, displayName: displayName) { options in
            // 2. Tạo credential với WebAuthn
            let credential = ASAuthorizationPlatformPublicKeyCredentialDescriptor(
                credentialID: options.rawId,
                transports: [.internal]
            )
            
            // 3. Thực hiện registration
            let request = ASAuthorizationPlatformPublicKeyCredentialRegistrationRequest(
                challenge: options.challenge,
                name: displayName,
                userID: username
            )
            
            // 4. Gửi kết quả về server
            self.registerCredential(request: request)
        }
    }
    
    func authenticateFido(username: String) {
        // 1. Lấy authentication options từ server
        getAuthenticationOptions(username: username) { options in
            // 2. Tạo assertion request
            let request = ASAuthorizationPlatformPublicKeyCredentialAssertionRequest(
                challenge: options.challenge,
                credentialID: options.allowCredentials.first?.id
            )
            
            // 3. Thực hiện authentication
            // 4. Gửi kết quả về server
        }
    }
}
```

### 2. Error Handling

```swift
enum FidoError: Error {
    case registrationFailed(String)
    case authenticationFailed(String)
    case networkError(String)
}

func handleFidoError(_ error: FidoError) {
    switch error {
    case .registrationFailed(let message):
        print("Registration failed: \(message)")
    case .authenticationFailed(let message):
        print("Authentication failed: \(message)")
    case .networkError(let message):
        print("Network error: \(message)")
    }
}
```

## Troubleshooting

### 1. SSL Certificate Issues

Nếu gặp lỗi SSL certificate, có thể:
- Import WSO2 IS certificate vào truststore
- Hoặc disable SSL verification (chỉ cho development)

### 2. CORS Issues

Đảm bảo CORS được cấu hình đúng trong WSO2 IS và Spring Boot app.

### 3. Authentication Issues

- Kiểm tra username/password của admin user
- Kiểm tra OAuth client configuration
- Kiểm tra service provider configuration

## Logging

Application sử dụng SLF4J với Logback. Log levels có thể được cấu hình trong `application.properties`:

```properties
logging.level.com.anhngo.wso2.fido=DEBUG
logging.level.org.springframework.web=DEBUG
```

## Security Considerations

1. **Production Environment**: 
   - Sử dụng proper SSL certificates
   - Cấu hình CORS restrictions
   - Implement rate limiting
   - Sử dụng secure password storage

2. **FIDO Security**:
   - Validate attestation objects
   - Implement proper challenge verification
   - Use secure random for challenges
   - Implement proper session management

3. **API Security**:
   - Implement proper authentication/authorization
   - Validate all inputs
   - Implement proper error handling
   - Use HTTPS in production 