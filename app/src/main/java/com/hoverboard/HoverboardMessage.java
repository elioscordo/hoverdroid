package com.hoverboard;

import java.nio.ByteBuffer;

public class HoverboardMessage {
    public static int BYTE_LENGTH = 2 * 9;
    private int frame;
    private int cmd1;
    private int cmd2;
    private int speedR;
    private int speedL;
    private int batV;
    private int cmdLed;
    private int temp;
    private int checksum;

    public static int asUnsignedShort(short s) {
        return s & 0xFFFF;
    }
    public static HoverboardMessage createMessage(byte[] bytes) {
        HoverboardMessage out = null;
        if (bytes.length == HoverboardMessage.BYTE_LENGTH) {
            out = new HoverboardMessage();
            for (int i = 0; i < bytes.length; i= i+2) {
                // Big endian
                byte[] slice ={bytes[i+1], bytes[i]};
                ByteBuffer wrapped = ByteBuffer.wrap(slice);
                switch (i) {
                    case 0:
                        out.setFrame(
                               HoverboardMessage.asUnsignedShort(
                                       wrapped.getShort()
                               )
                        );
                        break;
                    case 2:
                        out.setCmd1(wrapped.getShort());
                        break;
                    case 4:
                        out.setCmd2(wrapped.getShort());
                        break;
                    case 6:
                        out.setSpeedL(wrapped.getShort());
                        break;
                    case 8:
                        out.setSpeedR(wrapped.getShort());
                        break;
                    case 10:
                        out.setBatV(wrapped.getShort());
                        break;
                    case 12:
                        out.setTemp(wrapped.getShort());
                        break;
                    case 14:
                        out.setCmdLed(
                                HoverboardMessage.asUnsignedShort(
                                    wrapped.getShort()
                                )
                        );
                        break;
                    case 16:
                        out.setChecksum(
                                HoverboardMessage.asUnsignedShort(
                                        wrapped.getShort()
                                )
                        );
                        break;

                }
            }
        }
        return out;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public int getCmd1() {
        return cmd1;
    }

    public void setCmd1(int cmd1) {
        this.cmd1 = cmd1;
    }

    public int getCmd2() {
        return cmd2;
    }

    public void setCmd2(int cmd2) {
        this.cmd2 = cmd2;
    }

    public int getCmdLed() {
        return cmdLed;
    }

    public void setCmdLed(int cmdLed) {
        this.cmdLed = cmdLed;
    }

    public int getSpeedR() {
        return speedR;
    }

    public void setSpeedR(int speedR) {
        this.speedR = speedR;
    }

    public int getSpeedL() {
        return speedL;
    }

    public void setSpeedL(int speedL) {
        this.speedL = speedL;
    }

    public int getBatV() {
        return batV;
    }

    public void setBatV(int batV) {
        this.batV = batV;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getChecksum() {
        return checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    public String toString () {
        return String.format(
                "F:%d, T:%d, C1:%d, C2:%d, SpL:%d, SpR:%d \n",
                this.getFrame(),
                this.getTemp(),
                this.getCmd1(),
                this.getCmd2(),
                this.getSpeedL(),
                this.getSpeedR()
        );
    }

}
