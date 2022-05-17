#!/bin/bash
# 检测器监听eth0网卡，解析接收到的报文，生成result.json文件

cd detecter

if=`ifconfig | grep eth0`
iface=${if:0:7}

pid=`ps -ef | grep receivePacket.py | awk '{print $2}'`
kill ${pid[0]}

python collectPacket.py $iface