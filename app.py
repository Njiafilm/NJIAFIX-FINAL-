from flask import Flask, request, jsonify

app = Flask(__name__)

# Mfano wa kupokea amri kutoka kwenye Dashboard ya Web
@app.route('/api/fix-device', methods=['POST'])
def fix_device():
    data = request.json
    device_type = data.get('device_type') # simu, tv, decoder
    action = data.get('action') # reset, clear_cache, flash_fix
    
    # Hapa tunatuma amri kwenda kwa Local Agent ya fundi
    # (Inaweza kuwa kupitia MQTT, WebSockets, au Local IP ya PC ya fundi)
    
    return jsonify({
        "status": "success", 
        "message": f"Amri ya kurekebisha {device_type} ({action}) imetumwa kwa mafanikio."
    })

if __name__ == '__main__':
    app.run(debug=True)
