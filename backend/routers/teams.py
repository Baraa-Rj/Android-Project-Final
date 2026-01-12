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


@router.post("")
def create_team(team: dict):
    db = get_db_connection()
    cursor = db.cursor()
    cursor.execute(
        "INSERT INTO teams (name, description) VALUES (%s, %s)",
        (team["name"], team["description"]),
    )
    db.commit()
    team_id = cursor.lastrowid
    cursor.close()
    db.close()
    return {"id": team_id, **team}


@router.get("/{team_id}")
def get_team(team_id: int):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM teams WHERE id = %s", (team_id,))
    team = cursor.fetchone()
    cursor.close()
    db.close()
    return team


@router.put("/{team_id}")
def edit_team(team_id: int, team: dict):
    db = get_db_connection()
    cursor = db.cursor()
    cursor.execute(
        "UPDATE teams SET name = %s, description = %s WHERE id = %s",
        (team["name"], team["description"], team_id),
    )
    db.commit()
    cursor.close()
    db.close()
    return {"id": team_id, **team}


@router.delete("/{team_id}")
def delete_team(team_id: int):
    db = get_db_connection()
    cursor = db.cursor()
    cursor.execute("DELETE FROM teams WHERE id = %s", (team_id,))
    db.commit()
    cursor.close()
    db.close()
    return {"detail": "Team deleted successfully"}


@router.get("/{team_id}/members")
def get_team_members(team_id: int):
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute(
        "SELECT users.* FROM users JOIN team_members ON users.id = team_members.user_id WHERE team_members.team_id = %s",
        (team_id,),
    )
    members = cursor.fetchall()
    cursor.close()
    db.close()
    return members


@router.post("/{team_id}/members")
def add_team_member(team_id: int, member: dict):
    db = get_db_connection()
    cursor = db.cursor()
    cursor.execute(
        "INSERT INTO team_members (team_id, user_id) VALUES (%s, %s)",
        (team_id, member["user_id"]),
    )
    db.commit()
    cursor.close()
    db.close()
    return {"detail": "Member added successfully"}


@router.delete("/{team_id}/members/{user_id}")
def remove_team_member(team_id: int, user_id: int):
    db = get_db_connection()
    cursor = db.cursor()
    cursor.execute(
        "DELETE FROM team_members WHERE team_id = %s AND user_id = %s",
        (team_id, user_id),
    )
    db.commit()
    cursor.close()
    db.close()
    return {"detail": "Member removed successfully"}
