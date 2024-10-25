from whisper_handler.whisper_logic import whisper_transcribe
import io

def transcribe_audio(file):
    audio_data = file.read()
    audio_buffer = io.BytesIO(audio_data)
    result = whisper_transcribe(audio_buffer)
    return result
