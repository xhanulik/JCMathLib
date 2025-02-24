package opencrypto.jcmathlib;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.JCSystem;
import org.bouncycastle.util.encoders.Hex;


public class DifFuzzApplet extends Applet {
    ResourceManager rm;
    boolean initialized = false;
    BigNat bn1;
    BigNat bn2;
    BigNat bn3;

    public DifFuzzApplet() {
        OperationSupport.getInstance().setCard(OperationSupport.SIMULATOR); // TODO set your card
        if (!OperationSupport.getInstance().DEFERRED_INITIALIZATION) {
            initialize();
        }
    }

    public void initialize() {
        if (initialized) {
            return;
        }

        // Allocate resources for a required elliptic curve size (in bits)
        rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        bn2 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        bn3 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        initialized = true;
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new DifFuzzApplet().register();
    }

    public boolean select() {
        if (initialized) {
            rm.refreshAfterReset();
        }
        return true;
    }

    public void process(APDU apdu) {
        if (selectingApplet()) {
            return;
        }
        if (!initialized) {
            initialize();
        }

        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);

        Mem.clear();
        bn1.mult(bn2);
        long cost1 = Mem.instrCost;
        System.out.println("cost1= " + cost1);

        Mem.clear();
        bn1.isLesser(bn3);
        long cost2 = Mem.instrCost;

        System.out.println("cost2= " + cost2);
        System.out.println("|cost1 - cost2|= " + Math.abs(cost1 - cost2));

        Kelinci.addCost(Math.abs(cost1 - cost2));
    }
}
