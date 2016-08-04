from SimpleCV import Image
import socket
import cv2
import cv
import numpy as np
import sys

def connectToServerAndHandleConnection():
    
    HOST = 'localhost'
    PORT = 9898
    
    while True:
        try:
            
            sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
            sock.connect((HOST,PORT))
        
            img_str = sock.recv(100000)
            
            nparr = np.fromstring(img_str, np.uint8)
            img_np = cv2.imdecode(nparr, cv2.CV_LOAD_IMAGE_COLOR) # cv2.IMREAD_COLOR in OpenCV 3.1
            
            img_ipl = cv.CreateImageHeader((img_np.shape[1], img_np.shape[0]), cv.IPL_DEPTH_8U, 3)
            cv.SetData(img_ipl, img_np.tostring(), img_np.dtype.itemsize * 3 * img_np.shape[1])
            
            image = Image(img_ipl)
            barcodes = image.findBarcode()
            stringOut = '[]\n'
            if barcodes != None:
                stringOut = ''
                for barcode in barcodes:
                    stringOut += str([barcode.x,barcode.y,int(barcode.length()), int(barcode.width()), barcode.data]) + ';'
                stringOut = stringOut[:-1]
                stringOut += '\n'
            sock.send(stringOut)
            
        except:
            continue
        
connectToServerAndHandleConnection()

    
