from django.apps import AppConfig
from django.core.management import call_command
import sys

class RepairsConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'repairs'

    def ready(self):
        if 'runserver' in sys.argv or 'gunicorn' in sys.argv or 'wsgi' in sys.argv:
            try:
                # 1. Inajenga meza za database
                call_command('migrate', interactive=False)
                
                # 2. Inatengeneza admin mpya kiotomatiki kama hayupo
                from django.contrib.auth.models import User
                if not User.objects.filter(username='admin').exists():
                    User.objects.create_superuser('admin', 'admin@njiafix.com', 'admin1234')
            except Exception as e:
                pass
