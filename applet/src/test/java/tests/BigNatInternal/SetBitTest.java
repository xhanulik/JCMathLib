package tests.BigNatInternal;

import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SetBitTest {
    @Test
    public void oneByte_firstBit_one() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0b00000000};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);

        bn1.ctSetBit((byte) 1, 0);

        byte[] actualResult = new byte[1];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(new byte[]{0b00000001}, actualResult);
    }

    @Test
    public void oneByte_firstBit_zero() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0b00000001};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);

        bn1.ctSetBit((byte) 0, 0);

        byte[] actualResult = new byte[1];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(new byte[]{0b00000000}, actualResult);
    }

    @Test
    public void oneByte_lastBit_one() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0b00000000};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);

        bn1.ctSetBit((byte) 1, 7);

        byte[] actualResult = new byte[1];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(new byte[]{(byte) 0b10000000}, actualResult);
    }

    @Test
    public void oneByte_lastBit_zero() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {(byte) 0b10000001};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);

        bn1.ctSetBit((byte) 0, 7);

        byte[] actualResult = new byte[1];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(new byte[]{0b00000001}, actualResult);
    }

    @Test
    public void twoBytes_lastBit_one() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0b01000001, (byte) 0xf0};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);

        bn1.ctSetBit((byte) 1, 15);

        byte[] actualResult = new byte[2];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(new byte[]{(byte) 0b11000001, (byte) 0xf0}, actualResult);
    }

    @Test
    public void twoBytes_lastBit_zero() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {(byte) 0b11000001, (byte) 0xf0};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);

        bn1.ctSetBit((byte) 0, 15);

        byte[] actualResult = new byte[2];
        bn1.copyToByteArray(actualResult, (short) 0);
        Assertions.assertArrayEquals(new byte[]{(byte) 0b01000001, (byte) 0xf0}, actualResult);
    }
}
