# Postman Quick Start Guide for RepairApp Backend

## Step 1: Import the Collection

1. Open Postman
2. Click "Import" button
3. Choose "Upload Files"
4. Select `RepairApp_Postman_Collection.json`
5. Click "Import"

## Step 2: Set Up Environment Variables

After importing, the collection will have these variables pre-configured:
- `base_url`: `http://localhost:8080/api`
- `jwt_token`: (empty, will be auto-populated)
- `refresh_token`: (empty, will be auto-populated)

## Step 3: Start Your Application

Make sure your RepairApp backend is running:
```bash
# Navigate to your backend directory
cd "C:\Users\alams\Desktop\AI Generate\RepairApp\Backend"

# Run the application (use one of these methods)
./mvnw spring-boot:run
# OR
java -jar target/repair-service-backend-1.0.0.jar
# OR
./run.bat
```

## Step 4: Test Authentication

### Option A: Quick Demo Login
1. Go to `Authentication` → `Simple Login (Demo)`
2. Click "Send" (uses hardcoded admin/admin123 credentials)
3. Should return successful response with user data

### Option B: Full JWT Authentication
1. First register a user: `Authentication` → `User Registration`
2. Modify the request body with your details
3. Then login: `Authentication` → `User Login (JWT)`
4. The JWT token will be automatically stored for other requests

## Step 5: Test Public Endpoints (No Auth Required)

Test these endpoints without authentication:
1. `Repair Services` → `Get All Services`
2. `Repair Services` → `Get Services by Device Type`
3. `Repair Services` → `Search Services`

## Step 6: Test Protected Endpoints

After successful login, test protected endpoints:
1. `User Management` → `Get User Profile`
2. `User Management` → `Add User Address`
3. `Order Management` → `Create Order`
4. `Order Management` → `Get My Orders`

## API Testing Sequence

### For Customer Testing:
```
1. Register/Login → Get JWT token
2. Setup Profile → Add addresses and payment methods
3. Browse Services → Find repair service
4. Create Order → Book repair service
5. Track Order → Check order status
```

### For Technician Testing:
```
1. Login as Technician → Get JWT token
2. View Orders → Check assigned orders
3. Update Status → Confirm/Start/Complete orders
4. Schedule → Set appointment times
```

### For Admin Testing:
```
1. Login as Admin → Get JWT token
2. View All Orders → Monitor all activity
3. Assign Technicians → Manage workforce
4. View Statistics → Monitor business metrics
```

## Common Request Examples

### 1. Create Order
```http
POST {{base_url}}/orders
Authorization: Bearer {{jwt_token}}

Query Parameters:
- serviceId: 1
- problemDescription: "Screen is broken"
- pickupAddress: "123 Main St, NY"
- customerNotes: "Call before pickup"
```

### 2. Add Address
```json
POST {{base_url}}/users/addresses
Authorization: Bearer {{jwt_token}}
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

### 3. Update Order Status (Technician)
```http
PUT {{base_url}}/orders/1/status
Authorization: Bearer {{jwt_token}}

Query Parameters:
- status: IN_PROGRESS
- technicianNotes: "Started repair work"
```

## Troubleshooting

### Common Issues:

1. **401 Unauthorized**: Token expired or missing
   - Solution: Login again or refresh token

2. **403 Forbidden**: Insufficient permissions
   - Solution: Check user role requirements

3. **404 Not Found**: Resource doesn't exist
   - Solution: Verify IDs and endpoints

4. **400 Bad Request**: Validation errors
   - Solution: Check required fields and formats

### Token Management:
- Access tokens expire in 24 hours
- Refresh tokens expire in 7 days
- The collection automatically handles token storage
- Use "Refresh Token" endpoint when access token expires

## Validation Rules

### User Registration:
- Username: 3-50 characters
- Password: Minimum 8 characters
- Email: Valid email format
- Phone: Valid international format (+1234567890)

### Address:
- ZIP Code: 5 digits (12345) or 5+4 digits (12345-6789)

### Payment Method:
- Last Four: Exactly 4 digits
- Expiry Month: 01-12
- Expiry Year: 4 digits (e.g., 2025)

## Next Steps

1. Import the collection
2. Start your backend application  
3. Begin with authentication endpoints
4. Test public service endpoints
5. Move to protected user/order endpoints
6. Test role-based access with different user types

The collection includes automatic token management and proper request examples for all endpoints!
