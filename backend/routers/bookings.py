from fastapi import APIRouter, HTTPException
import mysql.connector
from models.BookingCreate import BookingCreate
from database import get_db_connection

router = APIRouter(prefix="/api/bookings", tags=["bookings"])


@router.get("")
def get_bookings():
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM bookings ORDER BY scheduled_time DESC")
    bookings = cursor.fetchall()
    cursor.close()
    db.close()
    return bookings


@router.get("/user/{user_id}")
def get_user_bookings(user_id: int):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute(
        "SELECT * FROM bookings WHERE user_id = %s ORDER BY scheduled_time DESC",
        (user_id,),
    )
    bookings = cursor.fetchall()
    cursor.close()
    db.close()
    return bookings


@router.post("")
def create_booking(booking: BookingCreate):
    db = get_db_connection()
    cursor = db.cursor()
    try:
        cursor.execute(
            """INSERT INTO bookings (user_id, car_id, service_id, team_id, vehicle_id, 
            location, location_lat, location_lng, scheduled_time, total_price, notes) 
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)""",
            (
                booking.user_id,
                booking.car_id,
                booking.service_id,
                booking.team_id,
                booking.vehicle_id,
                booking.location,
                booking.location_lat,
                booking.location_lng,
                booking.scheduled_time,
                booking.total_price,
                booking.notes,
            ),
        )
        db.commit()
        booking_id = cursor.lastrowid
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {err}")
    finally:
        cursor.close()
        db.close()
    return {"id": booking_id, **booking.model_dump()}
