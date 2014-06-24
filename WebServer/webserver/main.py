import sys
from cherrypy.wsgiserver import CherryPyWSGIServer
from pyramid.paster import get_app
from webserver import main


def run_server():
    isdev = sys.argv[1] == "dev"

    ini = None
    port = None

    if isdev:
        ini = 'development.ini'
        port = 8080
    else:
        ini = 'production.ini'
        port = 80

    print("Setting up the server in {0} mode.".format(sys.argv[1]))

    app = main(None)
    server = CherryPyWSGIServer(('0.0.0.0', port), app)

    try:
        server.start()
    finally:
        server.stop()

if __name__ == '__main__':
    run_server()