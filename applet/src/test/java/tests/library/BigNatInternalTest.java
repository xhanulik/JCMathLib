package tests.library;

import javacard.framework.ISO7816;
import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import opencrypto.jcmathlib.UnitTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class BigNatInternalTest {

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
    }

    @Test
    public void copy_thisShorter() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        BigNat bn2 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data1 = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = {0x0a, 0x0b, 0x0c, 0x0d, 0x0e};
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn2.resize((short) (data1.length + 3));

        bn1.copy(bn2);
    }
}
