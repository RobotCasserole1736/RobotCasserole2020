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
import math
import cv2
import picamera

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
# Yearly Code
##############################################################################
class VisionProcessor():

    ###################
    #Init
    ###################
    def init(self):
        self.debug=True
        self.camMatrix=np.array([[2.29925773e+03,0.00000000e+00,9.99462308e+02], [0.00000000e+00,2.25914533e+03,5.51498851e+02], [0.00000000e+00,0.00000000e+00,1.00000000e+00]])
        self.distCoeffs=np.array([[-4.15580768e-01,-4.63818812e-01,-4.28854580e-03,-5.07851815e-04,1.61811656e+00]])

        #Points of the Trapezoid
        # self.ObjPoints=np.array([
        #                     (0,0,0),
        #                     (2,0,0),
        #                     (11.1248,-15,0),
        #                     (28.1252,-15,0),
        #                     (37.25,0,0),
        #                     (39.25,0,0),
        #                     (29.4448,-17,0),
        #                     (9.8051,-17,0)
        #                     ],dtype=np.float64)

        self.ObjPoints=np.array([
                            (0,0,0),
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
        self.updateTable()
        if(self.debug):
            self.outputStr="{{{},{},{},{},{},{}}}\n".format(self.ret,self.angle,self.angle1,self.angle2,self.xval,self.yval)
            print(self.outputStr)
        return self.maskoutput

    ###################
    # Main Process
    ###################
    def updateTable(self):
        ntTable.putNumber("targetAngle_deg",self.angle1)
        ntTable.putNumber("targetVisible",self.ret)
        #TODO add stabilization
        ntTable.putNumber("targetPosStable",True)


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

        self.ret=False
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
            ____,rvec,tvec= cv2.solvePnP(self.ObjPoints,ImgPoints,self.camMatrix,self.distCoeffs)
                
            
            #othermatrix
            rvecmat=cv2.Rodrigues(rvec)[0]
            rvecmatinv=rvecmat.transpose()
            pzero_world=np.array(np.matmul(rvecmatinv,-tvec))
            self.angle2=math.atan2(pzero_world[0][0],pzero_world[2][0])
            ##Chooses the values we need for path planning
            distance = math.sqrt(tvec[0]**2 + tvec[2]**2)
            self.angle1=(math.atan2(tvec[0], tvec[2]))
            self.ret=True
            self.yaw=(str(rvec[2]).strip('[]'))
            self.xval=(str(tvec[2]).strip('[]'))
            self.yval=(str(tvec[0]).strip('[]'))


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

class cameraSettings():
    def __init__(self):
        camera=picamera.PiCamera()
        print(camera._get_camera_settings())
        camera.close()




if __name__ == "__main__":
    global img
    img = None

    print("Casserole Vision Processing starting")
    print("OpenCV Version: {}".format(cv2.__version__))
    print("numpy Version: {}".format(np.__version__))

    doop = cameraSettings()

    # start NetworkTables
    ntinst = NetworkTablesInstance.getDefault()
    print("Setting up NetworkTables client for team {}".format(1736))
    ntinst.startClientTeam(1736)


    
    capture = cv2.VideoCapture(0)
    capture.set(cv2.CAP_PROP_FRAME_WIDTH, 1920)
    capture.set(cv2.CAP_PROP_FRAME_HEIGHT, 1080)
    capture.set(cv2.CAP_PROP_FPS, 30)
    capture.set(cv2.CAP_PROP_EXPOSURE,.0005)
    capture.set(cv2.CAP_PROP_CONTRAST,1)
    capture.set(cv2.CAP_PROP_SATURATION,1)

    
    try:
        #server = ThreadedHTTPServer(('10.17.36.11', 5805), CamHandler)
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
            img = Processor.process(cam_img)

        proc_end_time = time.time_ns()/(10 ** 9)
        proc_time = proc_end_time - capture_time
        ntTable.putNumber("proc_duration_sec", proc_time)
        ntTable.putNumber("framerate_fps", 1.0/(capture_time - prev_cap_time))
        time.sleep(0)
        


