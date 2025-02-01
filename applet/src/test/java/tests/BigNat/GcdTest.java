package tests.BigNat;

import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GcdTest {
    @Test
    public void a12_b4_4() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat a = new BigNat((short) 7, memoryType, rm);
        BigNat b = new BigNat((short) 7, memoryType, rm);

        byte[] data1 = {12};
        a.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {4};
        b.fromByteArray(data2, (short) 0, (short) data2.length);
        a.gcd(b);

        Assertions.assertEquals(1, a.length());
        byte[] actualResult = new byte[1];
        a.copyToByteArray(actualResult, (short) 0);
        byte[] correct = {4};
        Assertions.assertArrayEquals(correct, actualResult);
    }

    @Test
    public void a12_b11_1() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat a = new BigNat((short) 7, memoryType, rm);
        BigNat b = new BigNat((short) 7, memoryType, rm);

        byte[] data1 = {12};
        a.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {11};
        b.fromByteArray(data2, (short) 0, (short) data2.length);
        a.gcd(b);

        Assertions.assertEquals(1, a.length());
        byte[] actualResult = new byte[1];
        a.copyToByteArray(actualResult, (short) 0);
        byte[] correct = {1};
        Assertions.assertArrayEquals(correct, actualResult);
    }

    @Test
    public void a18545_b5_5() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat a = new BigNat((short) 7, memoryType, rm);
        BigNat b = new BigNat((short) 7, memoryType, rm);

        byte[] data1 = {0x48, 0x71};
        a.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {5};
        b.fromByteArray(data2, (short) 0, (short) data2.length);
        a.gcd(b);

        Assertions.assertEquals(1, a.length());
        byte[] actualResult = new byte[1];
        a.copyToByteArray(actualResult, (short) 0);
        byte[] correct = {5};
        Assertions.assertArrayEquals(correct, actualResult);
    }

    @Test
    public void a20000_b150_50() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat a = new BigNat((short) 7, memoryType, rm);
        BigNat b = new BigNat((short) 7, memoryType, rm);

        byte[] data1 = {0x4E, 0x20};
        a.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 150};
        b.fromByteArray(data2, (short) 0, (short) data2.length);
        a.gcd(b);

        Assertions.assertEquals(1, a.length());
        byte[] actualResult = new byte[1];
        a.copyToByteArray(actualResult, (short) 0);
        byte[] correct = {50};
        Assertions.assertArrayEquals(correct, actualResult);
    }

    @Test
    public void a9876543210_b1234567890_90() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat a = new BigNat((short) 7, memoryType, rm);
        BigNat b = new BigNat((short) 7, memoryType, rm);

        byte[] data1 = {0x02, 0x4C, (byte) 0xB0, 0x16, (byte) 0xEA};
        a.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x49, (byte) 0x96, 0x02, (byte) 0xD2};
        b.fromByteArray(data2, (short) 0, (short) data2.length);
        a.gcd(b);

        Assertions.assertEquals(1, a.length());
        byte[] actualResult = new byte[1];
        a.copyToByteArray(actualResult, (short) 0);
        byte[] correct = {90};
        Assertions.assertArrayEquals(correct, actualResult);
    }

    @Test
    public void a1099511627775_b1048575_1048575() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat a = new BigNat((short) 7, memoryType, rm);
        BigNat b = new BigNat((short) 7, memoryType, rm);

        byte[] data1 = {(byte) 0xFFFF, (byte) 0xFFFF, (byte) 0xFFFF, (byte) 0xFFFF, (byte) 0xFF};
        a.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {(byte) 0x0F, (byte) 0xFF, (byte) 0xFF};
        b.fromByteArray(data2, (short) 0, (short) data2.length);
        a.gcd(b);

        Assertions.assertEquals(3, a.length());
        byte[] actualResult = new byte[3];
        a.copyToByteArray(actualResult, (short) 0);
        byte[] correct = {(byte) 0x0F, (byte) 0xFF, (byte) 0xFF};
        Assertions.assertArrayEquals(correct, actualResult);
    }
}
