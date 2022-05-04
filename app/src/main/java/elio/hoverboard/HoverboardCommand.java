package elio.hoverboard;

import elio.core.ByteUtils;

public class HoverboardCommand {
    public static int BYTE_LENGTH = 2 * 9;
    public int frame;
    public int steer;
    public int speed;

    public byte [] getFrame() {
        return ByteUtils.unsignedIntToBytes(this.frame);
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public byte [] getSteer() {
        return ByteUtils.unsignedIntToBytes(this.steer);
    }

    public void setSteer(int steer) {
        this.steer = steer;
    }

    public byte[] getSpeed() {
        return ByteUtils.unsignedIntToBytes(this.speed);
    }

    public void setSpeed(int speed) {
        this.speed = (short)speed;
    }

    public byte[] getChecksum() {
        int checksum = this.frame ^ this.speed ^ this.steer;
        return ByteUtils.unsignedIntToBytes(checksum);
    }

    public byte[] toBytes() {
        return new byte[] {
            this.getFrame()[0],
            this.getFrame()[1],
            this.getSpeed()[0],
            this.getSpeed()[1],
            this.getSteer()[0],
            this.getSteer()[1],
            this.getChecksum()[0],
            this.getChecksum()[1]
        };
    }

    public String toString() {
        return String.format(
                "SP:%d ST:%d \n",
                this.speed,
                this.steer
        );
    }

    public static HoverboardCommand zeroCommand() {
        HoverboardCommand command = new HoverboardCommand();
        command.setSpeed(0);
        command.setSteer(0);
        return command;
    }

}
