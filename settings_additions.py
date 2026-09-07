# Ongeza / badilisha haya kwenye settings.py yako uliyokuwa nayo tayari.
# USIBADILISHE muundo mzima wa settings.py -- haya ni MAREKEBISHO tu
# ya sehemu husika.

import os
import dj_database_url
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent

# --- SECURITY ---
SECRET_KEY = os.environ.get('SECRET_KEY', 'badilisha-hii-kwa-render-env-var')
DEBUG = os.environ.get('DEBUG', 'False') == 'True'
ALLOWED_HOSTS = ['*']  # kwa uzalishaji, weka domain halisi ya Render badala ya '*'

# --- APPS ---
INSTALLED_APPS = [
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'repairs',  # badilisha jina la app kama si 'repairs'
]

# --- MIDDLEWARE (ongeza whitenoise mara baada ya SecurityMiddleware) ---
MIDDLEWARE = [
    'django.middleware.security.SecurityMiddleware',
    'whitenoise.middleware.WhiteNoiseMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
]

ROOT_URLCONF = 'Njiafix.urls'  # badilisha kama jina la project ni tofauti
WSGI_APPLICATION = 'Njiafix.wsgi.application'

# --- DATABASE ---
# Render inatoa DATABASE_URL kiotomatiki ukitumia Render Postgres.
# Kama hutumii Postgres (unatumia SQLite tu), KUMBUKA: Render free tier
# HAINA persistent disk -- database ya SQLite itafutika kila deploy mpya.
DATABASES = {
    'default': dj_database_url.config(
        default=f"sqlite:///{BASE_DIR / 'db.sqlite3'}",
        conn_max_age=600,
    )
}

# --- STATIC FILES (kwa whitenoise) ---
STATIC_URL = '/static/'
STATIC_ROOT = BASE_DIR / 'staticfiles'
STATICFILES_STORAGE = 'whitenoise.storage.CompressedManifestStaticFilesStorage'

# --- MEDIA FILES (picha za guides) ---
MEDIA_URL = '/media/'
MEDIA_ROOT = BASE_DIR / 'media'
# TAHADHARI: kama unapakia picha (ImageField), Render free tier itafuta
# faili hizi kila deploy mpya kwa sababu hakuna persistent disk.
# Fikiria kutumia Cloudinary/S3 kwa picha za kudumu.
