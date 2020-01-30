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

import glob

from PIL import Image

from networktables import *
import ntcore
import os

    
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


##############################################################################
# Camera Calibrator
##############################################################################
class CameraParamGetter():
    def calibrateToChess(self):
        # termination criteria
        criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 30, 0.001)

        # prepare object points, like (0,0,0), (1,0,0), (2,0,0) ....,(6,5,0)
        objp = np.zeros((6*8,3), np.float32)
        objp[:,:2] = np.mgrid[0:8,0:6].T.reshape(-1,2)

        # Arrays to store object points and image points from all the images.
        objpoints = [] # 3d point in real world space
        imgpoints = [] # 2d points in image plane.

        images = glob.glob('*.jpg')
        graysize=cv2.cvtColor(cv2.imread(images[0]),cv2.COLOR_BGR2GRAY)
        for fname in images:
            print("Starting a new Image:"+fname)
            img = cv2.imread(fname)
            gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)

            # Find the chess board corners
            ret, corners = cv2.findChessboardCorners(gray, (8,6),None)

            # If found, add object points, image points (after refining them)
            if ret == True:
                objpoints.append(objp)

                corners2 = cv2.cornerSubPix(gray,corners,(11,11),(-1,-1),criteria)
                imgpoints.append(corners2)

                # Draw and display the corners
                img = cv2.drawChessboardCorners(img, (8,6), corners2,ret)
                cv2.imwrite(fname[:-4]+" chess board.jpg",img)
                print("completed another image")
                
        ret, mtx, dist, _, _ = cv2.calibrateCamera(objpoints, imgpoints, graysize.shape[::-1],None,None)       
        print(ret)
        print(mtx)
        print(dist)



if __name__ == "__main__":
    global img
    img = None

    print("Casserole Vision Processing starting")
    print("OpenCV Version: {}".format(cv2.__version__))
    print("numpy Version: {}".format(np.__version__))

    #cap = cv2.VideoCapture('0')
    PATH=os.getcwd()
    os.chdir(PATH+"//ChessSamples")

    calibrator=CameraParamGetter()
    calibrator.calibrateToChess()
    print("It calibrated all the images. but you are still probably dumb")

    # start NetworkTables
    ntinst = NetworkTablesInstance.getDefault()
    print("Setting up NetworkTables client for team {}".format(1736))
    ntinst.startClientTeam(1736)


    
    capture = cv2.VideoCapture(0)
    capture.set(cv2.CAP_PROP_FRAME_WIDTH, 1920)
    capture.set(cv2.CAP_PROP_FRAME_HEIGHT, 1080)
    capture.set(cv2.CAP_PROP_SATURATION,0.2)
