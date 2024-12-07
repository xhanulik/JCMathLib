package tests.BigNatInternal;

import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class GetBitTest {
    @Test
    public void byteFirstBit() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0b00000001};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);

        byte result = bn1.ctGetBit(0);
        Assertions.assertEquals(0x01, result);
    }

    @Test
    public void byteSecondBit() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0b00000001};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);

        byte result = bn1.ctGetBit(1);
        Assertions.assertEquals(0x00, result);
    }

    @Test
    public void byteSeventhBit() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {(byte) 0b01000000};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);

        byte result = bn1.ctGetBit(6);
        Assertions.assertEquals(0x01, result);
    }

    @Test
    public void byteEightBit() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {(byte) 0b10000000};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);

        byte result = bn1.ctGetBit(7);
        Assertions.assertEquals(0x01, result );
    }

    @Test
    public void moreBytes_sixthBit() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {(byte) 0b01000000, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);

        byte result = bn1.ctGetBit(14);
        Assertions.assertEquals(0x01, result );
    }

    @Test
    public void moreBytes_eightBit() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {(byte) 0b10000000, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);

        byte result = bn1.ctGetBit(15);
        Assertions.assertEquals(0x01, result );
    }

    @Test
    public void moreBytes_eightBit_zero() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {(byte) 0b01000000, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);

        byte result = bn1.ctGetBit(15);
        Assertions.assertEquals(0x00, result );
    }
}
