from datetime import datetime, timedelta

from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def _next_weekday_at(weekday: int, hour: int) -> datetime:
    """Return the next occurrence of `weekday` (Mon=0..Sun=6) at `hour`:00, always in the future."""
    now = datetime.now()
    days_ahead = (weekday - now.weekday()) % 7
    if days_ahead == 0:
        days_ahead = 7
    return (now + timedelta(days=days_ahead)).replace(hour=hour, minute=0, second=0, microsecond=0)


def test_available_slot_on_a_weekday_during_business_hours():
    appointment_time = _next_weekday_at(weekday=1, hour=10)  # Tuesday 10:00
    response = client.post(
        "/appointments/availability",
        json={"appointment_time": appointment_time.isoformat()},
    )
    assert response.status_code == 200
    assert response.json() == {"available": True, "reason": None}


def test_unavailable_on_weekend():
    appointment_time = _next_weekday_at(weekday=5, hour=10)  # Saturday 10:00
    response = client.post(
        "/appointments/availability",
        json={"appointment_time": appointment_time.isoformat()},
    )
    body = response.json()
    assert body["available"] is False
    assert body["reason"] == "Clinic is closed on weekends"


def test_unavailable_outside_business_hours():
    appointment_time = _next_weekday_at(weekday=2, hour=20)  # Wednesday 20:00
    response = client.post(
        "/appointments/availability",
        json={"appointment_time": appointment_time.isoformat()},
    )
    body = response.json()
    assert body["available"] is False
    assert body["reason"] == "Appointment time is outside clinic hours (09:00-17:00)"


def test_unavailable_in_the_past():
    appointment_time = datetime.now() - timedelta(days=1)
    response = client.post(
        "/appointments/availability",
        json={"appointment_time": appointment_time.isoformat()},
    )
    body = response.json()
    assert body["available"] is False
    assert body["reason"] == "Appointment time is in the past"
