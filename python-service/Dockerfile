# ---- build stage ----
FROM python:3.12-slim AS builder
WORKDIR /build

COPY requirements.txt .
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

COPY main.py models.py services.py ./

# ---- runtime stage ----
FROM python:3.12-slim

RUN groupadd --system appuser && useradd --system --gid appuser appuser

WORKDIR /app
COPY --from=builder /install /usr/local
COPY --from=builder /build/*.py ./
RUN chown -R appuser:appuser /app

ENV PYTHONUNBUFFERED=1

USER appuser

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8000/health')" || exit 1

ENTRYPOINT ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
