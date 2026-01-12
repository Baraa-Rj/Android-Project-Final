from pydantic import BaseModel
from typing import Optional


class CarCreateWithoutUserId(BaseModel):
    model: str
    plate_number: str
    color: str
    year: Optional[int] = None
