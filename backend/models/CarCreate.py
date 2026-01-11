from pydantic import BaseModel
from typing import Optional


class CarCreate(BaseModel):
    user_id: int
    model: str
    plate_number: str
    color: str
    year: Optional[int] = None
