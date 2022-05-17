package com.synx.delay;

import org.onlab.packet.BasePacket;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * 设计转发用的PDU
 * 打上时间戳和PDU标识
 */

public class MyDelay extends BasePacket {
    // 源交换机
    protected String fromSwitch;
    // 时间戳
    protected long timestamp;
    // 协议标识，用于区分数据包
    public static final short PROTOCOL_MYDELAY = 0x801;

    public MyDelay(String formSwitch,long timestamp){
        this.fromSwitch=formSwitch;
        this.timestamp=timestamp;
    }
    public String getFromSwitch(){
        return fromSwitch;
    }

    public long getTimestamp(){
        return timestamp;
    }

    @Override
    public byte[] serialize() {
        final byte[] data= new byte[128];
        final ByteBuffer bb = ByteBuffer.wrap(data);

        for(int i=0;i<this.fromSwitch.length();i++){
            bb.putChar(this.fromSwitch.charAt(i));
        }
        bb.putLong(this.timestamp);

        return data;
    }
}
