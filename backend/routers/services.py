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
