# Repair Service Backend API

A comprehensive Spring Boot application for managing repair services with user authentication, order management, and complete service catalog.

## Features

- **JWT Authentication**: Secure user authentication with email verification
- **Multi-Service Orders**: Support for multiple repair services in a single order
- **Service Management**: Comprehensive service catalog for different device types
- **User Management**: Profile management with addresses and payment methods
- **Order Tracking**: Complete order lifecycle management
- **Admin Panel**: Administrative endpoints for service and user management

## Technology Stack

- **Backend**: Spring Boot 3.2.0
- **Database**: MySQL 8.0+
- **Authentication**: JWT with Spring Security
- **Email**: Spring Mail with SMTP
- **Build Tool**: Maven
- **Java Version**: 17+

## Prerequisites

### MySQL Database Setup
This application requires MySQL to be installed and running on your local machine.

#### Database Configuration
- **Database Name**: `repairapp_db`
- **Username**: `root`
- **Password**: `ovalridge`
- **Port**: `3306` (default MySQL port)

#### Setting up the Database

1. **Install MySQL** (if not already installed)
   - Download from: https://dev.mysql.com/downloads/mysql/
   - Or use a package manager like Chocolatey: `choco install mysql`

2. **Start MySQL Service**
   ```bash
   # Windows (if installed as service)
   net start MySQL80
   
   # Or start manually if installed via zip
   mysqld --console
   ```

3. **Create the Database**
   ```sql
   -- Connect to MySQL as root
   mysql -u root -p
   
   -- Enter password: ovalridge
   
   -- Create the database
   CREATE DATABASE IF NOT EXISTS repairapp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   
   -- Verify database creation
   SHOW DATABASES;
   
   -- Exit MySQL
   EXIT;
   ```

4. **Alternative: Use the provided setup script**
   ```bash
   mysql -u root -p < setup-mysql.sql
   ```

## Running the Application

1. **Ensure MySQL is running** and the `repairapp_db` database exists
2. **Compile the application**:
   ```bash
   mvn clean compile
   ```
3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

The application will:
- Automatically create all necessary tables (due to `hibernate.ddl-auto=update`)
- Populate sample data from `data.sql`
- Start on `http://localhost:8080/api`

## API Documentation

Once the application is running, you can access:
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API Docs**: http://localhost:8080/api/api-docs

## Sample Credentials

The application comes with pre-configured sample users:

### Admin User
- **Mobile**: `7026666835`
- **Password**: `admin123`
- **Email**: `alam@repairapp.com`

### Technicians
- **Mike Johnson**: `mike@repairapp.com` / `admin123`
- **Sarah Wilson**: `sarah@repairapp.com` / `admin123`

### Customer
- **John Doe**: `john@example.com` / `admin123`

## Configuration

All database credentials are stored in `src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=ovalridge
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration  
- `POST /api/auth/logout` - User logout
- `GET /api/auth/me` - Get current user info
- `POST /api/auth/change-password` - Change password
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password with token

### Services (Public)
- `GET /api/services` - Get all repair services
- `GET /api/services/device/{deviceType}` - Get services by device type
- `GET /api/services/{serviceId}` - Get specific service
- `GET /api/services/search?q={query}` - Search services

### Orders (Authenticated)
- `POST /api/orders` - Create new order
- `GET /api/orders` - Get user orders
- `GET /api/orders/{orderId}` - Get specific order

### Users (Authenticated)
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `GET /api/users/addresses` - Get user addresses
- `POST /api/users/addresses` - Add new address

## Project Structure

```
src/main/java/com/repairapp/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Data transfer objects
├── entity/         # JPA entities
├── exception/      # Exception handling
├── repository/     # Data repositories
├── security/       # Security components
└── service/        # Business logic services
```

## Troubleshooting

### Database Connection Issues
1. Verify MySQL is running: `mysqladmin ping -u root -p`
2. Check if database exists: `mysql -u root -p -e "SHOW DATABASES;"`
3. Verify credentials work: `mysql -u root -povalridge -e "SELECT 1;"`

### Port Conflicts
- Default port is 8080. Change in `application.properties` if needed:
  ```properties
  server.port=8081
  ```

### JAVA_HOME Issues
If you get "JAVA_HOME not found" error:
1. Check if Java is installed: `java -version`
2. Set JAVA_HOME environment variable

## Frontend Integration

The backend is configured to work with the Next.js frontend:
- **Frontend URL**: http://localhost:3000
- **CORS**: Enabled for frontend domain
- **API Structure**: Compatible with frontend data expectations

## API Testing

You can test the APIs using:
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **Postman** or similar API testing tools
- **curl** commands

Example curl command:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
