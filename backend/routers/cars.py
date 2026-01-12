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
@router.put("/{car_id}")
def edit_car(car_id: int, car: CarCreate):
    db = get_db_connection()
    cursor = db.cursor()
    try:
        cursor.execute(
            "UPDATE cars SET user_id = %s, model = %s, plate_number = %s, color = %s, year = %s WHERE id = %s",
            (car.user_id, car.model, car.plate_number, car.color, car.year, car_id),
        )
        db.commit()
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {err}")
    finally:
        cursor.close()
        db.close()
    return {"id": car_id, **car.model_dump()}
@router.get("/{car_id}")
def get_car(car_id: int):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM cars WHERE id = %s", (car_id,))
    car = cursor.fetchone()
    cursor.close()
    db.close()
    if not car:
        raise HTTPException(status_code=404, detail="Car not found")
    return car
@router.delete("/{car_id}")
def delete_car(car_id: int):
    db = get_db_connection()
    cursor = db.cursor()
    try:
        cursor.execute("DELETE FROM cars WHERE id = %s", (car_id,))
        db.commit()
        if cursor.rowcount == 0:
            raise HTTPException(status_code=404, detail="Car not found")
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {err}")
    finally:
        cursor.close()
        db.close()
    return {"detail": "Car deleted successfully"}
@router.get("/user/{user_id}")
def get_cars_by_user(user_id: int):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM cars WHERE user_id = %s", (user_id,))
    cars = cursor.fetchall()
    cursor.close()
    db.close()
    return cars
@router.post("/user/{user_id}")
def create_car_for_user(user_id: int, car: CarCreate):
    db = get_db_connection()
    cursor = db.cursor()
    try:
        cursor.execute(
            "INSERT INTO cars (user_id, model, plate_number, color, year) VALUES (%s, %s, %s, %s, %s)",
            (user_id, car.model, car.plate_number, car.color, car.year),
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
@router.put("/user/{user_id}/car/{car_id}")
def edit_car_for_user(user_id: int, car_id: int, car: CarCreate):
    db = get_db_connection()
    cursor = db.cursor()
    try:
        cursor.execute(
            "UPDATE cars SET model = %s, plate_number = %s, color = %s, year = %s WHERE id = %s AND user_id = %s",
            (car.model, car.plate_number, car.color, car.year, car_id, user_id),
        )
        db.commit()
        if cursor.rowcount == 0:
            raise HTTPException(status_code=404, detail="Car not found for the specified user")
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {err}")
    finally:
        cursor.close()
        db.close()
    return {"id": car_id, **car.model_dump()}
@router.delete("/user/{user_id}/car/{car_id}")
def delete_car_for_user(user_id: int, car_id: int):
    db = get_db_connection()
    cursor = db.cursor()
    try:
        cursor.execute("DELETE FROM cars WHERE id = %s AND user_id = %s", (car_id, user_id))
        db.commit()
        if cursor.rowcount == 0:
            raise HTTPException(status_code=404, detail="Car not found for the specified user")
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {err}")
    finally:
        cursor.close()
        db.close()
    return {"detail": "Car deleted successfully"}

