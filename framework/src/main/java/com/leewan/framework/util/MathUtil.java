package com.leewan.framework.util;

import java.nio.ByteBuffer;

public class MathUtil {

    public static int getInt(byte[] bs){
        ByteBuffer buf = ByteBuffer.wrap(bs);
        return buf.getInt();
    }

    public static byte[] getBytes(int i){
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(i);
        return buf.array();
    }



}
