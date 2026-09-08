from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    initial = True

    dependencies = []

    operations = [
        migrations.CreateModel(
            name='DeviceCategory',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(max_length=50)),
                ('icon_name', models.CharField(blank=True, max_length=50)),
                ('order', models.IntegerField(default=0)),
            ],
            options={
                'ordering': ['order'],
            },
        ),
        migrations.CreateModel(
            name='RepairGuide',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('title', models.CharField(max_length=200)),
                ('brand', models.CharField(blank=True, max_length=100)),
                ('symptom', models.TextField()),
                ('solution_steps', models.TextField()),
                ('difficulty', models.CharField(choices=[('easy', 'Rahisi'), ('medium', 'Wastani'), ('hard', "Ngumu - Fundi Anahitajika")], default='easy', max_length=10)),
                ('image', models.ImageField(blank=True, null=True, upload_to='repair_guides/')),
                ('views', models.IntegerField(default=0)),
                ('created_at', models.DateTimeField(auto_now_add=True)),
                ('category', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='guides', to='repairs.devicecategory')),
            ],
        ),
    ]
