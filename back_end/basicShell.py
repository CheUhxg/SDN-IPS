from math import degrees
import requests
import json
import networkx as nx
from flask import request
from requests import auth
from requests.auth import HTTPBasicAuth
import time
import random


srcHost = ''
desHost = ''
path = []
time_now = 0
threatcount = {
    "XSS":0,
    "SQL":0,
    "FileScan":0,
    "CRLF":0,
    "SSI":0,
    "Unknown": 0
}
pknum_old = { }

controller_ip = "127.0.0.1"
auth = HTTPBasicAuth("karaf", "karaf")


# 获取时延信息
def get_delay():
    delay_url = "http://{}:8181/onos/get-link-delay/GetDelay".format(
        controller_ip)
    headers = {
        "Accept": "application/json"
    }

    resp = requests.get(url=delay_url, headers=headers, auth=auth)

    return resp.status_code, resp.text


# 获取主机和交换机关系的拓扑
def get_host():
    host_url = "http://{}:8181/onos/device-and-host/devicehost".format(
        controller_ip)
    headers = {
        "Accept": "application/json"
    }

    resp = requests.get(url=host_url, headers=headers, auth=auth)

    return resp.status_code, resp.text



# 获取热力图所需信息-每个交换机的包数量
def pack_num():
    counter_url = "http://{}:8181/onos/packet-counter/pktcounter".format(
        controller_ip)
    headers = {
        "Accept": "application/json"
    }

    resp = requests.get(url=counter_url, headers=headers, auth=auth)

    return resp.status_code, resp.text


# 生成前端所需的拓扑格式
def basicTopoDisplay():
    status1, de_resp = get_delay()
    status2, pk_resp = pack_num()
    status3, ho_resp = get_host()
    global pknum_old
    graph_show = {
        "nodes": [],
        "links": []
    }
    if status1==200 and status2==200 and status3==200:
        delay_info = json.loads(de_resp)
        host_info = json.loads(ho_resp)
        pknum_curr = json.loads(pk_resp)

        cout = 0
        pkdgree = 3
        pknum_tuple = list(pknum_curr.values())
        pknum_tuple = sorted(pknum_tuple)
        degree1 = pknum_tuple[7]
        degree2 = pknum_tuple[5]
        for key in host_info:
            pknum = pknum_curr[key]
            if pknum>degree1:
                pkdgree = 1
            elif pknum>degree2:
                pkdgree = 2
            else:
                pkdgree = 3
            switch_name = "BMV2:S"+str(int(key[18:],16))
            node = {
                "id": switch_name,
                "type": 2,
                "pkdgree": pkdgree
            }
            graph_show["nodes"].append(node)
            host = host_info[key]
            
            for i in range(len(host)):
                if i==1:
                    num = 0
                    host_name = "Server"
                else:
                    num = 1
                    host_name = "pc"+cout
                
                node = {
                    "id": host_name,
                    "type": num
                }
                graph_show["nodes"].append(node)
                link = {
                    "source": switch_name,
                    "target": host_name,
                    "value": "None"
                }
                graph_show["links"].append(link)
        
        delay_tuple = delay_info.values()
        temp = sorted(delay_tuple)
        middle = temp[len(delay_tuple)/2]
        for key in delay_info:
            delay = (delay_info[key]+delay_info[key[-19:]+" "+key[:19]])/2
            if delay<middle:
                delay_str = "good"
            else:
                delay_str = "bad"
            switch_src = "BMV2:S"+str(int(key[18:20],16))
            switch_des = "BMV2:S"+str(int(key[-2:],16))
            flag = 0
            for i in range(len(graph_show["links"])):
                if (switch_src in graph_show["links"][i].values()) and (switch_des in graph_show["links"][i].values()):
                    flag = 1
                    break
            if not flag:
                link = {
                            "source": switch_src,
                            "target": switch_des,
                            "value": delay_str
                        }
                graph_show["links"].append(link)
    return graph_show


# 发送需要禁止的IP
def baned_ip_send(ipdata):
    banip_url = "http://{}:8181/onos/defense-apply/defense".format(
        controller_ip)
    headers = {
        "Accept": "application/json"
    }

    resp = requests.post(url=banip_url, data=ipdata, headers=headers, auth=auth)
    return resp.status_code


# 获取深度检测模块检测结果并生成统计信息
def getResult_and_count():
    data = request.get_data()
    global time_now
    time_old = time_now
    time_now  =time.time()
    timeout = int(time_now-time_old)
    if timeout>1000:
        timeout = 5

    data = json.loads(data)
    ip_to_ban = {
        "ip": [],
        "timeout": timeout
    }
    for i in range(len(data)):
        threatnum = data[i]["threat"]
        if  threatnum != 0:
            ip_to_ban["ip"].append(data[i]["sip"])
            if threatnum == 1:
                threatcount["XSS"] += 1
            elif threatnum == 2:
                threatcount["SQL"] += 1
            elif threatnum == 3:
                threatcount["FileScan"] +=1
            elif threatnum ==4:
                threatcount["CRLF"] += 1
            elif threatnum == 5:
                threatcount["SSI"] += 1
            else:
                threatcount["Unknown"] += 1
    
    ip_to_ban = json.dumps(ip_to_ban)
    # TODO： 下方URL根据实际情况修改
    status_code = baned_ip_send(ip_to_ban)

    return str(status_code)
    

# 测试
if __name__ == "__main__":

    basicTopoDisplay()
