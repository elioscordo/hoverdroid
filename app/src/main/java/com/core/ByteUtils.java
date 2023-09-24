package com.core;

import com.hoverboard.HoverboardMessage;

import java.nio.ByteBuffer;

public class ByteUtils {
    public static int bytesToUnsignedInt(
            byte[] bytes
    ) {
        // big endian short in bytes to unsigned int
        byte[] bigEndian = {bytes[1], bytes[0]};
        ByteBuffer wrapped = ByteBuffer.wrap(bigEndian);
        return HoverboardMessage.asUnsignedShort(
                wrapped.getShort()
        );
    }

    public static byte[] unsignedIntToBytes(
            int shortInt
    ) {
        // unsigned int to big endian bytes
        byte big =  (byte) ((shortInt&0xFF00)>>8);
        byte little = (byte) (shortInt&0x00FF);
        byte[] bigEndian = {little, big};
        return bigEndian;
    }

    public static byte[] signedIntToBytes(
            int shortInt
    ) {
        // unsigned int to big endian bytes
        byte big =  (byte) ((shortInt)>>8);
        byte little = (byte) (shortInt);
        byte[] bigEndian = {little, big};
        return bigEndian;
    }

    public static int bytesToInt(
            byte[] bytes
    ) {
        // big endian short in bytes to int
        byte[] bigEndian = {bytes[1], bytes[0]};
        ByteBuffer wrapped = ByteBuffer.wrap(bigEndian);
        return wrapped.getShort();
    }


}
