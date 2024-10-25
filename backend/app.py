from flask import Flask
from api.routes import init_routes
from api.whisper_queue import queue_loop
import logging
import threading
import shutil
import os

app = Flask(__name__)

init_routes(app)

# if program crashed last time, delete all the temps left
def delete_temps():
    temp_dir = 'temp/'
    if os.path.exists(temp_dir):
        shutil.rmtree(temp_dir)
        os.makedirs(temp_dir)


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    delete_temps()
    processing_thread = threading.Thread(target=queue_loop, daemon=True)
    processing_thread.start()
    app.run(debug=True, host='0.0.0.0', port=5000, threaded=True)