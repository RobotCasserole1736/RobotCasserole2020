# Python 3 server example
from http.server import BaseHTTPRequestHandler, HTTPServer
import time, threading

hostName = 'localhost' # This should be "frcvision.local" on the pi
serverPort = 1736

mostRecentCmd = None

class TestWebserver(BaseHTTPRequestHandler, HTTPServer):

    def do_GET(self):
        global mostRecentCmd
        cmd = self.path.lstrip('/').strip()

        if(cmd != "" and cmd != "favicon.ico"):
            mostRecentCmd = cmd
            # print("Got Command: {}".format(mostRecentCmd))

            self.send_response(200)
            self.send_header("Content-type", "text/html")
            self.end_headers()
            self.wfile.write(bytes("<html><head><title>Casserole Vision App Command Line</title></head>", "utf-8"))
            self.wfile.write(bytes("<p>This is the Casserole Vision App Command Line Input Acknowledgement.</p>", "utf-8"))
            self.wfile.write(bytes("<p>Your Command: %s</p>" % cmd, "utf-8"))
            self.wfile.write(bytes("<body>", "utf-8"))
            self.wfile.write(bytes("</body></html>", "utf-8"))
        

def getMostRecentCmd():
    global mostRecentCmd
    tmp = mostRecentCmd
    mostRecentCmd = None
    return tmp


def serve(webServer):
    print("Config Server started http://%s:%s" % (hostName, serverPort))
    try:
        webServer.serve_forever()
    except KeyboardInterrupt:
        pass

    webServer.server_close()
    print("Config Server stopped.")

if __name__ == "__main__":        

    # This code should be called in the vision function init
    webServer = HTTPServer((hostName, serverPort), TestWebserver)
    serverThread = threading.Thread(target=serve, daemon=True, args=[webServer])
    serverThread.start()

    while(True):
        time.sleep(0.5)
        
        # This code should be called periodically during vision process to check for new commands... maybe once a second?
        cmd = getMostRecentCmd()
        if(cmd):
            print("User has sent command: {}".format(cmd))
