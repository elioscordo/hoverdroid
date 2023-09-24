package com.core;

public class SerialDevice {
    private int deviceId, portNum, baudRate;

    public SerialDevice(int deviceId, int portNum, int baudRate) {
        this.deviceId = deviceId;
        this.portNum = portNum;
        this.baudRate = baudRate;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public int getPortNum() {
        return portNum;
    }

    public int getBaudRate() {
        return baudRate;
    }
}
