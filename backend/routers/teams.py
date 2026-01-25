from fastapi import APIRouter, Depends
from database import get_db_connection
from dependencies import (
    CurrentUser,
    get_current_user,
    get_employee_or_manager,
    get_manager
)

router = APIRouter(prefix="/api/teams", tags=["teams"])


@router.get("")
def get_teams(current_user: CurrentUser = Depends(get_current_user)):
    """Get all teams - Available to all authenticated users"""
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM teams")
    teams = cursor.fetchall()
    cursor.close()
    db.close()
    return teams


@router.get("/{team_id}")
def get_team(
    team_id: int,
    current_user: CurrentUser = Depends(get_current_user)
):
    """Get team by ID - Available to all authenticated users"""
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM teams WHERE id = %s", (team_id,))
    team = cursor.fetchone()
    cursor.close()
    db.close()
    return team


@router.get("/{team_id}/members")
def get_team_members(
    team_id: int,
    current_user: CurrentUser = Depends(get_current_user)
):
    """Get team members - Available to all authenticated users"""
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    cursor.execute(
        "SELECT users.id, users.name, users.email, users.phone, users.role FROM users JOIN team_members ON users.id = team_members.user_id WHERE team_members.team_id = %s",
        (team_id,),
    )
    members = cursor.fetchall()
    cursor.close()
    db.close()
    return members


@router.post("")
def create_team(
    team: dict,
    current_user: CurrentUser = Depends(get_manager)
):
    """Create a new team - Requires manager role"""
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


@router.put("/{team_id}")
def edit_team(
    team_id: int,
    team: dict,
    current_user: CurrentUser = Depends(get_manager)
):
    """Update team - Requires manager role"""
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
def delete_team(
    team_id: int,
    current_user: CurrentUser = Depends(get_manager)
):
    """Delete team - Requires manager role"""
    db = get_db_connection()
    cursor = db.cursor()
    cursor.execute("DELETE FROM teams WHERE id = %s", (team_id,))
    db.commit()
    cursor.close()
    db.close()
    return {"detail": "Team deleted successfully"}


@router.post("/{team_id}/members")
def add_team_member(
    team_id: int,
    member: dict,
    current_user: CurrentUser = Depends(get_manager)
):
    """Add member to team - Requires manager role"""
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
def remove_team_member(
    team_id: int,
    user_id: int,
    current_user: CurrentUser = Depends(get_manager)
):
    """Remove member from team - Requires manager role"""
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
