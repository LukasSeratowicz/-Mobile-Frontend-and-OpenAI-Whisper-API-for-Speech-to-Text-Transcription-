from whisper_handler.whisper_logic import whisper_transcribe
from collections import deque
#import time #  - to simulate processing queue - delete later
import os
from uuid import UUID

audio_queue = deque()
finished_transcriptions = {}
_processing_now = None

class AudioFile:
    def __init__(self, file_path, token):
        self.file_path = file_path
        self.token = token

def transcribe_audio(file_path):
    result = whisper_transcribe(file_path)
    return result

def process_next():
    if audio_queue:
        global _processing_now
        next_audio_file = audio_queue.popleft()
        _processing_now = next_audio_file.token
        transcription_result = transcribe_audio(next_audio_file.file_path)
        #time.sleep(10) #sleep for queueing testing
        finished_transcriptions[next_audio_file.token] = transcription_result
        _processing_now = None
        os.remove(next_audio_file.file_path)
    
def queue_loop():
    while True:
        process_next()



# exported

def add_audio_to_queue(file_path,token):
    audio_file = AudioFile(file_path, token)
    audio_queue.append(audio_file)
    return token
    
def transcription_status(token):
    global _processing_now
    token = str(token)
    if token in [str(t) for t in finished_transcriptions]:
        transcription = finished_transcriptions[UUID(token)]
        del finished_transcriptions[UUID(token)]
        return "success", "OK", transcription

    if token == str(_processing_now):
        return "in_progress", "Your audio is being processed right now", "0"

    for index, audio_file in enumerate(audio_queue):
        if str(audio_file.token) == token:
            return "in_queue", "OK", index+1

    return "token_not_found", "Invalid Token, or already retrieved transcription", ""

