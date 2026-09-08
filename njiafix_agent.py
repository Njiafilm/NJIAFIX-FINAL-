import subprocess
from flask import Flask, jsonify, request
from flask_cors import CORS

app = Flask(__name__)
CORS(app)  # Huruhusu app yako ya Render (NjiaFix) kuwasiliana na agent hii ya ndani

@app.route('/scan', methods=['GET'])
def scan_devices():
    """Huchunguza vifaa vya USB/Android vilivyochomekwa kwenye PC ya fundi"""
    devices = []
    
    # 1. Angalia Vifaa vya Android kupitia ADB
    try:
        adb_res = subprocess.run(['adb', 'devices'], capture_output=True, text=True, check=True)
        lines = adb_res.stdout.strip().split('\n')[1:]
        for line in lines:
            if '\tdevice' in line:
                dev_id = line.split('\t')[0]
                devices.append({"id": dev_id, "type": "Android Phone / Tablet"})
    except Exception as e:
        print(f"Hitilafu ya ADB: {e}")

    # 2. Unaweza kuongeza hapa ukaguzi wa Serial Ports (kwa ajili ya Decoders/Routers)
    # Mfano: kutumia pyserial kuangalia COM ports zilizofunguliwa
    
    return jsonify({
        "status": "success",
        "devices": devices
    })

@app.route('/execute-fix', methods=['POST'])
def execute_fix():
    """Hutekeleza amri ya moja kwa moja (One-Click Fix) kwenye kifaa kilichochaguliwa"""
    data = request.json
    device_id = data.get('device_id')
    action = data.get('action') # mfano: 'reboot', 'clear_cache', 'factory_reset'
    
    try:
        if action == 'reboot':
            cmd = ['adb', '-s', device_id, 'reboot']
        elif action == 'clear_cache':
            cmd = ['adb', '-s', device_id, 'shell', 'pm', 'trim-caches', '999G']
        elif action == 'reboot_recovery':
            cmd = ['adb', '-s', device_id, 'reboot', 'recovery']
        else:
            return jsonify({"status": "error", "message": "Amri haijatambulika"}), 400
            
        # Tekeleza amri kwenye mfumo wa kompyuta ya fundi
        result = subprocess.run(cmd, capture_output=True, text=True, check=True)
        
        return jsonify({
            "status": "success",
            "message": f"Amri ya '{action}' imetekelezwa kwa mafanikio.",
            "output": result.stdout
        })
        
    except subprocess.CalledProcessError as e:
        return jsonify({
            "status": "error",
            "message": f"Imeshindikana kutekeleza amri: {e.stderr}"
        }), 500

if __name__ == '__main__':
    print("==========================================")
    print("  NjiaFix Local Agent inafanya kazi...  ")
    print("  Inasubiri maelekezo kutoka NjiaFix Cloud ")
    print("==========================================")
    app.run(host='127.0.0.1', port=5555)
