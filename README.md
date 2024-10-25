
# Speech-to-Text with OpenAI Whisper: API Backend and Mobile Integration

This project provides a **speech-to-text API** backend using **OpenAI's Whisper** model provided by faster_whisper for transcribing speech to text. The backend receives speech from a **mobile frontend** application, processes it, and returns the transcribed text.

## Features

- **OpenAI Whisper Integration**: Leveraging Whisper's advanced speech-to-text capabilities.
- **Mobile Frontend Integration**: The mobile app sends audio data to the backend for transcription.
- **RESTful API**: Exposes endpoints to handle audio input and return transcribed text.

## Technology Stack

- **Backend**: Flask-based API using OpenAI Whisper from faster_whisper for speech recognition.
- **Mobile Frontend**: Kotlin app developed to capture and send speech data.
- **API**: RESTful API facilitating the communication between mobile frontend and the backend.

## Getting Started

### Prerequisites

- Python (tested on 3.12.5)
- OpenAI Whisper from faster-whisper [huggingface](https://huggingface.co/Systran)
- Flask
- Kotlin development environment [Android Studio](https://developer.android.com/studio)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/LukasSeratowicz/Speech-to-Text-with-OpenAI-Whisper-API-Backend-and-Mobile-Integration.git
   cd Speech-to-Text-with-OpenAI-Whisper-API-Backend-and-Mobile-Integration
   ```

#### Backend
1. Open Backend folder:
   ```bash
   cd Backend
   ```
   
2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

3. Run the API backend:
   ```bash
   python3 app.py
   ```

#### FrontEnd

1. Open Frontend folder as a project in Android Studio
2. Enjoy!
   
### Usage

- Send an audio file from the mobile app to the backend API.
- The backend processes the file using OpenAI Whisper and returns the transcribed text.
- Example request for Windows Testing:
   ```bash
   curl -X POST -F "file=@harvard.wav" http://localhost:5000/transcribe
   ```
   A status will be displayed, to get the status curl `status`:
   ```bash
   curl http://localhost:5000/status/b3f2bb95-cd03-46e3-a416-74eb5678472c
   ```

## License

Feel free to copy and modify as you please, but make sure to credit us:

L. Seratowicz seratowiczlukasz@gmail.com

T. Czajkowski <replace me later>

