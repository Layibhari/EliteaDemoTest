# PetClinic Appointment & Reminder Service

A small, self-contained [FastAPI](https://fastapi.tiangolo.com/) microservice that lives alongside
the Spring PetClinic Java app in this monorepo. It has no database and no dependency on the Java
app — it's a stateless helper for two thematically-related jobs:

- Checking whether a proposed appointment slot falls within clinic business hours.
- Building a reminder schedule (with messages) for an upcoming appointment.

## Endpoints

| Method | Path                            | Description                                                              |
|--------|----------------------------------|----------------------------------------------------------------------------|
| GET    | `/health`                        | Liveness check, returns `{"status": "ok"}`.                               |
| POST   | `/appointments/availability`     | Validates an appointment time against clinic hours (Mon-Fri, 09:00-17:00). |
| POST   | `/appointments/reminder-plan`    | Builds a list of reminder events (24h and 2h before) for an appointment.   |

### Example requests

```bash
curl -X POST http://localhost:8000/appointments/availability \
  -H "Content-Type: application/json" \
  -d '{"appointment_time": "2030-01-08T10:00:00"}'

curl -X POST http://localhost:8000/appointments/reminder-plan \
  -H "Content-Type: application/json" \
  -d '{"appointment_time": "2030-01-08T10:00:00", "pet_name": "Rex", "channel": "both"}'
```

## Running locally

```bash
cd python-service
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt

uvicorn main:app --reload
```

The service listens on <http://localhost:8000>. Interactive docs are available at
<http://localhost:8000/docs>.

## Running tests

```bash
cd python-service
pip install -r requirements.txt
pytest
```

### Coverage

`pytest-cov` isn't in `requirements.txt` (kept minimal on purpose), but it's handy for checking
coverage locally or wiring up a CI coverage gate:

```bash
pip install pytest-cov
pytest --cov=. --cov-report=term-missing
```

Note: `services.build_reminder_plan` (and its `_format_offset` helper) is only exercised via the
happy-path "email channel" test in `tests/test_reminders.py`. The `sms`/`both` channel branches,
the past-appointment error branch, and the singular "1 hour" formatting branch are intentionally
left uncovered — this is a deliberate gap for demonstrating a coverage-threshold gate in the CI
pipeline.

## Building the container

```bash
cd python-service
docker build -t petclinic-appointment-service .
docker run -p 8000:8000 petclinic-appointment-service
```
