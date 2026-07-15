from fastapi import FastAPI, HTTPException

from models import (
    AvailabilityRequest,
    AvailabilityResponse,
    ReminderPlanRequest,
    ReminderPlanResponse,
)
from services import build_reminder_plan, check_availability

app = FastAPI(
    title="PetClinic Appointment & Reminder Service",
    description="Stateless helper service for appointment availability checks and reminder scheduling.",
    version="1.0.0",
)


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/appointments/availability", response_model=AvailabilityResponse)
def appointment_availability(request: AvailabilityRequest):
    available, reason = check_availability(request.appointment_time)
    return AvailabilityResponse(available=available, reason=reason)


@app.post("/appointments/reminder-plan", response_model=ReminderPlanResponse)
def reminder_plan(request: ReminderPlanRequest):
    try:
        reminders = build_reminder_plan(request.appointment_time, request.pet_name, request.channel)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    return ReminderPlanResponse(reminders=reminders)
