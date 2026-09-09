from django.db.models.signals import post_migrate
from django.dispatch import receiver
from .models import DeviceCategory, RepairGuide

@receiver(post_migrate)
def create_default_repair_data(sender, **kwargs):
    if sender.name == 'repairs':
        # 1. Kategoria Kubwa za Vifaa
        categories_data = [
            {"name": "Simu na Vifaa vya Mkononi", "slug": "simu"},
            {"name": "Kompyuta na Laptops", "slug": "kompyuta"},
            {"name": "Vifaa vya Ofisi na Printa", "slug": "ofisi"},
            {"name": "Kamera na Usalama", "slug": "kamera"},
            {"name": "Magari na Vyombo vya Usafiri", "slug": "magari"},
            {"name": "Televisheni na Entertainment", "slug": "tv"},
            {"name": "Ving'amuzi na Mitandao", "slug": "vingamuzi"},
        ]
        
        cat_objs = {}
        for cat in categories_data:
            obj, created = DeviceCategory.objects.get_or_create(
                slug=cat["slug"], 
                defaults={"name": cat["name"]}
            )
            cat_objs[cat["slug"]] = obj

        # 2. Orodha ya Miongozo 45 Muhimu
        guides_data = [
            # SIMU & BOOTLOADER / RECOVERY (10)
            {"title": "Utatuzi wa Betri na Kuchaji kwa Simu za Android", "category": cat_objs.get("simu"), "steps": "1. Kagua chaja na cable.\n2. Safisha tundu la kuchaji.\n3. Pima IC ya kuchaji."},
            {"title": "Kufungua Bootloader na Fastboot Mode", "category": cat_objs.get("simu"), "steps": "1. Washa OEM Unlocking na USB Debugging.\n2. Unganisha PC na utumie fastboot command."},
            {"title": "Kuingia kwenye Recovery Mode na Factory Reset", "category": cat_objs.get("simu"), "steps": "1. Zima simu.\n2. Bonyeza Power + Volume Down.\n3. Fanya Wipe Data."},
            {"title": "Kutibu Hitilafu ya Hanging on Logo", "category": cat_objs.get("simu"), "steps": "1. Fanya Wipe Cache.\n2. Flash Stock ROM."},
            {"title": "Kuondoa FRP Google Account Lock", "category": cat_objs.get("simu"), "steps": "1. Unganisha Wi-Fi.\n2. Tumia FRP bypass APK husika."},
            {"title": "Kurekebisha Simu Iliyoloa Maji", "category": cat_objs.get("simu"), "steps": "1. Zima mara moja.\n2. Ondoa betri kama inawezekana.\n3. Kausha kwa hewa safi."},
            {"title": "Kutatua Tatizo la Simu Kutokusoma Line (SIM Not Detected)", "category": cat_objs.get("simu"), "steps": "1. Safisha sehemu ya line.\n2. Hakikisha IMEI ipo sawa."},
            {"title": "Utatuzi wa WiFi na Bluetooth Kukataa kuwaka", "category": cat_objs.get("simu"), "steps": "1. Fanya Network Reset.\n2. Sasisha mfumo."},
            {"title": "Kufungua Pattern au Password iliyosahaulika", "category": cat_objs.get("simu"), "steps": "1. Tumia Recovery Mode kufanya Factory Reset."},
            {"title": "Kuboresha Kasi ya Simu Inayo-lag", "category": cat_objs.get("simu"), "steps": "1. Futa cache.\n2. Ondoaprogramu zisizotumika."},

            # KOMPYUTA & LAPTOPS (8)
            {"title": "Utatuzi wa Kompyuta Kuwa Slow Sana", "category": cat_objs.get("kompyuta"), "steps": "1. Fanya Disk Cleanup.\n2. Zima background startup programs."},
            {"title": "Kurekebisha Windows Blue Screen Error (BSOD)", "category": cat_objs.get("kompyuta"), "steps": "1. Endesha SFC /scannow.\n2. Angalia RAM."},
            {"title": "Kompyuta Inajipasha Moto Kupita Kiasi (Overheating)", "category": cat_objs.get("kompyuta"), "steps": "1. Safisha vumbi la ndani.\n2. Badilisha Thermal Paste."},
            {"title": "Betri ya Laptop Haichaji Kabisa", "category": cat_objs.get("kompyuta"), "steps": "1. Kagua Charger.\n2. Kagua Battery Driver kwenye Device Manager."},
            {"title": "Kurekebisha Hitilafu ya Hard Disk Kukosa Kusomeka", "category": cat_objs.get("kompyuta"), "steps": "1. Endesha CHKDSK.\n2. Kagua waya za SATA."},
            {"title": "Kinanda cha Kompyuta (Keyboard) Hakifanyi Kazi", "category": cat_objs.get("kompyuta"), "steps": "1. Safisha vumbi.\n2. Rejesha Driver zake."},
            {"title": "WiFi Not Connected kwenye Laptop", "category": cat_objs.get("kompyuta"), "steps": "1. Washa WLAN AutoConfig.\n2. Sasisha Network Drivers."},
            {"title": "Kusafisha Faili Taka za Mfumo (Temp Files)", "category": cat_objs.get("kompyuta"), "steps": "1. Tumia amri ya %temp% kufuta kila kitu."},

            # PRINTER & PHOTOCOPY (8)
            {"title": "Kurekebisha Karatasi Kukwama kwenye Printa (Paper Jam)", "category": cat_objs.get("ofisi"), "steps": "1. Zima printa.\n2. Vuta karatasi taratibu.\n3. Safisha rollers."},
            {"title": "Printa Inatoa Maandishi Hafifu au yenye Mistari", "category": cat_objs.get("ofisi"), "steps": "1. Endesha Print Head Cleaning.\n2. Kagua wino."},
            {"title": "Kusafisha Vioo vya Mashine ya Photocopy", "category": cat_objs.get("ofisi"), "steps": "1. Tumia kitambaa kisicho na unyevu kusafisha scanner glass."},
            {"title": "Kurekebisha Madoa Kwenye Karatasi za Photocopy", "category": cat_objs.get("ofisi"), "steps": "1. Safisha kioo cha ADF na roller za ndani."},
            {"title": "Printer Hairipoti au Haipokei Amri kutoka PC", "category": cat_objs.get("ofisi"), "steps": "1. Angalia Print Spooler.\n2. Unganisha vizuri USB."},
            {"title": "Hitilafu ya Toner Low kwenye Mashine za Ofisi", "category": cat_objs.get("ofisi"), "steps": "1. Tikisa unga wa toner au ongeza mwingine."},
            {"title": "Kurekebisha Karatasi Kukunjamana kwenye Fuser Unit", "category": cat_objs.get("ofisi"), "steps": "1. Kagua joto na hali ya fuser film."},
            {"title": "Kuweka na Kusanidi Printa Kwenye Mtandao wa Ofisi", "category": cat_objs.get("ofisi"), "steps": "1. Weka IP address sahihi."},

            # KAMERA & USALAMA - CCTV (6)
            {"title": "CCTV Camera Inatoa Picha ya Nusu au No Signal", "category": cat_objs.get("kamera"), "steps": "1. Kagua BNC connectors.\n2. Angalia power adapter."},
            {"title": "Kusafisha Lenzi ya Kamera na Kuondoa Ukungu", "category": cat_objs.get("kamera"), "steps": "1. Tumia microfiber kitambaa."},
            {"title": "DVR/NVR Haitunzi Rekodi (SD Card/HDD Error)", "category": cat_objs.get("kamera"), "steps": "1. Fanya Format ya Hard Disk kupitia DVR."},
            {"title": "Night Vision ya CCTV Haionui Usiku", "category": cat_objs.get("kamera"), "steps": "1. Kagua IR LEDs na umeme unaoingia."},
            {"title": "Kurekebisha IP Camera Iliyopoteza Connection", "category": cat_objs.get("kamera"), "steps": "1. Unganisha na RJ45 cable mpya."},
            {"title": "Kuweka Sahihi Password ya DVR Iliyosahaulika", "category": cat_objs.get("kamera"), "steps": "1. Tumia security questions au reset jumper."},

            # MAGARI & OBD-II (5)
            {"title": "Utatuzi wa Engine Check Light Kuwaka", "category": cat_objs.get("magari"), "steps": "1. Tumia OBD-II scanner kusoma DTC codes."},
            {"title": "Gari Linagoma Kuwaka (No Crank / No Start)", "category": cat_objs.get("magari"), "steps": "1. Kagua betri, starter, na fuse kuu."},
            {"title": "Gari Linapasha Moto Sana (Engine Overheating)", "category": cat_objs.get("magari"), "steps": "1. Kagua maji ya radiator na thermostat."},
            {"title": "Kupima Tatizo la Alternator Kutochaji Betri", "category": cat_objs.get("magari"), "steps": "1. Pima Volts kwa Multimeter."},
            {"title": "Kurekebisha Breki Zinazopiga Kelele (Brake Squealing)", "category": cat_objs.get("magari"), "steps": "1. Kagua unene wa brake pads."},

            # TV & VING'AMUZI (8)
            {"title": "Smart TV Haina Picha Lakini Sauti ipo (Backlight Fault)", "category": cat_objs.get("tv"), "steps": "1. Kagua LED strips za ndani."},
            {"title": "TV Inajizima Yenyewe Mara kwa Mara", "category": cat_objs.get("tv"), "steps": "1. Kagua Power Board capacitors."},
            {"title": "Hitilafu ya HDMI Port Kutosoma kwenye TV", "category": cat_objs.get("tv"), "steps": "1. Jaribu kabeli nyingine ya HDMI."},
            {"title": "King'amuzi Hakisomi Ishara (Signal Low au No Signal)", "category": cat_objs.get("vingamuzi"), "steps": "1. Rekebisha mwelekeo wa Diski/Antena."},
            {"title": "King'amuzi Kimeganda kwenye Logo (Hanging)", "category": cat_objs.get("vingamuzi"), "steps": "1. Chomoa umeme kisha rudisha baada ya dakika 2."},
            {"title": "Remote Control Haitaki Kufanya Kazi kwenye TV/Decoder", "category": cat_objs.get("tv"), "steps": "1. Badilisha betri.\n2. Kagua IR sensor."},
            {"title": "Kukosa Baadhi ya Vituo vya King'amuzi", "category": cat_objs.get("vingamuzi"), "steps": "1. Fanya Auto Search upya."},
            {"title": "Kurekebisha Sauti ya TV Iliyokata Ghafla", "category": cat_objs.get("vingamuzi"), "steps": "1. Kagua Audio Settings na speaker wires."}
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
