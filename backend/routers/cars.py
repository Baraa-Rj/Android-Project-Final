from fastapi import APIRouter, HTTPException
import mysql.connector
from models.CarCreate import CarCreate
from database import get_db_connection

router = APIRouter(prefix="/api/cars", tags=["cars"])


@router.get("")
def get_cars():
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM cars")
    cars = cursor.fetchall()
    cursor.close()
    db.close()
    return cars


@router.post("")
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
