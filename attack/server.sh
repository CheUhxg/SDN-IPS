#!/bin/bash
# 服务器监听80端口，存储收包结果

cd server

if=`ifconfig | grep eth0`
iface=${if:0:7}

ip=`ifconfig | grep 'inet '`
ipv4=${ip:13:8}

pid=`ps -ef | grep receivePacket.py | awk '{print $2}'`
kill ${pid[0]}

python savePacket.py $iface &
python receivePacket.py $ipv4 &

wait