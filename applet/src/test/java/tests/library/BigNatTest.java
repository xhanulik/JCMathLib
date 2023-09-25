package tests.library;

import cz.muni.fi.crocs.rcard.client.Util;
import javacard.framework.ISO7816;
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

    @Test
    public void modNegate() {
        ResourceManager rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        BigNat bn1 = new BigNat((short) 100, memoryType, rm);
        BigNat bn2 = new BigNat((short) 100, memoryType, rm);

        byte[] data1 = Util.hexStringToByteArray("ED647F0E12EEFE89C888F992059300B41B75FFB4909B23500C496EA32755425E4EDC26D8BCE74020345E1DD9B23ED7B3372EC14B4261E14E234D835E7347B5F3");
        bn1.fromByteArray(data1, (short) 0, (short) data1.length);
        byte[] data2 = Util.hexStringToByteArray("1A7C3978549948AA544A7D7E6D29293705A3F44830D46609A1838FC1B352006CB24D546C1714050D4609E66A9697B97FA6ED6D92C51937771D64C4923FFBB36A");
        bn2.fromByteArray(data2, (short) 0, (short) data2.length);
        bn1.modNegate(bn2);
    }

}
