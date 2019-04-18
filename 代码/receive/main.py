# -*- coding: utf-8 -*-
import serial
import requests
import matplotlib.pyplot as plt

filename = 'jox1.txt'
t = serial.Serial('COM9', 57600)
b = t.read(3)
status = 0
temp = 0
vaul = []
i = 0
y = 0
p = 0
joxt = [0]
joxm = [0]
joxm2 = [0]
joxi = 0
is_open = False
plt.ion()  # interactive mode
plt.figure(figsize=(8, 6), dpi=80)
print("搜索脑电波设备中")
bluetooth = 0
try:
    while b[0] != 170 or b[1] != 170 \
            or b[2] != 4:
        b = t.read(3)
        if bluetooth % 2 == 0:
            requests.get("http://192.168.0.111/gpio", params={"oper": "startr"})
        else:
            requests.get("http://192.168.0.111/gpio", params={"oper": "stoprgb"})
        bluetooth = bluetooth + 1

    if b[0] == b[1] == 170 and b[2] == 4:
        print("连接成功。")
        requests.get("http://192.168.0.111/gpio", params={"oper": "startb"})
        a = b + t.read(5)
        if a[0] == 170 and a[1] == 170 and a[2] == 4 and a[3] == 128 and a[4] == 2:
            while 1:
                try:
                    i = i + 1
                    #            print(i)
                    a = t.read(8)
                    #            print(a)
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
                        # print("wrroy1")
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

                    #            if a[0]==a[1]==170 and a[2]==4:
                    #    print(type(a))

                    if a[0] == 170 and a[1] == 170 and a[2] == 4 and a[3] == 128 and a[4] == 2:

                        high = a[5]
                        low = a[6]
                        #                print(a)
                        rawdata = (high << 8) | low
                        if rawdata > 32768:
                            rawdata = rawdata - 65536
                        #                vaul.append(rawdata)
                        sum = ((0x80 + 0x02 + high + low) ^ 0xffffffff) & 0xff
                        if sum == a[7]:
                            vaul.append(rawdata)
                        if sum != a[7]:
                            # print("wrroy2")
                            b = t.read(3)
                            c = b[0]
                            d = b[1]
                            e = b[2]
                            #                    print(b)
                            while c != 170 or d != 170 or e != 4:
                                c = d
                                d = e
                                e = t.read()
                                if c == b'\xaa' and d == b'\xaa' and e == b'\x04':
                                    g = t.read(5)
                                    if c == b'\xaa' and d == b'\xaa' and e == b'\x04' and g[0] == 128 and g[1] == 2:
                                        a = t.read(8)
                                        break
                    if a[0] == a[1] == 170 and a[2] == 32:
                        c = a + t.read(28)
                        # if c[32] >80:
                        if joxi > 20:  # 20次数据后，图像向后推移
                            joxt = joxt[-20:]
                            joxm = joxm[-20:]
                            joxm2 = joxm2[-20:]
                            plt.cla()
                        plt.grid(True)  # 添加网格
                        plt.xlabel('times')
                        plt.ylabel('data')
                        plt.title('EEG for jox')
                        joxi = joxi + 1
                        joxt.append(i)
                        joxm.append(c[32])
                        joxm2.append(c[34])

                        plt.plot(joxt, joxm, "bo-", linewidth=2.0, label="attention:" + str(c[32]))
                        plt.plot(joxt, joxm2, "g-.", linewidth=2.0, label="meditation:" + str(c[34]))
                        plt.legend(loc="upper left", shadow=True)
                        plt.show()



                        if c[32] >5 and not is_open:
                            print("开启灯成功")
                            requests.get("http://192.168.0.111/gpio", params={"oper": "startg"})
                            requests.get("http://192.168.0.111/gpio", params={"oper": "start"})
                            is_open = True


                        for v in vaul:
                            w = 0
                            if v <= 102:
                                w += v
                                q = w / len(vaul)
                                q = " v <= 102 "+ str(q)
                                with open(filename, 'a') as file_object:
                                    file_object.write(q)
                                    file_object.write("\n")
                            if 102 < v <= 204:
                                w += v
                                q = w / len(vaul)
                                q = "102 < v <= 204 "+ str(q)
                                with open(filename, 'a') as file_object:
                                    file_object.write(q)
                                    file_object.write("\n")
                            if 204 < v <= 306:
                                w += v
                                q = w / len(vaul)
                                q ="204 < v <= 306 " + str(q)
                                with open(filename, 'a') as file_object:
                                    file_object.write(q)
                                    file_object.write("\n")
                            if 306 < v <= 408:
                                w += v
                                q = w / len(vaul)
                                q = "306 < v <= 408 " + str(q)
                                with open(filename, 'a') as file_object:
                                    file_object.write(q)
                                    file_object.write("\n")
                            if 408 < v <= 510:
                                w += v
                                q = w / len(vaul)
                                q = "408 < v <= 510 "+ str(q)
                                with open(filename, 'a') as file_object:
                                    file_object.write(q)
                                    file_object.write("\n")
                        #                print(c)
                        vaul = []
                except Exception as e:
                    # print(e)
                    sse=1

except Exception as e:
    sse = 1
