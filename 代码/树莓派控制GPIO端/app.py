#!/usr/bin/env python
# encoding: utf-8
from flask import Flask,send_file
from flask_restful import reqparse, Resource,request,Api
from EEGApi import GPIO
import urllib2
import threading
import socket,time,SocketServer,struct,os,thread

app = Flask(__name__)
api = Api(app)


gpio = EEGApi.GPIO()
gpio.start()


class GPIORoute(Resource):
    def get(self):
        try:
            self.parser = reqparse.RequestParser()
            self.parser.add_argument('oper', type=str, help='status: type is str')
            self.args = self.parser.parse_args()
            self.oper = self.args['oper']
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
            elif self.oper == "stopall":
                gpio.jox_stop("LED")
                gpio.jox_start("RED")
            elif self.oper == "exit":
                gpio.jox_exit()

        except Exception as e:
            print(e)
            return "erro"



api.add_resource(GPIORoute, '/gpio')


if __name__ == '__main__':

    app.run(host='127.0.0.1',port=8081,threaded=True)
