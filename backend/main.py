from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import mysql.connector
from pydantic import BaseModel
import os
from dotenv import load_dotenv

from backend.models.car import CarCreate
from backend.models.user import UserCreate

load_dotenv()
app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


def get_db_connection():
    return mysql.connector.connect(
        host=os.getenv("DB_HOST"),
        user=os.getenv("DB_USER"),
        password=os.getenv("DB_PASSWORD"),
        database=os.getenv("DB_NAME"),
    )


@app.get("/")
def root():
    return {"message": "API is running"}


@app.get("/api/services")
def get_services():
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM services WHERE active = TRUE")
    services = cursor.fetchall()
    cursor.close()
    db.close()
    return services

@app.get("/api/users")
def get_users():
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT id, username, email, phone, role FROM users")
    users = cursor.fetchall()
    cursor.close()
    db.close()
    return users
@app.get("/api/cars")
def get_cars():
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM cars")
    cars = cursor.fetchall()
    cursor.close()
    db.close()
    return cars

@app.post("/api/users")
def create_user(user: UserCreate):
    db = get_db_connection()
    cursor = db.cursor()
    try:
        cursor.execute(
            "INSERT INTO users (username, email, password, phone, role) VALUES (%s, %s, %s, %s, %s)",
            (user.username, user.email, user.password, user.phone, user.role),
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

@app.post("/api/cars")
def create_car(car: CarCreate):
    db = get_db_connection()
    cursor = db.cursor()
    try:
        cursor.execute(
            "INSERT INTO cars (user_id, model, plate_number, color, year) VALUES (%s, %s, %s, %s, %s)",
            (car.user_id, car.model, car.plate_number, car.color, car.year),
        )
        db.commit()
        car_id = cursor.lastrowid
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {err}")
    finally:
        cursor.close()
        db.close()
    return {"id": car_id, **car.model_dump()}  



def main():
    pass


if __name__ == "__main__":
    main()
