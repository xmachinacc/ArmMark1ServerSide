# -*- coding: utf-8 -*-
"""
Created on Tue Aug 09 10:33:03 2016

@author: trist
"""
import SimpleCV
import socket
import cv2
import cv
import numpy as np

def connectToServerAndHandleConnection():
    
    HOST = 'localhost'
    PORT = 9696
    
    while True:
        #try:
            
            sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
            sock.connect((HOST,PORT))
    
            img_str = sock.recv(100000)
            
            nparr = np.fromstring(img_str, np.uint8)
            img_np = cv2.imdecode(nparr, cv2.CV_LOAD_IMAGE_COLOR) # cv2.IMREAD_COLOR in OpenCV 3.1
            
            #img_ipl = cv.CreateImageHeader((img_np.shape[1], img_np.shape[0]), cv.IPL_DEPTH_8U, 3)
            #cv.SetData(img_ipl, img_np.tostring(), img_np.dtype.itemsize * 3 * img_np.shape[1])
            
            #######
            gray = cv2.cvtColor(img_np, cv2.COLOR_BGR2GRAY)
            circles = cv2.HoughCircles(gray, cv2.cv.CV_HOUGH_GRADIENT, 1.2, 100)
            stringOut = '[]\n'
            if circles != None:
                    stringOut = ''
                    for circle in circles:
                        circle = circle[0]
                        stringOut += str([int(circle[0]),int(circle[1]),int(circle[2])]) + ';'
                    stringOut = stringOut[:-1]
                    stringOut += '\n'
            sock.send(stringOut)
            
            '''
            image = SimpleCV.Image(img_ipl, verbose = False)
            
            #dist = image.colorDistance(SimpleCV.Color.BLACK).dilate(5)
            #segmented = dist.stretch(50,150)
            #blobs = segmented.findBlobs()
            blobs = image.findBlobs()
            
            stringOut = '[]\n'
            
            if blobs:
            
                circles = blobs.filter([blob.isCircle(0.8) for blob in blobs])
                
                if circles:
                    stringOut = ''
                    for circle in circles:
                        stringOut += str([circle.x,circle.y,int(circle.radius())]) + ';'
                    stringOut = stringOut[:-1]
                    stringOut += '\n'
                
            
            sock.send(stringOut)
            '''
            
        #except:
            #continue
        
connectToServerAndHandleConnection()
