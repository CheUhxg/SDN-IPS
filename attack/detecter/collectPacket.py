#!/usr/bin/python3
# -*- coding: UTF-8 -*-

from scapy.all import *
from scapy.layers.http import *
from threading import Thread
from shutil import copyfile
from DL_Traffic_Detector.train import train
import json

global COUNT,BUFSIZE,RESULT
BUFSIZE = 50            # 每次检测的包数
COUNT = 0               # 当前缓冲区包数
RESULT = []

def CallBack(packet):
    global COUNT,BUFSIZE,RESULT
    if packet.haslayer('HTTP Request'):
        print('[+] Collecting Packet'+str(COUNT))
        pkt = {}
        # pkt['smac'] = packet.src                       #添加源MAC
        # pkt['dmac'] = packet.dst                       #添加目的MAC
        pkt['sip'] = packet.payload.src                #添加源IP
        pkt['dip'] = packet.payload.dst                #添加目的IP
        # pkt['sport'] = packet.payload.payload.sport    #添加源PORT
        # pkt['dport'] = packet.payload.payload.dport    #添加目的PORT
    
        http = packet.payload.payload.payload   # 获得http报文内容，此时为字节流
        txt = str(bytes(http['HTTP Request']), encoding="utf-8").split('\r\n')  # 将http字符流转化为字符串数组
        with open('input.txt', 'a') as fd:
            for line in txt:
                if 'threat' in line:
                    pkt['threat'] = line.split(': ')[1]
                fd.write(line+'\n')
        COUNT += 1
        RESULT.append(pkt)
    if COUNT == BUFSIZE:
        # 清空缓冲区，并且为检测提供资源
        COUNT = 0
        copyfile('input.txt', 'train.txt')
        cl = open('input.txt', 'w')
        cl.truncate()
        cl.close()
        # 将收集到的头部信息保存到json中
        with open('result.json', 'w') as rs:
            json.dump(RESULT, rs, indent=4)
        # 使用线程开启检测，train.txt为检测所需的文本
        thrd = Thread(target=train.main())
        thrd.start()

def main():
    iface = sys.argv[1]
    sniff(prn=CallBack, iface=iface, count=0)  #iface为要监听的端口，count=0表示无限抓包

if __name__ == '__main__':
    main()