# -*- coding: utf-8 -*-
"""
This example demonstrates many of the 2D plotting capabilities
in pyqtgraph. All of the plots may be panned/scaled by dragging with
the left/right mouse buttons. Right click on any plot to show a context menu.
"""


from pyqtgraph.Qt import QtGui, QtCore
import threading
import pyqtgraph as pg
import serial
import requests
import time
data = []
data2 = []
data3 = []
old_data = []

delta_data = []

class EEGThread(threading.Thread):

    def __init__(self, parent=None):
        super(EEGThread, self).__init__(parent)
        self.filename = 'jox.txt'
        self.ip = "http://192.168.0.113"
        self.com = "COM9"
        self.bps = 57600
        self.vaul = []
        self.is_open = False
        self.is_close = True
    def checkList(self,list,num):
        list_num = 0
        for i in list:
            if i > num:
                list_num += 1
        return list_num
    def checkEeg(self):
        old_num = 0
        delta_num = 0
        for old in old_data:
            if self.checkList(old,200)>5:
                old_num += 1

        delta_num =self.checkList(delta_data, 50000)

        if old_num > 3 and delta_num > 4:
            return True
        else:
            return False


    def run(self):
        global data,data2,data3,old_data,delta_data
        try:

            t = serial.Serial(self.com, self.bps)
            b = t.read(3)
            requests.get(self.ip + "/gpio", params={"oper": "startr"})
            print(str(time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time())))+"脑电波设备配对中")
            while b[0] != 170 or b[1] != 170 \
                    or b[2] != 4:
                b = t.read(3)

            if b[0] == b[1] == 170 and b[2] == 4:
                print(str(time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time())))+"配对成功。")
                requests.get(self.ip+"/gpio", params={"oper": "startb"})
                a = b + t.read(5)

                if a[0] == 170 and a[1] == 170 and a[2] == 4 and a[3] == 128 and a[4] == 2:
                    while 1:
                        try:

                            a = t.read(8)
                            sum = ((0x80 + 0x02 + a[5] + a[6]) ^ 0xffffffff) & 0xff
                            if a[0] == a[1] == 170 and a[2] == 32:
                                y = 1
                            else:
                                y = 0
                            if a[0] == 170 and a[1] == 170 and a[2] == 4 and a[3] == 128 and a[4] == 2:
                                p = 1
                            else:
                                p = 0
                            if sum != a[7] and y != 1 and p != 1:
                                b = t.read(3)
                                c = b[0]
                                d = b[1]
                                e = b[2]
                                while c != 170 or d != 170 or e != 4:
                                    c = d
                                    d = e
                                    e = t.read()

                                    if c == (b'\xaa' or 170) and d == (b'\xaa' or 170) and e == b'\x04':
                                        g = t.read(5)
                                        if c == b'\xaa' and d == b'\xaa' and e == b'\x04' and g[0] == 128 and g[1] == 2:
                                            a = t.read(8)
                                            break

                            if a[0] == 170 and a[1] == 170 and a[2] == 4 and a[3] == 128 and a[4] == 2:

                                high = a[5]
                                low = a[6]
                                rawdata = (high << 8) | low
                                if rawdata > 32768:
                                    rawdata = rawdata - 65536
                                sum = ((0x80 + 0x02 + high + low) ^ 0xffffffff) & 0xff
                                if sum == a[7]:
                                    self.vaul.append(rawdata)
                                if sum != a[7]:
                                    b = t.read(3)
                                    c = b[0]
                                    d = b[1]
                                    e = b[2]
                                    while c != 170 or d != 170 or e != 4:
                                        c = d
                                        d = e
                                        e = t.read()
                                        if c == b'\xaa' and d == b'\xaa' and e == b'\x04':
                                            g = t.read(5)
                                            if c == b'\xaa' and d == b'\xaa' and e == b'\x04' and g[0] == 128 and g[
                                                1] == 2:
                                                a = t.read(8)
                                                break
                            if a[0] == a[1] == 170 and a[2] == 32:
                                c = a + t.read(28)
                                delta = (c[7] << 16) | (c[8] << 8) | (c[9])
                                # print(delta)

                                data = self.vaul

                                old_data.append(data)
                                if len(old_data) > 10:
                                    old_data = old_data[-10:]

                                delta_data.append(delta)
                                if len(delta_data) > 10:
                                    delta_data = delta_data[-10:]

                                flag = self.checkEeg()
                                data2.append(c[32])

                                if len(data2) > 20:
                                    data2 = data2[-20:]

                                data3.append(c[34])

                                if len(data3) > 20:
                                    data3 = data3[-20:]

                                if self.is_open and flag and not self.is_close:
                                    print(str(time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time())))+"关闭灯成功")
                                    requests.get(self.ip+"/gpio", params={"oper": "stopall"})
                                    # requests.get(self.ip + "/gpio", params={"oper": "startr"})
                                    self.is_close = True
                                    self.is_open = False

                                if c[32] > 70 and not self.is_open and self.is_close and not flag:
                                    print(str(time.strftime('%Y-%m-%d %H:%M:%S',time.localtime(time.time())))+"开启灯成功")
                                    requests.get(self.ip+"/gpio", params={"oper": "startg"})
                                    requests.get(self.ip+"/gpio", params={"oper": "start"})
                                    self.is_open = True
                                    self.is_close = False

                                self.vaul = []
                        except Exception as e:
                            # print(e)
                            sse =1

        except Exception as e:
            # print(e)
            sse = 1


class ShowThread(threading.Thread):
    def __init__(self, parent=None):
        super(ShowThread, self).__init__(parent)
        self.is_started = threading.Event()
        self.win = pg.GraphicsWindow(title="脑电波")
        self.win.resize(1000, 600)
        self.win.setWindowTitle('脑电波检测值')
        pg.setConfigOptions(antialias=True)
        self.p2 = self.win.addPlot(title="专注值(蓝色)/放松值(绿色)")

        self.p6 = self.win.addPlot(title="脑电波值")
        self.curve = self.p6.plot(pen='y')
        self.curve2 = self.p2.plot(pen=(0, 255, 0), name="放松值")
        self.curve3 = self.p2.plot(pen=(0, 0, 255), name="专注值")


        self.ptr = 0
        self.ptr2 = 0

    def run(self):
        while True:
            self.curve.setData(data)
            self.curve2.setData(data2)
            self.curve3.setData(data3)
            self.is_started.wait(timeout=0.2)


## Start Qt event loop unless running in interactive mode or using pyside.
if __name__ == '__main__':
    # if (sys.flags.interactive != 1) or not hasattr(QtCore, 'PYQT_VERSION'):
    eeg = EEGThread()
    eeg.start()
    show = ShowThread()
    show.start()
    QtGui.QApplication.instance().exec_()


