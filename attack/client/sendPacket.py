from time import sleep
import requests
import sys
import re
from threading import Thread

targetIP = sys.argv[1]
filename = sys.argv[2]

def sendHttp(pkt):
    reqLine = pkt[0].split(' ')
    method = reqLine[0]
    url    = reqLine[1]
    txtIP = re.findall(r"http://(.*?)/", url)[0]
    url = url.replace(txtIP, targetIP)
    headers = {}
    for i in range(1,len(pkt)):
        # 只加入头部分
        if ': ' not in pkt[i]:
            break
        # 拆分键值对
        kv = pkt[i].split(': ')
        headers[kv[0]] = kv[1][:-1]
    headers['Host'] = targetIP
    try:
        if method in 'GET':     # GET请求
            res = requests.get(url=url, headers=headers, timeout=0.1)
        if method in 'POST':    # POST请求
            dataLine = pkt[-2][:-1].split('&')
            # 定义POST传的参数
            data = {}
            for kv in dataLine:
                key   = kv.split('=')[0]    # 键
                value = kv.split('=')[1]    # 值
                data[key] = value
            res = requests.post(url=url, headers=headers, data=data, timeout=0.1)
    except:
        pass

def main():
    # 从抓好的HTTP包中提取每个请求的报文内容
    with open(filename, 'r') as fd:
        txt = fd.readlines()
    start = 0
    end = 0
    for i in range(1, len(txt)):
        method = txt[i][:4]
        # 分解成单个请请求进行发送
        if 'GET'in method or 'POST'in method or 'FIN' in method:
            end = i
            sendHttp(txt[start:end])
            # thrd = Thread(target=sendHttp, args=(txt[start:end]))
            # thrd.start()
            start = end

if __name__ == '__main__':
    for i in range(3):
        main()
        sleep(1)