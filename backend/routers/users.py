from fastapi import APIRouter, HTTPException
import mysql.connector
from models.UserCreate import UserCreate
from database import get_db_connection

router = APIRouter(prefix="/api/users", tags=["users"])


@router.get("")
def get_users():
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT id, name, email, phone, role FROM users")
    users = cursor.fetchall()
    cursor.close()
    db.close()
    return users


@router.post("")
def create_user(user: UserCreate):
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
def get_user(user_id: int):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM users WHERE id = %s", (user_id,))
    user = cursor.fetchone()
    cursor.close()
    db.close()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


@router.put("/{user_id}")
def edit_user(user_id: int, user: UserCreate):
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
def delete_user(user_id: int):
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
