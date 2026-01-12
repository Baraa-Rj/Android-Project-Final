"""
Authentication and Authorization Dependencies for RBAC
"""
from typing import Optional
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from auth import decode_access_token
from database import get_db_connection

security = HTTPBearer()


class CurrentUser:
    """Current authenticated user"""
    def __init__(self, id: int, email: str, role: str, name: str):
        self.id = id
        self.email = email
        self.role = role
        self.name = name


def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(security)
) -> CurrentUser:
    """
    Dependency to get the current authenticated user from JWT token
    """
    token = credentials.credentials

    # Decode the token
    payload = decode_access_token(token)
    if not payload:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token",
            headers={"WWW-Authenticate": "Bearer"},
        )

    user_id = payload.get("sub")
    email = payload.get("email")
    role = payload.get("role")

    if not user_id or not email or not role:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token payload",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # Verify user still exists in database
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    try:
        cursor.execute(
            "SELECT id, name, email, role FROM users WHERE id = %s",
            (user_id,)
        )
        user = cursor.fetchone()

        if not user:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="User not found",
            )

        return CurrentUser(
            id=user["id"],
            email=user["email"],
            role=user["role"],
            name=user["name"]
        )
    finally:
        cursor.close()
        db.close()


def require_role(*allowed_roles: str):
    """
    Dependency factory to check if user has one of the allowed roles

    Usage:
        @router.get("/admin-only", dependencies=[Depends(require_role("manager"))])

    Args:
        *allowed_roles: Variable number of role names that are allowed
    """
    def role_checker(current_user: CurrentUser = Depends(get_current_user)) -> CurrentUser:
        if current_user.role not in allowed_roles:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"Access denied. Required roles: {', '.join(allowed_roles)}",
            )
        return current_user

    return role_checker


# Convenient role dependencies
def get_manager(current_user: CurrentUser = Depends(get_current_user)) -> CurrentUser:
    """Require manager role"""
    if current_user.role != "manager":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Manager access required",
        )
    return current_user


def get_employee_or_manager(current_user: CurrentUser = Depends(get_current_user)) -> CurrentUser:
    """Require employee or manager role"""
    if current_user.role not in ["employee", "manager"]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Employee or manager access required",
        )
    return current_user


def get_authenticated_user(current_user: CurrentUser = Depends(get_current_user)) -> CurrentUser:
    """Just require any authenticated user (any role)"""
    return current_user


def verify_user_access(
    user_id: int,
    current_user: CurrentUser = Depends(get_current_user)
) -> CurrentUser:
    """
    Verify that the current user can access data for the given user_id
    - Customers can only access their own data
    - Employees and managers can access all data
    """
    if current_user.role == "customer" and current_user.id != user_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access denied. You can only access your own data",
        )
    return current_user
