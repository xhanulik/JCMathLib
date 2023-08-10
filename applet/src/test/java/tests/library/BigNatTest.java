package tests.library;

import cz.muni.fi.crocs.rcard.client.Util;
import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class BigNatTest {

    @Test
    public void copy() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        BigNat tmp = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        byte[] data = Util.hexStringToByteArray("26336676FF91D8B2");
        bn.fromByteArray(data, (short) 0, (short) data.length);
        bn.sq();
        Assertions.assertTrue(bn.equals(tmp));
    }

}
