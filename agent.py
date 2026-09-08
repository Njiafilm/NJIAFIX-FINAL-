import os
from flask import Flask, jsonify, request

app = Flask(__name__)

# Hifadhi ya Miongozo ya Ukarabati na Utatuzi
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
    },
    "tv": {
        "title": "Utatuzi wa TV (TV Troubleshooting)",
        "steps": [
            "1. Angalia muunganisho wa umeme na waya wa HDMI/AV.",
            "2. Fanya 'Power Cycle' kwa kuchomoa TV kwa dakika 5 kisha urudishe.",
            "3. Kagua mfumo wa bodi ya mwanga (Backlight) kama sauti ipo lakini hakuna picha."
        ]
    }
}

@app.route('/', methods=['GET'])
def home():
    return jsonify({
        "status": "success",
        "message": "Karibu kwenye NjiaFix Diagnostic Engine API",
        "modules": list(REPAIR_GUIDES.keys())
    }), 200

# API ya Kupata Mwongozo wa Kifaa Maalum
@app.route('/api/repair-guides/<device_type>', methods=['GET'])
def get_repair_guide(device_type):
    guide = REPAIR_GUIDES.get(device_type.lower())
    if guide:
        return jsonify({"status": "success", "data": guide}), 200
    else:
        return jsonify({
            "status": "error", 
            "message": "Mwongozo haupatikani kwa kifaa hiki."
        }), 404

# API ya Kutafuta (Search Endpoint) inayoweza kutumiwa na sanduku la utafutaji la Frontend
@app.route('/api/search', methods=['GET'])
def search_guides():
    query = request.args.get('q', '').lower().strip()
    results = {}
    
    for key, data in REPAIR_GUIDES.items():
        if query in key or query in data['title'].lower() or any(query in step.lower() for step in data['steps']):
            results[key] = data
            
    return jsonify({
        "status": "success",
        "query": query,
        "results": results if results else REPAIR_GUIDES # Ikiwa hakuna iliyopatikana maalum, rudisha zote au tumia kichujio
    }), 200

if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5555)
