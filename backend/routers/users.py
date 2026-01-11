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
