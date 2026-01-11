from pydantic import BaseModel
from typing import Optional


class ServiceResponse(BaseModel):
    id: int
    name: str
    description: Optional[str] = None
    price: float
    duration: int  # in minutes
    is_active: bool
