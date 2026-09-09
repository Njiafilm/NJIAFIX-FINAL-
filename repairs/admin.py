from django.contrib import admin
from .models import DeviceCategory, RepairGuide

@admin.register(DeviceCategory)
class DeviceCategoryAdmin(admin.ModelAdmin):
    list_display = ('name',)

@admin.register(RepairGuide)
class RepairGuideAdmin(admin.ModelAdmin):
    list_display = ('title', 'category')
    search_fields = ('title', 'steps')
    list_filter = ('category',)
