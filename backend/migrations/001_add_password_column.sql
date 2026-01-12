-- Migration: Add password column to users table
-- Date: 2026-01-12

USE car_wash_db;

-- Add password column to users table
ALTER TABLE users
ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '' AFTER phone;

-- Update existing users with a default hashed password (should be changed by users)
-- This is the bcrypt hash for "ChangeMe123" - users should change their password
UPDATE users
SET password = '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lSuQ5MCr7Yqi'
WHERE password = '';

-- Verify the changes
SELECT id, name, email, phone, role,
       CASE WHEN password != '' THEN 'Password Set' ELSE 'No Password' END as password_status
FROM users;
