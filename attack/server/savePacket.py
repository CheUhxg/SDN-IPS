#!/usr/bin/python3
# -*- coding: UTF-8 -*-

from scapy.all import *
from scapy.layers.http import *

def CallBack(packet):
    if packet.haslayer('HTTP Request'):    
        http = packet.payload.payload.payload   # 获得http报文内容，此时为字节流
        txt = str(bytes(http['HTTP Request']), encoding="utf-8").split('\r\n')  # 将http字符流转化为字符串数组
        with open('result.txt', 'a') as fd:
            for line in txt:
                fd.write(line+'\n')

def main():
    iface = sys.argv[1]
    print('Start Listening : '+iface)
    sniff(prn=CallBack, iface=iface, count=0)  #iface为要监听的端口，count=0表示无限抓包

if __name__ == '__main__':
    main()