from fastapi import APIRouter
from database import get_db_connection

router = APIRouter(prefix="/api/services", tags=["services"])


@router.get("")
def get_services():
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM services WHERE is_active = TRUE")
    services = cursor.fetchall()
    cursor.close()
    db.close()
    return services


@router.post("")
def create_service(service: dict):
    db = get_db_connection()
    cursor = db.cursor()
    cursor.execute(
        "INSERT INTO services (name, description, price, duration, is_active) VALUES (%s, %s, %s, %s, %s)",
        (
            service["name"],
            service["description"],
            service["price"],
            service["duration"],
            service.get("is_active", True),
        ),
    )
    db.commit()
    service_id = cursor.lastrowid
    cursor.close()
    db.close()
    return {"id": service_id, **service}


@router.get("/{service_id}")
def get_service(service_id: int):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM services WHERE id = %s", (service_id,))
    service = cursor.fetchone()
    cursor.close()
    db.close()
    return service


@router.put("/{service_id}")
def edit_service(service_id: int, service: dict):
    db = get_db_connection()
    cursor = db.cursor()
    cursor.execute(
        "UPDATE services SET name = %s, description = %s, price = %s, duration = %s, is_active = %s WHERE id = %s",
        (
            service["name"],
            service["description"],
            service["price"],
            service["duration"],
            service.get("is_active", True),
            service_id,
        ),
    )
    db.commit()
    cursor.close()
    db.close()
    return {"id": service_id, **service}


@router.delete("/{service_id}")
def delete_service(service_id: int):
    db = get_db_connection()
    cursor = db.cursor()
    cursor.execute("DELETE FROM services WHERE id = %s", (service_id,))
    db.commit()
    cursor.close()
    db.close()
    return {"detail": "Service deleted successfully"}


@router.get("/active")
def get_active_services():
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM services WHERE is_active = TRUE")
    services = cursor.fetchall()
    cursor.close()
    db.close()
    return services


@router.post("/activate/{service_id}")
def activate_service(service_id: int):
    db = get_db_connection()
    cursor = db.cursor()
    cursor.execute(
        "UPDATE services SET is_active = TRUE WHERE id = %s",
        (service_id,),
    )
    db.commit()
    cursor.close()
    db.close()
    return {"detail": "Service activated successfully"}


@router.post("/deactivate/{service_id}")
def deactivate_service(service_id: int):
    db = get_db_connection()
    cursor = db.cursor()
    cursor.execute(
        "UPDATE services SET is_active = FALSE WHERE id = %s",
        (service_id,),
    )
    db.commit()
    cursor.close()
    db.close()
    return {"detail": "Service deactivated successfully"}
