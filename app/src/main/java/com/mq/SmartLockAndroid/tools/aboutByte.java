package com.mq.SmartLockAndroid.tools;

public class aboutByte {
    /**
     * byte转化为int
     */
    public static int[] bytesToInt(byte[] src) {
        int[] value = new int[src.length];
        for (int i = 0; i < src.length; i++) {
            value[i] = src[i] & 0xFF;
        }
        return value;
    }

}
