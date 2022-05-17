from socket import *
from sys import argv
from threading import Thread

def rcvPkt(conn, addr):
    print('Connected with', end=' ')
    print(addr)
    try:
        data = conn.recv(1024)
        conn.send(b'Server Response')
    except:
        pass
    finally:
        conn.close()


def main():
    serverIP = argv[1]
    server = socket(AF_INET, SOCK_STREAM)
    server.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
    server.bind((serverIP, 80))
    server.listen(0)
    while True:
        conn,addr = server.accept()
        thrd = Thread(target=rcvPkt, args=(conn, addr))
        thrd.start()

if __name__ == '__main__':
    main()