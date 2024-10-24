
# Speech-to-Text with OpenAI Whisper: API Backend and Mobile Integration

This project provides a **speech-to-text API** backend using **OpenAI's Whisper** model for transcribing speech to text. The backend receives speech from a **mobile frontend** application, processes it, and returns the transcribed text.

## Features

- **OpenAI Whisper Integration**: Leveraging Whisper's advanced speech-to-text capabilities.
- **Mobile Frontend Integration**: The mobile app sends audio data to the backend for transcription.
- **RESTful API**: Exposes endpoints to handle audio input and return transcribed text.

## Technology Stack

- **Backend**: Flask-based API using OpenAI Whisper for speech recognition.
- **Mobile Frontend**: Kotlin app developed to capture and send speech data.
- **API**: RESTful API facilitating the communication between mobile frontend and the backend.

## Getting Started

### Prerequisites

- Python 3.x or later
- OpenAI Whisper (link placeholder to huggingface)
- Flask
- Kotlin development environment (Android Studio)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/changethislater
   cd changethislater
   ```

2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

3. Run the API backend:
   ```bash
   python app.py
   ```

### Usage

- Send an audio file from the mobile app to the backend API.
- The backend processes the file using OpenAI Whisper and returns the transcribed text.
- Example request:
   ```bash
   curl change this in the future
   ```

## Contributing

Feel free to open an issue if you want to contribute, but this is an educational project for our university, so code contribution would be considered cheating :)

## License

Feel free to copy and modify as you please, but make sure to credit us:

L. Seratowicz seratowiczlukasz@gmail.com 

T. Czajkowski <replace me later>

