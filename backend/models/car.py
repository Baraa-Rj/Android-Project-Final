
from pydantic import BaseModel

class CarCreate(BaseModel):
    user_id: int
    model: str
    plate_number: str
    color: str
    year: int = None