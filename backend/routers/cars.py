from fastapi import APIRouter, HTTPException, Depends
import mysql.connector
from models.CarCreate import CarCreate
from models.CarCreateWithoutUserId import CarCreateWithoutUserId
from database import get_db_connection
from dependencies import CurrentUser, get_current_user

router = APIRouter(prefix="/api/cars", tags=["cars"])


def check_car_ownership(car_id: int, current_user: CurrentUser):
    """
    Check if customer owns the car
    - Customers: must own the car
    - Employees/Managers: can access any car
    """
    if current_user.role == "customer":
        db = get_db_connection()
        cursor = db.cursor(dictionary=True)
        try:
            cursor.execute("SELECT user_id FROM cars WHERE id = %s", (car_id,))
            car = cursor.fetchone()
            if not car:
                raise HTTPException(status_code=404, detail="Car not found")
            if car["user_id"] != current_user.id:
                raise HTTPException(
                    status_code=403,
                    detail="Access denied. You can only access your own cars"
                )
        finally:
            cursor.close()
            db.close()


@router.get("")
def get_cars(current_user: CurrentUser = Depends(get_current_user)):
    """
    Get all cars
    - Customers: Only see their own cars
    - Employees/Managers: See all cars
    """
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)

    if current_user.role == "customer":
        # Customers can only see their own cars
        cursor.execute("SELECT * FROM cars WHERE user_id = %s", (current_user.id,))
    else:
        # Employees and managers see all cars
        cursor.execute("SELECT * FROM cars")

    cars = cursor.fetchall()
    cursor.close()
    db.close()
    return cars


@router.post("")
def create_car(
    car: CarCreate,
    current_user: CurrentUser = Depends(get_current_user)
):
    """
    Create a new car
    - Customers: Can only create cars for themselves
    - Employees/Managers: Can create cars for any user
    """
    # Customers can only create cars for themselves
    if current_user.role == "customer" and car.user_id != current_user.id:
        raise HTTPException(
            status_code=403,
            detail="Access denied. You can only create cars for yourself"
        )

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
@router.get("/{car_id}")
def get_car(
    car_id: int,
    current_user: CurrentUser = Depends(get_current_user)
):
    """Get car by ID - Customers can only access their own cars"""
    check_car_ownership(car_id, current_user)

    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM cars WHERE id = %s", (car_id,))
    car = cursor.fetchone()
    cursor.close()
    db.close()
    if not car:
        raise HTTPException(status_code=404, detail="Car not found")
    return car


@router.put("/{car_id}")
def edit_car(
    car_id: int,
    car: CarCreate,
    current_user: CurrentUser = Depends(get_current_user)
):
    """Update car by ID - Customers can only update their own cars"""
    check_car_ownership(car_id, current_user)

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


@router.delete("/{car_id}")
def delete_car(
    car_id: int,
    current_user: CurrentUser = Depends(get_current_user)
):
    """Delete car by ID - Customers can only delete their own cars"""
    check_car_ownership(car_id, current_user)

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
def get_cars_by_user(
    user_id: int,
    current_user: CurrentUser = Depends(get_current_user)
):
    """Get all cars for a user - Customers can only access their own cars"""
    # Customers can only access their own cars
    if current_user.role == "customer" and current_user.id != user_id:
        raise HTTPException(
            status_code=403,
            detail="Access denied. You can only access your own cars"
        )

    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM cars WHERE user_id = %s", (user_id,))
    cars = cursor.fetchall()
    cursor.close()
    db.close()
    return cars


@router.post("/user/{user_id}")
def create_car_for_user(
    user_id: int,
    car: CarCreateWithoutUserId,
    current_user: CurrentUser = Depends(get_current_user)
):
    """Create car for a user - Customers can only create for themselves"""
    # Customers can only create cars for themselves
    if current_user.role == "customer" and current_user.id != user_id:
        raise HTTPException(
            status_code=403,
            detail="Access denied. You can only create cars for yourself"
        )

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
    return {"id": car_id, "user_id": user_id, **car.model_dump()}


@router.put("/user/{user_id}/car/{car_id}")
def edit_car_for_user(
    user_id: int,
    car_id: int,
    car: CarCreateWithoutUserId,
    current_user: CurrentUser = Depends(get_current_user)
):
    """Update car for a user - Customers can only update their own cars"""
    # Customers can only update their own cars
    if current_user.role == "customer" and current_user.id != user_id:
        raise HTTPException(
            status_code=403,
            detail="Access denied. You can only update your own cars"
        )

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
    return {"id": car_id, "user_id": user_id, **car.model_dump()}


@router.delete("/user/{user_id}/car/{car_id}")
def delete_car_for_user(
    user_id: int,
    car_id: int,
    current_user: CurrentUser = Depends(get_current_user)
):
    """Delete car for a user - Customers can only delete their own cars"""
    # Customers can only delete their own cars
    if current_user.role == "customer" and current_user.id != user_id:
        raise HTTPException(
            status_code=403,
            detail="Access denied. You can only delete your own cars"
        )

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

