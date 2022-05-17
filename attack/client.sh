#!/bin/bash
# 客户端访问服务器，发送HTTP请求

if [ $# -le 0 ]
then
    echo "Usage: %prog <target ip>"
    exit
fi

cd client

python sendPacket.py $1 normal.txt