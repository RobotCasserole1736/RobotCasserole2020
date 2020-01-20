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
    def calibrateToChess(self,inimg):
        # termination criteria
        criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 30, 0.001)

        # prepare object points, like (0,0,0), (1,0,0), (2,0,0) ....,(6,5,0)
        objp = np.zeros((6*7,3), np.float32)
        objp[:,:2] = np.mgrid[0:7,0:6].T.reshape(-1,2)

        # Arrays to store object points and image points from all the images.
        objpoints = [] # 3d point in real world space
        imgpoints = [] # 2d points in image plane.

        images = glob.glob('*.jpg')
        graysize=cv2.imread(images[0])

        for fname in images:
            img = cv2.imread(fname)
            gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)

            # Find the chess board corners
            ret, corners = cv2.findChessboardCorners(gray, (7,6),None)

            # If found, add object points, image points (after refining them)
            if ret == True:
                objpoints.append(objp)

                corners2 = cv2.cornerSubPix(gray,corners,(11,11),(-1,-1),criteria)
                imgpoints.append(corners2)

                # Draw and display the corners
                img = cv2.drawChessboardCorners(img, (7,6), corners2,ret)
                
        ret, mtx, dist, rvecs, tvecs = cv2.calibrateCamera(objpoints, imgpoints, graysize.shape[::-1],None,None)       



##############################################################################
# Yearly Code
##############################################################################
class VisionProcessor():

    ###################
    #Init
    ###################
    def init(self):
        self.debug=True
        #self.camMatrix=np.
        #self.distCoeffs=

        #Points of the Trapezoid
        self.ObjPoints=np.array([
                            (0,0,0),
                            (2,0,0),
                            (11.1248,-15,0),
                            (28.1252,-15,0),
                            (37.25,0,0),
                            (39.25,0,0),
                            (29.4448,-17,0),
                            (9.8051,-17,0)
                            ],dtype=np.float64)

        ###Ranges###

        ##Test Vision Range
        self.lowerBrightness = np.array([0,0,75])
        self.upperBrightness = np.array([255,255,255])

        ##I dont remember what these were for Range
        #lowerBrightness = np.array([70,0,150])
        #upperBrightness = np.array([100,200,255])
        
        ##Standard Green Vision Tape Range
        #self.lowerBrightness=np.array([50,0,75])
        #self.upperBrightness=np.array([100,200,255])



    ###################
    # Main Process
    ###################
    def process(self, inFrame):
        self.inimg=inFrame
        self.LightFilter()
        _, contours, _ = cv2.findContours(self.mask, mode=cv2.RETR_LIST, method=cv2.CHAIN_APPROX_SIMPLE)
        filteredContours = self.filtercontours(contours, 100.0, 20.0, 1000.0)
        self.TargetDetection(filteredContours)
        self.outputStr="{{{},{},{},{},{},{}}}\n".format(self.ret,self.angle,self.angle1,self.angle2,self.xval,self.yval)
        return self.outputStr, self.maskoutput

    ###################
    # Range Application
    ###################
    def LightFilter(self):
        #Sets image to hsv colors
        hsv = cv2.cvtColor(self.inimg, cv2.COLOR_RGB2HSV)

        #Creates mask based on hsv range
        self.mask = cv2.inRange(hsv, self.lowerBrightness, self.upperBrightness)

        #Makes an image using the mask if streaming
        if self.debug:
            self.maskoutput = cv2.cvtColor(self.mask, cv2.COLOR_GRAY2BGR)

    ###################
    # Target Detection
    ###################

    def TargetDetection(self,contourinput):

        self.ret="F"
        self.angle=0
        self.angle1=0
        self.angle2=0
        self.yaw=0
        self.xval=0
        self.yval=0

        if contourinput:
            #get the largest contour
            polygon = sorted(contourinput, key=cv2.contourArea, reverse=True)[0]

            #Experimental Subpixel stuff
            CornerIDs=self.cornerFinder(polygon)
            corners=np.float32([polygon[CornerIDs[0]],polygon[CornerIDs[1]],polygon[CornerIDs[2]],polygon[CornerIDs[3]]])
            criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 30, 0.1)
            SC = cv2.cornerSubPix(self.mask, corners, (5,5), (-1,-1), criteria) 
            
            #Draws line between pairs if streaming
            if self.debug:
                cv2.circle(self.maskoutput,tuple(polygon[CornerIDs[0]][0]),10,[255,0,0])
                cv2.circle(self.maskoutput,tuple(polygon[CornerIDs[1]][0]),10,[0,255,0])
                cv2.circle(self.maskoutput,tuple(polygon[CornerIDs[2]][0]),10,[0,0,255])
                cv2.circle(self.maskoutput,tuple(polygon[CornerIDs[3]][0]),10,[128,128,128])
                cv2.circle(self.maskoutput,tuple(SC[0][0]),10,[200,0,0])
                cv2.circle(self.maskoutput,tuple(SC[1][0]),10,[0,200,0])
                cv2.circle(self.maskoutput,tuple(SC[2][0]),10,[0,0,200])
                cv2.circle(self.maskoutput,tuple(SC[3][0]),10,[100,100,100])
                
            #Gets bottom, leftmost, topmost, and rightmost points of each contour
            #ImgPoints=np.array([approx[cornerIDs[0]],innerCornerList[innerCornerIDs[0]],innerCornerList[innerCornerIDs[3]],innerCornerList[innerCornerIDs[2]],innerCornerList[innerCornerIDs[1]],approx[cornerIDs[1]],approx[cornerIDs[2]],approx[cornerIDs[3]]], dtype="double")
            
            ImgPoints=np.array([polygon[CornerIDs[0]],polygon[CornerIDs[1]],polygon[CornerIDs[2]],polygon[CornerIDs[3]]], dtype="double")
            #Actual Pnp algorithm, takes the points we calculated along with predefined points of target
            # ____,rvec,tvec= cv2.solvePnP(self.ObjPoints,ImgPoints,self.camMatrix,self.distCoeffs)
                
            
            # #othermatrix
            # rvecmat=cv2.Rodrigues(rvec)[0]
            # rvecmatinv=rvecmat.transpose()
            # pzero_world=np.array(np.matmul(rvecmatinv,-tvec))
            # self.angle2=math.atan2(pzero_world[0][0],pzero_world[2][0])
            # ##Chooses the values we need for path planning
            # distance = math.sqrt(tvec[0]**2 + tvec[2]**2)
            # ##self.angle=(((lcoordflt[0]+rcoordflt[0])/2)-(self.w/2))/20
            # self.angle1=(math.atan2(tvec[0], tvec[2]))
            # self.ret="T"
            # self.yaw=(str(rvec[2]).strip('[]'))
            # self.xval=(str(tvec[2]).strip('[]'))
            # self.yval=(str(tvec[0]).strip('[]'))
            
            # self.angle=((self.findCentroid([polygon]))-(self.w/2))/20



    ###################
    # Target Detection
    ###################

    @staticmethod
    def filtercontours(input_contours, min_area, min_width, max_width):
        output = []
        for contour in input_contours:
            x,y,w,h = cv2.boundingRect(contour)
            if (w < min_width or w>max_width):
                continue
            area = cv2.contourArea(contour)
            if (area < min_area):
                continue
            output.append(contour)
        return output

    @staticmethod
    def cornerFinder(polygon):
        top_left = (polygon[:, :, 0] + polygon[:, :, 1]).argmin()
        bottom_left = (polygon[:, :, 0] - polygon[:, :, 1]).argmin()
        top_right = (polygon[:, :, 0] - polygon[:, :, 1]).argmax()
        bottom_right = (polygon[:, :, 0] + polygon[:, :, 1]).argmax()
        return [top_left,top_right,bottom_right,bottom_left]
        


##############################################################################
# END OF YEARLY CODE
##############################################################################

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
    capture.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
    capture.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
    capture.set(cv2.CAP_PROP_SATURATION,0.2)

    
    try:
        #server = ThreadedHTTPServer(('10.17.36.10', 5805), CamHandler)
        server = ThreadedHTTPServer(('frcvision.local', 5805), CamHandler)
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
    Processor=VisionProcessor()
    Processor.init()
    calibrator=CameraParamGetter()

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
            
            calibrator.calibrateToChess(cam_img)
            consoleOutput, img = Processor.process(cam_img)
            print(consoleOutput)
            #TODO - image processing here

        proc_end_time = time.time_ns()/(10 ** 9)
        proc_time = proc_end_time - capture_time
        ntTable.putNumber("proc_duration_sec", proc_time)
        ntTable.putNumber("framerate_fps", 1.0/(capture_time - prev_cap_time))
        time.sleep(0)
        


