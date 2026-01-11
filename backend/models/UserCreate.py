from pydantic import BaseModel
from typing import Optional


class UserCreate(BaseModel):
    name: str
    email: str
    phone: str
    role: str  # customer, employee, manager
