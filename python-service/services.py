from datetime import datetime, time as dtime, timedelta
from typing import Optional

CLINIC_OPEN = dtime(9, 0)
CLINIC_CLOSE = dtime(17, 0)
REMINDER_OFFSETS = (timedelta(hours=24), timedelta(hours=2))


def check_availability(appointment_time: datetime) -> tuple[bool, Optional[str]]:
    if appointment_time <= datetime.now():
        return False, "Appointment time is in the past"
    if appointment_time.weekday() >= 5:
        return False, "Clinic is closed on weekends"
    if not (CLINIC_OPEN <= appointment_time.time() < CLINIC_CLOSE):
        return False, "Appointment time is outside clinic hours (09:00-17:00)"
    return True, None


def _format_offset(offset: timedelta) -> str:
    hours = int(offset.total_seconds() // 3600)
    unit = "hour" if hours == 1 else "hours"
    return f"{hours} {unit}"


def build_reminder_plan(appointment_time: datetime, pet_name: str, channel: str) -> list[dict]:
    if appointment_time <= datetime.now():
        raise ValueError("Cannot schedule reminders for a past appointment")

    channels = ("email", "sms") if channel == "both" else (channel,)
    reminders = []
    for offset in REMINDER_OFFSETS:
        send_at = appointment_time - offset
        label = _format_offset(offset)
        for ch in channels:
            reminders.append(
                {
                    "send_at": send_at,
                    "channel": ch,
                    "message": f"Reminder: {pet_name}'s appointment is in {label}.",
                }
            )
    return reminders
