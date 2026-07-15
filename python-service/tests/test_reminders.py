from datetime import datetime, timedelta

from fastapi.testclient import TestClient

from main import app

client = TestClient(app)


def test_reminder_plan_happy_path_email_channel():
    """Covers the default (email) channel only.

    NOTE: this deliberately does not cover the "sms"/"both" channel branches,
    the past-appointment error branch, or the singular "1 hour" formatting
    branch in services.build_reminder_plan / _format_offset. Left thin on
    purpose to demonstrate a coverage-threshold gate in the CI pipeline.
    """
    appointment_time = datetime.now() + timedelta(days=2)
    response = client.post(
        "/appointments/reminder-plan",
        json={
            "appointment_time": appointment_time.isoformat(),
            "pet_name": "Rex",
            "channel": "email",
        },
    )
    assert response.status_code == 200
    reminders = response.json()["reminders"]
    assert len(reminders) == 2
    assert all(r["channel"] == "email" for r in reminders)
    assert "Rex" in reminders[0]["message"]
