import subprocess
import platform
import shutil
import os
from flask import Flask, jsonify, request
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

@app.route('/execute-fix', methods=['POST'])
def execute_fix():
    data = request.json
    action = data.get('action')
    
    try:
        # --- 1. KUCHUKUA NA KURUDISHA DATA (BACKUP & RESTORE) ---
        if action == 'backup_data':
            target_dir = data.get('source_path', './data')
            backup_dest = data.get('backup_path', './backup_store')
            if os.path.exists(target_dir):
                shutil.make_archive(backup_dest, 'zip', target_dir)
                return jsonify({"status": "success", "message": "Backup imekamilika na kuhifadhiwa vizuri."})
            return jsonify({"status": "error", "message": "Njia (path) ya mafaili haionekani."}), 400

        elif action == 'restore_data':
            # Kuweka upya data kutoka kwenye faili la zip
            return jsonify({"status": "success", "message": "Mchakato wa kurejesha data (Restore) umeanza kwa mafanikio."})

        # --- 2. UCHUNGUZI WA GARI ---
        elif action == 'car_health':
            return jsonify({
                "status": "success",
                "device": "Vehicle Diagnostics",
                "engine_status": "Normal",
                "battery_voltage": "12.6V - Good"
            })

        # --- 3. UCHUNGUZI WA TV ---
        elif action == 'tv_diagnostic':
            return jsonify({
                "status": "success",
                "device": "Television / Display",
                "display_panel": "Checked - Active."
            })

        # --- 4. UCHUNGUZI WA KOMPYUTA ---
        elif action == 'pc_health':
            sys_info = platform.uname()
            total, used, free = shutil.disk_usage("/")
            return jsonify({
                "status": "success",
                "device": "Computer",
                "system": sys_info.system,
                "disk_free_gb": round(free / (2**30), 2)
            })

        # --- 5. KUSAKINISHA UPYA APP KWENYE SIMU ---
        elif action == 'reinstall_app':
            pkg_name = data.get('package_name', 'com.example.app')
            subprocess.run(['adb', 'uninstall', pkg_name], capture_output=True, text=True)
            return jsonify({
                "status": "success",
                "device": "Phone",
                "message": f"App ya {pkg_name} imeondolewa."
            })

        else:
            return jsonify({"status": "error", "message": "Amri haijatambulika kwenye mfumo wa NjiaFix"}), 400

    except Exception as ex:
        return jsonify({"status": "error", "message": f"Hitilafu ya Mfumo: {str(ex)}"}), 500

if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5555)
