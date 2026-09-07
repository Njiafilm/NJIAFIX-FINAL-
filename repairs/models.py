from django.db import models

class DeviceCategory(models.Model):
    name = models.CharField(max_length=50)
    icon_name = models.CharField(max_length=50, blank=True)
    order = models.IntegerField(default=0)

    class Meta:
        ordering = ['order']

    def __str__(self):
        return self.name


class RepairGuide(models.Model):
    DIFFICULTY = [
        ('easy', 'Rahisi'),
        ('medium', 'Wastani'),
        ('hard', 'Ngumu - Fundi Anahitajika'),
    ]
    category = models.ForeignKey(DeviceCategory, on_delete=models.CASCADE, related_name='guides')
    title = models.CharField(max_length=200)
    brand = models.CharField(max_length=100, blank=True)
    symptom = models.TextField()
    solution_steps = models.TextField()
    difficulty = models.CharField(max_length=10, choices=DIFFICULTY, default='easy')
    image = models.ImageField(upload_to='repair_guides/', blank=True, null=True)
    views = models.IntegerField(default=0)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"[{self.category.name}] {self.title}"
