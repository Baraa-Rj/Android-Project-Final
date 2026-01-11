from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class BookingCreate(BaseModel):
    user_id: int
    car_id: int
    service_id: int
    team_id: Optional[int] = None
    vehicle_id: Optional[int] = None
    location: str
    location_lat: Optional[float] = None
    location_lng: Optional[float] = None
    scheduled_time: datetime
    total_price: float
    notes: Optional[str] = None
