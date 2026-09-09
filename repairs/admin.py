from django.db.models.signals import post_migrate
from django.dispatch import receiver
from .models import DeviceCategory, RepairGuide  # Badilisha zilingane na majina ya models zako

@receiver(post_migrate)
def create_default_repair_data(sender, **kwargs):
    # Hakikisha inafanya kazi kwenye app ya repairs pekee
    if sender.name == 'repairs':
        # 1. Orodha ya Kategoria
        categories_data = [
            {"name": "Simu na Vifaa vya Mkononi", "slug": "simu"},
            {"name": "Kompyuta na Laptops", "slug": "kompyuta"},
            {"name": "Vifaa vya Ofisi na Printa", "slug": "ofisi"},
            {"name": "Kamera na Usalama", "slug": "kamera"},
            {"name": "Magari na Vyombo vya Usafiri", "slug": "magari"}
        ]
        
        cat_objs = {}
        for cat in categories_data:
            obj, created = DeviceCategory.objects.get_or_create(
                slug=cat["slug"], 
                defaults={"name": cat["name"]}
            )
            cat_objs[cat["slug"]] = obj

        # 2. Orodha ya Miongozo na Hatua zake
        guides_data = [
            {
                "title": "Ukarabati wa Simu na Programu (Mobile Maintenance)",
                "category": cat_objs.get("simu"),
                "steps": "1. Angalia afya ya betri na chaji kupitia ADB.\n2. Safisha cache na faili taka.\n3. Sasisha mfumo wa uendeshaji (OS)."
            },
            {
                "title": "Kufungua na Kutatua Bootloader (Bootloader Unlocking & Fixes)",
                "category": cat_objs.get("simu"),
                "steps": "1. Washa OEM Unlocking na USB Debugging.\n2. Washa fastboot mode kupitia PC.\n3. Hakikisha USB drivers zimesakinishwa vizuri."
            },
            {
                "title": "Matumizi ya Recovery Mode na Kuweka Upya",
                "category": cat_objs.get("simu"),
                "steps": "1. Bonyeza Power + Volume Down.\n2. Chagua Wipe Data/Factory Reset.\n3. Tumia Apply update from ADB kusakinisha mfumo."
            },
            {
                "title": "Utatuzi wa Printa na Hitilafu za Wino (Printer Troubleshooting)",
                "category": cat_objs.get("ofisi"),
                "steps": "1. Angalia karatasi zilizokwama (Paper Jam).\n2. Endesha Print Head Cleaning.\n3. Hakikisha mtandao na USB vipo sawa."
            },
            {
                "title": "Matengenezo na Utatuzi wa Mashine za Photocopy",
                "category": cat_objs.get("ofisi"),
                "steps": "1. Safisha kioo cha scanner na vioo vya ndani.\n2. Kagua hali ya Toner na Fuser Unit.\n3. Safisha au badilisha Pickup Rollers."
            },
            {
                "title": "Utatuzi wa Kamera za Kidijitali na CCTV",
                "category": cat_objs.get("kamera"),
                "steps": "1. Safisha lenzi kwa kitambaa cha microfiber.\n2. Fanya format ya SD Card kwenye kamera.\n3. Kagua miunganisho ya waya za CCTV."
            }
        ]

        for guide in guides_data:
            if guide["category"]:
                RepairGuide.objects.get_or_create(
                    title=guide["title"],
                    defaults={
                        "category": guide["category"],
                        "steps": guide["steps"]
                    }
                )
