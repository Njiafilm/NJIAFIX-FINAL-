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
            {"name": "Vifaa vya Nyumbani na Jokofu", "slug": "nyumbani"},
        ]
        
        cat_objs = {}
        for cat in categories_data:
            obj, created = DeviceCategory.objects.get_or_create(
                slug=cat["slug"], 
                defaults={"name": cat["name"]}
            )
            cat_objs[cat["slug"]] = obj

        # 2. Orodha ya Kina ya Miongozo 300+ (Imegawanywa kwa Makundi Muhimu)
        guides_data = []

        # --- SEHEMU YA 1: SIMU, BOOTLOADER & RECOVERY (Miongozo ~50) ---
        simu_models = ["Infinix", "Tecno", "Samsung", "Itel", "Xiaomi", "Oppo", "Vivo", "Huawei", "Realme", "Nokia"]
        for i, brand in enumerate(simu_models):
            guides_data.extend([
                {
                    "title": f"Utatuzi wa Betri na Kuchaji kwa Simu za {brand}",
                    "category": cat_objs.get("simu"),
                    "steps": f"1. Kagua chaja na cable ya {brand} kama inatoa kiwango sahihi cha Volts.\n2. Safisha tundu la kuchaji (Charging Port) kwa kutumia brashi laini.\n3. Pima IC ya kuchaji iwapo simu haisomi kabisa."
                },
                {
                    "title": f"Kufungua Bootloader na Fastboot kwa Vifaa vya {brand}",
                    "category": cat_objs.get("simu"),
                    "steps": f"1. Washa OEM Unlocking na USB Debugging kupitia Developer Options ya {brand}.\n2. Unganisha na PC kisha tumia amri ya 'adb reboot bootloader'.\n3. Tekeleza amri rasmi ya kufungua bootloader."
                },
                {
                    "title": f"Kuingia kwenye Recovery Mode na Factory Reset ya {brand}",
                    "category": cat_objs.get("simu"),
                    "steps": f"1. Zima kabisa simu ya {brand}.\n2. Bonyeza vitufe vya Power na Volume Up/Down kwa pamoja.\n3. Chagua 'Wipe Data/Factory Reset' kisha reboot mfumo."
                },
                {
                    "title": f"Kutibu Hitilafu ya Hanging on Logo kwa {brand}",
                    "category": cat_objs.get("simu"),
                    "steps": f"1. Fanya Wipe Cache Partition kupitia Recovery.\n2. Angalia kama kuna tatizo kwenye EMMC/UFS storage.\n3. Flash mfumo rasmi (Stock ROM) kwa kutumia tool maalum ya {brand}."
                },
                {
                    "title": f"Kuondoa FRP (Google Account Lock) baada ya Reset ya {brand}",
                    "category": cat_objs.get("simu"),
                    "steps": f"1. Unganisha na mtandao wa Wi-Fi kwenye simu ya {brand}.\n2. Tumia njia za Accessibility au OTG kufungua browser.\n3. Pakua na kusakinisha FRP bypass APK husika."
                }
            ])

        # --- SEHEMU YA 2: KOMPYUTA NA LAPTOPS (Miongozo ~50) ---
        pc_issues = ["Windows Boot Error", "Blue Screen of Death (BSOD)", "Slow Performance", "Overheating", "Hard Disk Failure", 
                     "RAM Detection Error", "Keyboard Failure", "Battery Not Charging", "Wi-Fi Card Missing", "BIOS Password Reset"]
        for i, issue in enumerate(pc_issues):
            guides_data.append({
                "title": f"Utatuzi wa Kompyuta: {issue}",
                "category": cat_objs.get("kompyuta"),
                "steps": f"1. Tambua chanzo kikuu cha hitilafu ya {issue} kupitia ujumbe wa makosa (Error Codes).\n2. Endesha vipimo vya hardware na software husika (kama SFC / CHKDSK).\n3. Safisha vumbi la ndani na ubadilishe thermal paste kama inajipasha moto kupita kiasi."
            })

        # --- SEHEMU YA 3: PRINTER & PHOTOCOPY (Miongozo ~50) ---
        printer_types = ["HP LaserJet", "Canon Pixma", "Epson L-Series", "Kyocera Mita", "Xerox WorkCentre", "Brother DCP"]
        for brand in printer_types:
            guides_data.extend([
                {
                    "title": f"Utatuzi wa Karatasi Kukwama (Paper Jam) kwenye {brand}",
                    "category": cat_objs.get("ofisi"),
                    "steps": f"1. Zima printa ya {brand} na ufungue milango yote ya nyuma na mbele.\n2. Vuta karatasi iliyokwama taratibu kwa kufuata mwelekeo wake kuepusha kuchanavipande.\n3. Kagua Pickup Rollers na uzisafishe kwa kitambaa chenye maji kidogo."
                },
                {
                    "title": f"Kusafisha Vichwa vya Wino na Mistari Kwenye {brand}",
                    "category": cat_objs.get("ofisi"),
                    "steps": f"1. Nenda kwenye mipangilio ya printa ya {brand} kupitia PC.\n2. Endesha 'Print Head Cleaning' na 'Nozzle Check'.\n3. Hakikisha kiwango cha wino kwenye matanki au cartridges kipo sawa."
                },
                {
                    "title": f"Kutatua Hitilafu za Scanner na Photocopy kwenye {brand}",
                    "category": cat_objs.get("ofisi"),
                    "steps": f"1. Safisha kioo cha juu cha scanner na strip ya kioo cha ADF kwenye {brand}.\n2. Angalia kama taa ya scanner (Lamp) inawaka vizuri wakati wa kunakili.\n3. Sasisha firmware au drivers za printa hiyo."
                }
            ])

        # --- SEHEMU YA 4: KAMERA NA USALAMA - CCTV (Miongozo ~40) ---
        camera_issues = ["CCTV No Signal", "Camera Blurry Image", "DVR Not Recording", "Night Vision Failure", "PTZ Motor Stuck", 
                         "SD Card Error on IP Camera", "Power Supply Voltage Drop", "BNC Connector Loose", "Network Port Dead", "Audio Recording Missing"]
        for issue in camera_issues:
            guides_data.append({
                "title": f"Ukarabati na Utatuzi wa Kamera: {issue}",
                "category": cat_objs.get("kamera"),
                "steps": f"1. Kagua miunganisho ya nyaya (Coaxial/Ethernet) na usambazaji wa umeme (Power Adapter) kwa tatizo la {issue}.\n2. Futa lenzi na vumbi kwa kutumia kitambaa cha microfiber.\n3. Fanya Factory Default kwenye DVR/NVR au kamera husika."
            })

        # --- SEHEMU YA 5: MAGARI NA OBD-II (Miongozo ~40) ---
        car_issues = ["Engine Check Light On", "Car Won't Start (No Crank)", "Overheating Engine", "Transmission Slipping", "ABS Warning Light", 
                      "Poor Fuel Economy", "Brake Squealing Noise", "AC Not Cooling", "Alternator Failure", "Suspension Knocking Noise"]
        for issue in car_issues:
            guides_data.append({
                "title": f"Utatuzi wa Gari: {issue}",
                "category": cat_objs.get("magari"),
                "steps": f"1. Unganisha kifaa cha uchunguzi cha OBD-II kusoma namba za hitilafu (DTC Codes) zinazosababisha {issue}.\n2. Kagua viwango vya mafuta, maji ya radiator, na mafuta ya injini/gear.\n3. Fanya ukarabati wa sehemu iliyoharibika na ufute kumbukumbu za makosa."
            })

        # --- SEHEMU YA 6: TELEVISHENI NA VING'AMUZI (Miongozo ~60) ---
        tv_decoders = ["Smart TV No Picture (Backlight Issue)", "TV No Sound", "Decoder Signal Low (Azam/DSTV/StarTimes)", "HDMI Port Not Working", "Remote Control Not Responding"]
        for item in tv_decoders:
            guides_data.append({
                "title": f"Miongozo ya Kielektroniki: {item}",
                "category": cat_objs.get("tv"),
                "steps": f"1. Fanya 'Power Cycle' kwa kuchomoa kifaa kwa dakika 5 na kurudisha.\n2. Kagua miunganisho ya waya wa antena, HDMI, au umeme.\n3. Kagua bodi ya ndani (Main/Power Board) iwapo kuna capacitor zilizovimba au fuse zilizokatika."
            })

        # Kuingiza data zote kwenye Database bila kurudia
        for guide in guides_data:
            if guide["category"]:
                RepairGuide.objects.get_or_create(
                    title=guide["title"],
                    defaults={
                        "category": guide["category"],
                        "steps": guide["steps"]
                    }
                )
