# RepairApp Authentication API - cURL Examples

## Overview
The RepairApp supports **two login methods**:
1. **Mobile + Password Login** (traditional)
2. **Mobile + OTP Login** (passwordless)

The hardcoded OTP for development is: `666666`

---

## 🔐 LOGIN METHODS

### 1. Login with Mobile & Password

**Single-step login using mobile number and password:**

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210",
    "password": "password123",
    "rememberMe": false
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "9876543210",
    "role": "CUSTOMER"
  }
}
```

### 2. Login with Mobile & OTP (Two-step process)

#### Step 1: Send OTP to Mobile

```bash
curl -X POST "http://localhost:8080/api/auth/login/send-otp" \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login OTP sent to mobile number",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    "message": "OTP sent to 98***10",
    "maskedMobile": "98***10",
    "expiresAt": "2024-01-15T10:40:00",
    "nextStep": "verify-login-otp"
  }
}
```

#### Step 2: Verify OTP and Login

```bash
curl -X POST "http://localhost:8080/api/auth/login/verify-otp" \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210",
    "otpCode": "666666"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "OTP login successful",
  "timestamp": "2024-01-15T10:32:00Z",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "9876543210",
    "role": "CUSTOMER"
  }
}
```

#### Resend Login OTP

```bash
curl -X POST "http://localhost:8080/api/auth/login/resend-otp" \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210"
  }'
```

---

## 📝 REGISTRATION (3-Step Process)

### Step 1: Initiate Signup with Mobile

```bash
curl -X POST "http://localhost:8080/api/auth/signup/mobile" \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "8765432109"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "OTP sent to mobile number",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    "message": "OTP sent to 87***09",
    "maskedMobile": "87***09",
    "expiresAt": "2024-01-15T10:40:00",
    "nextStep": "verify-otp"
  }
}
```

### Step 2: Verify Mobile OTP

```bash
curl -X POST "http://localhost:8080/api/auth/signup/verify-otp" \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "8765432109",
    "otpCode": "666666"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "OTP verified successfully",
  "timestamp": "2024-01-15T10:32:00Z",
  "data": {
    "message": "Mobile number verified successfully",
    "mobile": "8765432109",
    "nextStep": "complete-registration"
  }
}
```

### Step 3: Complete Registration

```bash
curl -X POST "http://localhost:8080/api/auth/signup/complete" \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "8765432109",
    "name": "Jane Smith",
    "email": "jane.smith@example.com",
    "password": "password123",
    "city": "Mumbai",
    "state": "Maharashtra",
    "country": "India"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Registration completed successfully",
  "timestamp": "2024-01-15T10:33:00Z",
  "data": {
    "user": {
      "id": 2,
      "name": "Jane Smith",
      "email": "jane.smith@example.com",
      "mobile": "8765432109",
      "city": "Mumbai",
      "state": "Maharashtra"
    },
    "tokens": {
      "token": "eyJhbGciOiJIUzUxMiJ9...",
      "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
      "id": 2,
      "name": "Jane Smith",
      "email": "jane.smith@example.com",
      "phone": "8765432109",
      "role": "CUSTOMER"
    }
  }
}
```

---

## 🔧 TOKEN MANAGEMENT

### Refresh Access Token

```bash
curl -X POST "http://localhost:8080/api/auth/refresh?refreshToken=YOUR_REFRESH_TOKEN" \
  -H "Content-Type: application/x-www-form-urlencoded"
```

### Get Current User Info

```bash
curl -X GET "http://localhost:8080/api/auth/me" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Logout

```bash
curl -X POST "http://localhost:8080/api/auth/logout" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## 🔑 PASSWORD MANAGEMENT

### Forgot Password (Send OTP to Email)

```bash
curl -X POST "http://localhost:8080/api/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com"
  }'
```

### Reset Password with OTP

```bash
curl -X POST "http://localhost:8080/api/auth/reset-password" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "otpCode": "666666",
    "newPassword": "newpassword123"
  }'
```

### Change Password (Authenticated)

```bash
curl -X POST "http://localhost:8080/api/auth/change-password" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "oldPassword": "oldpassword123",
    "newPassword": "newpassword123"
  }'
```

---

## 🧪 TESTING FLOWS

### Complete OTP Login Flow

```bash
# Step 1: Send OTP
curl -X POST "http://localhost:8080/api/auth/login/send-otp" \
  -H "Content-Type: application/json" \
  -d '{"mobile": "9876543210"}'

# Step 2: Login with OTP (use 666666)
curl -X POST "http://localhost:8080/api/auth/login/verify-otp" \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210",
    "otpCode": "666666"
  }'
```

### Complete Registration Flow

```bash
# Step 1: Start registration
curl -X POST "http://localhost:8080/api/auth/signup/mobile" \
  -H "Content-Type: application/json" \
  -d '{"mobile": "7654321098"}'

# Step 2: Verify mobile OTP
curl -X POST "http://localhost:8080/api/auth/signup/verify-otp" \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "7654321098",
    "otpCode": "666666"
  }'

# Step 3: Complete registration
curl -X POST "http://localhost:8080/api/auth/signup/complete" \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "7654321098",
    "name": "Test User",
    "email": "test@example.com",
    "password": "testpass123",
    "city": "Delhi"
  }'
```

---

## ⚠️ IMPORTANT NOTES

1. **Demo OTP**: For development, the hardcoded OTP is `666666`
2. **Mobile Format**: Use 10-digit mobile numbers (e.g., 9876543210)
3. **Session**: OTP verification steps maintain session state
4. **Tokens**: Both login methods return access token and refresh token
5. **Cart Merge**: Session cart is automatically merged with user cart after login
6. **Rate Limiting**: Max 5 OTP requests per hour per mobile number
7. **OTP Expiry**: OTPs expire after 10 minutes
8. **Resend Cooldown**: 2 minutes cooldown between resend requests

---

## 🏃‍♂️ QUICK START

To test immediately with existing users (demo accounts):

### Admin Login:
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210",
    "password": "admin123"
  }'
```

### Customer Login:
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "8765432109",
    "password": "user123"
  }'
```

### OTP Login (works for any registered mobile):
```bash
# Send OTP
curl -X POST "http://localhost:8080/api/auth/login/send-otp" \
  -H "Content-Type: application/json" \
  -d '{"mobile": "9876543210"}'

# Login with OTP
curl -X POST "http://localhost:8080/api/auth/login/verify-otp" \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210",
    "otpCode": "666666"
  }'
```
