# RepairApp Backend API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication
The API uses JWT Bearer tokens for authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Common Response Format
All APIs return responses in this format:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { /* response data */ },
  "timestamp": "2024-01-01T12:00:00",
  "error": null
}
```

---

## 1. Authentication APIs

### 1.1 User Login
**POST** `/auth/login`

**Request Body:**
```json
{
  "username": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "username": "user@example.com",
      "email": "user@example.com",
      "name": "John Doe",
      "role": "CUSTOMER"
    }
  }
}
```

### 1.2 User Registration with OTP
**POST** `/auth/register`

**Request Body:**
```json
{
  "username": "newuser@example.com",
  "email": "newuser@example.com",
  "password": "newpassword123",
  "name": "Jane Smith",
  "phone": "+1234567890",
  "otpCode": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully! Username: newuser@example.com"
}
```

### 1.3 Forgot Password
**POST** `/auth/forgot-password`

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Password reset OTP sent successfully",
  "data": {
    "message": "Password reset OTP sent to us***@example.com",
    "expiresAt": "2024-01-01T12:10:00"
  }
}
```

### 1.4 Reset Password with OTP
**POST** `/auth/reset-password`

**Request Body:**
```json
{
  "email": "user@example.com",
  "otpCode": "123456",
  "newPassword": "newpassword123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Password reset successfully"
}
```

### 1.5 Change Password
**POST** `/auth/change-password`
*Requires Authentication*

**Request Body:**
```json
{
  "oldPassword": "oldpassword123",
  "newPassword": "newpassword123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

### 1.6 Get Current User
**GET** `/auth/me`
*Requires Authentication*

**Response:**
```json
{
  "success": true,
  "message": "User information retrieved",
  "data": {
    "id": 1,
    "username": "user@example.com",
    "email": "user@example.com",
    "name": "John Doe",
    "role": "CUSTOMER",
    "isActive": true,
    "emailVerified": true
  }
}
```

### 1.7 Refresh Token
**POST** `/auth/refresh?refreshToken={token}`

**Response:**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

### 1.8 Check Email Availability
**GET** `/auth/check-email?email=test@example.com`

**Response:**
```json
{
  "success": true,
  "message": "Email availability checked",
  "data": {
    "email": "test@example.com",
    "isAvailable": true
  }
}
```

### 1.9 Check Username Availability
**GET** `/auth/check-username?username=testuser`

**Response:**
```json
{
  "success": true,
  "message": "Username availability checked",
  "data": {
    "username": "testuser",
    "isAvailable": true
  }
}
```

---

## 2. OTP Verification APIs

### 2.1 Generate OTP
**POST** `/otp/generate`

**Request Body:**
```json
{
  "identifier": "user@example.com",
  "otpType": "EMAIL_VERIFICATION"
}
```

**OTP Types:** `EMAIL_VERIFICATION`, `PHONE_VERIFICATION`, `PASSWORD_RESET`, `ORDER_CONFIRMATION`, `ACCOUNT_RECOVERY`, `TWO_FACTOR_AUTH`, `EMAIL_CHANGE_VERIFICATION`

**Response:**
```json
{
  "success": true,
  "message": "OTP generated successfully",
  "data": {
    "message": "OTP sent successfully to us***@example.com",
    "expiresAt": "2024-01-01T12:10:00",
    "remainingAttempts": 5
  }
}
```

### 2.2 Verify OTP
**POST** `/otp/verify`

**Request Body:**
```json
{
  "identifier": "user@example.com",
  "otpCode": "123456",
  "otpType": "EMAIL_VERIFICATION"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP verified successfully"
}
```

### 2.3 Resend OTP
**POST** `/otp/resend`

**Request Body:**
```json
{
  "identifier": "user@example.com",
  "otpType": "EMAIL_VERIFICATION"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP resent successfully",
  "data": {
    "message": "OTP resent successfully to us***@example.com",
    "expiresAt": "2024-01-01T12:10:00"
  }
}
```

### 2.4 Generate Signup OTP
**POST** `/otp/generate/signup`

**Request Body:**
```json
{
  "email": "newuser@example.com"
}
```

### 2.5 Generate Password Reset OTP
**POST** `/otp/generate/password-reset`

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

### 2.6 Generate Order Confirmation OTP
**POST** `/otp/generate/order-confirmation`
*Requires Authentication*

**Request Body:**
```json
{
  "identifier": "user@example.com"
}
```

### 2.7 Get OTP Status
**GET** `/otp/status?identifier=user@example.com&otpType=EMAIL_VERIFICATION`

**Response:**
```json
{
  "success": true,
  "message": "OTP status retrieved successfully",
  "data": {
    "hasActiveOTP": true,
    "remainingAttempts": 3,
    "canResend": false,
    "maskedIdentifier": "us***@example.com"
  }
}
```

---

## 3. Repair Services APIs

### 3.1 Get All Services
**GET** `/services`

**Response:**
```json
{
  "success": true,
  "message": "Services retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Screen Replacement",
      "description": "Professional mobile screen replacement with warranty",
      "deviceType": "MOBILE",
      "basePrice": 45.00,
      "estimatedDurationHours": 2,
      "isActive": true,
      "isFeatured": true,
      "imageUrl": "https://example.com/image.jpg",
      "badge": "Popular",
      "features": ["Warranty Included", "Original Parts", "Quick Service"]
    }
  ]
}
```

### 3.2 Get Service by ID
**GET** `/services/{serviceId}`

### 3.3 Get Services by Device Type
**GET** `/services/device/{deviceType}`

**Device Types:** `LAPTOP`, `MOBILE`, `TABLET`, `WATCH`

### 3.4 Search Services
**GET** `/services/search?q=screen`

### 3.5 Get Services by Price Range
**GET** `/services/price-range?minPrice=20&maxPrice=100`

### 3.6 Get Services by Duration
**GET** `/services/duration?maxHours=2`

### 3.7 Get Popular Services
**GET** `/services/popular`

### 3.8 Get Featured Services
**GET** `/services/featured`

### 3.9 Advanced Search with Filters
**GET** `/services/filter?searchTerm=screen&deviceType=LAPTOP&minPrice=50&maxPrice=150&maxDuration=3&sortBy=price_asc`

**Sort Options:** `price_asc`, `price_desc`, `duration_asc`, `duration_desc`, `name_asc`, `name_desc`

### 3.10 Get Services with Pagination
**GET** `/services/paginated?page=0&size=10&sortBy=name_asc`

### 3.11 Get Services by Category
**GET** `/services/category/{category}`

### 3.12 Get Services by Multiple Device Types
**POST** `/services/device-types`

**Request Body:**
```json
["LAPTOP", "MOBILE"]
```

### 3.13 Get Service Statistics
**GET** `/services/statistics`

**Response:**
```json
{
  "success": true,
  "message": "Statistics retrieved successfully",
  "data": {
    "totalServices": 17,
    "activeServices": 17,
    "laptopServices": 6,
    "mobileServices": 5,
    "tabletServices": 3,
    "watchServices": 3,
    "priceRanges": {
      "underTwentyFive": 1,
      "twentyFiveToFifty": 8,
      "fiftyToHundred": 6,
      "overHundred": 2
    }
  }
}
```

### 3.14 Create Service (Admin Only)
**POST** `/services/admin/create`
*Requires ADMIN role*

**Request Body:**
```json
{
  "name": "New Service",
  "description": "Service description",
  "deviceType": "LAPTOP",
  "basePrice": 75.00,
  "estimatedDurationHours": 2,
  "isActive": true,
  "isFeatured": false,
  "imageUrl": "https://example.com/image.jpg",
  "badge": "New",
  "features": ["Feature 1", "Feature 2"]
}
```

### 3.15 Update Service (Admin Only)
**PUT** `/services/admin/{serviceId}`
*Requires ADMIN role*

### 3.16 Deactivate Service (Admin Only)
**DELETE** `/services/admin/{serviceId}`
*Requires ADMIN role*

### 3.17 Toggle Featured Status (Admin Only)
**PUT** `/services/admin/{serviceId}/featured`
*Requires ADMIN role*

---

## 4. Cart Management APIs

### 4.1 Add Item to Cart
**POST** `/cart/items`

**Request Body:**
```json
{
  "serviceId": 1,
  "quantity": 2,
  "notes": "Handle with care"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Item added to cart successfully",
  "data": {
    "id": 1,
    "quantity": 2,
    "unitPrice": 45.00,
    "totalPrice": 90.00,
    "notes": "Handle with care",
    "service": {
      "id": 1,
      "name": "Screen Replacement",
      "deviceType": "MOBILE"
    }
  }
}
```

### 4.2 Get Cart Items
**GET** `/cart/items`

**Response:**
```json
{
  "success": true,
  "message": "Cart items retrieved successfully",
  "data": [
    {
      "id": 1,
      "quantity": 2,
      "unitPrice": 45.00,
      "totalPrice": 90.00,
      "notes": "Handle with care",
      "service": {
        "id": 1,
        "name": "Screen Replacement",
        "deviceType": "MOBILE"
      }
    }
  ]
}
```

### 4.3 Get Cart Summary
**GET** `/cart/summary`

**Response:**
```json
{
  "success": true,
  "message": "Cart summary retrieved successfully",
  "data": {
    "itemCount": 2,
    "totalAmount": 135.00,
    "isEmpty": false
  }
}
```

### 4.4 Update Cart Item Quantity
**PUT** `/cart/items/{cartItemId}`

**Request Body:**
```json
{
  "quantity": 3
}
```

### 4.5 Remove Item from Cart
**DELETE** `/cart/items/{cartItemId}`

### 4.6 Clear Cart
**DELETE** `/cart/clear`

### 4.7 Merge Cart (After Login)
**POST** `/cart/merge`
*Requires Authentication*

### 4.8 Check if Service in Cart
**GET** `/cart/check-service/{serviceId}`

**Response:**
```json
{
  "success": true,
  "message": "Service check completed",
  "data": {
    "serviceId": 1,
    "isInCart": true
  }
}
```

### 4.9 Extend Cart Expiry
**POST** `/cart/extend-expiry/{cartId}?hours=24`

---

## 5. Order Management APIs

### 5.1 Create Order with OTP Verification
**POST** `/orders/create-with-otp`

**Request Body:**
```json
{
  "contactIdentifier": "user@example.com",
  "otpCode": "123456",
  "userId": 1,
  "problemDescription": "Screen is cracked and not responding",
  "pickupAddress": "123 Main St, City, State 12345",
  "customerNotes": "Please handle carefully",
  "orderItems": [
    {
      "serviceId": 1,
      "quantity": 1,
      "notes": "Priority repair"
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Order created successfully with OTP verification",
  "data": {
    "id": 1,
    "orderNumber": "ORD-20240101-001",
    "status": "PENDING",
    "totalPrice": 45.00,
    "problemDescription": "Screen is cracked and not responding",
    "pickupAddress": "123 Main St, City, State 12345",
    "customerNotes": "Please handle carefully",
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "user@example.com"
    },
    "orderItems": [
      {
        "id": 1,
        "quantity": 1,
        "unitPrice": 45.00,
        "totalPrice": 45.00,
        "service": {
          "id": 1,
          "name": "Screen Replacement"
        }
      }
    ]
  }
}
```

### 5.2 Create Order (Basic)
**POST** `/orders/create`

**Request Body:**
```json
{
  "userId": 1,
  "problemDescription": "Battery drains quickly",
  "pickupAddress": "123 Main St, City, State 12345",
  "customerNotes": "Evening pickup preferred",
  "orderItems": [
    {
      "serviceId": 2,
      "quantity": 1,
      "notes": "Original battery preferred"
    }
  ]
}
```

### 5.3 Get User Orders
**GET** `/orders/my-orders`
*Requires Authentication*

**Response:**
```json
{
  "success": true,
  "message": "Orders retrieved successfully",
  "data": [
    {
      "id": 1,
      "orderNumber": "ORD-20240101-001",
      "status": "IN_PROGRESS",
      "totalPrice": 45.00,
      "orderDate": "2024-01-01T10:00:00",
      "estimatedCompletionDate": "2024-01-02T10:00:00"
    }
  ]
}
```

### 5.4 Get Order by ID
**GET** `/orders/{orderId}`
*Requires Authentication*

### 5.5 Get Order by Number
**GET** `/orders/number/{orderNumber}`
*Requires Authentication*

### 5.6 Update Order Status (Technician/Admin)
**PUT** `/orders/{orderId}/status?status=IN_PROGRESS&technicianNotes=Started repair`
*Requires TECHNICIAN or ADMIN role*

**Order Statuses:** `PENDING`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `ON_HOLD`

### 5.7 Assign Technician (Admin)
**PUT** `/orders/{orderId}/assign-technician?technicianId=2`
*Requires ADMIN role*

### 5.8 Schedule Order
**PUT** `/orders/{orderId}/schedule?scheduledDate=2024-01-02T14:00:00`
*Requires TECHNICIAN or ADMIN role*

### 5.9 Cancel Order
**PUT** `/orders/{orderId}/cancel?reason=Customer request`

### 5.10 Cancel Order by Number
**PUT** `/orders/cancel/{orderNumber}?reason=Customer request`

### 5.11 Get Order Items
**GET** `/orders/{orderId}/items`

### 5.12 Get All Orders (Admin)
**GET** `/orders/all`
*Requires ADMIN role*

### 5.13 Get Orders by Status
**GET** `/orders/status/{status}`
*Requires TECHNICIAN or ADMIN role*

### 5.14 Search Orders
**GET** `/orders/search?q=ORD-20240101`
*Requires TECHNICIAN or ADMIN role*

### 5.15 Get Unassigned Orders (Admin)
**GET** `/orders/unassigned`
*Requires ADMIN role*

---

## 6. User Management APIs

### 6.1 Get User Profile
**GET** `/users/profile`
*Requires Authentication*

**Response:**
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "id": 1,
    "username": "user@example.com",
    "email": "user@example.com",
    "name": "John Doe",
    "phone": "+1234567890",
    "role": "CUSTOMER",
    "isActive": true,
    "emailVerified": true,
    "addresses": []
  }
}
```

### 6.2 Update User Profile
**PUT** `/users/profile`
*Requires Authentication*

**Request Body:**
```json
{
  "name": "John Smith",
  "phone": "+1234567891"
}
```

### 6.3 Add User Address
**POST** `/users/addresses`
*Requires Authentication*

**Request Body:**
```json
{
  "type": "HOME",
  "street": "123 Main Street",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "country": "USA",
  "isDefault": true
}
```

### 6.4 Get User Addresses
**GET** `/users/addresses`
*Requires Authentication*

### 6.5 Update Address
**PUT** `/users/addresses/{addressId}`
*Requires Authentication*

### 6.6 Delete Address
**DELETE** `/users/addresses/{addressId}`
*Requires Authentication*

---

## 7. Common Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "Invalid request data",
  "error": "Validation failed for field 'email': must be a well-formed email address",
  "timestamp": "2024-01-01T12:00:00"
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Authentication required",
  "error": "JWT token is missing or invalid",
  "timestamp": "2024-01-01T12:00:00"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Access denied",
  "error": "Insufficient privileges to access this resource",
  "timestamp": "2024-01-01T12:00:00"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Resource not found",
  "error": "RepairService not found with id: 999",
  "timestamp": "2024-01-01T12:00:00"
}
```

### 429 Too Many Requests
```json
{
  "success": false,
  "message": "Rate limit exceeded",
  "error": "Too many OTP requests. Please try again later.",
  "timestamp": "2024-01-01T12:00:00"
}
```

---

## 8. Headers and Authentication

### Required Headers
```
Content-Type: application/json
Accept: application/json
```

### Authentication Header (for protected endpoints)
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### CORS Headers
All endpoints support CORS with:
- Origin: `*` (configurable)
- Max Age: 3600 seconds
- Methods: GET, POST, PUT, DELETE, OPTIONS

---

## 9. Rate Limiting and Security

### OTP Rate Limiting
- Maximum 5 OTP generations per hour per identifier
- 2-minute cooldown between resend requests
- Maximum 5 verification attempts per OTP

### Security Features
- JWT token expiration (1 hour for access token)
- Role-based access control (CUSTOMER, TECHNICIAN, ADMIN)
- Input validation and sanitization
- SQL injection protection through JPA
- XSS protection through proper encoding

---

## 10. Environment Configuration

### Development Environment
```properties
server.port=8080
spring.profiles.active=dev
app.jwtSecret=mySecretKey
app.jwtExpirationInMs=3600000
app.mail.from=noreply@repairapp.com
app.name=RepairApp
```

### Database Configuration
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
```
