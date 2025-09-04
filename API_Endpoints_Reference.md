# RepairApp Backend - Complete API Endpoints Reference

## Base URL
`http://localhost:8080/api`

---

## 🔐 Authentication Endpoints

### 1. Simple Login (Demo)
- **Endpoint**: `POST /auth/simple-login`
- **Auth Required**: ❌ No
- **Payload**:
```json
{
  "username": "admin",
  "password": "admin123",
  "rememberMe": false
}
```
- **Success Response**: User object with role information

### 2. JWT Login
- **Endpoint**: `POST /auth/login`
- **Auth Required**: ❌ No
- **Payload**:
```json
{
  "username": "string (3-50 chars)",
  "password": "string (min 8 chars)",
  "rememberMe": boolean
}
```
- **Success Response**: JWT tokens + user info

### 3. User Registration
- **Endpoint**: `POST /auth/register`
- **Auth Required**: ❌ No
- **Payload**:
```json
{
  "username": "string (3-50 chars)",
  "name": "string (max 100 chars)",
  "email": "valid email",
  "password": "string (min 8 chars)",
  "phone": "string (+1234567890 format)"
}
```

### 4. Refresh Token
- **Endpoint**: `POST /auth/refresh`
- **Auth Required**: ❌ No
- **Query Params**: `refreshToken=string`
- **Success Response**: New JWT tokens

### 5. Change Password
- **Endpoint**: `POST /auth/change-password`
- **Auth Required**: ✅ Yes
- **Query Params**: 
  - `username=string`
  - `oldPassword=string`
  - `newPassword=string`

### 6. Forgot Password
- **Endpoint**: `POST /auth/forgot-password`
- **Auth Required**: ❌ No
- **Query Params**: `email=string`

### 7. Get Current User
- **Endpoint**: `GET /auth/me`
- **Auth Required**: ✅ Yes
- **Success Response**: Current user profile

### 8. Logout
- **Endpoint**: `POST /auth/logout`
- **Auth Required**: ✅ Yes
- **Note**: Client-side token removal

---

## 👤 User Management Endpoints

### 1. Get User Profile
- **Endpoint**: `GET /users/profile`
- **Auth Required**: ✅ Yes
- **Role**: All authenticated users
- **Success Response**: User profile data

### 2. Update User Profile
- **Endpoint**: `PUT /users/profile`
- **Auth Required**: ✅ Yes
- **Role**: All authenticated users
- **Payload**:
```json
{
  "name": "string",
  "email": "valid email",
  "phone": "string"
}
```

### 3. Get User Addresses
- **Endpoint**: `GET /users/addresses`
- **Auth Required**: ✅ Yes
- **Role**: All authenticated users
- **Success Response**: Array of address objects

### 4. Add User Address
- **Endpoint**: `POST /users/addresses`
- **Auth Required**: ✅ Yes
- **Role**: All authenticated users
- **Payload**:
```json
{
  "type": "string (Home/Work/Other)",
  "address": "string (max 255 chars)",
  "city": "string (max 100 chars)",
  "state": "string (max 100 chars)",
  "zipCode": "string (12345 or 12345-6789)",
  "isDefault": boolean
}
```

### 5. Update User Address
- **Endpoint**: `PUT /users/addresses/{addressId}`
- **Auth Required**: ✅ Yes
- **Role**: All authenticated users
- **Path Params**: `addressId=number`
- **Payload**: Same as Add Address

### 6. Delete User Address
- **Endpoint**: `DELETE /users/addresses/{addressId}`
- **Auth Required**: ✅ Yes
- **Role**: All authenticated users
- **Path Params**: `addressId=number`

### 7. Get User Payment Methods
- **Endpoint**: `GET /users/payment-methods`
- **Auth Required**: ✅ Yes
- **Role**: All authenticated users
- **Success Response**: Array of payment method objects

### 8. Add User Payment Method
- **Endpoint**: `POST /users/payment-methods`
- **Auth Required**: ✅ Yes
- **Role**: All authenticated users
- **Payload**:
```json
{
  "type": "CREDIT_CARD|DEBIT_CARD|DIGITAL_WALLET|BANK_TRANSFER",
  "lastFour": "string (4 digits)",
  "brand": "string (Visa/MasterCard/etc)",
  "expiryMonth": "string (01-12)",
  "expiryYear": "string (4 digits)",
  "cardholderName": "string",
  "isDefault": boolean
}
```

### 9. Delete User Payment Method
- **Endpoint**: `DELETE /users/payment-methods/{paymentMethodId}`
- **Auth Required**: ✅ Yes
- **Role**: All authenticated users
- **Path Params**: `paymentMethodId=number`

### 10. Get Available Technicians
- **Endpoint**: `GET /users/technicians`
- **Auth Required**: ✅ Yes
- **Role**: ADMIN, CUSTOMER
- **Success Response**: Array of technician user objects

---

## 🔧 Repair Services Endpoints

### 1. Get All Services
- **Endpoint**: `GET /services`
- **Auth Required**: ❌ No (Public)
- **Success Response**: Array of all repair services

### 2. Get Service by ID
- **Endpoint**: `GET /services/{serviceId}`
- **Auth Required**: ❌ No (Public)
- **Path Params**: `serviceId=number`
- **Success Response**: Single service object

### 3. Get Services by Device Type
- **Endpoint**: `GET /services/device/{deviceType}`
- **Auth Required**: ❌ No (Public)
- **Path Params**: `deviceType=LAPTOP|MOBILE|TABLET|WATCH`
- **Success Response**: Array of services for device type

### 4. Search Services
- **Endpoint**: `GET /services/search`
- **Auth Required**: ❌ No (Public)
- **Query Params**: `q=string` (search term)
- **Success Response**: Array of matching services

### 5. Get Services by Price Range
- **Endpoint**: `GET /services/price-range`
- **Auth Required**: ❌ No (Public)
- **Query Params**: 
  - `minPrice=decimal`
  - `maxPrice=decimal`
- **Success Response**: Array of services in price range

### 6. Get Services by Duration
- **Endpoint**: `GET /services/duration`
- **Auth Required**: ❌ No (Public)
- **Query Params**: `maxHours=integer`
- **Success Response**: Array of services within duration

### 7. Get Popular Services
- **Endpoint**: `GET /services/popular`
- **Auth Required**: ❌ No (Public)
- **Success Response**: Array of most popular services

### 8. Get Service Statistics
- **Endpoint**: `GET /services/statistics`
- **Auth Required**: ❌ No (Public)
- **Success Response**: Service statistics object

---

## 📋 Order Management Endpoints

### 1. Create Order
- **Endpoint**: `POST /orders`
- **Auth Required**: ✅ Yes
- **Role**: CUSTOMER, ADMIN
- **Query Params**:
  - `serviceId=number` (required)
  - `problemDescription=string` (required)
  - `pickupAddress=string` (required)
  - `customerNotes=string` (optional)
- **Success Response**: Created order object

### 2. Get My Orders
- **Endpoint**: `GET /orders/my-orders`
- **Auth Required**: ✅ Yes
- **Role**: All authenticated users
- **Success Response**: Array of user's orders

### 3. Get My Orders (Paginated)
- **Endpoint**: `GET /orders/my-orders/page`
- **Auth Required**: ✅ Yes
- **Role**: All authenticated users
- **Query Params**:
  - `page=number` (0-based)
  - `size=number`
  - `sort=string` (e.g., "createdAt,desc")
- **Success Response**: Paginated order data

### 4. Get Order by ID
- **Endpoint**: `GET /orders/{orderId}`
- **Auth Required**: ✅ Yes
- **Role**: All authenticated users (with access control)
- **Path Params**: `orderId=number`
- **Success Response**: Single order object

### 5. Get Order by Order Number
- **Endpoint**: `GET /orders/number/{orderNumber}`
- **Auth Required**: ✅ Yes
- **Role**: All authenticated users (with access control)
- **Path Params**: `orderNumber=string`
- **Success Response**: Single order object

### 6. Update Order Status
- **Endpoint**: `PUT /orders/{orderId}/status`
- **Auth Required**: ✅ Yes
- **Role**: TECHNICIAN, ADMIN
- **Path Params**: `orderId=number`
- **Query Params**:
  - `status=PENDING|CONFIRMED|IN_PROGRESS|COMPLETED|CANCELLED|REFUNDED`
  - `technicianNotes=string` (optional)
- **Success Response**: Updated order object

### 7. Assign Technician
- **Endpoint**: `PUT /orders/{orderId}/assign-technician`
- **Auth Required**: ✅ Yes
- **Role**: ADMIN
- **Path Params**: `orderId=number`
- **Query Params**: `technicianId=number`
- **Success Response**: Updated order object

### 8. Schedule Order
- **Endpoint**: `PUT /orders/{orderId}/schedule`
- **Auth Required**: ✅ Yes
- **Role**: TECHNICIAN, ADMIN
- **Path Params**: `orderId=number`
- **Query Params**: `scheduledDate=ISO8601` (e.g., 2024-08-30T10:00:00)
- **Success Response**: Updated order object

### 9. Cancel Order
- **Endpoint**: `PUT /orders/{orderId}/cancel`
- **Auth Required**: ✅ Yes
- **Role**: All authenticated users
- **Path Params**: `orderId=number`
- **Query Params**: `reason=string`
- **Success Response**: Updated order object

### 10. Get All Orders (Admin)
- **Endpoint**: `GET /orders/all`
- **Auth Required**: ✅ Yes
- **Role**: ADMIN
- **Success Response**: Array of all orders

### 11. Get Orders by Status
- **Endpoint**: `GET /orders/status/{status}`
- **Auth Required**: ✅ Yes
- **Role**: TECHNICIAN, ADMIN
- **Path Params**: `status=PENDING|CONFIRMED|IN_PROGRESS|COMPLETED|CANCELLED|REFUNDED`
- **Success Response**: Array of orders with specified status

### 12. Search Orders
- **Endpoint**: `GET /orders/search`
- **Auth Required**: ✅ Yes
- **Role**: TECHNICIAN, ADMIN
- **Query Params**: `q=string` (search term)
- **Success Response**: Array of matching orders

### 13. Get Unassigned Orders
- **Endpoint**: `GET /orders/unassigned`
- **Auth Required**: ✅ Yes
- **Role**: ADMIN
- **Success Response**: Array of orders needing technician assignment

---

## 📊 Response Format

All API responses follow this standardized format:

### Success Response
```json
{
  "success": true,
  "message": "Operation successful message",
  "data": {
    // Response data object or array
  }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

---

## 🏷️ Data Models

### User Object
```json
{
  "id": 1,
  "username": "string",
  "name": "string",
  "email": "string",
  "phone": "string",
  "role": "CUSTOMER|TECHNICIAN|ADMIN",
  "isActive": true,
  "emailVerified": false,
  "phoneVerified": false,
  "createdAt": "2024-08-29T18:00:00",
  "updatedAt": "2024-08-29T18:00:00"
}
```

### Address Object
```json
{
  "id": 1,
  "type": "Home|Work|Other",
  "address": "string",
  "city": "string",
  "state": "string", 
  "zipCode": "string",
  "isDefault": true,
  "createdAt": "2024-08-29T18:00:00",
  "updatedAt": "2024-08-29T18:00:00"
}
```

### Payment Method Object
```json
{
  "id": 1,
  "type": "CREDIT_CARD|DEBIT_CARD|DIGITAL_WALLET|BANK_TRANSFER",
  "lastFour": "1234",
  "brand": "Visa",
  "expiryMonth": "12",
  "expiryYear": "2025",
  "cardholderName": "John Doe",
  "isDefault": true,
  "isActive": true,
  "createdAt": "2024-08-29T18:00:00",
  "updatedAt": "2024-08-29T18:00:00"
}
```

### Repair Service Object
```json
{
  "id": 1,
  "name": "Screen Replacement",
  "description": "Complete screen replacement service",
  "deviceType": "LAPTOP|MOBILE|TABLET|WATCH",
  "basePrice": 150.00,
  "estimatedDurationHours": 24,
  "isActive": true,
  "imageUrl": "string",
  "createdAt": "2024-08-29T18:00:00",
  "updatedAt": "2024-08-29T18:00:00"
}
```

### Repair Order Object
```json
{
  "id": 1,
  "orderNumber": "RO20240829001",
  "status": "PENDING|CONFIRMED|IN_PROGRESS|COMPLETED|CANCELLED|REFUNDED",
  "totalPrice": 150.00,
  "problemDescription": "string",
  "solutionDescription": "string",
  "scheduledDate": "2024-08-30T10:00:00",
  "completedDate": null,
  "pickupAddress": "string",
  "customerNotes": "string",
  "technicianNotes": "string",
  "service": {
    // RepairService object
  },
  "technician": {
    // User object (if assigned)
  },
  "createdAt": "2024-08-29T18:00:00",
  "updatedAt": "2024-08-29T18:00:00"
}
```

---

## 🔄 API Call Sequence Examples

### Complete Customer Journey

#### 1. User Registration & Setup
```http
# Step 1: Register
POST /auth/register
{
  "username": "johndoe",
  "name": "John Doe", 
  "email": "john@example.com",
  "password": "securepass123",
  "phone": "+1234567890"
}

# Step 2: Login (get JWT token)
POST /auth/login
{
  "username": "johndoe",
  "password": "securepass123",
  "rememberMe": true
}

# Step 3: Setup profile (add address)
POST /users/addresses
Authorization: Bearer {jwt_token}
{
  "type": "Home",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY", 
  "zipCode": "10001",
  "isDefault": true
}

# Step 4: Add payment method
POST /users/payment-methods
Authorization: Bearer {jwt_token}
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

#### 2. Service Discovery & Ordering
```http
# Step 1: Browse services
GET /services

# Step 2: Filter by device type
GET /services/device/LAPTOP

# Step 3: Search for specific service
GET /services/search?q=screen repair

# Step 4: Get service details
GET /services/1

# Step 5: Create repair order
POST /orders?serviceId=1&problemDescription=Broken screen&pickupAddress=123 Main St&customerNotes=Call first
Authorization: Bearer {jwt_token}

# Step 6: Check order status
GET /orders/my-orders
Authorization: Bearer {jwt_token}
```

### Technician Workflow

#### 1. View and Process Orders
```http
# Step 1: Login as technician
POST /auth/login
{
  "username": "tech1",
  "password": "techpass123"
}

# Step 2: View pending orders
GET /orders/status/PENDING
Authorization: Bearer {jwt_token}

# Step 3: View order details
GET /orders/1
Authorization: Bearer {jwt_token}

# Step 4: Confirm order
PUT /orders/1/status?status=CONFIRMED&technicianNotes=Order confirmed
Authorization: Bearer {jwt_token}

# Step 5: Schedule appointment
PUT /orders/1/schedule?scheduledDate=2024-08-30T10:00:00
Authorization: Bearer {jwt_token}

# Step 6: Start work
PUT /orders/1/status?status=IN_PROGRESS&technicianNotes=Started repair
Authorization: Bearer {jwt_token}

# Step 7: Complete work
PUT /orders/1/status?status=COMPLETED&technicianNotes=Repair completed successfully
Authorization: Bearer {jwt_token}
```

### Admin Management Workflow

#### 1. Overview & Management
```http
# Step 1: Login as admin
POST /auth/login
{
  "username": "admin",
  "password": "admin123"
}

# Step 2: View all orders
GET /orders/all
Authorization: Bearer {jwt_token}

# Step 3: View unassigned orders
GET /orders/unassigned
Authorization: Bearer {jwt_token}

# Step 4: Get available technicians
GET /users/technicians
Authorization: Bearer {jwt_token}

# Step 5: Assign technician to order
PUT /orders/1/assign-technician?technicianId=2
Authorization: Bearer {jwt_token}

# Step 6: Monitor progress
GET /orders/status/IN_PROGRESS
Authorization: Bearer {jwt_token}

# Step 7: View service statistics
GET /services/statistics
Authorization: Bearer {jwt_token}
```

---

## ⚠️ Important Notes

### Token Management
1. **Access Token**: Valid for 24 hours
2. **Refresh Token**: Valid for 7 days
3. **Auto-Refresh**: Use refresh token before access token expires
4. **Storage**: Store tokens securely on client side

### Authentication Requirements
1. **Public Endpoints**: All `/services/*` endpoints
2. **Authentication Required**: All `/users/*` and `/orders/*` endpoints
3. **Role-Based**: Some endpoints require specific roles (TECHNICIAN, ADMIN)

### Request Headers
- **Content-Type**: `application/json` for POST/PUT with body
- **Authorization**: `Bearer {jwt_token}` for protected endpoints

### Error Codes
- **401**: Invalid or expired token → Login again
- **403**: Insufficient permissions → Check user role
- **400**: Validation error → Check request payload
- **404**: Resource not found → Verify IDs

### Order Status Flow
```
PENDING → CONFIRMED → IN_PROGRESS → COMPLETED
    ↓
CANCELLED
    ↓
REFUNDED (if applicable)
```

### Testing Tips
1. Start with Simple Login for quick testing
2. Use JWT Login for full authentication testing
3. Test public endpoints first (services)
4. Then test protected endpoints with proper tokens
5. Test role-based access with different user types

This reference covers all available endpoints with their complete specifications, payloads, and usage examples.
