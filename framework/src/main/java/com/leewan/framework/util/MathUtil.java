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



    public static void main(String[] args) {
        byte[] bytes = getBytes(Integer.MAX_VALUE);
        for (byte aByte : bytes) {
            System.out.println(aByte);
        }
        System.out.println(getInt(new byte[]{127,-1,-1,-1}));
    }
}
