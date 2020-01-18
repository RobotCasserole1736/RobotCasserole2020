#!/usr/bin/env python3
#----------------------------------------------------------------------------
# Copyright (c) 2018 FIRST. All Rights Reserved.
# Open Source Software - may be modified and shared by FRC teams. The code
# must be accompanied by the FIRST BSD license file in the root directory of
# the project.
#----------------------------------------------------------------------------

import json
import time
import sys

import numpy as np
import cv2

from PIL import Image

from networktables import *
import ntcore

    
import threading
from http.server import BaseHTTPRequestHandler,HTTPServer
from socketserver import ThreadingMixIn

from io import StringIO ## for Python 3

import time
import threading

class CamHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        global img
        if self.path.endswith('.mjpg'):
            self.send_response(200)
            self.send_header('Content-type','multipart/x-mixed-replace; boundary=--jpgboundary')
            self.end_headers()
            while True:
                try:
                    if(img is not None):
                        ret, jpg = cv2.imencode('.jpg', img)
                        # print 'Compression ratio: %d4.0:1'%(compress(img.size,jpg.size))
                        self.wfile.write(b'--jpgboundary')
                        self.send_header('Content-type', 'image/jpeg')
                        # self.send_header('Content-length',str(tmpFile.len))
                        self.send_header('Content-length', str(jpg.size))
                        self.end_headers()
                        self.wfile.write(jpg.tostring())
                except KeyboardInterrupt:
                    break
            return

        if self.path.endswith('.html'):
            self.send_response(200)
            self.send_header('Content-type','text/html')
            self.end_headers()
            self.wfile.write('<html><head></head><body>')
            self.wfile.write('<img src="http://10.17.36.10:5805/cam.mjpg"/>')
            self.wfile.write('</body></html>')
            return


class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    """Handle requests in a separate thread."""

if __name__ == "__main__":
    global img
    img = None

    print("Casserole Vision Processing starting")
    print("OpenCV Version: {}".format(cv2.__version__))
    print("numpy Version: {}".format(np.__version__))

    #cap = cv2.VideoCapture('0')


    # start NetworkTables
    ntinst = NetworkTablesInstance.getDefault()
    print("Setting up NetworkTables client for team {}".format(1736))
    ntinst.startClientTeam(1736)


    
    capture = cv2.VideoCapture(0)
    capture.set(cv2.CAP_PROP_FRAME_WIDTH, 1280)
    capture.set(cv2.CAP_PROP_FRAME_HEIGHT, 1024)
    capture.set(cv2.CAP_PROP_SATURATION,0.2)

    
    try:
        server = ThreadedHTTPServer(('10.17.36.10', 5805), CamHandler)
        t = threading.Thread(target=server.serve_forever)
        t.start()
        print("MJPEG Server Started")
    except KeyboardInterrupt:
        capture.release()
        server.socket.close()

    ntTable = NetworkTables.getTable("VisionData")

    prev_cap_time = 0
    capture_time = 0

    print("Vision Processing Starting..")

    # loop forever
    while True:
        prev_cap_time = capture_time
        rc,cam_img = capture.read()
        capture_time = time.time_ns()/(10 ** 9)
        
        if not rc:
            img = None
            print("Bad Image Capture!")
            continue
        else:
            img = cam_img
            #TODO - image processing here

        proc_end_time = time.time_ns()/(10 ** 9)
        proc_time = proc_end_time - capture_time
        ntTable.putNumber("proc_duration_sec", proc_time)
        ntTable.putNumber("framerate_fps", 1.0/(capture_time - prev_cap_time))
        time.sleep(0)
        
