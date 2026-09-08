import os
import shutil
import platform
import subprocess
from flask import Flask, jsonify, request

app = Flask(__name__)

# Moduli ya Miongozo ya Ukarabati (Repair Guides Module)
REPAIR_GUIDES = {
    "phone": {
        "title": "Ukarabati wa Simu (Mobile Maintenance)",
        "steps": [
            "1. Angalia afya ya betri na kiwango cha chaji kupitia ADB.",
            "2. Safisha cache na faili taka zinazojaza kumbukumbu.",
            "3. Hakikisha mfumo wa uendeshaji (OS) umesasishwa ili kuzuia mdudu wa kiufundi."
        ]
    },
    "pc": {
        "title": "Ukarabati wa Kompyuta (PC Health & Cleanup)",
        "steps": [
            "1. Kagua nafasi iliyobaki kwenye diski kuu (Disk Space Check).",
            "2. Simamisha programu zinazotumia rasilimali nyingi wakati wa kuwasha (Startup Optimization).",
            "3. Endesha uchunguzi wa faili za mfumo ili kurekebisha hitilafu zilizopo."
        ]
    },
    "car": {
        "title": "Utatuzi wa Awali wa Magari (OBD-II Diagnostics)",
        "steps": [
            "1. Unganisha kifaa cha OBD-II kwenye bandari ya gari.",
            "2. Soma namba za makosa (Diagnostic Trouble Codes - DTC).",
            "3. Futa makosa ya muda baada ya kufanya marekebisho ya mitambo au sensa."
        ]
    }
}

@app.route('/', methods=['GET'])
def home():
    return jsonify({
        "status": "success",
        "message": "Karibu kwenye NjiaFix Diagnostic Engine API",
        "modules": ["phone", "pc", "car", "tv", "backup", "repair-guides"]
    }), 200

@app.route('/api/repair-guides/<device_type>', methods=['GET'])
def get_repair_guide(device_type):
    guide = REPAIR_GUIDES.get(device_type.lower())
    if guide:
        return jsonify({"status": "success", "data": guide}), 200
    else:
        return jsonify({
            "status": "error", 
            "message": "Mwongozo haupatikani kwa kifaa hiki. Jaribu: phone, pc, au car"
        }), 404

if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5555)
