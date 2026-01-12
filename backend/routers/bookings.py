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


@router.get("/{booking_id}")
def get_booking(booking_id: int):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM bookings WHERE id = %s", (booking_id,))
    booking = cursor.fetchone()
    cursor.close()
    db.close()
    if not booking:
        raise HTTPException(status_code=404, detail="Booking not found")
    return booking


@router.put("/{booking_id}")
def edit_booking(booking_id: int, booking: BookingCreate):
    db = get_db_connection()
    cursor = db.cursor()
    try:
        cursor.execute(
            """UPDATE bookings SET user_id = %s, car_id = %s, service_id = %s, team_id = %s, vehicle_id = %s, 
            location = %s, location_lat = %s, location_lng = %s, scheduled_time = %s, total_price = %s, notes = %s 
            WHERE id = %s""",
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
                booking_id,
            ),
        )
        db.commit()
        if cursor.rowcount == 0:
            raise HTTPException(status_code=404, detail="Booking not found")
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {err}")
    finally:
        cursor.close()
        db.close()
    return {"id": booking_id, **booking.model_dump()}


@router.delete("/{booking_id}")
def delete_booking(booking_id: int):
    db = get_db_connection()
    cursor = db.cursor()
    try:
        cursor.execute("DELETE FROM bookings WHERE id = %s", (booking_id,))
        db.commit()
        if cursor.rowcount == 0:
            raise HTTPException(status_code=404, detail="Booking not found")
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {err}")
    finally:
        cursor.close()
        db.close()
    return {"detail": "Booking deleted successfully"}


@router.get("/service/{service_id}")
def get_bookings_by_service(service_id: int):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute(
        "SELECT * FROM bookings WHERE service_id = %s ORDER BY scheduled_time DESC",
        (service_id,),
    )
    bookings = cursor.fetchall()
    cursor.close()
    db.close()
    return bookings


@router.get("/team/{team_id}")
def get_bookings_by_team(team_id: int):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute(
        "SELECT * FROM bookings WHERE team_id = %s ORDER BY scheduled_time DESC",
        (team_id,),
    )
    bookings = cursor.fetchall()
    cursor.close()
    db.close()
    return bookings


@router.get("/vehicle/{vehicle_id}")
def get_bookings_by_vehicle(vehicle_id: int):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute(
        "SELECT * FROM bookings WHERE vehicle_id = %s ORDER BY scheduled_time DESC",
        (vehicle_id,),
    )
    bookings = cursor.fetchall()
    cursor.close()
    db.close()
    return bookings


@router.get("/car/{car_id}")
def get_bookings_by_car(car_id: int):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute(
        "SELECT * FROM bookings WHERE car_id = %s ORDER BY scheduled_time DESC",
        (car_id,),
    )
    bookings = cursor.fetchall()
    cursor.close()
    db.close()
    return bookings


@router.get("/scheduled/{scheduled_time}")
def get_bookings_by_scheduled_time(scheduled_time: str):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute(
        "SELECT * FROM bookings WHERE scheduled_time = %s ORDER BY scheduled_time DESC",
        (scheduled_time,),
    )
    bookings = cursor.fetchall()
    cursor.close()
    db.close()
    return bookings


@router.get("/location/{location}")
def get_bookings_by_location(location: str):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute(
        "SELECT * FROM bookings WHERE location = %s ORDER BY scheduled_time DESC",
        (location,),
    )
    bookings = cursor.fetchall()
    cursor.close()
    db.close()
    return bookings


@router.get("/total_price/{total_price}")
def get_bookings_by_total_price(total_price: float):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute(
        "SELECT * FROM bookings WHERE total_price = %s ORDER BY scheduled_time DESC",
        (total_price,),
    )
    bookings = cursor.fetchall()
    cursor.close()
    db.close()
    return bookings


@router.get("/notes/{notes}")
def get_bookings_by_notes(notes: str):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute(
        "SELECT * FROM bookings WHERE notes = %s ORDER BY scheduled_time DESC",
        (notes,),
    )
    bookings = cursor.fetchall()
    cursor.close()
    db.close()
    return bookings


@router.get("/all/{field}/{value}")
def get_bookings_by_field(field: str, value: str):
    valid_fields = {
        "user_id",
        "car_id",
        "service_id",
        "team_id",
        "vehicle_id",
        "location",
        "scheduled_time",
        "total_price",
        "notes",
    }
    if field not in valid_fields:
        raise HTTPException(status_code=400, detail="Invalid field parameter")

    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    query = f"SELECT * FROM bookings WHERE {field} = %s ORDER BY scheduled_time DESC"
    cursor.execute(query, (value,))
    bookings = cursor.fetchall()
    cursor.close()
    db.close()
    return bookings
