from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel
from typing import Optional
import mysql.connector
from database import get_db_connection
from dependencies import CurrentUser, get_current_user

router = APIRouter(prefix="/api/wallet", tags=["wallet"])


class DepositRequest(BaseModel):
    amount: float
    description: Optional[str] = "Added funds to wallet"


@router.get("/balance")
def get_wallet_balance(current_user: CurrentUser = Depends(get_current_user)):
    """Get user's wallet balance (calculated from transactions)"""
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)

    try:
        # Calculate balance from transactions
        cursor.execute("""
            SELECT
                COALESCE(SUM(CASE WHEN type = 'credit' THEN amount ELSE 0 END), 0) as credits,
                COALESCE(SUM(CASE WHEN type = 'debit' THEN amount ELSE 0 END), 0) as debits
            FROM transactions
            WHERE user_id = %s
        """, (current_user.id,))

        result = cursor.fetchone()
        credits = float(result['credits']) if result['credits'] else 0.0
        debits = float(result['debits']) if result['debits'] else 0.0
        balance = credits - debits

        return {
            "user_id": current_user.id,
            "balance": balance,
            "total_credits": credits,
            "total_debits": debits
        }
    finally:
        cursor.close()
        db.close()


@router.get("/transactions")
def get_wallet_transactions(
    limit: int = 20,
    offset: int = 0,
    current_user: CurrentUser = Depends(get_current_user)
):
    """Get user's transaction history"""
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)

    try:
        cursor.execute("""
            SELECT t.*, b.status as booking_status, s.name as service_name
            FROM transactions t
            LEFT JOIN bookings b ON t.booking_id = b.id
            LEFT JOIN services s ON b.service_id = s.id
            WHERE t.user_id = %s
            ORDER BY t.created_at DESC
            LIMIT %s OFFSET %s
        """, (current_user.id, limit, offset))

        transactions = cursor.fetchall()

        # Convert Decimal to float for JSON serialization
        for t in transactions:
            t['amount'] = float(t['amount'])

        return transactions
    finally:
        cursor.close()
        db.close()


@router.post("/deposit")
def deposit_funds(
    deposit: DepositRequest,
    current_user: CurrentUser = Depends(get_current_user)
):
    """Add funds to user's wallet"""
    if deposit.amount <= 0:
        raise HTTPException(status_code=400, detail="Amount must be positive")

    if deposit.amount > 1000:
        raise HTTPException(status_code=400, detail="Maximum deposit amount is $1000")

    db = get_db_connection()
    cursor = db.cursor()

    try:
        cursor.execute("""
            INSERT INTO transactions (user_id, amount, type, description)
            VALUES (%s, %s, 'credit', %s)
        """, (current_user.id, deposit.amount, deposit.description))

        db.commit()
        transaction_id = cursor.lastrowid

        # Get new balance
        cursor.execute("""
            SELECT
                COALESCE(SUM(CASE WHEN type = 'credit' THEN amount ELSE 0 END), 0) -
                COALESCE(SUM(CASE WHEN type = 'debit' THEN amount ELSE 0 END), 0) as balance
            FROM transactions
            WHERE user_id = %s
        """, (current_user.id,))

        result = cursor.fetchone()
        new_balance = float(result[0]) if result[0] else 0.0

        return {
            "success": True,
            "transaction_id": transaction_id,
            "amount": deposit.amount,
            "new_balance": new_balance,
            "message": f"Successfully added ${deposit.amount:.2f} to your wallet"
        }
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Database error: {err}")
    finally:
        cursor.close()
        db.close()
