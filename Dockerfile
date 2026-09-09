# Tumia Python rasmi ya kisasa
FROM python:3.12-slim

# Weka mazingira ya kuzuia Python isitunze cache kwenye kumbukumbu
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Weka eneo la kazi ndani ya kontena (container)
WORKDIR /app

# Sakinisha vifaa muhimu vya mfumo
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# Nakili na usakinishe mahitaji ya mradi
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Nakili faili zote za mradi ndani ya kontena
COPY . .

# Kusanya faili za static za Django
RUN python manage.py collectstatic --noinput

# Fungua mlango ambao Google Cloud itautumia kuwasiliana na tovuti
EXPOSE 8080

# Amri ya kuwakha seva kwa kutumia Gunicorn
CMD ["gunicorn", "--bind", "0.0.0.0:8080", "Njiafix.wsgi:application"]
