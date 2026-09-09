from django.apps import AppConfig
from django.core.management import call_command
import sys

class RepairsConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'repairs'

    def ready(self):
        if 'runserver' in sys.argv or 'gunicorn' in sys.argv or 'wsgi' in sys.argv:
            try:
                call_command('migrate', interactive=False)
            except Exception as e:
                pass
