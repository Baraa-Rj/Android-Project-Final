from fastapi import APIRouter, HTTPException, Depends
import mysql.connector
from models.UserCreate import UserCreate
from database import get_db_connection
from dependencies import (
    CurrentUser,
    get_employee_or_manager,
    get_manager,
    get_current_user,
)

router = APIRouter(prefix="/api/users", tags=["users"])


@router.get("")
def get_users(current_user: CurrentUser = Depends(get_employee_or_manager)):
    """Get all users - Requires employee or manager role"""
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT id, name, email, phone, role FROM users")
    users = cursor.fetchall()
    cursor.close()
    db.close()
    return users


@router.post("")
def create_user(
    user: UserCreate,
    current_user: CurrentUser = Depends(get_manager)
):
    """Create a new user - Requires manager role"""
    db = get_db_connection()
    cursor = db.cursor()
    try:
        cursor.execute(
            "INSERT INTO users (name, email, phone, role) VALUES (%s, %s, %s, %s)",
            (user.name, user.email, user.phone, user.role),
        )
        db.commit()
        user_id = cursor.lastrowid
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {err}")
    finally:
        cursor.close()
        db.close()
    return {"id": user_id, **user.model_dump()}


@router.get("/{user_id}")
def get_user(
    user_id: int,
    current_user: CurrentUser = Depends(get_current_user)
):
    """
    Get user by ID
    - Customers: Can only view their own profile
    - Employees/Managers: Can view any user
    """
    # Check access: customers can only view their own data
    if current_user.role == "customer" and current_user.id != user_id:
        raise HTTPException(
            status_code=403,
            detail="Access denied. You can only view your own profile"
        )

    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT id, name, email, phone, role FROM users WHERE id = %s", (user_id,))
    user = cursor.fetchone()
    cursor.close()
    db.close()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


@router.put("/{user_id}")
def edit_user(
    user_id: int,
    user: UserCreate,
    current_user: CurrentUser = Depends(get_current_user)
):
    """
    Update user by ID
    - Customers/Employees: Can only update their own profile
    - Managers: Can update any user
    """
    # Check access: customers and employees can only edit their own data
    if current_user.role != "manager" and current_user.id != user_id:
        raise HTTPException(
            status_code=403,
            detail="Access denied. You can only edit your own profile"
        )

    db = get_db_connection()
    cursor = db.cursor()
    try:
        cursor.execute(
            "UPDATE users SET name = %s, email = %s, phone = %s, role = %s WHERE id = %s",
            (user.name, user.email, user.phone, user.role, user_id),
        )
        db.commit()
        if cursor.rowcount == 0:
            raise HTTPException(status_code=404, detail="User not found")
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {err}")
    finally:
        cursor.close()
        db.close()
    return {"id": user_id, **user.model_dump()}


@router.delete("/{user_id}")
def delete_user(
    user_id: int,
    current_user: CurrentUser = Depends(get_manager)
):
    """Delete user by ID - Requires manager role"""
    db = get_db_connection()
    cursor = db.cursor()
    try:
        cursor.execute("DELETE FROM users WHERE id = %s", (user_id,))
        db.commit()
        if cursor.rowcount == 0:
            raise HTTPException(status_code=404, detail="User not found")
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {err}")
    finally:
        cursor.close()
        db.close()
    return {"detail": "User deleted successfully"}
