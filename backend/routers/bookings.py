from fastapi import APIRouter, HTTPException, Depends
import mysql.connector
from models.BookingCreate import BookingCreate
from database import get_db_connection
from dependencies import CurrentUser, get_current_user, get_employee_or_manager

router = APIRouter(prefix="/api/bookings", tags=["bookings"])


def check_booking_ownership(booking_id: int, current_user: CurrentUser):
    """
    Check if customer owns the booking
    - Customers: must own the booking
    - Employees/Managers: can access any booking
    """
    if current_user.role == "customer":
        db = get_db_connection()
        cursor = db.cursor(dictionary=True)
        try:
            cursor.execute("SELECT user_id FROM bookings WHERE id = %s", (booking_id,))
            booking = cursor.fetchone()
            if not booking:
                raise HTTPException(status_code=404, detail="Booking not found")
            if booking["user_id"] != current_user.id:  # type: ignore[index]
                raise HTTPException(
                    status_code=403,
                    detail="Access denied. You can only access your own bookings",
                )
        finally:
            cursor.close()
            db.close()


@router.get("")
def get_bookings(current_user: CurrentUser = Depends(get_current_user)):
    """
    Get all bookings
    - Customers: Only see their own bookings
    - Employees: See bookings assigned to their team
    - Managers: See all bookings
    """
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)

    if current_user.role == "customer":
        # Customers can only see their own bookings
        cursor.execute(
            "SELECT * FROM bookings WHERE user_id = %s ORDER BY scheduled_time DESC",
            (current_user.id,),
        )
    elif current_user.role == "employee":
        # Employees see only bookings assigned to their team
        cursor.execute(
            "SELECT team_id FROM team_members WHERE user_id = %s", (current_user.id,)
        )
        team_result = cursor.fetchone()

        if team_result and team_result["team_id"]:
            cursor.execute(
                "SELECT * FROM bookings WHERE team_id = %s ORDER BY scheduled_time DESC",
                (team_result["team_id"],),
            )
        else:
            # Employee not assigned to any team - return empty list
            cursor.execute("SELECT * FROM bookings WHERE 1=0")
    else:
        # Managers see all bookings
        cursor.execute("SELECT * FROM bookings ORDER BY scheduled_time DESC")

    bookings = cursor.fetchall()
    cursor.close()
    db.close()
    return bookings


@router.get("/user/{user_id}")
def get_user_bookings(
    user_id: int, current_user: CurrentUser = Depends(get_current_user)
):
    """Get bookings for a user - Customers can only access their own bookings"""
    # Customers can only access their own bookings
    if current_user.role == "customer" and current_user.id != user_id:
        raise HTTPException(
            status_code=403,
            detail="Access denied. You can only access your own bookings",
        )

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
def create_booking(
    booking: BookingCreate, current_user: CurrentUser = Depends(get_current_user)
):
    """
    Create a new booking
    - Customers: Can only create bookings for themselves
    - Employees/Managers: Can create bookings for any user
    """
    # Customers can only create bookings for themselves
    if current_user.role == "customer" and booking.user_id != current_user.id:
        raise HTTPException(
            status_code=403,
            detail="Access denied. You can only create bookings for yourself",
        )

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
def get_booking(booking_id: int, current_user: CurrentUser = Depends(get_current_user)):
    """Get booking by ID - Customers can only access their own bookings"""
    check_booking_ownership(booking_id, current_user)

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
def edit_booking(
    booking_id: int,
    booking: BookingCreate,
    current_user: CurrentUser = Depends(get_current_user),
):
    """Update booking by ID - Customers can only update their own bookings"""
    check_booking_ownership(booking_id, current_user)

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
def delete_booking(
    booking_id: int, current_user: CurrentUser = Depends(get_current_user)
):
    """Delete booking by ID - Customers can only delete their own bookings"""
    check_booking_ownership(booking_id, current_user)

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
def get_bookings_by_service(
    service_id: int, current_user: CurrentUser = Depends(get_employee_or_manager)
):
    """Get bookings by service - Requires employee or manager role"""
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
def get_bookings_by_team(
    team_id: int, current_user: CurrentUser = Depends(get_employee_or_manager)
):
    """Get bookings by team - Requires employee or manager role"""
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
def get_bookings_by_vehicle(
    vehicle_id: int, current_user: CurrentUser = Depends(get_employee_or_manager)
):
    """Get bookings by vehicle - Requires employee or manager role"""
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
def get_bookings_by_car(
    car_id: int, current_user: CurrentUser = Depends(get_current_user)
):
    """
    Get bookings by car
    - Customers: Can only access bookings for their own cars
    - Employees/Managers: Can access all car bookings
    """
    # For customers, verify car ownership
    if current_user.role == "customer":
        db = get_db_connection()
        cursor = db.cursor(dictionary=True)
        try:
            cursor.execute("SELECT user_id FROM cars WHERE id = %s", (car_id,))
            car = cursor.fetchone()
            if not car:
                raise HTTPException(status_code=404, detail="Car not found")
            if car["user_id"] != current_user.id:  # type: ignore[index]
                raise HTTPException(
                    status_code=403,
                    detail="Access denied. You can only access bookings for your own cars",
                )
        finally:
            cursor.close()
            db.close()

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
def get_bookings_by_scheduled_time(
    scheduled_time: str, current_user: CurrentUser = Depends(get_employee_or_manager)
):
    """Get bookings by scheduled time - Requires employee or manager role"""
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
def get_bookings_by_location(
    location: str, current_user: CurrentUser = Depends(get_employee_or_manager)
):
    """Get bookings by location - Requires employee or manager role"""
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
def get_bookings_by_total_price(
    total_price: float, current_user: CurrentUser = Depends(get_employee_or_manager)
):
    """Get bookings by total price - Requires employee or manager role"""
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
def get_bookings_by_notes(
    notes: str, current_user: CurrentUser = Depends(get_employee_or_manager)
):
    """Get bookings by notes - Requires employee or manager role"""
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
def get_bookings_by_field(
    field: str, value: str, current_user: CurrentUser = Depends(get_employee_or_manager)
):
    """Get bookings by field - Requires employee or manager role"""
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


@router.patch("/{booking_id}/notes")
def update_booking_notes(
    booking_id: int, notes: str, current_user: CurrentUser = Depends(get_current_user)
):
    """Update only the notes of a booking - Customers can only update their own bookings"""
    check_booking_ownership(booking_id, current_user)

    db = get_db_connection()
    cursor = db.cursor()
    try:
        cursor.execute(
            "UPDATE bookings SET notes = %s WHERE id = %s",
            (notes, booking_id),
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
    return {"id": booking_id, "notes": notes}


@router.patch("/{booking_id}/status")
def update_booking_status(
    booking_id: int,
    status: str,
    current_user: CurrentUser = Depends(get_employee_or_manager),
):
    """Update only the status of a booking - Requires employee or manager role"""
    db = get_db_connection()
    cursor = db.cursor()
    try:
        cursor.execute(
            "UPDATE bookings SET status = %s WHERE id = %s",
            (status, booking_id),
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
    return {"id": booking_id, "status": status}


@router.patch("/{booking_id}/team")
def assign_team_to_booking(
    booking_id: int,
    team_id: int,
    current_user: CurrentUser = Depends(get_manager),
):
    """Assign a team to a booking - Requires manager role"""
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    try:
        cursor.execute(
            "UPDATE bookings SET team_id = %s WHERE id = %s",
            (team_id, booking_id),
        )
        db.commit()
        if cursor.rowcount == 0:
            raise HTTPException(status_code=404, detail="Booking not found")

        # Get the updated booking
        cursor.execute("SELECT * FROM bookings WHERE id = %s", (booking_id,))
        booking = cursor.fetchone()
        return booking
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {err}")
    finally:
        cursor.close()
        db.close()
