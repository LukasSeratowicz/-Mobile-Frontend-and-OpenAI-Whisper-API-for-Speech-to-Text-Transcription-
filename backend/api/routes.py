from flask import request, jsonify
from .whisper_queue import add_audio_to_queue, transcription_status
import os
from uuid import uuid4

def init_routes(app):
    @app.route('/transcribe', methods=['POST'])
    def transcribe():
        if 'file' not in request.files:
            return jsonify({"error": "No file provided"}), 400
        
        file = request.files['file']

        if file.filename == '':
            return jsonify({"error": "No selected file"}), 400
        
        temp_dir = 'temp'
        os.makedirs(temp_dir, exist_ok=True)

        file_extension = os.path.splitext(file.filename)[1]
        if file_extension not in ['.mp3', '.wav', '.ogg', '.aac', '.m4a', '.mp4']:
            return jsonify({"error": "Selected file is not an audio file"}), 400
        
        token = uuid4()
        temp_file_path = os.path.join(temp_dir, f"{token}{file_extension}")
        file.save(temp_file_path)

        status = add_audio_to_queue(temp_file_path, token)

        return jsonify({"status": status})
    
    @app.route('/status/<token>', methods=['GET'])
    def get_status(token):
        code, status_info, value = transcription_status(token)
        if code == "token_not_found" :
            return jsonify({"error": "Token not found"}), 400
        elif code == "success":
            return jsonify({"transcription": value})
        elif code == "in_queue":
            return jsonify({"in_queue": value})
        elif code == "in_progress":
            return jsonify({"in_progress": status_info})
        else:
            return jsonify(status_info), 200
