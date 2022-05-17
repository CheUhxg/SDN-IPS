# 网络技术挑战赛-流量模拟

## client
* 客户端访问服务器，发送`HTTP`请求。
```bash
bash client.sh <target ip>
```

## server
* 服务器监听`80`端口，存储收包结果。
```bash
bash server.sh
```

## hacker
* 攻击者针对指定服务器使用各种攻击。
```bash
bash hacker.sh <target ip> <method>
```
> Method: [1: Discovering; 2: DoS; 3: XSS&SQL]

## detecter
* 检测器监听`eth0`网卡，解析接收到的报文，生成`result.json`文件。
```bash
bash detecter.sh
```