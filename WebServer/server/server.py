from flask import Flask

app = Flask(__name__, static_url_path='')
app.config.from_object(__name__)

from views import *

if __name__ == '__main__':
    app.run()