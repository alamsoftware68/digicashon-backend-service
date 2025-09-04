# Three-Step Signup API Documentation

## Overview

The signup process is divided into three steps:
1. **Mobile Number Input**: User provides mobile number → receives SMS OTP (hardcoded: 666666)
2. **Mobile OTP Verification**: User enters OTP → mobile number is verified (no email verification needed)
3. **Complete Registration**: User provides full details (name, email, password, country) → account is created + auto-login

## API Endpoints

### Step 1: Initiate Signup with Mobile Number

**Endpoint**: `POST /api/auth/signup/mobile`

**Description**: Start the signup process by providing a mobile number. An OTP (666666) will be sent to the mobile number for verification.

**Request Body**:
```json
{
  "mobile": "9876543210"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/auth/signup/mobile \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210"
  }'
```

**Success Response** (200):
```json
{
  "success": true,
  "message": "OTP sent to mobile number",
  "data": {
    "message": "OTP sent to 98***10",
    "maskedMobile": "98***10",
    "expiresAt": "2025-01-01T10:10:00",
    "nextStep": "verify-otp"
  },
  "timestamp": "2025-01-01T10:00:00"
}
```

**Error Response** (400):
```json
{
  "success": false,
  "message": "Mobile number is already registered",
  "data": null,
  "timestamp": "2025-01-01T10:00:00"
}
```

---

### Step 2: Verify Mobile OTP

**Endpoint**: `POST /api/auth/signup/verify-otp`

**Description**: Verify the OTP sent to the mobile number. The OTP is hardcoded as **666666** for development/testing purposes.

**Request Body**:
```json
{
  "mobile": "9876543210",
  "otpCode": "666666"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/auth/signup/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210",
    "otpCode": "666666"
  }'
```

**Success Response** (200):
```json
{
  "success": true,
  "message": "OTP verified successfully",
  "data": {
    "message": "Mobile number verified successfully",
    "mobile": "9876543210",
    "nextStep": "complete-registration"
  },
  "timestamp": "2025-01-01T10:01:00"
}
```

**Error Response** (400):
```json
{
  "success": false,
  "message": "Invalid OTP. Please try again.",
  "data": {
    "remainingAttempts": 4,
    "canResend": false
  },
  "timestamp": "2025-01-01T10:01:00"
}
```

---

### Step 3: Complete Registration

**Endpoint**: `POST /api/auth/signup/complete`

**Description**: Complete the registration by providing full user details (name, email, password, country). Email verification is NOT required - only mobile OTP verification is needed. This will create the user account and automatically log them in.

**Request Body**:
```json
{
  "mobile": "9876543210",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "country": "India",
  "city": "Mumbai",
  "state": "Maharashtra"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/auth/signup/complete \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "country": "India",
    "city": "Mumbai",
    "state": "Maharashtra"
  }'
```

**Success Response** (200):
```json
{
  "success": true,
  "message": "Registration completed successfully",
  "data": {
    "user": {
      "id": 5,
      "username": "user_9876543210",
      "name": "John Doe",
      "email": "john.doe@example.com",
      "mobile": "9876543210"
    },
    "tokens": {
      "token": "eyJhbGciOiJIUzUxMiJ9...",
      "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
      "type": "Bearer",
      "expiresIn": 86400000
    }
  },
  "timestamp": "2025-01-01T10:02:00"
}
```

**Error Response** (400):
```json
{
  "success": false,
  "message": "Email address is already registered",
  "data": null,
  "timestamp": "2025-01-01T10:02:00"
}
```

---

### Resend OTP

**Endpoint**: `POST /api/auth/signup/resend-otp`

**Description**: Resend OTP to the mobile number if the previous one expired or wasn't received.

**Request Body**:
```json
{
  "mobile": "9876543210"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/auth/signup/resend-otp \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210"
  }'
```

**Success Response** (200):
```json
{
  "success": true,
  "message": "OTP resent successfully",
  "data": {
    "message": "OTP resent to 98***10",
    "maskedMobile": "98***10",
    "expiresAt": "2025-01-01T10:15:00"
  },
  "timestamp": "2025-01-01T10:05:00"
}
```

---

## Validation Endpoints

### Check Email Availability

**Endpoint**: `GET /api/auth/check-email?email={email}`

**cURL Example**:
```bash
curl -X GET "http://localhost:8080/api/auth/check-email?email=john.doe@example.com"
```

**Response**:
```json
{
  "success": true,
  "message": "Email availability checked",
  "data": {
    "email": "john.doe@example.com",
    "isAvailable": true
  }
}
```

### Check Username Availability

**Endpoint**: `GET /api/auth/check-username?username={username}`

**cURL Example**:
```bash
curl -X GET "http://localhost:8080/api/auth/check-username?username=user_9876543210"
```

**Response**:
```json
{
  "success": true,
  "message": "Username availability checked",
  "data": {
    "username": "user_9876543210",
    "isAvailable": false
  }
}
```

---

## Testing the Complete Flow

### 1. Start the Signup Process

```bash
curl -X POST http://localhost:8080/api/auth/signup/mobile \
  -H "Content-Type: application/json" \
  -d '{"mobile": "9876543210"}'
```

**Expected Response**:
```json
{
  "success": true,
  "message": "OTP sent to mobile number",
  "data": {
    "message": "OTP sent to 98***10",
    "maskedMobile": "98***10",
    "expiresAt": "2025-01-01T10:10:00",
    "nextStep": "verify-otp"
  }
}
```

### 2. Verify the OTP (use 666666)

```bash
curl -X POST http://localhost:8080/api/auth/signup/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210",
    "otpCode": "666666"
  }'
```

**Expected Response**:
```json
{
  "success": true,
  "message": "OTP verified successfully",
  "data": {
    "message": "Mobile number verified successfully",
    "mobile": "9876543210",
    "nextStep": "complete-registration"
  }
}
```

### 3. Complete Registration

```bash
curl -X POST http://localhost:8080/api/auth/signup/complete \
  -H "Content-Type: application/json" \
  -d '{
    "mobile": "9876543210",
    "name": "John Doe",
    "email": "john.doe@example.com", 
    "password": "password123",
    "country": "India",
    "city": "Mumbai",
    "state": "Maharashtra"
  }'
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Registration completed successfully",
  "data": {
    "user": {
      "id": 5,
      "username": "user_9876543210",
      "name": "John Doe",
      "email": "john.doe@example.com",
      "mobile": "9876543210"
    },
    "tokens": {
      "token": "eyJhbGciOiJIUzUxMiJ9...",
      "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
      "type": "Bearer",
      "expiresIn": 86400000
    }
  }
}
```

### 4. Verify Login with New User

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

---

## Important Notes

1. **OTP Code**: Always use `666666` for testing (hardcoded in development)
2. **Session**: The verification state is maintained in HTTP session between steps 2 and 3
3. **Automatic Login**: Step 3 automatically logs in the user and returns JWT tokens
4. **Username Generation**: Username is auto-generated as `user_{mobile_number}`
5. **Session Timeout**: OTP verification expires after 30 minutes
6. **Rate Limiting**: Maximum 5 OTP generations per hour per mobile number

## Error Scenarios

- **Mobile Already Registered**: Step 1 will fail if mobile is already in use
- **Invalid OTP**: Step 2 will fail with remaining attempts count
- **Session Expired**: Step 3 will fail if more than 30 minutes elapsed since OTP verification
- **Email Already Registered**: Step 3 will fail if email is already in use
- **Mobile Mismatch**: Step 3 will fail if mobile number doesn't match the verified one

## Development Setup

The OTP is hardcoded as `666666` for development. In production, you would:
1. Remove the hardcoded OTP logic
2. Integrate with SMS service (Twilio, AWS SNS, etc.)
3. Configure proper OTP generation and delivery
