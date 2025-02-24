package opencrypto.jcmathlib;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import javacard.framework.*;
import org.bouncycastle.util.encoders.Hex;

/**
 * Applet for Differential fuzzing of JCMathLib
 *
 * @author Veronika Hanulíková
 */
public class DifFuzzApplet extends Applet {
    ResourceManager rm;
    boolean initialized = false;
    BigNat bn1;
    BigNat bn2;
    BigNat bn3;
    BigNat bn4;
    BigNat bn5;

    Integer int1;
    Integer int2;
    Integer int3;
    Integer int4;

    ECCurve curve;
    ECPoint point1;
    ECPoint point2;
    ECPoint point3;

    /* Fixed modulus */
    final byte[] modulus = {118, 106, 70, 26, -77, 126, 109, 16, 85, 71, -119, 74,
            -74, 7, 60, -42, -3, 81, -13, 75, 77, -25, -43, 26, -32, -83, -124, -51, 100, -81, -75, -57, -16, 98, -77, -15, 56,
            55, 81, 7, 43, -94, 39, -59, 22, 120, -119, -5, 97, 88, 98, 10, -23, -90, 22, 119, -71, 55, 48, -8, 44, 28, 77, 37};

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

        // Allocate resources during initialization
        rm = new ResourceManager((short) 256);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        bn2 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        bn3 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        bn4 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        bn5 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        int1 = new Integer((short) 70, rm);
        int2 = new Integer((short) 70, rm);
        int3 = new Integer((short) 70, rm);
        int4 = new Integer((short) 70, rm);
        curve = new ECCurve(opencrypto.jcmathlib.SecP256r1.p, opencrypto.jcmathlib.SecP256r1.a, opencrypto.jcmathlib.SecP256r1.b,
                opencrypto.jcmathlib.SecP256r1.G, opencrypto.jcmathlib.SecP256r1.r, opencrypto.jcmathlib.SecP256r1.k, rm);
        point1 = new ECPoint(curve);
        point2 = new ECPoint(curve);
        point3 = new ECPoint(curve);
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
        switch (apduBuffer[ISO7816.OFFSET_INS]) {
            case DifFuzzOps.INS_BN_PREPEND:
                fuzzBnPrepend(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_ADD:
                fuzzBnAdd(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_SUB:
                fuzzBnSub(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_INC:
                fuzzBnInc(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_DIV:
                fuzzBnDiv(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_LESSER:
                fuzzBnLesser(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_EQUAL:
                fuzzBnEqual(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_STR:
                fuzzBnStr(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_MULT:
                fuzzBnMult(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_ZERO:
                fuzzBnIsZero(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_SHRINK:
                fuzzBnShrink(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_CLONE:
                fuzzBnClone(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_COPY:
                fuzzBnCopy(apduBuffer);
                break;
            /* BigNat class */
            case DifFuzzOps.INS_BN_GCD:
                fuzzBnGcd(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_MODNEG:
                fuzzBnModNegate(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_MODADD:
                fuzzBnModAdd(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_MODSUB:
                fuzzBnModSub(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_MODEXP:
                fuzzBnModExp(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_MODSQ:
                fuzzBnModSq(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_QUAD:
                fuzzBnIsQuadraticResidue(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_MODMULT:
                fuzzBnModMult(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_MODINV:
                fuzzBnModInv(apduBuffer);
                break;
            case DifFuzzOps.INS_BN_COP:
                fuzzBnIsCoprime(apduBuffer);
                break;
            /* Integer class */
            case DifFuzzOps.INS_INT_LES:
                fuzzIntLesser(apduBuffer);
                break;
            case DifFuzzOps.INS_INT_ADD:
                fuzzIntAdd(apduBuffer);
                break;
            case DifFuzzOps.INS_INT_SUB:
                fuzzIntSub(apduBuffer);
                break;
            /* ECPoint class */
            case DifFuzzOps.INS_EC_NEG:
                fuzzEcNeg(apduBuffer);
                break;
            case DifFuzzOps.INS_EC_EQ:
                fuzzEcEqual(apduBuffer);
                break;
            case DifFuzzOps.INS_EC_MUL:
                fuzzEcMul(apduBuffer);
                break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    /**
     * Set instrumentation cost into Kelinci
     * @param cost1 cost of the first operation
     * @param cost2 cost of the seconds operation
     */
    void saveCost(long cost1, long cost2) {
        System.out.println("Final cost= " + Math.abs(cost1 - cost2));
        Kelinci.addCost(Math.abs(cost1 - cost2));
    }

    /**
     * Methods for profiling
     */

    void fuzzBnPrepend(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink(); // allow non-constant sizes, when padded with zeroes
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();
        byte[] buffer1 = new byte[(byte) (rm.MAX_BIGNAT_SIZE + 1)];
        byte[] buffer2 = new byte[(byte) (rm.MAX_BIGNAT_SIZE + 1)];

        /*
        Calculating cost according to https://github.com/isstac/diffuzz
         */
        Mem.clear();
        bn1.prependZeros((short) buffer1.length, buffer1, (short) 0);
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn2.prependZeros((short) buffer2.length, buffer2, (short) 0);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnInc(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();

        Mem.clear();
        bn1.increment();
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn2.increment();
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnAdd(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);
        bn3.shrink();

        Mem.clear();
        bn1.add(bn2);
        // bn1.ctAdd(bn2);
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn1.add(bn3);
        // bn1.ctAdd(bn3);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }
    void fuzzBnSub(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1); // private value 1
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1); // private value 2

        Mem.clear();
        bn1.subtract(bn2);
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn1.subtract(bn3);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnMult(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);
        bn3.shrink();

        Mem.clear();
        ((BigNatInternal)bn1).mult(bn2);
        long cost1 = Mem.instrCost;

        Mem.clear();
        ((BigNatInternal)bn1).mult(bn3);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnIsZero(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();

        Mem.clear();
        bn1.isZero();
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn2.isZero();
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnShrink(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);

        Mem.clear();
        bn1.shrink();
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn2.shrink();
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnDiv(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);
        bn3.shrink();
        bn4.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 3 * p1), p1);
        bn4.shrink();

        Mem.clear();
        bn1.remainderDivide(bn2, bn5);
        // bn1.ctRemainderDivideOptimized(bn2, bn5);
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn3.remainderDivide(bn4, bn5);
        // bn3.ctRemainderDivideOptimized(bn4, bn5);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnLesser(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1); // public value
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);
        bn3.shrink();
        bn4.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 3 * p1), p1);
        bn4.shrink();

        Mem.clear();
        bn1.isLesser(bn2);
        // bn1.ctIsLesser(bn2);
        long cost1 = Mem.instrCost;

        Mem.clear();
        //bn1.isLesser(bn3);
        bn3.isLesser(bn4);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnEqual(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);
        bn3.shrink();
        bn4.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);
        bn4.shrink();

        Mem.clear();
        bn1.equals(bn2);
        // bn1.ctEquals(bn2);
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn3.equals(bn4);
        // bn1.ctEquals(bn2);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnClone(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();

        bn3.zero();
        bn3.setSize((byte) 64);
        Mem.clear();
        bn3.clone(bn1);
        long cost1 = Mem.instrCost;

        bn3.zero();
        bn3.setSize((byte) 64);
        Mem.clear();
        bn3.clone(bn2);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnCopy(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1); // public value
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();

        bn3.zero();
        bn3.setSize((byte) 64);
        Mem.clear();
        bn3.copy(bn1);
        long cost1 = Mem.instrCost;

        bn3.zero();
        bn3.setSize((byte) 64);
        Mem.clear();
        bn3.copy(bn2);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnStr(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        Mem.clear();
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnGcd(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1); // public value
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);
        bn3.shrink();
        bn4.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 3 * p1), p1);
        bn4.shrink();

        Mem.clear();
        bn1.gcd(bn2);
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn3.gcd(bn4);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnIsCoprime(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);
        bn3.shrink();

        Mem.clear();
        bn4.isCoprime(bn1, bn2);
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn4.isCoprime(bn1, bn3);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnModNegate(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);
        bn3.shrink();

        Mem.clear();
        bn2.modNegate(bn1);
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn3.modNegate(bn1);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnModAdd(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);
        bn3.shrink();
        bn4.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 3 * p1), p1);
        bn4.shrink();

        Mem.clear();
        bn1.modAdd(bn2, bn4);
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn1.modAdd(bn3, bn4);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnModSub(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);
        bn3.shrink();
        bn4.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 3 * p1), p1);
        bn4.shrink();

        Mem.clear();
        bn1.modSub(bn2, bn4);
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn1.modSub(bn3, bn4);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnModExp(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);
        bn3.shrink();

        bn4.fromByteArray(modulus, (short) 0, (short) 64);
        bn4.shrink();

        Mem.clear();
        bn2.modExp(bn1, bn4);
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn3.modExp(bn1, bn4);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnModSq(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();

        bn3.fromByteArray(modulus, (short) 0, (short) 64);
        rm.fixModSqMod(bn3);

        Mem.clear();
        bn1.modSqFixed();
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn2.modSqFixed();
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnModMult(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);
        bn3.shrink();
        bn4.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 3 * p1), p1);
        bn4.shrink();
        bn5.fromByteArray(modulus, (short) 0, (short) 64);
        bn5.shrink();

        Mem.clear();
//        bn1.modMult(bn2, bn4);
        bn1.modMult(bn2, bn5);
        long cost1 = Mem.instrCost;

        Mem.clear();
//        bn1.modMult(bn3, bn4);
        bn3.modMult(bn4, bn5);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnModInv(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();

        bn3.fromByteArray(modulus, (short) 0, (short) 64);
        bn3.shrink();

        Mem.clear();
        bn1.modInv(bn3);
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn2.modInv(bn3);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzBnIsQuadraticResidue(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        bn2.shrink();

        bn3.fromByteArray(modulus, (short) 0, (short) 64);
        System.out.println(Hex.toHexString(modulus));

        Mem.clear();
        bn1.isQuadraticResidue(bn3);
        long cost1 = Mem.instrCost;

        Mem.clear();
        bn2.isQuadraticResidue(bn3);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzIntLesser(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        int1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        int2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        int3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);

        Mem.clear();
        int1.lesser(int2);
        long cost1 = Mem.instrCost;

        Mem.clear();
        int1.lesser(int3);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzIntAdd(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        int1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        int2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        int3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);

        Mem.clear();
        int1.add(int2);
        // int1.ctAdd(int2);
        long cost1 = Mem.instrCost;

        Mem.clear();
        int3.add(int4);
        // int3.ctAdd(int4);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzIntSub(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        int1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        int2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p1);
        int3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + 2 * p1), p1);

        Mem.clear();
        int1.subtract(int2);
        long cost1 = Mem.instrCost;

        Mem.clear();
        int2.subtract(int3);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzEcNeg(byte[] apduBuffer) {
        point1.decode(apduBuffer, ISO7816.OFFSET_CDATA, curve.POINT_SIZE);
        point2.decode(apduBuffer, (short) (ISO7816.OFFSET_CDATA + curve.POINT_SIZE), curve.POINT_SIZE);

        Mem.clear();
        point1.negate();
        // point1.ctNegate();
        long cost1 = Mem.instrCost;

        Mem.clear();
        point2.negate();
        // point2.ctNegate();
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzEcEqual(byte[] apduBuffer) {
        point1.decode(apduBuffer, ISO7816.OFFSET_CDATA, curve.POINT_SIZE);
        point2.decode(apduBuffer, (short) (ISO7816.OFFSET_CDATA + curve.POINT_SIZE), curve.POINT_SIZE);
        point3.decode(apduBuffer, (short) (ISO7816.OFFSET_CDATA + curve.POINT_SIZE), curve.POINT_SIZE);

        Mem.clear();
        point1.isEqual(point2);
        long cost1 = Mem.instrCost;

        Mem.clear();
        point1.isEqual(point3);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }

    void fuzzEcMul(byte[] apduBuffer) {
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        point1.decode(apduBuffer, ISO7816.OFFSET_CDATA, curve.POINT_SIZE);
        bn1.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + curve.POINT_SIZE), p1);
        bn1.shrink();
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + curve.POINT_SIZE + p1), p1);
        bn2.shrink();

        Mem.clear();
        point1.multiplication(bn1);
        // point1.ctMultiplication(bn1);
        long cost1 = Mem.instrCost;

        Mem.clear();
        point1.multiplication(bn2);
        // point1.ctMultiplication(bn2);
        long cost2 = Mem.instrCost;

        saveCost(cost1, cost2);
    }
}
