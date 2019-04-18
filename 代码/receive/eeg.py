import matplotlib.pyplot as plt
import time
import requests

bluetooth = 0
print("搜索脑电波设备中")
while True:
    if bluetooth % 2 == 0:
        requests.get("http://192.168.0.111/gpio", params={"oper": "startr"})
    else:
        requests.get("http://192.168.0.111/gpio", params={"oper": "stoprgb"})
    bluetooth = bluetooth + 1
    time.sleep(1)
    if bluetooth == 5:
        print("连接中。。。")
        requests.get("http://192.168.0.111/gpio", params={"oper": "startr"})
        time.sleep(2)
        break
print("连接成功。")





f = open("jox.txt","r+")
fl = f.readlines()

plt.ion()  # interactive mode
plt.figure(figsize=(8, 6), dpi=80)

t = [0]
m = [0]
m2 = [0]
i = 0
is_open = False

requests.get("http://192.168.0.111/gpio", params={"oper": "startb"})
for line in fl:
    line = line.strip("\n")
    attention = int(line.split(",")[0])
    meditation = int(line.split(",")[1])
    if i > 20:  # 20次数据后，图像向后推移
        t = t[-20:]
        m = m[-20:]
        m2 = m2[-20:]
        plt.cla()
    plt.grid(True)  # 添加网格
    plt.xlabel('times')
    plt.ylabel('data')
    plt.title('EEG for jox')


    i = i + 1
    t.append(i)
    m.append(attention)
    m2.append(meditation)
    if attention>85 and not is_open:
        requests.get("http://192.168.0.111/gpio", params={"oper": "startg"})
        requests.get("http://192.168.0.111/gpio",params={"oper": "start"})
        is_open =True


    plt.plot(t, m, "bo-", linewidth=2.0, label="attention:"+str(attention))
    plt.plot(t, m2, "g-.", linewidth=2.0, label="meditation:"+str(meditation))
    plt.legend(loc="upper left",  shadow=True)
    plt.show()
    time.sleep(1)
    plt.pause(0.002)
