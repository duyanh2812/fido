// Global variables
let accessToken = null;
let refreshToken = null;
let currentUser = null;
let currentDisplayName = null;

// API endpoints
const API_BASE = window.location.origin;
const WSO2_BASE = 'https://localhost:9443';
const FIDO_ENDPOINTS = {
    registrationOptions: `${API_BASE}/fido/registration-options`,
    register: `${API_BASE}/fido/register`,
    authenticationOptions: `${API_BASE}/fido/authentication-options`,
    authenticate: `${API_BASE}/fido/authenticate`,
    health: `${API_BASE}/fido/health`
};

const NATIVE_AUTH_ENDPOINTS = {
    init: `${API_BASE}/native-auth/init`,
    challenge: `${API_BASE}/native-auth/challenge`,
    verify: `${API_BASE}/native-auth/verify`,
    health: `${API_BASE}/native-auth/health`
};

// WSO2 OAuth2 credentials - removed as backend uses config

// Utility functions
function decodeJWT(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (error) {
        console.error('Error decoding JWT:', error);
        return null;
    }
}

// WebAuthn utility functions
function base64ToArrayBuffer(base64) {
    // Convert base64url to base64
    const base64Standard = base64.replace(/-/g, '+').replace(/_/g, '/');
    
    // Add padding if needed
    const padded = base64Standard + '='.repeat((4 - base64Standard.length % 4) % 4);
    
    try {
        const binaryString = atob(padded);
        const bytes = new Uint8Array(binaryString.length);
        for (let i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i);
        }
        return bytes.buffer;
    } catch (error) {
        console.error('Error converting base64 to ArrayBuffer:', error);
        console.error('Original base64:', base64);
        console.error('Standardized base64:', padded);
        throw error;
    }
}

function arrayBufferToBase64(buffer) {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    for (let i = 0; i < bytes.byteLength; i++) {
        binary += String.fromCharCode(bytes[i]);
    }
    // Convert to base64url format (remove padding and replace + with -, / with _)
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
}

function showLoading(message = 'Processing...') {
    document.getElementById('loading-message').textContent = message;
    document.getElementById('loading-modal').style.display = 'flex';
}

function hideLoading() {
    document.getElementById('loading-modal').style.display = 'none';
}

function showMessage(elementId, message, type = 'info') {
    const element = document.getElementById(elementId);
    element.textContent = message;
    element.className = `status-message ${type}`;
    element.style.display = 'block';
}

function hideMessage(elementId) {
    document.getElementById(elementId).style.display = 'none';
}

function showCard(cardId) {
    document.getElementById(cardId).style.display = 'block';
    document.getElementById(cardId).classList.add('fade-in');
}

function hideCard(cardId) {
    document.getElementById(cardId).style.display = 'none';
}

// API functions
async function makeRequest(url, options = {}) {
    try {
        console.log('Making request to:', url, 'with options:', options);
        
        // Create a new options object to avoid mutating the original
        const requestOptions = { ...options };
        
        // Ensure Content-Type is set correctly
        const headers = {
            'Content-Type': 'application/json',
            ...(requestOptions.headers || {})
        };
        
        // Remove Content-Type from requestOptions.headers if it exists to avoid conflicts
        if (requestOptions.headers && requestOptions.headers['Content-Type']) {
            delete requestOptions.headers['Content-Type'];
        }
        
        // Set the final headers
        requestOptions.headers = headers;
        
        console.log('Final headers:', headers);
        console.log('Request body:', requestOptions.body);
        console.log('Full request options:', requestOptions);
        
        console.log('🔍 MAKING HTTP REQUEST...');
        const response = await fetch(url, requestOptions);

        console.log('🔍 Response status:', response.status);
        console.log('🔍 Response headers:', response.headers);
        console.log('🔍 Response ok:', response.ok);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('❌ HTTP ERROR - Response not ok:');
            console.error('🔍 Status:', response.status);
            console.error('🔍 Error text:', errorText);
            throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
        }
        
        console.log('✅ HTTP REQUEST SUCCESS - Status 200 received');

        const responseData = await response.json();
        console.log('Response data:', responseData);
        console.log('Response data type:', typeof responseData);
        console.log('Response data success field:', responseData.success);
        console.log('Response data success type:', typeof responseData.success);
        return responseData;
    } catch (error) {
        console.error('Request failed:', error);
        throw error;
    }
}

// WSO2 Login with Basic Authentication
async function loginWithWso2() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    if (!username || !password) {
        showMessage('login-status-message', 'Please enter username and password', 'error');
        return;
    }

    try {
        showLoading('Logging in with WSO2 IS...');
        hideMessage('login-status-message');

        // Prepare form data
        const formData = new URLSearchParams();
        formData.append('username', username);
        formData.append('password', password);

        // Make request to our proxy endpoint
        const response = await fetch(`${API_BASE}/wso2-proxy/token`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: formData
        });

        console.log('Proxy Response status:', response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Proxy Response error:', errorText);
            throw new Error(`Login failed: ${response.status} - ${errorText}`);
        }

        const responseData = await response.json();
        console.log('Proxy response:', responseData);

        if (!responseData.success) {
            throw new Error(responseData.error || 'Login failed');
        }

        const tokenData = responseData.data;
        console.log('Token data:', tokenData);

        // Store tokens
        accessToken = tokenData.access_token;
        refreshToken = tokenData.refresh_token;

        // Show success message
        showMessage('login-status-message', 'Login successful! Redirecting to success page...', 'success');

        // Store tokens in localStorage for success page
        localStorage.setItem('accessToken', tokenData.access_token);
        localStorage.setItem('tokenType', tokenData.token_type || 'Bearer');
        localStorage.setItem('expiresIn', tokenData.expires_in || '3600');
        localStorage.setItem('scope', tokenData.scope || 'openid profile');
        if (tokenData.refresh_token) {
            localStorage.setItem('refreshToken', tokenData.refresh_token);
        }
        
        // Store ID token if available
        if (tokenData.id_token) {
            localStorage.setItem('idToken', tokenData.id_token);
            console.log('🔍 ID Token stored:', tokenData.id_token);
            
            // Decode and log ID token payload
            try {
                const payload = tokenData.id_token.split('.')[1];
                const decodedPayload = JSON.parse(atob(payload));
                console.log('🔍 ID Token payload:', decodedPayload);
                
                // Extract and log username and displayName
                const username = decodedPayload.username || decodedPayload.preferred_username || decodedPayload.sub;
                const displayName = decodedPayload.displayName || decodedPayload.name || username;
                
                console.log('🔍 Username from ID Token:', username);
                console.log('🔍 DisplayName from ID Token:', displayName);
                
                // Log all available user fields
                console.log('🔍 All user fields in ID Token:');
                Object.keys(decodedPayload).forEach(key => {
                    if (key.includes('name') || key.includes('user') || key.includes('sub') || key.includes('email')) {
                        console.log(`  ${key}:`, decodedPayload[key]);
                    }
                });
                
            } catch (e) {
                console.warn('Could not decode ID token:', e);
            }
        } else {
            console.warn('No ID token received from login response');
        }
        
        // Log userId from access token for credential mapping
        try {
            const accessToken = localStorage.getItem('accessToken');
            if (accessToken) {
                const payload = accessToken.split('.')[1];
                const decodedPayload = JSON.parse(atob(payload));
                console.log('🔍 Access Token payload for userId logging:', decodedPayload);
                
                // Extract and log userId
                const userId = decodedPayload.sub || decodedPayload.user_id || decodedPayload.userId;
                if (userId) {
                    console.log('🔍 UserId from Access Token (for credential mapping):', userId);
                    console.log('🔍 Credential will be stored as: credentialId' + userId);
                } else {
                    console.warn('No userId found in access token for credential mapping');
                }
            }
        } catch (e) {
            console.warn('Could not decode userId from access token:', e);
        }

        // Redirect to success page after a short delay
        setTimeout(() => {
            window.location.href = '/success.html?token=success&auth_type=password';
        }, 2000);

    } catch (error) {
        console.error('Login failed:', error);
        showMessage('login-status-message', `Login failed: ${error.message}`, 'error');
        hideLoading();
    }
}

// Redirect to WSO2 IS Login Page for Passkey Authentication
function redirectToWso2PasskeyLogin() {
    const username = document.getElementById('username').value;
    
    if (!username) {
        showMessage('login-status-message', 'Please enter username before redirecting to WSO2 IS login', 'error');
        return;
    }
    
    try {
        showLoading('Redirecting to WSO2 IS login page...');
        
        // WSO2 IS Login URL with passkey support
        // Try different login endpoints
        const wso2LoginUrl = 'https://localhost:9443/oauth2/authorize';
        
        // Add parameters for OAuth2 authorization
        const params = new URLSearchParams({
            'response_type': 'code',
            'client_id': 'iNd9bRH7tknjJYOtnijxEt39LDUa', // Use existing OAuth2 client
            'redirect_uri': 'https://anhngo.site/oauth2/code/wso2',
            'scope': 'openid profile',
            'state': 'passkey_login',
            'username': username // Pre-fill username
        });
        
        const fullUrl = `${wso2LoginUrl}?${params.toString()}`;
        
        showMessage('login-status-message', 'Redirecting to WSO2 IS login page for passkey authentication...', 'info');
        
        // Redirect to WSO2 IS login page
        setTimeout(() => {
            window.location.href = fullUrl;
        }, 1000);
        
    } catch (error) {
        console.error('Error redirecting to WSO2 IS login:', error);
        showMessage('login-status-message', `Failed to redirect: ${error.message}`, 'error');
        hideLoading();
    }
}


// FIDO Login with Biometric Authentication (REMOVED - Not supported by WSO2 IS 7.1.0)
// WSO2 IS requires user to be authenticated first before using FIDO APIs
async function loginWithFido() {
    showMessage('login-status-message', 'FIDO passwordless login is not supported by WSO2 IS 7.1.0. Please use "Login with Passkey (WSO2 IS)" button instead.', 'error');
}

async function performWebAuthnAuthentication(authenticationOptions, username) {
    try {
        showMessage('login-status-message', 'Starting biometric authentication...', 'info');
        
        // Convert base64 challenge to ArrayBuffer
        const challenge = base64ToArrayBuffer(authenticationOptions.publicKeyCredentialRequestOptions.challenge);
        
        // Convert credential ID to ArrayBuffer
        const credentialId = base64ToArrayBuffer(authenticationOptions.publicKeyCredentialRequestOptions.allowCredentials[0].id);
        
        // Prepare the public key credential request options
        const currentHostname = window.location.hostname;
        const rpId = authenticationOptions.publicKeyCredentialRequestOptions.rpId || window.location.origin;
        console.log('🔍 Authentication rpId from server:', authenticationOptions.publicKeyCredentialRequestOptions.rpId);
        console.log('🔍 Authentication rpId we will use:', rpId);
        
        const publicKeyCredentialRequestOptions = {
            ...authenticationOptions.publicKeyCredentialRequestOptions,
            challenge: challenge,
            allowCredentials: [{
                ...authenticationOptions.publicKeyCredentialRequestOptions.allowCredentials[0],
                id: credentialId
            }],
            rpId: rpId // Use rpId from server or current hostname
        };
        
        console.log('Public key credential request options:', publicKeyCredentialRequestOptions);
        
        // Call WebAuthn API to get credentials
        showMessage('login-status-message', 'Please scan your biometric (fingerprint/face)...', 'info');
        
        const assertion = await navigator.credentials.get({
            publicKey: publicKeyCredentialRequestOptions
        });
        
        console.log('WebAuthn assertion:', assertion);
        showMessage('login-status-message', 'Biometric authentication successful!', 'success');
        
        // Convert assertion data to base64
        const assertionObject = arrayBufferToBase64(assertion.response.authenticatorData);
        const clientDataJSON = arrayBufferToBase64(assertion.response.clientDataJSON);
        const signature = arrayBufferToBase64(assertion.response.signature);
        const rawId = arrayBufferToBase64(assertion.rawId);
        
        // Prepare authentication data for backend
        const authenticationData = {
            username: username,
            assertionObject: assertionObject,
            clientDataJSON: clientDataJSON,
            signature: signature,
            rawId: rawId
        };
        
        showMessage('login-status-message', 'Authenticating with server...', 'info');
        
        // Send to backend
        const authResponse = await makeRequest(FIDO_ENDPOINTS.authenticate, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(authenticationData)
        });
        
        if (!authResponse.success) {
            throw new Error(authResponse.message || 'Failed to authenticate with server');
        }
        
        showMessage('login-status-message', 'FIDO authentication successful!', 'success');
        
        // Store tokens from authentication response
        if (authResponse.data && authResponse.data.access_token) {
            accessToken = authResponse.data.access_token;
            refreshToken = authResponse.data.refresh_token;
            
            // Set user info
            const decodedToken = decodeJWT(accessToken);
            if (decodedToken) {
                currentUser = decodedToken.sub || decodedToken.username || username;
                currentDisplayName = decodedToken.name || decodedToken.display_name || currentUser;
            }
            
            // Show success and token info
            showTokenInfo(authResponse.data);
            
            // Show next steps
            setTimeout(() => {
                showCard('token-card');
                showCard('fido-card');
                hideLoading();
            }, 1000);
        } else {
            showMessage('login-status-message', 'Authentication successful but no token received', 'warning');
            hideLoading();
        }
        
    } catch (error) {
        console.error('WebAuthn authentication failed:', error);
        
        if (error.name === 'NotAllowedError') {
            showMessage('login-status-message', 'Biometric authentication was cancelled or failed. Please try again.', 'error');
        } else if (error.name === 'NotSupportedError') {
            showMessage('login-status-message', 'Biometric authentication is not supported on this device.', 'error');
        } else if (error.name === 'InvalidStateError') {
            showMessage('login-status-message', 'No FIDO credential found for this user. Please register first.', 'error');
        } else {
            showMessage('login-status-message', `FIDO authentication failed: ${error.message}`, 'error');
        }
        hideLoading();
    }
}

function showTokenInfo(tokenData) {
    const tokenInfo = document.getElementById('token-info');
    
    // Decode JWT token to get user information
    const decodedToken = decodeJWT(tokenData.access_token);
    console.log('Decoded token:', decodedToken);
    
    if (decodedToken) {
        // Extract user information from token
        currentUser = decodedToken.sub || decodedToken.username || 'Unknown';
        currentDisplayName = decodedToken.name || decodedToken.display_name || currentUser;
        
        // Update user info display (only if elements exist)
        const currentUserElement = document.getElementById('current-user');
        const currentDisplayNameElement = document.getElementById('current-display-name');
        const authUserDisplayElement = document.getElementById('auth-user-display');
        
        if (currentUserElement) currentUserElement.textContent = currentUser;
        if (currentDisplayNameElement) currentDisplayNameElement.textContent = currentDisplayName;
        if (authUserDisplayElement) authUserDisplayElement.textContent = currentUser;
    }
    
    tokenInfo.innerHTML = `
        <h3>Access Token Information</h3>
        <p><strong>Token Type:</strong> ${tokenData.token_type}</p>
        <p><strong>Expires In:</strong> ${tokenData.expires_in} seconds</p>
        <p><strong>Scope:</strong> ${tokenData.scope}</p>
        <p><strong>User:</strong> ${currentUser}</p>
        <p><strong>Display Name:</strong> ${currentDisplayName}</p>
        <h4>Access Token:</h4>
        <pre style="word-break: break-all; font-size: 12px;">${tokenData.access_token}</pre>
        <h4>ID Token:</h4>
        <pre style="word-break: break-all; font-size: 12px;">${tokenData.id_token}</pre>
    `;
}

// FIDO Registration
async function startFidoRegistration() {
    if (!accessToken) {
        showMessage('fido-status-message', 'No access token available. Please login first.', 'error');
        return;
    }

    if (!currentUser || !currentDisplayName) {
        showMessage('fido-status-message', 'User information not available. Please login first.', 'error');
        return;
    }

    try {
        showLoading('Starting FIDO registration...');
        hideMessage('fido-status-message');

        // Step 1: Get FIDO registration options
        showMessage('fido-status-message', 'Getting FIDO registration options...', 'info');

        const optionsResponse = await makeRequest(FIDO_ENDPOINTS.registrationOptions, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${accessToken}`
            },
            body: JSON.stringify({
                username: currentUser,
                displayName: currentDisplayName
            })
        });

        if (!optionsResponse) {
            throw new Error('Failed to get FIDO registration options');
        }

        showMessage('fido-status-message', 'FIDO registration options received', 'success');

        // Step 2: Perform WebAuthn registration with biometric
        showMessage('fido-status-message', 'Starting WebAuthn credential creation...', 'info');

        // Use real WebAuthn API to create credentials (now raw payload)
        await performWebAuthnRegistration(optionsResponse);

    } catch (error) {
        console.error('FIDO registration failed:', error);
        showMessage('fido-status-message', `FIDO registration failed: ${error.message}`, 'error');
        hideLoading();
    }
}

async function performWebAuthnRegistration(registrationOptions) {
    try {
        showMessage('fido-status-message', 'Starting biometric registration...', 'info');
        
        // Debug: Log full registration options from server
        console.log('🔍 Full registration options from server:', registrationOptions);
        console.log('🔍 publicKeyCredentialCreationOptions from server:', registrationOptions.publicKeyCredentialCreationOptions);
        
        // Convert base64 challenge to ArrayBuffer
        console.log('🔍 Raw challenge from server:', registrationOptions.publicKeyCredentialCreationOptions.challenge);
        let challenge;
        try {
            challenge = base64ToArrayBuffer(registrationOptions.publicKeyCredentialCreationOptions.challenge);
            console.log('🔍 Converted challenge:', challenge);
        } catch (error) {
            console.error('❌ Error converting challenge:', error);
            throw new Error(`Failed to convert challenge: ${error.message}`);
        }
        
        // Convert user ID to ArrayBuffer
        console.log('🔍 Raw user ID from server:', registrationOptions.publicKeyCredentialCreationOptions.user.id);
        let userId;
        try {
            userId = base64ToArrayBuffer(registrationOptions.publicKeyCredentialCreationOptions.user.id);
            console.log('🔍 Converted user ID:', userId);
        } catch (error) {
            console.error('❌ Error converting user ID:', error);
            throw new Error(`Failed to convert user ID: ${error.message}`);
        }
        
        // Prepare the public key credential creation options
        const currentHostname = window.location.hostname;
        console.log('🔍 Current hostname:', currentHostname);
        console.log('🔍 Current origin:', window.location.origin);
        
        // Use the rp.id from server response, or fallback to current origin
        const serverRpId = registrationOptions.publicKeyCredentialCreationOptions.rp?.id;
        console.log('🔍 rpId from server:', serverRpId);
        console.log('🔍 rp object from server:', registrationOptions.publicKeyCredentialCreationOptions.rp);
        
        // Determine the best rp.id to use
        let rpId;
        if (serverRpId && serverRpId !== '') {
            rpId = serverRpId;
            console.log('🔍 Using rpId from server:', rpId);
        } else {
            rpId = window.location.hostname; // Use hostname instead of full origin
            console.log('🔍 Using hostname as rpId:', rpId);
        }
        
        console.log('🔍 Final rpId we will use:', rpId);
        console.log('🔍 Current origin:', window.location.origin);
        console.log('🔍 Current hostname:', window.location.hostname);
        
        const publicKeyCredentialCreationOptions = {
            ...registrationOptions.publicKeyCredentialCreationOptions,
            challenge: challenge,
            rp: {
                ...registrationOptions.publicKeyCredentialCreationOptions.rp,
                id: rpId // Use the rpId from server or current hostname
            },
            user: {
                ...registrationOptions.publicKeyCredentialCreationOptions.user,
                id: userId
            },
            authenticatorSelection: {
                ...registrationOptions.publicKeyCredentialCreationOptions.authenticatorSelection,
                requireResidentKey: false,
                residentKey: "discouraged",
                userVerification: "preferred"
            },
            attestation: "direct"
        };
        
        console.log('Public key credential creation options:', publicKeyCredentialCreationOptions);
        console.log('🔍 Final rp.id being used:', publicKeyCredentialCreationOptions.rp?.id);
        console.log('🔍 Final rp.name being used:', publicKeyCredentialCreationOptions.rp?.name);
        
        // Call WebAuthn API to create credentials
        showMessage('fido-status-message', 'Please scan your biometric (fingerprint/face)...', 'info');
        
        const credential = await navigator.credentials.create({
            publicKey: publicKeyCredentialCreationOptions
        });
        
        console.log('WebAuthn credential created:', credential);
        showMessage('fido-status-message', 'Biometric credential created successfully!', 'success');
        
        // Convert credential data to base64
        const attestationObject = arrayBufferToBase64(credential.response.attestationObject);
        const clientDataJSON = arrayBufferToBase64(credential.response.clientDataJSON);
        const rawId = arrayBufferToBase64(credential.rawId);
        
        // Prepare registration data for backend
        const registrationData = {
            username: currentUser,
            displayName: currentDisplayName,
            requestId: registrationOptions.requestId,
            attestationObject: attestationObject,
            clientDataJSON: clientDataJSON,
            rawId: rawId
        };
        
        showMessage('fido-status-message', 'Registering FIDO credential with server...', 'info');
        
        // Send to backend
        const registerResponse = await makeRequest(FIDO_ENDPOINTS.register, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${accessToken}`
            },
            body: JSON.stringify(registrationData)
        });
        
        if (!registerResponse.success) {
            throw new Error(registerResponse.message || 'Failed to register FIDO credential');
        }
        
        // Store the credential ID for later use in authentication
        if (registerResponse.data && registerResponse.data.credential && registerResponse.data.credential.id) {
            window.registeredCredentialId = registerResponse.data.credential.id;
            // Also store in localStorage for persistence across sessions (per userId from access token)
            let userId = 'admin'; // fallback
            
            try {
                const accessToken = localStorage.getItem('accessToken');
                if (accessToken) {
                    const payload = accessToken.split('.')[1];
                    const decodedPayload = JSON.parse(atob(payload));
                    console.log('🔍 Access Token payload for userId extraction in registration:', decodedPayload);
                    
                    // Extract userId from access token
                    userId = decodedPayload.sub || decodedPayload.user_id || decodedPayload.userId;
                    if (userId) {
                        console.log('🔍 Using userId from Access Token for registration:', userId);
                    } else {
                        console.warn('No userId found in access token for registration, using fallback');
                    }
                }
            } catch (e) {
                console.warn('Could not extract userId from access token for registration:', e);
            }
            
            // Store single credentialId
            localStorage.setItem('credentialId', registerResponse.data.credential.id);
            console.log(`🔍 Stored single credentialId: ${registerResponse.data.credential.id}`);
            console.log('🔍 Stored credential ID for authentication:', window.registeredCredentialId);
        }
        
        showMessage('fido-status-message', 'FIDO credential registered successfully!', 'success');
        
        // Show deregister button and hide register button
        document.getElementById('fido-deregister-btn').style.display = 'inline-block';
        document.getElementById('fido-register-btn').style.display = 'none';
        
        // Show FIDO authentication card
        setTimeout(() => {
            showCard('fido-auth-card');
            hideLoading();
        }, 1000);
        
    } catch (error) {
        console.error('WebAuthn registration failed:', error);
        
        if (error.name === 'NotAllowedError') {
            showMessage('fido-status-message', 'Biometric registration was cancelled or failed. Please try again.', 'error');
        } else if (error.name === 'NotSupportedError') {
            showMessage('fido-status-message', 'Biometric authentication is not supported on this device.', 'error');
        } else {
            showMessage('fido-status-message', `FIDO registration failed: ${error.message}`, 'error');
        }
        hideLoading();
    }
}

// FIDO Authentication
async function startFidoAuthentication() {
    if (!accessToken) {
        showMessage('fido-auth-status-message', 'No access token available. Please login first.', 'error');
        return;
    }

    if (!currentUser) {
        showMessage('fido-auth-status-message', 'User information not available. Please login first.', 'error');
        return;
    }

    try {
        showLoading('Starting FIDO authentication...');
        hideMessage('fido-auth-status-message');

        // Step 1: Get FIDO authentication options
        showMessage('fido-auth-status-message', 'Getting FIDO authentication options...', 'info');

        const optionsResponse = await makeRequest(FIDO_ENDPOINTS.authenticationOptions, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${accessToken}`
            },
            body: JSON.stringify({
                username: currentUser
            })
        });

        if (!optionsResponse.success) {
            throw new Error(optionsResponse.message || 'Failed to get FIDO authentication options');
        }

        showMessage('fido-auth-status-message', 'FIDO authentication options received', 'success');

        // Step 2: Simulate FIDO authentication
        showMessage('fido-auth-status-message', 'Simulating FIDO authentication...', 'info');

        await simulateFidoAuthentication(optionsResponse.data);

    } catch (error) {
        console.error('FIDO authentication failed:', error);
        showMessage('fido-auth-status-message', `FIDO authentication failed: ${error.message}`, 'error');
        hideLoading();
    }
}

async function simulateFidoAuthentication(authenticationOptions) {
    // Simulate FIDO authentication for demo purposes
    setTimeout(async () => {
        try {
            showMessage('fido-auth-status-message', 'FIDO authentication completed successfully', 'success');

            // Simulate sending authentication data
            const mockAuthenticationData = {
                username: currentUser,
                assertionObject: 'mock_assertion_object_' + Date.now(),
                clientDataJSON: 'mock_client_data_json_' + Date.now(),
                rawId: 'mock_raw_id_' + Date.now()
            };

            showMessage('fido-auth-status-message', 'Verifying FIDO authentication with server...', 'info');

            const authResponse = await makeRequest(FIDO_ENDPOINTS.authenticate, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                },
                body: JSON.stringify(mockAuthenticationData)
            });

            if (!authResponse.success) {
                throw new Error(authResponse.message || 'Failed to authenticate with FIDO');
            }

            showMessage('fido-auth-status-message', 'FIDO authentication successful!', 'success');

            // Show results
            showResults(authResponse.data);

        } catch (error) {
            console.error('FIDO authentication failed:', error);
            showMessage('fido-auth-status-message', `FIDO authentication failed: ${error.message}`, 'error');
            hideLoading();
        }
    }, 2000);
}

function showResults(data) {
    const resultsContent = document.getElementById('results-content');
    resultsContent.innerHTML = `
        <h3>Authentication Results</h3>
        <p><strong>Status:</strong> <span style="color: green;">Success</span></p>
        <p><strong>User:</strong> ${currentUser || 'Unknown'}</p>
        <p><strong>Method:</strong> FIDO Biometric Authentication</p>
        <p><strong>Timestamp:</strong> ${new Date().toLocaleString()}</p>
        <h4>Response Data:</h4>
        <pre>${JSON.stringify(data, null, 2)}</pre>
    `;

    showCard('results-card');
    hideLoading();
}

function resetFlow() {
    // Reset all cards
    hideCard('token-card');
    hideCard('fido-card');
    hideCard('fido-auth-card');
    hideCard('results-card');
    showCard('login-card');

    // Reset variables
    accessToken = null;
    refreshToken = null;
    currentUser = null;
    currentDisplayName = null;

    // Clear messages
    hideMessage('login-status-message');
    hideMessage('fido-status-message');
    hideMessage('fido-auth-status-message');
    hideMessage('native-auth-status-message');

    // Reset form fields
    document.getElementById('username').value = 'admin';
    document.getElementById('password').value = 'admin';
    document.getElementById('auth-username').value = '';
    
    // Reset user info display
    document.getElementById('current-user').textContent = 'Loading...';
    document.getElementById('current-display-name').textContent = 'Loading...';

    // Hide token info
    document.getElementById('token-info').innerHTML = '';
}

// Native Authentication Functions
async function startNativeAuth() {
    try {
        // Clear any existing WebAuthn session before starting new authentication
        console.log('🔍 Clearing WebAuthn session before starting Native Authentication...');
        if (navigator.credentials && navigator.credentials.preventSilentAccess) {
            navigator.credentials.preventSilentAccess();
            console.log('🔍 Cleared WebAuthn silent access');
        }
        
        // Clear any WebAuthn session storage
        if (sessionStorage) {
            sessionStorage.clear();
            console.log('🔍 Cleared session storage');
        }
        
        showLoading('Starting biometric authentication...');
        hideMessage('native-auth-status-message');

        // Step 1: Initialize biometric authentication flow
        showMessage('native-auth-status-message', 'Initializing authentication flow...', 'info');

        const initResponse = await makeRequest(NATIVE_AUTH_ENDPOINTS.init, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                redirectUri: window.location.origin + '/oauth2/code/wso2',
                scope: 'openid profile',
                responseType: 'code',
                responseMode: 'direct'
            })
        });

        if (!initResponse) {
            throw new Error('Failed to initialize biometric authentication');
        }

        showMessage('native-auth-status-message', 'Authentication flow initialized', 'success');

        // Step 2: Use challenge data directly from init response (now raw payload)
        const challengeData = initResponse;
        console.log('🔍 Full init response (challenge data):', initResponse);
        console.log('🔍 Challenge data:', challengeData);
        console.log('🔍 Flow ID:', challengeData.flowId);
        console.log('🔍 Next step:', challengeData.nextStep);
        console.log('🔍 Authenticators:', challengeData.nextStep?.authenticators);
        
        // Check if this is challenge data (has nextStep with authenticators)
        if (challengeData.nextStep && challengeData.nextStep.authenticators) {
            console.log('🔍 Challenge data received, proceeding with WebAuthn...');
            showMessage('native-auth-status-message', 'Challenge received, proceeding with biometric authentication...', 'info');
            
            // Find biometric authenticator
            const biometricOption = challengeData.nextStep.authenticators.find(auth => 
                auth.authenticator === 'Passkey' || 
                auth.authenticatorId === 'RklET0F1dGhlbnRpY2F0b3I6TE9DQUw' ||
                auth.authenticatorId === 'FIDOAuthenticator:LOCAL' ||
                auth.authenticatorId === 'FIDOAuthenticator'
            );
            
            if (biometricOption) {
                console.log('🔍 Biometric option found:', biometricOption);
                await performWebAuthnNativeAuth(challengeData, challengeData.flowId, biometricOption.authenticatorId);
            } else {
                console.log('🔍 No biometric option found in challenge data');
                console.log('🔍 Available authenticators:', challengeData.nextStep.authenticators);
                throw new Error('Biometric option not available in challenge data');
            }
        } else {
            // Fallback: if init response doesn't contain challenge data, try old logic
            console.log('🔍 No challenge data in init response, falling back to old logic...');
            console.log('🔍 Next step:', challengeData.nextStep);
            console.log('🔍 Step type:', challengeData.nextStep?.stepType);
            console.log('🔍 Authenticators:', challengeData.nextStep?.authenticators);
            
            // Try to find biometric option regardless of step type
            let biometricOption = null;
            if (challengeData.nextStep && challengeData.nextStep.authenticators) {
                biometricOption = challengeData.nextStep.authenticators.find(auth => 
                    auth.authenticator === 'Passkey' || 
                    auth.authenticatorId === 'RklET0F1dGhlbnRpY2F0b3I6TE9DQUw' ||
                    auth.authenticatorId === 'FIDOAuthenticator:LOCAL' ||
                    auth.authenticatorId === 'FIDOAuthenticator'
                );
            }
            
            console.log('🔍 Biometric option found:', biometricOption);
            
            if (biometricOption) {
                showMessage('native-auth-status-message', 'Biometric option found, getting challenge...', 'info');
                await getPasskeyChallenge(challengeData.flowId, biometricOption.authenticatorId);
            } else {
                console.log('🔍 No biometric option found in authenticators');
                console.log('🔍 Available authenticators:', challengeData.nextStep?.authenticators);
                
                // Check if there are any authenticators at all
                if (!challengeData.nextStep || !challengeData.nextStep.authenticators || challengeData.nextStep.authenticators.length === 0) {
                    console.log('🔍 No authenticators available, creating mock authenticator for WebAuthn...');
                    
                    // Create a mock authenticator to allow WebAuthn to proceed
                    const mockAuthenticator = {
                        authenticatorId: 'MANUAL_FIDO',
                        authenticator: 'Manual FIDO',
                        metadata: {
                            additionalData: {
                                challengeData: btoa(JSON.stringify({
                                    publicKeyCredentialRequestOptions: {
                                        challenge: 'mock-challenge',
                                        allowCredentials: [{
                                            id: 'mock-credential-id',
                                            type: 'public-key'
                                        }]
                                    },
                                    requestId: 'manual-request-id'
                                }))
                            }
                        }
                    };
                    
                    console.log('🔍 Created mock authenticator for WebAuthn');
                    showMessage('native-auth-status-message', 'Proceeding with WebAuthn authentication...', 'info');
                    await getPasskeyChallenge(challengeData.flowId, mockAuthenticator.authenticatorId);
                } else {
                    throw new Error('Biometric option not available');
                }
            }
        }

    } catch (error) {
        console.error('Biometric authentication failed:', error);
        showMessage('native-auth-status-message', `Biometric authentication failed: ${error.message}`, 'error');
        hideLoading();
    }
}

async function getPasskeyChallenge(flowId, authenticatorId) {
    try {
        // Check if this is a manual FIDO request
        if (authenticatorId === 'MANUAL_FIDO') {
            console.log('🔍 Manual FIDO request detected, skipping challenge call');
            showMessage('native-auth-status-message', 'Using manual credential ID, performing WebAuthn authentication...', 'info');
            
            // Create mock challenge data for manual FIDO
            const mockChallengeData = {
                nextStep: {
                    authenticators: [{
                        authenticatorId: 'MANUAL_FIDO',
                        metadata: {
                            additionalData: {
                                challengeData: btoa(JSON.stringify({
                                    publicKeyCredentialRequestOptions: {
                                        challenge: 'mock-challenge',
                                        allowCredentials: [{
                                            id: 'manual-credential-id',
                                            type: 'public-key'
                                        }]
                                    },
                                    requestId: 'manual-request-id'
                                }))
                            }
                        }
                    }]
                }
            };
            
            // Step 3: Perform WebAuthn authentication with mock data
            await performWebAuthnNativeAuth(mockChallengeData, flowId, authenticatorId);
        } else {
            const challengeResponse = await makeRequest(NATIVE_AUTH_ENDPOINTS.challenge, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    flowId: flowId,
                    authenticatorId: authenticatorId
                })
            });

            if (!challengeResponse.success) {
                throw new Error(challengeResponse.message || 'Failed to get passkey challenge');
            }

            showMessage('native-auth-status-message', 'Challenge received, performing WebAuthn authentication...', 'info');

            // Step 3: Perform WebAuthn authentication
            await performWebAuthnNativeAuth(challengeResponse.data, flowId, authenticatorId);
        }

    } catch (error) {
        console.error('Error getting passkey challenge:', error);
        showMessage('native-auth-status-message', `Failed to get challenge: ${error.message}`, 'error');
        hideLoading();
    }
}

async function performWebAuthnNativeAuth(challengeData, flowId, authenticatorId) {
    try {
        // Clear any existing WebAuthn session before starting new authentication
        console.log('🔍 Clearing WebAuthn session before authentication...');
        if (navigator.credentials && navigator.credentials.preventSilentAccess) {
            navigator.credentials.preventSilentAccess();
            console.log('🔍 Cleared WebAuthn silent access');
        }
        
        // Clear any WebAuthn session storage
        if (sessionStorage) {
            sessionStorage.clear();
            console.log('🔍 Cleared session storage');
        }
        
        // Extract challenge data from the response
        let challengeOptions;
        let requestId;
        let credentialId = null; // Initialize to null to avoid undefined errors
        if (challengeData.nextStep && challengeData.nextStep.authenticators && challengeData.nextStep.authenticators[0]) {
            const authenticator = challengeData.nextStep.authenticators[0];
            if (authenticator.metadata && authenticator.metadata.additionalData && authenticator.metadata.additionalData.challengeData) {
                // Decode the base64 challenge data
                const decodedChallenge = atob(authenticator.metadata.additionalData.challengeData);
                console.log('🔍 Decoded challenge data:', decodedChallenge);
                challengeOptions = JSON.parse(decodedChallenge);
                requestId = challengeOptions.requestId;
                
                // Extract credential ID from challenge data (already registered)
                console.log('🔍 Full challenge options structure:', JSON.stringify(challengeOptions, null, 2));
                console.log('🔍 Public key credential request options:', challengeOptions.publicKeyCredentialRequestOptions);
                console.log('🔍 Allow credentials:', challengeOptions.publicKeyCredentialRequestOptions.allowCredentials);
                
                // Also check if there are any other credential-related fields
                console.log('🔍 All keys in challengeOptions:', Object.keys(challengeOptions));
                console.log('🔍 Current user context - window.registeredCredentialId:', window.registeredCredentialId);
                // Get current username from JWT token (for passkey login)
                let currentUsername = 'admin'; // fallback
                
                // Try to get userId from access token
                try {
                    const accessToken = localStorage.getItem('accessToken');
                    if (accessToken) {
                        const payload = accessToken.split('.')[1];
                        const decodedPayload = JSON.parse(atob(payload));
                        console.log('🔍 Access Token payload for passkey userId extraction:', decodedPayload);
                        
                        // Extract userId from access token
                        const userId = decodedPayload.sub || decodedPayload.user_id || decodedPayload.userId;
                        if (userId) {
                            currentUsername = userId;
                            console.log('🔍 Using userId from Access Token for passkey:', userId);
                        } else {
                            console.warn('No userId found in access token for passkey');
                        }
                    }
                } catch (e) {
                    console.warn('Could not decode userId from Access Token for passkey:', e);
                }
                
                // Get single credential ID
                credentialId = localStorage.getItem('credentialId');
                console.log(`🔍 Looking for single credential ID:`, credentialId);
                
                // If no credential found, show popup
                if (!credentialId) {
                    console.log('🔍 No credential found in localStorage, showing popup...');
                    const userCredentialId = prompt('No credential ID found in localStorage. Please enter your credential ID:');
                    if (!userCredentialId || userCredentialId.trim() === '') {
                        throw new Error('Credential ID is required for authentication.');
                    }
                    credentialId = userCredentialId.trim();
                    console.log('🔍 Using manually entered credential ID:', credentialId);
                }
                
                console.log('🔍 Final credential ID for authentication:', credentialId);
                console.log('🔍 All localStorage keys:');
                for (let i = 0; i < localStorage.length; i++) {
                    const key = localStorage.key(i);
                    if (key && key.includes('registeredCredentialId')) {
                        console.log(`🔍 Found credential key: ${key} = ${localStorage.getItem(key)}`);
                    }
                }
                if (challengeOptions.publicKeyCredentialRequestOptions) {
                    console.log('🔍 All keys in publicKeyCredentialRequestOptions:', Object.keys(challengeOptions.publicKeyCredentialRequestOptions));
                }
                
                // Check if allowCredentials exists and has items
                if (challengeOptions.publicKeyCredentialRequestOptions.allowCredentials && 
                    challengeOptions.publicKeyCredentialRequestOptions.allowCredentials.length > 0) {
                    credentialId = challengeOptions.publicKeyCredentialRequestOptions.allowCredentials[0].id;
                    console.log('🔍 Credential ID from challenge data:', credentialId);
                } else {
                    console.warn('⚠️ No allowCredentials found in challenge data');
                    console.log('🔍 This usually means:');
                    console.log('🔍 1. No credentials have been registered yet');
                    console.log('🔍 2. WSO2 IS is not configured to return allowCredentials');
                    console.log('🔍 3. The user has no registered passkeys');
                    console.log('🔍 4. WSO2 IS is using usernameless authentication flow');
                    
                    // Try to find credential ID in other places
                    console.log('🔍 Searching for credential ID in other fields...');
                    
                    // Check if there's a credentialId field directly
                    if (challengeOptions.credentialId) {
                        credentialId = challengeOptions.credentialId;
                        console.log('🔍 Found credentialId in challengeOptions:', credentialId);
                    }
                    // Check if there's a credentialId in metadata
                    else if (challengeOptions.metadata && challengeOptions.metadata.credentialId) {
                        credentialId = challengeOptions.metadata.credentialId;
                        console.log('🔍 Found credentialId in metadata:', credentialId);
                    }
                    // Check if there's a credentialId in additionalData
                    else if (challengeOptions.additionalData && challengeOptions.additionalData.credentialId) {
                        credentialId = challengeOptions.additionalData.credentialId;
                        console.log('🔍 Found credentialId in additionalData:', credentialId);
                    }
                    // Use credentialId from fallback strategy if available
                    else if (credentialId) {
                        console.log('🔍 Using credential ID from fallback strategy:', credentialId);
                    }
                    // Use the stored credential ID from registration
                    else if (window.registeredCredentialId) {
                        credentialId = window.registeredCredentialId;
                        console.log('🔍 Using stored credential ID from registration:', credentialId);
                    }
                    // Check localStorage for previously registered credential ID (per username)
                    else if (localStorage.getItem(`registeredCredentialId_${currentUsername}`)) {
                        credentialId = localStorage.getItem(`registeredCredentialId_${currentUsername}`);
                        window.registeredCredentialId = credentialId; // Set in window for consistency
                        console.log('🔍 Using credential ID from localStorage for user:', currentUsername, credentialId);
                    }
                    else {
                        console.error('❌ No credentialId found anywhere - this should not happen');
                        console.log('🔍 This means:');
                        console.log('🔍 1. No credentials have been registered yet');
                        console.log('🔍 2. The user has no registered passkeys');
                        console.log('🔍 3. localStorage was cleared');
                        
                        // Last resort: scan all localStorage for any registeredCredentialId
                        console.log('🔍 Last resort: scanning all localStorage for any registeredCredentialId...');
                        for (let i = 0; i < localStorage.length; i++) {
                            const key = localStorage.key(i);
                            if (key && key.startsWith('registeredCredentialId_')) {
                                const foundCredentialId = localStorage.getItem(key);
                                console.log(`🔍 Found credential in localStorage: ${key} = ${foundCredentialId}`);
                                credentialId = foundCredentialId;
                                break;
                            }
                        }
                        
                        if (!credentialId) {
                            console.log('🔍 No credential ID found, showing popup for manual input...');
                            
                            // Show popup to enter credentialId
                            const userCredentialId = prompt('No credential ID found. Please enter your credential ID manually:');
                            if (!userCredentialId || userCredentialId.trim() === '') {
                                throw new Error('Credential ID is required but not found. Please register a passkey first.');
                            }
                            
                            credentialId = userCredentialId.trim();
                            console.log('🔍 Using manually entered credential ID:', credentialId);
                        }
                    }
                }
                
                console.log('🔍 Parsed challenge options:', challengeOptions);
                console.log('🔍 RequestId from WSO2 IS:', requestId);
                console.log('🔍 Challenge from WSO2 IS:', challengeOptions.publicKeyCredentialRequestOptions.challenge);
            }
        }
        
        if (!challengeOptions) {
            throw new Error('No challenge data found in response');
        }

        // Convert challenge data to WebAuthn format
        let publicKeyCredentialRequestOptions;
        
        if (authenticatorId === 'MANUAL_FIDO') {
            // For manual FIDO, check localStorage for credentialId first
            console.log('🔍 Creating challenge data for manual FIDO WebAuthn...');
            const mockChallenge = 'mock-challenge-for-webauthn';
            
            // Check localStorage for single credentialId
            const storedCredentialId = localStorage.getItem('credentialId');
            let realCredentialId;
            
            if (storedCredentialId) {
                realCredentialId = storedCredentialId;
                console.log('🔍 Using stored credentialId from localStorage:', realCredentialId);
            } else {
                console.log('🔍 No credentialId found in localStorage, showing popup...');
                const userCredentialId = prompt('No credential ID found in localStorage. Please enter your credential ID for WebAuthn authentication:');
                if (!userCredentialId || userCredentialId.trim() === '') {
                    throw new Error('Credential ID is required for WebAuthn authentication.');
                }
                realCredentialId = userCredentialId.trim();
                console.log('🔍 Using manually entered credential ID for WebAuthn:', realCredentialId);
            }
            
            // Validate credential ID format
            try {
                // Try to convert to ArrayBuffer to validate format
                const testArrayBuffer = base64ToArrayBuffer(realCredentialId);
                console.log('🔍 Credential ID format is valid');
                
                publicKeyCredentialRequestOptions = {
                    challenge: base64ToArrayBuffer(btoa(mockChallenge)),
                    allowCredentials: [{
                        id: testArrayBuffer,
                        type: 'public-key',
                        transports: ['internal']
                    }],
                    userVerification: 'preferred',
                    timeout: 60000
                };
            } catch (error) {
                console.error('🔍 Invalid credential ID format:', error);
                throw new Error('Invalid credential ID format. Please check your credential ID.');
            }
            
            console.log('🔍 Using challenge data for manual FIDO WebAuthn');
        } else {
            // For normal FIDO, use real challenge data
            publicKeyCredentialRequestOptions = {
                challenge: base64ToArrayBuffer(challengeOptions.publicKeyCredentialRequestOptions.challenge),
                allowCredentials: challengeOptions.publicKeyCredentialRequestOptions.allowCredentials ? 
                    challengeOptions.publicKeyCredentialRequestOptions.allowCredentials.map(cred => ({
                        id: base64ToArrayBuffer(cred.id),
                        type: cred.type,
                        transports: cred.transports
                    })) : [],
                userVerification: challengeOptions.publicKeyCredentialRequestOptions.userVerification || 'preferred',
                timeout: challengeOptions.publicKeyCredentialRequestOptions.timeout || 60000
            };
        }

        console.log('WebAuthn authentication options prepared:', publicKeyCredentialRequestOptions);

        // Store challenge data for later use instead of calling WebAuthn immediately
        window.currentChallengeData = {
            flowId: flowId,
            authenticatorId: authenticatorId,
            requestId: requestId,
            publicKeyCredentialRequestOptions: publicKeyCredentialRequestOptions
        };

        console.log('🔍 Challenge data prepared, ready for WebAuthn authentication');
        showMessage('native-auth-status-message', 'Challenge data prepared. Ready for WebAuthn authentication.', 'info');
        
        // Automatically proceed with WebAuthn authentication
        console.log('🔍 Automatically proceeding with WebAuthn authentication...');
        await performWebAuthnAuthentication();

    } catch (error) {
        console.error('Failed to prepare challenge data:', error);
        showMessage('native-auth-status-message', `Failed to prepare challenge: ${error.message}`, 'error');
        hideLoading();
    }
}

// Function to perform WebAuthn authentication when user is ready
async function performWebAuthnAuthentication() {
    try {
        if (!window.currentChallengeData) {
            throw new Error('No challenge data available. Please initialize authentication first.');
        }

        const { flowId, authenticatorId, requestId, publicKeyCredentialRequestOptions } = window.currentChallengeData;
        
        console.log('🔍 Starting WebAuthn authentication...');
        showLoading('Performing WebAuthn authentication...');
        showMessage('native-auth-status-message', 'Please scan your biometric (fingerprint/face)...', 'info');

        // Perform WebAuthn authentication
        const credential = await navigator.credentials.get({
            publicKey: publicKeyCredentialRequestOptions
        });

        console.log('WebAuthn authentication successful:', credential);

        // Convert credential to base64url format
        const clientDataJSON = arrayBufferToBase64(credential.response.clientDataJSON);
        const authenticatorData = arrayBufferToBase64(credential.response.authenticatorData);
        const signature = arrayBufferToBase64(credential.response.signature);
        const userHandle = credential.response.userHandle ? 
            arrayBufferToBase64(credential.response.userHandle) : null;

        showMessage('native-auth-status-message', 'WebAuthn authentication successful, verifying...', 'info');

        // Step 4: Verify authentication
        await verifyPasskeyAuth(flowId, authenticatorId, {
            clientDataJSON,
            authenticatorData,
            signature,
            userHandle,
            credentialId: credential.id
        }, requestId);

    } catch (error) {
        console.error('WebAuthn authentication failed:', error);
        showMessage('native-auth-status-message', `WebAuthn authentication failed: ${error.message}`, 'error');
        hideLoading();
    }
}

async function verifyPasskeyAuth(flowId, authenticatorId, credentials, requestId) {
    try {
        console.log('🔍 VERIFY PASSKEY AUTH - Starting...');
        
        // Check localStorage for single credentialId first
        console.log('🔍 Checking localStorage for single credentialId...');
        const storedCredentialId = localStorage.getItem('credentialId');
        let credentialId;
        
        if (storedCredentialId) {
            credentialId = storedCredentialId;
            console.log('🔍 Using stored credentialId from localStorage:', credentialId);
        } else {
            console.log('🔍 No credentialId found in localStorage, showing popup...');
            const userCredentialId = prompt('No credential ID found in localStorage. Please enter your credential ID for authentication:');
            if (!userCredentialId || userCredentialId.trim() === '') {
                throw new Error('Credential ID is required for authentication.');
            }
            credentialId = userCredentialId.trim();
            console.log('🔍 Using manually entered credentialId:', credentialId);
        }
        
        // Update credentials with the manually entered credentialId
        credentials.credentialId = credentialId;
        
        console.log('🔍 Sending requestId to backend:', requestId);
        console.log('🔍 Credentials object:', credentials);
        console.log('🔍 CredentialId in credentials:', credentials.credentialId);
        
        const requestBody = {
            flowId: flowId,
            authenticatorId: authenticatorId,
            credentials: credentials,
            requestId: requestId
        };
        
        console.log('🔍 Full request body:', requestBody);
        console.log('🔍 About to make API call to:', NATIVE_AUTH_ENDPOINTS.verify);
        
        const verifyResponse = await makeRequest(NATIVE_AUTH_ENDPOINTS.verify, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });

        console.log('🔍 Raw verify response:', verifyResponse);
        console.log('🔍 HTTP 200 response received - treating as success!');
        console.log('✅ SUCCESS FLOW - No errors, proceeding to success page');
        console.log('🔍 SERVER RESPONSE SUCCESS - This is a valid success from server');

        // Store credential ID for success page (if available)
        if (credentialId && credentialId !== '') {
            localStorage.setItem('lastUsedCredentialId', credentialId);
            console.log('🔍 Stored credentialId in localStorage');
        } else {
            console.log('🔍 No credentialId to store (using empty string)');
        }
        
        // Redirect to shared success page with Native Authentication type
        console.log('🔍 WOULD REDIRECT TO SUCCESS PAGE...');
        console.log('🔍 Success URL:', `/success.html?auth_type=native&credentialId=${encodeURIComponent(credentialId || '')}`);
        console.log('🔍 credentialId for redirect:', credentialId);
        console.log('🔍 encoded credentialId:', encodeURIComponent(credentialId || ''));
        
        window.location.href = `/success.html?auth_type=native&credentialId=${encodeURIComponent(credentialId || '')}`;
        console.log('🔍 SUCCESS: Redirecting to shared success page...');

    } catch (error) {
        console.error('❌ ERROR CAUGHT - Analyzing error type:');
        console.error('🔍 Error type:', typeof error);
        console.error('🔍 Error message:', error.message);
        console.error('🔍 Error stack:', error.stack);
        console.error('🔍 Full error object:', error);
        
        // Check if this is a server error (HTTP error) or client logic error
        const isServerError = error.message && error.message.includes('HTTP error');
        const isCredentialIdError = error.message && error.message.includes('credentialId');
        
        console.log('🔍 Is server error:', isServerError);
        console.log('🔍 Is credentialId error:', isCredentialIdError);
        
        if (isServerError) {
            console.error('🔍 SERVER ERROR - This is a valid server error, should redirect to error page');
            console.log('🔍 WOULD REDIRECT TO ERROR PAGE (SERVER ERROR)...');
        } else if (isCredentialIdError) {
            console.error('🔍 CLIENT LOGIC ERROR - This is a client logic error, should NOT redirect to error page');
            console.log('🔍 WOULD NOT REDIRECT - This is a client logic error, not a server error');
        } else {
            console.error('🔍 UNKNOWN ERROR TYPE:', error.message);
            console.log('🔍 WOULD REDIRECT TO ERROR PAGE (UNKNOWN ERROR)...');
        }
        
        // Only redirect for server errors, not client logic errors
        if (isServerError) {
            const errorMessage = error.message || 'Unknown error';
            console.log('🔍 Error message for redirect:', errorMessage);
            console.log('🔍 Error URL:', `/native-auth-failed.html?error=${encodeURIComponent(errorMessage)}`);
            
            window.location.href = `/native-auth-failed.html?error=${encodeURIComponent(errorMessage)}`;
            console.log('🔍 ERROR: Redirecting to error page...');
        } else {
            console.log('🔍 CLIENT ERROR: Would NOT redirect to error page - this is a client logic error');
        }
    }
}

async function exchangeCodeForTokens(code) {
    try {
        console.log('🔍 Authorization code received:', code);
        
        // Redirect to Native Authentication success page
        console.log('🔍 WOULD REDIRECT TO SUCCESS PAGE...');
        console.log('🔍 Success URL:', `/native-auth-success.html?code=${encodeURIComponent(code)}`);
        
        window.location.href = `/native-auth-success.html?code=${encodeURIComponent(code)}`;
        console.log('🔍 SUCCESS: Redirecting to success page...');
        
    } catch (error) {
        console.error('Error exchanging code for tokens:', error);
        
        // Redirect to Native Authentication failed page
        const errorMessage = error.message || 'Token exchange failed';
        console.log('🔍 WOULD REDIRECT TO ERROR PAGE...');
        console.log('🔍 Error message for redirect:', errorMessage);
        console.log('🔍 Error URL:', `/native-auth-failed.html?error=${encodeURIComponent(errorMessage)}`);
        
        window.location.href = `/native-auth-failed.html?error=${encodeURIComponent(errorMessage)}`;
        console.log('🔍 ERROR: Redirecting to error page...');
    }
}

// Deregister FIDO Credential
async function deregisterFidoCredential() {
    if (!accessToken) {
        showMessage('fido-status-message', 'No access token available. Please login first.', 'error');
        return;
    }

    if (!window.registeredCredentialId) {
        showMessage('fido-status-message', 'No registered credential found. Please register a passkey first.', 'error');
        return;
    }

    // Confirm deregistration
    if (!confirm('Are you sure you want to deregister this FIDO credential? This action cannot be undone.')) {
        return;
    }

    try {
        showLoading('Deregistering FIDO credential...');
        hideMessage('fido-status-message');

        const response = await makeRequest(`/fido/deregister/${window.registeredCredentialId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${accessToken}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.success) {
            throw new Error(response.message || 'Failed to deregister FIDO credential');
        }

        showMessage('fido-status-message', 'FIDO credential deregistered successfully!', 'success');
        
        // Clear stored credential ID
        window.registeredCredentialId = null;
        localStorage.removeItem('credentialId');
        
        // Hide deregister button and show register button
        document.getElementById('fido-deregister-btn').style.display = 'none';
        document.getElementById('fido-register-btn').style.display = 'inline-block';
        
        hideLoading();

    } catch (error) {
        console.error('FIDO deregistration failed:', error);
        showMessage('fido-status-message', `FIDO deregistration failed: ${error.message}`, 'error');
        hideLoading();
    }
}

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    console.log('FIDO Authentication UI loaded');
    
    // Simple check for single credentialId in localStorage
    console.log('🔍 Checking for single credentialId in localStorage...');
    const storedCredentialId = localStorage.getItem('credentialId');
    
    if (storedCredentialId) {
        window.registeredCredentialId = storedCredentialId;
        console.log('🔍 Found credentialId in localStorage:', storedCredentialId);
        
        // Show deregister button if credential is available
        const deregisterBtn = document.getElementById('fido-deregister-btn');
        const registerBtn = document.getElementById('fido-register-btn');
        if (deregisterBtn && registerBtn) {
            deregisterBtn.style.display = 'inline-block';
            registerBtn.style.display = 'none';
            console.log('🔍 Showing deregister button, hiding register button');
        }
    } else {
        console.log('🔍 No credentialId found in localStorage');
        
        // Show register button if no credential is available
        const deregisterBtn = document.getElementById('fido-deregister-btn');
        const registerBtn = document.getElementById('fido-register-btn');
        if (deregisterBtn && registerBtn) {
            deregisterBtn.style.display = 'none';
            registerBtn.style.display = 'inline-block';
            console.log('🔍 Showing register button, hiding deregister button');
        }
    }
    
    // Check if user is returning with token
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('token') === 'success') {
        // User has successfully logged in with passkey
        const accessToken = localStorage.getItem('accessToken');
        const tokenType = localStorage.getItem('tokenType');
        const expiresIn = localStorage.getItem('expiresIn');
        const scope = localStorage.getItem('scope');
        
        if (accessToken) {
            // Set global variables
            window.accessToken = accessToken;
            window.tokenType = tokenType;
            window.expiresIn = expiresIn;
            window.scope = scope;
            
            // Decode user info from token
            try {
                const tokenParts = accessToken.split('.');
                if (tokenParts.length === 3) {
                    const payload = JSON.parse(atob(tokenParts[1]));
                    window.currentUser = payload.sub || 'Unknown';
                    window.currentDisplayName = payload.name || payload.display_name || window.currentUser;
                }
            } catch (error) {
                console.error('Error decoding JWT:', error);
            }
            
            // Show success state
            showCard('token-card');
            showCard('fido-card');
            showCard('fido-auth-card');
            hideCard('login-card');
            
            // Show success message
            showMessage('login-status-message', '🎉 Passkey login successful! You can now use FIDO APIs.', 'success');
            
            // Clear URL parameters
            window.history.replaceState({}, document.title, window.location.pathname);
        }
    }
}); 