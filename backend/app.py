from flask import Flask
from api.routes import init_routes
import logging

app = Flask(__name__)

init_routes(app)

if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    app.run(debug=True, host='0.0.0.0', port=5000)