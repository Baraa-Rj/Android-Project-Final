# Authentication Setup Guide

## Overview
This guide explains how to set up and use the authentication system for the Car Wash API.

## Database Migration Required

### Step 1: Add Password Column to Users Table

The `users` table needs a `password` column to store hashed passwords. You have two options:

#### Option A: Run the Migration Script (Recommended)
```bash
mysql -u root -p car_wash_db < backend/migrations/001_add_password_column.sql
```

#### Option B: Manual SQL Command
```sql
USE car_wash_db;

ALTER TABLE users
ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '' AFTER phone;

-- Set default password for existing users (hash of "Password123")
UPDATE users
SET password = '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lSuQ5MCr7Yqi'
WHERE password = '';
```

#### Option C: Recreate Database
If you want to start fresh, you can drop and recreate the database using the updated schema:
```bash
mysql -u root -p < car_wash_db.sql
```

## Authentication Endpoints

### 1. Register New User
**POST** `/api/auth/register`

Creates a new user account with hashed password and returns a JWT token.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "password": "SecurePass123",
  "role": "customer"
}
```

**Validation Rules:**
- `name`: 2-100 characters, no whitespace only
- `email`: Valid email format
- `phone`: At least 10 digits
- `password`: Minimum 8 characters, must contain at least one letter and one number
- `role`: Must be "customer", "employee", or "manager" (default: "customer")

**Response:**
```json
{
  "id": 12,
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "role": "customer",
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer"
}
```

### 2. Login
**POST** `/api/auth/login`

Authenticates a user and returns a JWT token.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

**Response:**
```json
{
  "id": 12,
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "role": "customer",
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer"
}
```

### 3. Get Current User
**GET** `/api/auth/me`

Returns the current user's information based on the provided JWT token.

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
{
  "id": 12,
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "role": "customer",
  "created_at": "2026-01-12 10:30:00"
}
```

## Testing the Authentication

### Using cURL

**Register:**
```bash
curl -X POST http://localhost:8000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "phone": "1234567890",
    "password": "TestPass123",
    "role": "customer"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPass123"
  }'
```

**Get Current User:**
```bash
curl -X GET http://localhost:8000/api/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Default Test Accounts

After running the migration, all existing users have the default password:
- **Password:** `Password123`

Test accounts:
- Customer: `ahmad@example.com` / `Password123`
- Employee: `khaled@example.com` / `Password123`
- Manager: `fatima@example.com` / `Password123`

## Security Features

1. **Password Hashing**: All passwords are hashed using bcrypt with 12 rounds
2. **JWT Tokens**:
   - Algorithm: HS256
   - Expiration: 24 hours
   - Contains: user_id, email, role
3. **Input Validation**: Pydantic models validate all inputs
4. **Password Strength**: Enforced minimum requirements (8 chars, letters + numbers)
5. **Email Validation**: Proper email format checking
6. **Role-Based Access**: Three roles supported (customer, employee, manager)

## Environment Configuration

Make sure your `.env` file has the JWT secret configured:

```env
JWT_SECRET_KEY=your_jwt_secret_key_here_change_in_production
```

**Important:** Change the default JWT secret in production!

## API Documentation

Once the server is running, you can view the interactive API documentation at:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## Troubleshooting

### "Unknown column 'password'"
Run the migration script to add the password column to the users table.

### "Invalid or expired token"
The JWT token has expired (24 hours). Login again to get a new token.

### "Incorrect email or password"
Check that you're using the correct credentials. Remember that passwords are case-sensitive.

### Database Connection Errors
Verify your database credentials in the `.env` file match your MySQL setup.
