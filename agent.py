import subprocess
from flask import Flask, jsonify, request
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

@app.route('/execute-fix', methods=['POST'])
def execute_fix():
    data = request.json
    action = data.get('action')
    try:
        if action == 'reboot':
            cmd = ['adb', 'reboot']
        elif action == 'clear_cache':
            cmd = ['adb', 'shell', 'pm', 'trim-caches', '999G']
        else:
            return jsonify({"status": "error", "message": "Amri haijatambulika"}), 400
        result = subprocess.run(cmd, capture_output=True, text=True, check=True)
        return jsonify({"status": "success", "output": result.stdout})
    except subprocess.CalledProcessError as e:
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5555)
