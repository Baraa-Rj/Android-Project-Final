from fastapi import APIRouter, Depends, HTTPException, status
from database import get_db_connection
from dependencies import (
    CurrentUser,
    get_current_user,
    get_employee_or_manager,
    get_manager
)

router = APIRouter(prefix="/api/teams", tags=["teams"])


@router.get("/my-team")
def get_my_team(current_user: CurrentUser = Depends(get_current_user)):
    """Get current user's team and team members"""
    db = get_db_connection()
    cursor = db.cursor(dictionary=True, buffered=True)

    # Get employee's team
    cursor.execute(
        "SELECT team_id FROM team_members WHERE user_id = %s",
        (current_user.id,)
    )
    team_result = cursor.fetchone()

    if not team_result or not team_result['team_id']:
        cursor.close()
        db.close()
        return {"team": None, "members": []}

    team_id = team_result['team_id']

    # Get team details
    cursor.execute("SELECT * FROM teams WHERE id = %s", (team_id,))
    team = cursor.fetchone()

    # Get team members
    cursor.execute(
        """SELECT users.id, users.name, users.email, users.phone, users.role
           FROM users
           JOIN team_members ON users.id = team_members.user_id
           WHERE team_members.team_id = %s""",
        (team_id,)
    )
    members = cursor.fetchall()

    cursor.close()
    db.close()

    return {"team": team, "members": members}


@router.get("")
def get_teams(current_user: CurrentUser = Depends(get_current_user)):
    """Get all teams - Available to all authenticated users"""
    db = get_db_connection()
    cursor = db.cursor(dictionary=True, buffered=True)
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
    cursor = db.cursor(dictionary=True, buffered=True)
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
    cursor = db.cursor(dictionary=True, buffered=True)
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
    cursor = db.cursor(dictionary=True, buffered=True)

    try:
        user_id = member["user_id"]

        # Check if user exists and is an employee
        cursor.execute(
            "SELECT id, role, name FROM users WHERE id = %s",
            (user_id,)
        )
        user = cursor.fetchone()

        if not user:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="User not found"
            )

        if user["role"] != "employee":
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Only employees can be added to teams. {user['name']} is a {user['role']}."
            )

        # Check if employee is already in a team
        cursor.execute(
            "SELECT team_id FROM team_members WHERE user_id = %s",
            (user_id,)
        )
        existing_team = cursor.fetchone()

        if existing_team:
            # Get the team name for better error message
            cursor.execute(
                "SELECT name FROM teams WHERE id = %s",
                (existing_team["team_id"],)
            )
            team_info = cursor.fetchone()
            team_name = team_info["name"] if team_info else f"Team #{existing_team['team_id']}"

            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"{user['name']} is already assigned to {team_name}. Remove them from that team first."
            )

        # Add member to team
        cursor.execute(
            "INSERT INTO team_members (team_id, user_id) VALUES (%s, %s)",
            (team_id, user_id),
        )
        db.commit()

        return {"detail": "Member added successfully"}

    finally:
        cursor.close()
        db.close()


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
