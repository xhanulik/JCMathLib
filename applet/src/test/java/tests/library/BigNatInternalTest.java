package tests.library;

import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class BigNatInternalTest {

    @Test
    public void resize_smaller() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn.fromByteArray(data, (short) 0, (short) data.length);
        bn.resize((short) (data.length - 3));
        Assertions.assertEquals(data.length - 3, bn.length());
    }

    @Test
    public void resize_bigger() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn.fromByteArray(data, (short) 0, (short) data.length);
        bn.resize((short) (data.length * 2));
        Assertions.assertEquals(data.length * 2, bn.length());
    }

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
    public void clone_otherSizeSameAsThis() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 4, memoryType, rm);
        BigNat bn2 = new BigNat((short) 4, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x0a, 0x0b, 0x0c, 0x0d, 0x0e};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.clone(bn2);
        Assertions.assertTrue(bn1.equals(bn2));
    }

    @Test
    public void clone_otherSizeBiggerThanThis() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 4, memoryType, rm);
        BigNat bn2 = new BigNat((short) 5, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);

        Assertions.assertThrows(ISOException.class, () -> bn1.clone(bn2));
    }

    @Test
    public void clone_otherSizeSmallerThanThis() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 4, memoryType, rm);
        BigNat bn2 = new BigNat((short) 4, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x0a, 0x0b};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.clone(bn2);
        Assertions.assertTrue(bn1.equals(bn2));
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
    public void isLesser_bigger_noShiftNoStart() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x00, 0x01, 0x02, 0x03};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2, (short) 0, (short) 0));
    }

    @Test
    public void isLesser_same_noShiftNoStart() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2, (short) 0, (short) 0));
    }

    @Test
    public void isLesser_lesser_noShiftNoStart() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x01, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2, (short) 0, (short) 0));
    }

    @Test
    public void isLesser_same_noShiftStart2() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2, (short) 0, (short) 2));
    }

    @Test
    public void isLesser_same_noShiftStart5() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2, (short) 0, (short) 5));
    }

    @Test
    public void isLesser_lesser_shiftNoStart() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2, (short) 1, (short) 0));
    }
    @Test
    public void isLesser_same_shiftNoStart() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04, 0x05};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertFalse(bn1.isLesser(bn2, (short) 1, (short) 0));
    }

    @Test
    public void isLesser_lesser2_shiftNoStart() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x00};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x01, 0x02, 0x03, 0x04, 0x05};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        Assertions.assertTrue(bn1.isLesser(bn2, (short) 2, (short) 0));
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
    public void add_noShift() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 10, memoryType, rm);
        BigNat bn2 = new BigNat((short) 10, memoryType, rm);

        byte[] data1 = {0x01, 0x7f};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x7f, 0x7f};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.add(bn2);
    }
}
