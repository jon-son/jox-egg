#!/usr/bin/env python
# encoding: utf-8

import RPi.GPIO
import time
from threading import Thread,Event


class GPIO(Thread):
    def __init__(self):
        Thread.__init__(self)
        self.LED = 15
        self.SLEDR,self.SLEDG,self.SLEDB =13,19,26


    def jox_start(self,jox_type):
        print(jox_type+":start")
  
        RPi.GPIO.setmode(RPi.GPIO.BCM)
        if jox_type == "LED":
            self.led_exit = Event()
            RPi.GPIO.setup(self.LED, RPi.GPIO.OUT)

            self.pwm = RPi.GPIO.PWM(self.LED, 70)
            self.pwm.start(0)
            self.pwm.ChangeDutyCycle(100)
        else:

            RPi.GPIO.setup(self.SLEDR, RPi.GPIO.OUT)
            RPi.GPIO.setup(self.SLEDG, RPi.GPIO.OUT)
            RPi.GPIO.setup(self.SLEDB, RPi.GPIO.OUT)

            self.pwmR = RPi.GPIO.PWM(self.SLEDR, 70)
            self.pwmG = RPi.GPIO.PWM(self.SLEDG, 70)
            self.pwmB = RPi.GPIO.PWM(self.SLEDB, 70)
            self.pwmR.start(0)
            self.pwmG.start(0)
            self.pwmB.start(0)
            if jox_type == "RED":
                self.pwmR.ChangeDutyCycle(100)
                self.pwmG.ChangeDutyCycle(0)
                self.pwmB.ChangeDutyCycle(0)
            elif jox_type == "GREEN":
                self.pwmR.ChangeDutyCycle(0)
                self.pwmG.ChangeDutyCycle(100)
                self.pwmB.ChangeDutyCycle(0)

            elif jox_type == "BULE":
                self.pwmR.ChangeDutyCycle(0)
                self.pwmG.ChangeDutyCycle(0)
                self.pwmB.ChangeDutyCycle(100)

        
            
    def jox_stop(self,jox_type):
        print(jox_type+":stop")
        if jox_type == "LED":
            self.pwm.ChangeDutyCycle(0)
        elif jox_type =="RGB":
            self.pwmR.ChangeDutyCycle(0)
            self.pwmG.ChangeDutyCycle(0)
            self.pwmB.ChangeDutyCycle(0)

    def jox_exit(self):
        RPi.GPIO.cleanup()
