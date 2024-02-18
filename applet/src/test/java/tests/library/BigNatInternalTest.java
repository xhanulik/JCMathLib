package tests.library;

import com.licel.jcardsim.bouncycastle.util.encoders.Hex;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class BigNatInternalTest {

    @Test
    public void prependZeroes_targetLengthBigger() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        byte[] out = new byte[10];
        bn.fromByteArray(data, (short) 0, (short) data.length);
        bn.prependZeros((short) 10, out, (short) 0);
        Assertions.assertArrayEquals(
                new byte[]{0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06},
                out);
    }

    @Test
    public void prependZeroes_targetLengthSmaller() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        byte[] out = new byte[6];
        bn.fromByteArray(data, (short) 0, (short) data.length);
        bn.prependZeros((short) data.length, out, (short) 0);
        Assertions.assertArrayEquals(
                data,
                out);
    }

    @Test
    public void shrink_biggerThanActual_downsize() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn.fromByteArray(data, (short) 0, (short) data.length);
        bn.resize((short) (2 * data.length));
        Assertions.assertEquals(2 * data.length, bn.length());

        bn.shrink();
        Assertions.assertEquals(data.length, bn.length());
    }

    @Test
    public void shrink_sameAsActual_nothing() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn.fromByteArray(data, (short) 0, (short) data.length);
        Assertions.assertEquals(data.length, bn.length());

        bn.shrink();
        Assertions.assertEquals(data.length, bn.length());
    }

    @Test
    public void shrink_zero_setSizeTo0() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data = {0x00};
        bn.fromByteArray(data, (short) 0, (short) data.length);
        Assertions.assertEquals(data.length, bn.length());

        bn.shrink();
        Assertions.assertEquals(0, bn.length());
    }

    @Test
    public void copy_thisLonger() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        BigNat bn2 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x0a, 0x0b, 0x0c, 0x0d, 0x0e};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);

        bn1.copy(bn2);
        bn1.shrink();
        bn2.shrink();
        Assertions.assertTrue(bn1.equals(bn2));
    }

    @Test
    public void copy_leadingZeroes() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        BigNat bn2 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x00, 0x00, 0x00, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.copy(bn2);
        bn1.shrink();
        bn2.shrink();
        Assertions.assertTrue(bn1.equals(bn2));
    }

    @Test
    public void copy_thisShorter() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        BigNat bn2 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x0a, 0x0b, 0x0c, 0x0d, 0x0e};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);

        Assertions.assertThrows(ISOException.class, () -> bn1.copy(bn2));
    }

    @Test
    public void isZero_zero() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data1 = {0x00, 0x00, 0x00, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        Assertions.assertTrue(bn1.isZero());
    }

    @Test
    public void isZero_notZero_firstByte() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data1 = {0x00, 0x00, 0x00, 0x01};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        Assertions.assertFalse(bn1.isZero());
    }

    @Test
    public void isZero_notZero_lastByte() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x10, 0x00, 0x00, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        Assertions.assertFalse(bn1.isZero());
    }

    @Test
    public void isZero_notZero_middleByte() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data1 = {0x00, 0x00, 0x05, 0x00, 0x04, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        Assertions.assertFalse(bn1.isZero());
    }

    @Test
    public void isOne_notOne() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data1 = {0x00, 0x00, 0x00, 0x00, 0x01, 0x01};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        Assertions.assertFalse(bn1.isOne());
    }

    @Test
    public void isOne_one() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data1 = {0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        Assertions.assertTrue(bn1.isOne());
    }

    @Test
    public void isOne_zero() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data1 = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        Assertions.assertFalse(bn1.isOne());
    }

    @Test
    public void equals_sameLength_true() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x01, 0x02, 0x03};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn2.fromByteArray(data1, (short) 0, (short) data1.length);
        Assertions.assertTrue(bn1.equals(bn2));
    }

    @Test
    public void equals_sameLength_false() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x00, 0x01, 0x02, 0x03};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x05, 0x01, 0x02, 0x03};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.equals(bn2));
    }

    @Test
    public void equals_otherLonger_true() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x05, 0x01, 0x02, 0x03};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x00, 0x00, 0x00, 0x00, 0x05, 0x01, 0x02, 0x03};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.equals(bn2));
    }

    @Test
    public void equals_otherLonger_false() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x05, 0x01, 0x01, 0x03};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x00, 0x00, 0x00, 0x00, 0x00, 0x05, 0x01, 0x02, 0x03};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.equals(bn2));
    }

    @Test
    public void equals_otherLonger2_false() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x05, 0x01, 0x01, 0x03};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x01, 0x01, 0x03};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.equals(bn2));
    }

    @Test
    public void equals_thisLonger_true() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data2 = {0x00, 0x00, 0x00, 0x00, 0x05, 0x01, 0x02, 0x03};
        bn1.fromByteArray(data2, (short) 0, (short) data2.length);
        byte[] data1 = {0x05, 0x01, 0x02, 0x03};
        bn2.fromByteArray(data1, (short) 0, (short) data1.length);
        Assertions.assertTrue(bn1.equals(bn2));
    }

    @Test
    public void equals_thisLonger_false() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data2 = {0x00, 0x00, 0x00, 0x00, 0x05, 0x01, 0x02, 0x03};
        bn1.fromByteArray(data2, (short) 0, (short) data2.length);
        byte[] data1 = {0x05, 0x03, 0x02, 0x03};
        bn2.fromByteArray(data1, (short) 0, (short) data1.length);
        Assertions.assertFalse(bn1.equals(bn2));
    }

    @Test
    public void equals_thisLonger2_false() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data2 = {0x00, 0x00, 0x00, 0x01, 0x05, 0x03, 0x02, 0x03};
        bn1.fromByteArray(data2, (short) 0, (short) data2.length);
        byte[] data1 = {0x05, 0x03, 0x02, 0x03};
        bn2.fromByteArray(data1, (short) 0, (short) data1.length);
        Assertions.assertFalse(bn1.equals(bn2));
    }

    @Test
    public void increment_firstByte_noOverflow() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.increment();

        BigNat bn2 = new BigNat((short) 10, memoryType, rm);
        bn2.fromByteArray(new byte[]{0x01, 0x03}, (short) 0, (short) 2);
        Assertions.assertTrue(bn1.equals_original(bn2));
    }

    @Test
    public void increment_firstByte_overflow() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, (byte) 0xff};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.increment();

        BigNat bn2 = new BigNat((short) 10, memoryType, rm);
        bn2.fromByteArray(new byte[]{0x02, 0x00}, (short) 0, (short) 2);
        Assertions.assertTrue(bn1.equals_original(bn2));
    }

    @Test
    public void increment_lastByte_overflow() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {(byte) 0xff, (byte) 0xff};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.increment();

        BigNat bn2 = new BigNat((short) 10, memoryType, rm);
        bn2.fromByteArray(new byte[]{0x00, 0x00}, (short) 0, (short) 2);
        Assertions.assertTrue(bn1.equals_original(bn2));
        Assertions.assertEquals(2, bn1.length());
    }

    @Test
    public void decrement_firstByte_noUnderflow() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {(byte) 0x01, (byte) 0x02};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.decrement();

        BigNat bn2 = new BigNat((short) 10, memoryType, rm);
        bn2.fromByteArray(new byte[]{(byte) 0x01, (byte) 0x01}, (short) 0, (short) 2);
        Assertions.assertTrue(bn1.equals_original(bn2));
        Assertions.assertEquals(2, bn1.length());
    }

    @Test
    public void decrement_firstByte_underflow() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {(byte) 0x01, (byte) 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.decrement();

        BigNat bn2 = new BigNat((short) 10, memoryType, rm);
        bn2.fromByteArray(new byte[]{(byte) 0x00, (byte) 0xff}, (short) 0, (short) 2);
        Assertions.assertTrue(bn1.equals_original(bn2));
        Assertions.assertEquals(2, bn1.length());
    }

    @Test
    public void decrement_lastByte_propagate() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.decrement();

        BigNat bn2 = new BigNat((short) 10, memoryType, rm);
        bn2.fromByteArray(new byte[]{(byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff}, (short) 0, (short) 4);
        Assertions.assertTrue(bn1.equals_original(bn2));
        Assertions.assertEquals(4, bn1.length());
    }

    @Test
    public void mult_1digit_1() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 2, memoryType, rm);

        byte[] data1 = Hex.decode("1234");
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = Hex.decode("01");
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.mult(bn2);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = Hex.decode("1234");
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void mult_1digit_10() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 3, memoryType, rm);

        byte[] data1 = Hex.decode("1234");
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = Hex.decode("10");
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.mult(bn2);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = Hex.decode("012340");
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void mult_1digit_EA() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 2, memoryType, rm);

        byte[] data1 = Hex.decode("1234");
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = Hex.decode("EA");
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.mult(bn2);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = Hex.decode("10A388");
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void mult_1digit_F1() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 2, memoryType, rm);

        byte[] data1 = Hex.decode("1234");
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = Hex.decode("F1");
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.mult(bn2);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = Hex.decode("1122F4");
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void mult_2digit_1234() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 4, memoryType, rm);

        byte[] data1 = Hex.decode("1234");
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = Hex.decode("1234");
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.mult(bn2);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = Hex.decode("014B5A90");
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void mult_3digit_123456() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 3, memoryType, rm);

        byte[] data1 = Hex.decode("F1");
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = Hex.decode("123456");
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.mult(bn2);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = Hex.decode("112344F6");
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void mult_2digit_1234_2() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 3, memoryType, rm);

        byte[] data1 = Hex.decode("F1");
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = Hex.decode("1234");
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.mult(bn2);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = Hex.decode("1122F4");
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void mult_4digit_123456() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 5, memoryType, rm);
        BigNat bn2 = new BigNat((short) 4, memoryType, rm);

        byte[] data1 = Hex.decode("F1");
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = Hex.decode("12345678");
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.mult(bn2);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = Hex.decode("11234566F8");
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void shiftRight_0() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x05, 0x08};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.shiftRight((short) 0);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x05, 0x08};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void shiftRight_1() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x05, 0x08};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.shiftRight((short) 1);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x02, (byte) 0x84};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void shiftRight_7() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x40, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        bn1.shiftRight((short) 7);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x00, (byte) 0x80};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        Assertions.assertTrue(bn1.equals(bn3));
    }

    @Test
    public void remainderDivide() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x19};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x05};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.remainderDivide(bn2, null);

        BigNat bn3 = new BigNat((short) 10, memoryType, rm);
        byte[] data3 = {0x00, (byte) 0x80};
        bn3.fromByteArray(data3, (short) 0, (short) data3.length);
        //Assertions.assertTrue(bn1.equals(bn3));
    }
}
