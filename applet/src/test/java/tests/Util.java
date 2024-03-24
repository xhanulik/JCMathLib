package tests;

public class Util {
    public static byte[] intToBytes(int val) {
        byte[] data = new byte[5];
        if (val < 0) {
            data[0] = 0x01;
        } else {
            data[0] = 0x00;
        }

        int unsigned = Math.abs(val);
        data[1] = (byte) (unsigned >>> 24);
        data[2] = (byte) (unsigned >>> 16);
        data[3] = (byte) (unsigned >>> 8);
        data[4] = (byte) unsigned;

        return data;
    }
}
