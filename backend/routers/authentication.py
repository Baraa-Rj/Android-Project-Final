from typing import Any, Dict, cast
from fastapi import APIRouter, HTTPException, status, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import mysql.connector
from models.UserLogin import UserLogin
from models.UserRegister import UserRegister
from database import get_db_connection

from auth import (
    verify_password,
    get_password_hash,
    create_access_token,
    decode_access_token,
)

security = HTTPBearer()

router = APIRouter(prefix="/api/auth", tags=["authentication"])


@router.post("/register")
def register(user: UserRegister):
    """Register a new user"""
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)

    try:
        # Check if user already exists
        cursor.execute("SELECT id FROM users WHERE email = %s", (user.email,))
        existing_user = cursor.fetchone()

        if existing_user:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Email already registered",
            )

        # Hash the password
        hashed_password = get_password_hash(user.password)

        # Insert new user
        cursor.execute(
            "INSERT INTO users (name, email, phone, password, role) VALUES (%s, %s, %s, %s, %s)",
            (user.name, user.email, user.phone, hashed_password, user.role),
        )
        db.commit()
        user_id = cursor.lastrowid

        # Create access token
        access_token = create_access_token(
            data={"sub": str(user_id), "email": user.email, "role": user.role}
        )

        return {
            "id": user_id,
            "name": user.name,
            "email": user.email,
            "phone": user.phone,
            "role": user.role,
            "access_token": access_token,
            "token_type": "bearer",
        }

    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Database error: {err}",
        )
    finally:
        cursor.close()
        db.close()


@router.post("/login")
def login(user: UserLogin):
    """Login with email and password"""
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)

    try:
        # Find user by email
        cursor.execute("SELECT * FROM users WHERE email = %s", (user.email,))
        db_user = cursor.fetchone()

        if not db_user:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Incorrect email or password",
            )

        # Cast to Dict for type checking (db_user is guaranteed to be a dict here)
        db_user = cast(Dict[str, Any], db_user)

        # Verify password
        if not verify_password(user.password, db_user["password"]):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Incorrect email or password",
            )

        # Create access token
        access_token = create_access_token(
            data={
                "sub": str(db_user["id"]),
                "email": db_user["email"],
                "role": db_user["role"],
            }
        )

        return {
            "id": db_user["id"],
            "name": db_user["name"],
            "email": db_user["email"],
            "phone": db_user["phone"],
            "role": db_user["role"],
            "access_token": access_token,
            "token_type": "bearer",
        }

    finally:
        cursor.close()
        db.close()


@router.get("/me")
def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(security)):
    """Get current user from token"""
    token = credentials.credentials

    payload = decode_access_token(token)
    if not payload:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token",
            headers={"WWW-Authenticate": "Bearer"},
        )

    user_id = payload.get("sub")
    if not user_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token payload",
            headers={"WWW-Authenticate": "Bearer"},
        )

    db = get_db_connection()
    cursor = db.cursor(dictionary=True)

    try:
        cursor.execute(
            "SELECT id, name, email, phone, role, created_at FROM users WHERE id = %s", (user_id,)
        )
        user = cursor.fetchone()

        if not user:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="User not found"
            )

        return user

    finally:
        cursor.close()
        db.close()
