#!/usr/bin/env python
# encoding: utf-8
from flask_restful import reqparse, Resource,request
from jox_EEG import jox_GPIO

gpio = jox_GPIO.GPIO()
gpio.start()


class GPIORoute(Resource):
    def get(self):
        try:
            self.parser = reqparse.RequestParser()
            self.parser.add_argument('oper', type=str, help='status: type is str')
            self.args = self.parser.parse_args()
            self.oper = int(self.args['oper'])
            if self.oper == "start":
                gpio.jox_start("LED")
            elif self.oper == "stop":
                gpio.jox_stop("LED")
            elif self.oper == "startr":
                oper=""
                gpio.jox_start("RED")
            elif self.oper == "startg":
                gpio.jox_start("GREEN")
            elif self.oper == "startb":
                gpio.jox_start("BULE")
            elif self.oper == "stoprgb":
                gpio.jox_stop("RGB")
            elif self.oper == "exit":
                gpio.jox_exit()
	    

        except Exception as e:
            return "erro"



