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
}
