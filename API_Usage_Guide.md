# RepairApp Backend API Usage Guide

## Overview
This guide provides complete information about using the RepairApp Backend APIs, including authentication, token management, and API execution sequence.

## Server Configuration
- **Base URL**: `http://localhost:8080/api`
- **Server Port**: 8080
- **Context Path**: `/api`

## Authentication & Token Management

### 1. Authentication Methods

#### Simple Login (Demo Mode)
For quick testing and demo purposes:
```http
POST /auth/simple-login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123",
  "rememberMe": false
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "Alam Khan",
    "email": "alam@example.com",
    "phone": "+91 7026666835",
    "role": "ADMIN"
  }
}
```

#### JWT Login (Production Mode)
For full authentication with JWT tokens:
```http
POST /auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123",
  "rememberMe": true
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "refreshToken": "refresh_token_here",
    "id": 1,
    "username": "testuser",
    "name": "Test User",
    "email": "test@example.com",
    "phone": "+1234567890",
    "role": "CUSTOMER"
  }
}
```

### 2. Token Usage

#### For Protected Endpoints
All protected endpoints require the JWT token in the Authorization header:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Token Refresh
When your access token expires:
```http
POST /auth/refresh?refreshToken=your_refresh_token_here
```

**Response:**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "token": "new_jwt_token_here",
    "type": "Bearer",
    "refreshToken": "new_refresh_token_here",
    "id": 1,
    "username": "testuser",
    "name": "Test User",
    "email": "test@example.com",
    "phone": "+1234567890",
    "role": "CUSTOMER"
  }
}
```

## User Roles & Permissions

### Role Types
- **CUSTOMER**: Can create orders, view own orders, manage profile
- **TECHNICIAN**: Can view assigned orders, update order status, view all orders
- **ADMIN**: Full access to all endpoints, can assign technicians, view all data

### Role-Based Access Control
| Endpoint | Customer | Technician | Admin |
|----------|----------|------------|-------|
| `/auth/*` | ✅ | ✅ | ✅ |
| `/services/*` | ✅ (public) | ✅ (public) | ✅ (public) |
| `/users/profile` | ✅ | ✅ | ✅ |
| `/users/addresses` | ✅ | ✅ | ✅ |
| `/users/payment-methods` | ✅ | ✅ | ✅ |
| `/users/technicians` | ✅ | ✅ | ✅ |
| `/orders` (create) | ✅ | ❌ | ✅ |
| `/orders/my-orders` | ✅ | ✅ | ✅ |
| `/orders/{id}/status` | ❌ | ✅ | ✅ |
| `/orders/{id}/assign-technician` | ❌ | ❌ | ✅ |
| `/orders/all` | ❌ | ❌ | ✅ |
| `/orders/unassigned` | ❌ | ❌ | ✅ |

## API Execution Sequence

### Customer Flow

#### 1. Registration/Login
```
1. POST /auth/register (if new user)
2. POST /auth/login (get JWT token)
3. Store the JWT token for subsequent requests
```

#### 2. Profile Setup
```
1. GET /users/profile (check current profile)
2. PUT /users/profile (update if needed)
3. POST /users/addresses (add pickup addresses)
4. POST /users/payment-methods (add payment methods)
```

#### 3. Browse Services & Create Order
```
1. GET /services (browse all services)
2. GET /services/device/LAPTOP (filter by device type)
3. GET /services/search?q=screen (search specific services)
4. POST /orders (create repair order)
5. GET /orders/my-orders (check order status)
```

### Technician Flow

#### 1. Login & View Assignments
```
1. POST /auth/login (login as technician)
2. GET /orders/status/PENDING (view pending orders)
3. GET /orders/status/CONFIRMED (view confirmed orders)
4. GET /orders/{orderId} (view specific order details)
```

#### 2. Work on Orders
```
1. PUT /orders/{orderId}/status?status=CONFIRMED (confirm order)
2. PUT /orders/{orderId}/schedule?scheduledDate=2024-08-30T10:00:00 (schedule)
3. PUT /orders/{orderId}/status?status=IN_PROGRESS (start work)
4. PUT /orders/{orderId}/status?status=COMPLETED&technicianNotes=Repair completed (finish)
```

### Admin Flow

#### 1. Management Overview
```
1. POST /auth/login (login as admin)
2. GET /orders/all (view all orders)
3. GET /orders/unassigned (view orders needing assignment)
4. GET /users/technicians (view available technicians)
```

#### 2. Order Management
```
1. PUT /orders/{orderId}/assign-technician?technicianId=2 (assign work)
2. GET /orders/status/IN_PROGRESS (monitor active work)
3. GET /services/statistics (view service statistics)
```

## Request & Response Examples

### User Registration
```http
POST /auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securepass123",
  "phone": "+1234567890"
}
```

### Add User Address
```http
POST /users/addresses
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "type": "Home",
  "address": "123 Main Street, Apt 4B",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "isDefault": true
}
```

### Add Payment Method
```http
POST /users/payment-methods
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "type": "CREDIT_CARD",
  "lastFour": "1234",
  "brand": "Visa",
  "expiryMonth": "12",
  "expiryYear": "2025",
  "cardholderName": "John Doe",
  "isDefault": true
}
```

**Payment Types Available:**
- `CREDIT_CARD`
- `DEBIT_CARD` 
- `DIGITAL_WALLET`
- `BANK_TRANSFER`

### Create Repair Order
```http
POST /orders?serviceId=1&problemDescription=Screen is cracked&pickupAddress=123 Main St&customerNotes=Call before pickup
Authorization: Bearer {jwt_token}
```

**Order Status Flow:**
1. `PENDING` → `CONFIRMED` → `IN_PROGRESS` → `COMPLETED`
2. `PENDING` → `CANCELLED`
3. `COMPLETED` → `REFUNDED` (if needed)

## Data Models & Validation

### Device Types
- `LAPTOP`: Laptop computers
- `MOBILE`: Mobile phones
- `TABLET`: Tablet devices
- `WATCH`: Smart watches

### Address Validation
- `zipCode`: Must match pattern `^\d{5}(-\d{4})?$`
- `phone`: Must match pattern `^\+?[1-9]\d{1,14}$`

### User Validation
- `username`: 3-50 characters
- `password`: Minimum 8 characters
- `email`: Valid email format
- `name`: Maximum 100 characters

## Error Handling

### Common HTTP Status Codes
- `200`: Success
- `400`: Bad Request (validation errors)
- `401`: Unauthorized (invalid/missing token)
- `403`: Forbidden (insufficient permissions)
- `404`: Not Found (resource doesn't exist)
- `500`: Internal Server Error

### Error Response Format
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

## Testing with Postman

### Environment Setup
1. Import the `RepairApp_Postman_Collection.json`
2. Set environment variables:
   - `base_url`: `http://localhost:8080/api`
   - `jwt_token`: (auto-populated after login)
   - `refresh_token`: (auto-populated after login)

### Authentication Flow
1. Use "Simple Login (Demo)" for quick testing
2. Use "User Login (JWT)" for full authentication
3. The collection automatically adds JWT tokens to protected endpoints

### Testing Sequence
1. **Start with Authentication**: Login first to get tokens
2. **Public Endpoints**: Test service endpoints (no auth required)
3. **User Management**: Test profile and address management
4. **Order Creation**: Create and manage orders
5. **Admin Functions**: Test with admin role for full access

## Database Setup

Before testing, ensure your database is set up:

```sql
-- The application will auto-create tables, but you can run setup-database.sql for initial data
-- Default admin credentials are in the demo login endpoint
```

## API Documentation

- **Swagger UI**: Available at `http://localhost:8080/api/swagger-ui.html`
- **API Docs**: Available at `http://localhost:8080/api/api-docs`

## Security Notes

1. **JWT Tokens**: Have 24-hour expiration (configurable)
2. **Refresh Tokens**: Have 7-day expiration (configurable)
3. **CORS**: Configured for localhost:3000 and 3001
4. **Password**: Minimum 8 characters required
5. **Rate Limiting**: Not implemented (can be added if needed)

## Environment Variables (Optional)

You can override default configurations using environment variables:
- `JWT_SECRET`: Custom JWT signing secret
- `CORS_ALLOWED_ORIGINS`: Custom CORS origins
- `SMTP_HOST`: Email server for password reset
- `EMAIL_USERNAME` & `EMAIL_PASSWORD`: Email credentials

## Production Considerations

1. Change default JWT secret in production
2. Use HTTPS for all communications
3. Implement proper password reset with email
4. Add rate limiting for authentication endpoints
5. Use environment-specific database configurations
6. Enable SQL script initialization only in development

This guide provides everything needed to test and integrate with the RepairApp Backend API using Postman or any other REST client.
