
# -*- coding: utf-8 -*-
import serial
import re
import time
import jox_GPIO

gpio =jox_GPIO.GPIO()

gpio.start()


ser=serial.Serial("/dev/ttyUSB0",9600,timeout=1)
 
ser.write("AT+INQ\r\n".encode())
tmp=ser.readline()
print(tmp)
if tmp==b'OK':
    gpio.jox_start("RED")

while 1:
    tmp=ser.readline()
    print(tmp)





PYTHOprint(tmp)
