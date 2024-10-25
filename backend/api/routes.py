from flask import request, jsonify
from .whisper_service import transcribe_audio
import os

def is_audio_file_by_extension(file):
    allowed_extensions = ['.mp3', '.wav', '.ogg', '.aac', '.m4a', '.mp4']
    _, extension = os.path.splitext(file.filename)
    return extension.lower() in allowed_extensions

def init_routes(app):
    @app.route('/transcribe', methods=['POST'])
    def transcribe():
        if 'file' not in request.files:
            return jsonify({"error": "No file provided"}), 400
        
        file = request.files['file']

        if file.filename == '':
            return jsonify({"error": "No selected file"}), 400
        
        if not is_audio_file_by_extension(file):
            return jsonify({"error": "Selected file is not an audio file"}), 400

        transcript = transcribe_audio(file)
        return jsonify({"transcript": transcript})
