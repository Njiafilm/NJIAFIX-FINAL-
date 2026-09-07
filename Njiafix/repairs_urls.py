# repairs/urls.py (app-level)
from django.urls import path
from . import views

urlpatterns = [
    path('matengenezo/', views.matengenezo_page, name='matengenezo_page'),
    path('api/categories/', views.api_categories, name='api_categories'),
    path('api/guides/', views.api_guides, name='api_guides'),
]
