from fastapi import APIRouter
from database import get_db_connection

router = APIRouter(prefix="/api/teams", tags=["teams"])


@router.get("")
def get_teams():
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM teams")
    teams = cursor.fetchall()
    cursor.close()
    db.close()
    return teams
