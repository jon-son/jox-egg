import serial
import matplotlib.pyplot as plt
import numpy as np
import time
import re
import random

plt.grid(True)  # 添加网格
plt.ion()  # interactive mode
plt.figure(1)
plt.xlabel('times')
plt.ylabel('data')
plt.title('EEG for jox')
t = [0]
m = [0]
i = 0
intdata = 0

count = 0

while True:
    i = i + 1
    t.append(i)
    x =random.randint(10,20)
    m.append(x)
    plt.plot(t, m, '-r')
    plt.scatter(i, x)
    plt.draw()
    time.sleep(1)
    plt.pause(0.002)
