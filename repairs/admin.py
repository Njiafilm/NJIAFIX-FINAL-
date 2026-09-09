from django.contrib import admin
from .models import DeviceCategory, RepairGuide


@admin.register(DeviceCategory)
class DeviceCategoryAdmin(admin.ModelAdmin):
    list_display = ("name", "icon_name", "order")
    list_editable = ("order",)
    ordering = ("order", "name")
    search_fields = ("name",)


@admin.register(RepairGuide)
class RepairGuideAdmin(admin.ModelAdmin):
    list_display = (
        "title",
        "category",
        "brand",
        "difficulty",
        "views",
        "created_at",
    )

    list_filter = (
        "category",
        "difficulty",
        "brand",
    )

    search_fields = (
        "title",
        "brand",
        "symptom",
        "solution_steps",
    )

    readonly_fields = (
        "views",
        "created_at",
    )

    list_per_page = 25

    ordering = ("-created_at",)
