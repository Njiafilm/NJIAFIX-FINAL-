import subprocess
from flask import Flask, jsonify, request
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

@app.route('/execute-fix', methods=['POST'])
def execute_fix():
    data = request.json
    device_category = data.get('category') # simu, kompyuta, tv, decoder
    action = data.get('action')
    
    try:
        cmd = []
        # 1. Marekebisho ya Simu (Android kupitia ADB)
        if device_category == 'simu':
            if action == 'reboot':
                cmd = ['adb', 'reboot']
            elif action == 'clear_cache':
                cmd = ['adb', 'shell', 'pm', 'trim-caches', '999G']
                
        # 2. Marekebisho ya Kompyuta (Windows network/system commands)
        elif device_category == 'kompyuta':
            if action == 'flush_dns':
                cmd = ['ipconfig', '/flushdns']
            elif action == 'reset_network':
                cmd = ['netsh', 'winsock', 'reset']
                
        # 3. Ving'amuzi / Sat (Kupitia Serial/COM ports au IP commands)
        elif device_category == 'decoder':
            if action == 'reboot_sat':
                # Amri maalum ya serial port au telnet kwenda kwenye decoder
                pass

        if cmd:
            result = subprocess.run(cmd, capture_output=True, text=True, check=True)
            return jsonify({"status": "success", "output": result.stdout})
        else:
            return jsonify({"status": "error", "message": "Amri haijatambuliwa kwa kifaa hiki."}), 400

    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5555)
