import os
from flask import Flask, jsonify, request

app = Flask(__name__)

# Hifadhi ya Kina ya Miongozo na Kategoria za Kudumu (Permanent Repair Guides & Categories)
REPAIR_GUIDES = {
    # --- VIFAA VYA MKONONI NA SMARTPHONES ---
    "phone": {
        "title": "Ukarabati wa Simu na Programu (Mobile Maintenance)",
        "category": "Simu na Vifaa vya Mkononi",
        "steps": [
            "1. Angalia afya ya betri na kiwango cha chaji kupitia ADB au multimeter.",
            "2. Safisha cache na faili taka zinazojaza kumbukumbu ya ndani (Storage).",
            "3. Hakikisha mfumo wa uendeshaji (OS) umesasishwa ili kuzuia mdudu wa kiufundi."
        ]
    },
    "bootloader": {
        "title": "Kufungua na Kutatua Bootloader (Bootloader Unlocking & Fixes)",
        "category": "Simu na Vifaa vya Mkononi",
        "steps": [
            "1. Washa 'OEM Unlocking' na 'USB Debugging' kupitia Developer Options kwenye simu.",
            "2. Unganisha simu kwenye PC na uwashe mode ya fastboot kwa amri: 'adb reboot bootloader'.",
            "3. Hakikisha madereva (USB Drivers) ya simu yamesakinishwa vizuri kwenye PC kabla ya kutuma amri ya kufungua."
        ]
    },
    "recovery": {
        "title": "Matumizi ya Recovery Mode na Kuweka Upya (Recovery Mode & Flashing)",
        "category": "Simu na Vifaa vya Mkononi",
        "steps": [
            "1. Zima simu kabisa, kisha bonyeza kitufe cha kuwasha na sauti chini (Power + Volume Down) kwa wakati mmoja.",
            "2. Chagua 'Wipe Data/Factory Reset' iwapo simu imefungwa au ina-hang kwenye logo.",
            "3. Tumia 'Apply update from ADB' au maalum custom recovery kusakinisha mfumo mpya."
        ]
    },

    # --- KOMPYUTA NA LAPTOPS ---
    "pc": {
        "title": "Ukarabati na Usalama wa Kompyuta (PC Health & Cleanup)",
        "category": "Kompyuta na Laptops",
        "steps": [
            "1. Kagua nafasi iliyobaki kwenye diski kuu (Disk Space Check) na uendeshe Disk Cleanup.",
            "2. Simamisha programu zisizohitajika zinazotumia rasilimali nyingi wakati wa kuwasha (Startup Optimization).",
            "3. Endesha uchunguzi wa mfumo (SFC / DISM scan) kurekebisha faili zilizoharibika za Windows."
        ]
    },

    # --- VIFAA VYA OFISI (PRINTER & PHOTOCOPY) ---
    "printer": {
        "title": "Utatuzi wa Printa na Hitilafu za Wino (Printer Troubleshooting)",
        "category": "Vifaa vya Ofisi na Printa",
        "steps": [
            "1. Angalia kama kuna karatasi zilizokwama (Paper Jam) kwenye sehemu ya kuvutia au kutoa karatasi.",
            "2. Endesha kipengele cha kusafisha vichwa vya wino (Print Head Cleaning) kupitia kompyuta kama maandishi yanatoka hafifu au mistari.",
            "3. Hakikisha miunganisho ya mtandao (Wi-Fi) au waya wa USB iko sawa na printa ipo kwenye 'Online' mode."
        ]
    },
    "photocopy": {
        "title": "Matengenezo na Utatuzi wa Mashine za Photocopy",
        "category": "Vifaa vya Ofisi na Printa",
        "steps": [
            "1. Safisha kioo cha scanner na vioo vya ndani kwa kitambaa kisicho na unyevu ili kuondoa madoa na michirizi kwenye nakala.",
            "2. Angalia hali ya wino wa unga (Toner) na usafishe eneo la kupasha joto karatasi (Fuser Unit) kuzuia karatasi kukunjamana.",
            "3. Kagua roli za kuvutia karatasi (Pickup Rollers); kama zimechakaa au zina vumbi, zisafishe au uzibadilishe."
        ]
    },

    # --- CAMERA NA DIGITAL MEDIA ---
    "camera": {
        "title": "Utatuzi wa Kamera za Kidijitali na CCTV (Camera Maintenance)",
        "category": "Kamera na Usalama",
        "steps": [
            "1. Safisha lenzi ya kamera kwa kitambaa maalum cha microfiber na kimiminika cha kusafishia lenzi.",
            "2. Hakikisha kadi ya kumbukumbu (SD Card) imesafishwa au kuumbizwa upya (Formatted) kwenye kamera ili kuepusha makosa ya kusoma faili.",
            "3. Kagua miunganisho ya nyaya za umeme na mtandao (BNC / Ethernet cables) kwa kamera za usalama (CCTV) zinapokosa kuonekana."
        ]
    },

    # --- VYOMBO VYA USAHIRI NA MAGARI ---
    "car": {
        "title": "Utatuzi wa Awali wa Magari (OBD-II Diagnostics)",
        "category": "Magari na Vyombo vya Usafiri",
        "steps": [
            "1. Unganisha kifaa cha uchunguzi cha OBD-II kwenye bandari ya kompyuta ya gari (Dashboard port).",
            "2. Soma namba za makosa (Diagnostic Trouble Codes - DTC) ili kubaini eneo lenye tatizo kwenye injini au sensa.",
            "3. Futa makosa ya muda baada ya kufanya marekebisho ya mitambo au kubadilisha sehemu iliyoharibika."
        ]
    }
}

@app.route('/', methods=['GET'])
def home():
    return jsonify({
        "status": "success",
        "message": "Karibu kwenye NjiaFix Diagnostic Engine API",
        "categories": list(set([data["category"] for data in REPAIR_GUIDES.values()])),
        "modules": list(REPAIR_GUIDES.keys())
    }), 200

# API ya Kurejesha Kategoria Zote
@app.route('/api/categories', methods=['GET'])
def get_categories():
    categories = list(set([data["category"] for data in REPAIR_GUIDES.values()]))
    return jsonify({"status": "success", "categories": categories}), 200

# API ya Kupata Mwongozo wa Kifaa Maalum
@app.route('/api/repair-guides/<device_type>', methods=['GET'])
def get_repair_guide(device_type):
    guide = REPAIR_GUIDES.get(device_type.lower())
    if guide:
        return jsonify({"status": "success", "data": guide}), 200
    else:
        return jsonify({
            "status": "error", 
            "message": "Mwongozo haupatikani kwa kifaa hiki au haijasajiliwa kwenye mfumo."
        }), 404

# API ya Kutafuta
@app.route('/api/search', methods=['GET'])
def search_guides():
    query = request.args.get('q', '').lower().strip()
    results = {}
    
    for key, data in REPAIR_GUIDES.items():
        if query in key or query in data['title'].lower() or query in data['category'].lower() or any(query in step.lower() for step in data['steps']):
            results[key] = data
            
    return jsonify({
        "status": "success",
        "query": query,
        "results": results if results else REPAIR_GUIDES
    }), 200

if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5555)
