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
import os

from PIL import Image

from networktables import *
import ntcore


import threading
from http.server import BaseHTTPRequestHandler, HTTPServer
from socketserver import ThreadingMixIn

from io import StringIO ## for Python 3

class CamHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        global img
        if self.path.endswith('.mjpg'):
            self.send_response(200)
            self.send_header('Content-type', 'multipart/x-mixed-replace; boundary=--jpgboundary')
            self.end_headers()
            while True:
                try:
                    if img is not None:
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
            self.wfile.write('<img src="http://10.17.36.11:5805/cam.mjpg"/>')
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
        self.debug = True
        self.innerAim=False
        self.counter=0
        self.stableidx=0
        self.stableList=[None,None,None,None,None]
        #self.camMatrix = np.array([[2.29925773e+03,0.00000000e+00,9.99462308e+02], [0.00000000e+00,2.25914533e+03,5.51498851e+02], [0.00000000e+00,0.00000000e+00,1.00000000e+00]])
        self.camMatrix = np.array([[2306.29109, 0, 960], [0, 2305.12837, 540], [0, 0, 1]])
        #self.distCoeffs=np.array([[-4.15580768e-01,-4.63818812e-01,-4.28854580e-03,-5.07851815e-04,1.61811656e+00]])
        self.distCoeffs = np.array([[-0.51752, 0.29812, -0.01135, -0.00401, 0.00000]])
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

        # self.ObjPoints = np.array([
        #     (0, 0, 0),
        #     (39.25, 0, 0),
        #     (29.4448, -17, 0),
        #     (9.8051, -17, 0)
        #     ], dtype=np.float64)

        self.ObjPointsInner = np.array([
            (-19.625, 0, 24.25),
            (19.625, 0, 24.25),
            (9.8198, -17, 24.25),
            (-9.8198, -17, 24.25)
            ], dtype=np.float64)
        self.ObjPointsOuter = np.array([
            (-19.625, 0, 0),
            (19.625, 0, 0),
            (9.8198, -17, 0),
            (-9.8198, -17, 0)
            ], dtype=np.float64)
        self.stableTarget=False



        ###Ranges###

        ##Test Vision Range
        # self.lowerBrightness = np.array([0, 0, 75])
        # self.upperBrightness = np.array([255, 255, 255])

        ##I dont remember what these were for Range
        #lowerBrightness = np.array([70,0,150])
        #upperBrightness = np.array([100,200,255])

        ##Standard Green Vision Tape Range
        self.lowerBrightness=np.array([30,170,50])
        self.upperBrightness=np.array([90,255,190])



    ###################
    # Main Process
    ###################
    def process(self, inFrame):
        Timer=(time.time() * 1000)
        if(self.stableidx%20==0):
            self.innerAim=ntTable.getEntry("InnerAim").value
        self.inimg = inFrame
        self.LightFilter()
        _, contours, _ = cv2.findContours(self.mask, mode=cv2.RETR_LIST, method=cv2.CHAIN_APPROX_SIMPLE)
        filteredContours = self.filtercontours(contours, 100.0, 175.0, math.inf)
        self.TargetDetection(filteredContours)
        self.updateTable()
        return self.maskoutput

    ###################
    # Main Process
    ###################
    def updateTable(self):
        self.stability()
        if self.angle1 is None:
            self.angle1=0
        ntTable.putNumber("targetAngle_deg", self.angle1)
        ntTable.putNumber("targetVisible", self.ret)
        ntTable.putNumber("targetPosStable", self.stableTarget)
        if(self.stableidx%15==0):
            if(self.stableidx%30==0):
                ntTable.putNumber("Heartbeat", 0.0)
                print("Heartbeat")
            else:
                ntTable.putNumber("Heartbeat", 1.0)
                print("Heartbeat")
        self.outputStr = "{{{},{},{},{},{}}}".format(self.ret, self.angle1, self.xval, self.yval, self.innerAim)
        print(self.outputStr)
        self.stableidx += 1


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

    def TargetDetection(self, contourinput):

        self.ret = False
        self.angle1 = None
        self.xval = 0
        self.yval = 0

        if contourinput:
            #get the largest contour
            polygon = sorted(contourinput, key=cv2.contourArea, reverse=True)[0]

            #Experimental Subpixel stuff
            CornerIDs = self.cornerFinder(polygon)
            corners = np.float32([polygon[CornerIDs[0]],polygon[CornerIDs[1]],polygon[CornerIDs[2]],polygon[CornerIDs[3]]])

            #Draws line between pairs if streaming
            if self.debug:
                cv2.circle(self.maskoutput,tuple(polygon[CornerIDs[0]][0]),10,[255,0,0])
                cv2.circle(self.maskoutput,tuple(polygon[CornerIDs[1]][0]),10,[0,255,0])
                cv2.circle(self.maskoutput,tuple(polygon[CornerIDs[2]][0]),10,[0,0,255])
                cv2.circle(self.maskoutput,tuple(polygon[CornerIDs[3]][0]),10,[128,128,128])
                cv2.drawContours(self.maskoutput,[polygon], -1, (0,255,0), 3)


            ImgPoints = np.array([polygon[CornerIDs[0]], polygon[CornerIDs[1]], polygon[CornerIDs[2]], polygon[CornerIDs[3]]], dtype="double")
            #Actual Pnp algorithm, takes the points we calculated along with predefined points of target
            if(self.innerAim):
                _, rvec, tvec = cv2.solvePnP(self.ObjPointsInner, ImgPoints, self.camMatrix, self.distCoeffs)
            else:
                _, rvec, tvec = cv2.solvePnP(self.ObjPointsOuter, ImgPoints, self.camMatrix, self.distCoeffs)
            self.angle1 = -1*np.degrees((math.atan2(tvec[0], tvec[2])))
            self.ret = True
            self.xval = (str(tvec[2]).strip('[]'))
            self.yval = (str(tvec[0]).strip('[]'))


    ###################
    # Target Detection
    ###################
    def stability(self):
        self.stableList[self.stableidx%len(self.stableList)]=self.angle1
        if None not in self.stableList:
            if(max(self.stableList)-min(self.stableList)<1.2):
                self.stableTarget=True
            else:
                self.stableTarget=False
        else:
            self.stableTarget=False

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
        os.system("v4l2-ctl --set-fmt-video pixelformat=H264,width=1920,height=1080")
        cmdStart="v4l2-ctl --set-ctrl "
        #Turns off Auto setting that would change the ones we put in place
        os.system(cmdStart+"auto_exposure=1")
        os.system(cmdStart+"white_balance_auto_preset=0")

        #User Controls
        os.system(cmdStart+"brightness=30")
        os.system(cmdStart+"contrast=100")
        os.system(cmdStart+"saturation=10")
        os.system(cmdStart+"red_balance=1000")
        os.system(cmdStart+"blue_balance=1000")
        os.system(cmdStart+"horizontal_flip=0")
        os.system(cmdStart+"vertical_flip=0")
        os.system(cmdStart+"power_line_frequency=2")
        os.system(cmdStart+"sharpness=100")
        os.system(cmdStart+"color_effects=0")
        os.system(cmdStart+"rotate=0")
        os.system(cmdStart+"color_effects_cbcr=3896")

        #Codec Controls
        os.system(cmdStart+"video_bitrate_mode=1")
        os.system(cmdStart+"video_bitrate=25000000")
        os.system(cmdStart+"repeat_sequence_header=0")
        os.system(cmdStart+"h264_i_frame_period=60")
        os.system(cmdStart+"h264_level=11")
        os.system(cmdStart+"h264_profile=4")

        #Camera Controls
        os.system(cmdStart+"exposure_time_absolute=93")
        os.system(cmdStart+"exposure_dynamic_framerate=0")
        os.system(cmdStart+"auto_exposure_bias=0")
        os.system(cmdStart+"image_stabilization=0")
        os.system(cmdStart+"iso_sensitivity=0")
        os.system(cmdStart+"iso_sensitivity_auto=0")
        os.system(cmdStart+"exposure_metering_mode=0")
        os.system(cmdStart+"scene_mode=0")

        #JPEG Compression controls
        os.system(cmdStart+"compression_quality=100")

class WebcamVideoStream:
    def __init__(self, src=0):
        # initialize the video camera stream and read the first frame
        # from the stream
        self.stream = cv2.VideoCapture(src,cv2.CAP_V4L2)
        self.stream.set(cv2.CAP_PROP_FRAME_WIDTH, 1920)
        self.stream.set(cv2.CAP_PROP_FRAME_HEIGHT, 1080)
        self.stream.set(cv2.CAP_PROP_FPS, 30)
        self.stream.set(cv2.CAP_PROP_FOURCC,cv2.VideoWriter_fourcc("H","2","6","4"))
        (self.grabbed, self.frame) = self.stream.read()
        # initialize the variable used to indicate if the thread should
        # be stopped
        self.stopped = False
        self.start()
    def start(self):
        # start the thread to read frames from the video stream
        threading.Thread(target=self.update, args=()).start()
        return self
    def update(self):
        # keep looping infinitely until the thread is stopped
        while True:
            Timer=(time.time() * 1000)
            # if the thread indicator variable is set, stop the thread
            if self.stopped:
                return
            # otherwise, read the next frame from the stream
            (self.grabbed, self.frame) = self.stream.read()
            print("Image Capture Time: "+str((time.time() * 1000)-Timer))
    def read(self):
        # return the frame most recently read
        if(self.grabbed):
            self.grabbed=False
            return (True, self.frame)
        else:
            return (False, self.frame)
    def stop(self):
        # indicate that the thread should be stopped
        self.stopped = True


if __name__ == "__main__":
    global img
    img = None
    

    print("Casserole Vision Processing starting")
    print("OpenCV Version: {}".format(cv2.__version__))
    print("numpy Version: {}".format(np.__version__))
    cameraSettings()
    # start NetworkTables
    ntinst = NetworkTablesInstance.getDefault()
    print("Setting up NetworkTables client for team {}".format(1736))
    ntinst.startClientTeam(1736)
    


    cameraHandler=WebcamVideoStream()

    
    try:
        server = ThreadedHTTPServer(('10.17.36.11', 5805), CamHandler)
        #### #server = ThreadedHTTPServer(('frcvision.local', 5805), CamHandler)
        t = threading.Thread(target=server.serve_forever)
        t.start()
        print("MJPEG Server Started")
    except KeyboardInterrupt:
        #cameraHandler.stop()
        server.socket.close()

    ntTable = NetworkTables.getTable("VisionData")
    ntTable.putBoolean("InnerAim", True)


    prev_cap_time = 0
    capture_time = 0
    start_time = time.time_ns()/(10 ** 9)

    print("Vision Processing Starting..")
    Processor=VisionProcessor()
    Processor.init()
    ENDTIMEMS=0
    # loop forever
    while True:
        STARTTIMEMS=time.time() * 1000
        prev_cap_time = capture_time
        rc,cam_img = cameraHandler.read()
        if rc:
            capture_time = time.time_ns()/(10 ** 9)
            img = Processor.process(cv2.imdecode(np.frombuffer(cam_img,dtype=np.uint8),cv2.IMREAD_COLOR))
            # img = cam_img
            # 26 deg
            proc_end_time = time.time_ns()/(10 ** 9)
            proc_time = proc_end_time - capture_time
            ntTable.putNumber("proc_duration_sec", proc_time)
            ntTable.putNumber("framerate_fps", 1.0/(capture_time - prev_cap_time))
            time.sleep(0)
            print("Processing Time: "+str((time.time() * 1000)-STARTTIMEMS))
            print("Total Time: "+str((time.time() * 1000)-ENDTIMEMS))
            ENDTIMEMS=time.time() * 1000
