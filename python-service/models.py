from datetime import datetime
from typing import Literal, Optional

from pydantic import BaseModel


class AvailabilityRequest(BaseModel):
    appointment_time: datetime


class AvailabilityResponse(BaseModel):
    available: bool
    reason: Optional[str] = None


class ReminderPlanRequest(BaseModel):
    appointment_time: datetime
    pet_name: str
    channel: Literal["email", "sms", "both"] = "email"


class ReminderEvent(BaseModel):
    send_at: datetime
    channel: str
    message: str


class ReminderPlanResponse(BaseModel):
    reminders: list[ReminderEvent]
