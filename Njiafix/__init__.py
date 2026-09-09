import pymysql  # Unaweza kuacha kama unatumia MySQL, au futa kama unatumia SQLite ya kawaida

# Hii inahakikisha Django inasoma vizuri muundo wa mradi
from .celery import app as celery_app  # Iache kama unatumia Celery, la sivyo acha wazi

__all__ = ('celery_app',)
