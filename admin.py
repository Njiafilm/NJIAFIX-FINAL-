from django.contrib import admin
from .models import DeviceCategory, RepairGuide

@admin.register(DeviceCategory)
class DeviceCategoryAdmin(admin.ModelAdmin):
    list_display = ('name', 'order')

@admin.register(RepairGuide)
class RepairGuideAdmin(admin.ModelAdmin):
    list_display = ('title', 'category', 'brand', 'difficulty', 'views')
    list_filter = ('category', 'difficulty', 'brand')
    search_fields = ('title', 'symptom', 'brand')
