#!/usr/bin/env python
# encoding: utf-8

import jox_GPIO

gpio =jox_GPIO.GPIO()

gpio.start()

oper = ""
while oper!="exit":
    oper = input("qing shu ru caozuo:")
    if oper == "start":
        oper=""
        gpio.jox_start("LED")
    elif oper == "stop":
        oper=""
        gpio.jox_stop("LED")
    elif oper == "startr":
        oper=""
        gpio.jox_start("RED")
    elif oper == "startg":
        oper=""
        gpio.jox_start("GREEN")
    elif oper == "startb":
        oper=""
        gpio.jox_start("BULE")
    elif oper == "stoprgb":
        oper=""
        gpio.jox_stop("RGB")
    elif oper == "exit":
        gpio.jox_exit()
        break
    else:
        print("wu xiao zhi ling")
        
