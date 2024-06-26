package opencrypto.jcmathlib;


import javacard.framework.*;
import javacard.security.CryptoException;
import javacard.security.RandomData;


public class SPAUnitTests extends Applet {

    // Applet AID: same as for UnitTests
    //public final static short CARD_TYPE = OperationSupport.SIMULATOR; // TODO set your card
    //public final static short CARD_TYPE = OperationSupport.JCOP21;
    //public final static short CARD_TYPE = OperationSupport.SECORA;
    //public final static short CARD_TYPE = OperationSupport.JCOP3_P60;
    public final static short CARD_TYPE = OperationSupport.JCOP4_P71;
    //public final static short CARD_TYPE = OperationSupport.GD60;
    //public final static short CARD_TYPE = OperationSupport.GD70;

    public final static byte CLA_OC_UT = (byte) 0xB0;
    public final static byte INS_CLEANUP = (byte) 0x03;
    public final static byte INS_FREE_MEMORY = (byte) 0x06;
    public final static byte INS_GET_ALLOCATOR_STATS = (byte) 0x07;
    public final static byte INS_GET_PROFILE_LOCKS = (byte) 0x08;

    public final static byte INS_BN_FROMARRAY = (byte) 0x20;
    public final static byte INS_BN_ADD = (byte) 0x21;
    public final static byte INS_BN_SUB = (byte) 0x22;
    public final static byte INS_BN_MUL = (byte) 0x23;
    public final static byte INS_BN_SHIFT_RIGHT = (byte) 0x24;
    public final static byte INS_BN_MUL_SCHOOL = (byte) 0x27;
    public final static byte INS_BN_SET_VALUE = (byte) 0x28;
    public final static byte INS_BN_SHIFT_LEFT = (byte) 0x29;

    // other tests
    public final static byte INS_BN_LESSER = (byte) 0x50;
    public final static byte INS_BN_EQUAL = (byte) 0x51;
    public final static byte INS_BN_RESIZE = (byte) 0x52;
    public final static byte INS_BN_PREPEND = (byte) 0x53;
    public final static byte INS_BN_CP = (byte) 0x54;
    public final static byte INS_BN_SHRINK = (byte) 0x55;
    public final static byte INS_BN_CLONE = (byte) 0x56;
    public final static byte INS_BN_ZERO = (byte) 0x57;
    public final static byte INS_BN_ONE = (byte) 0x58;
    public final static byte INS_BN_INC = (byte) 0x59;
    public final static byte INS_BN_DEC = (byte) 0x5A;
    public final static byte INS_BN_TOARRAY = (byte) 0x5B;

    // Specific codes to propagate exceptions caught
    // lower byte of exception is value as defined in JCSDK/api_classic/constant-values.htm
    public final static short SW_Exception                      = (short) 0xff01;
    public final static short SW_ArrayIndexOutOfBoundsException = (short) 0xff02;
    public final static short SW_ArithmeticException            = (short) 0xff03;
    public final static short SW_ArrayStoreException            = (short) 0xff04;
    public final static short SW_NullPointerException           = (short) 0xff05;
    public final static short SW_NegativeArraySizeException     = (short) 0xff06;
    public final static short SW_CryptoException_prefix         = (short) 0xf100;
    public final static short SW_SystemException_prefix         = (short) 0xf200;
    public final static short SW_PINException_prefix            = (short) 0xf300;
    public final static short SW_TransactionException_prefix    = (short) 0xf400;
    public final static short SW_CardRuntimeException_prefix    = (short) 0xf500;

    boolean initialized = false;

    short[] memoryInfo;
    short memoryInfoOffset = 0;

    ResourceManager rm;
    ECCurve curve;
    ECPoint point1;
    ECPoint point2;

    BigNat bn1;
    BigNat bn2;
    BigNat bn3;

    Integer int1;
    Integer int2;

    private RandomData randomDataDivision  = null;
    private byte ramDivisionData[] = null;
    final static short ramDivisionDataSize = (short) 0x100;
    final short pauseOuterCycles = 1000;
    final short pauseInnerCycles = 100;

    public SPAUnitTests() {
        OperationSupport.getInstance().setCard(CARD_TYPE);
        if (OperationSupport.getInstance().DEFERRED_INITIALIZATION != (short) 0xffff) {
            initialize();
        }
    }

    public void initialize() {
        if (initialized) {
            return;
        }
        memoryInfo = new short[(short) (7 * 3)]; // Contains RAM and EEPROM memory required for basic library objects
        memoryInfoOffset = snapshotAvailableMemory((short) 1, memoryInfo, memoryInfoOffset);
        rm = new ResourceManager((short) 256);
        memoryInfoOffset = snapshotAvailableMemory((short) 2, memoryInfo, memoryInfoOffset);


        // Pre-allocate test objects (no new allocation for every tested operation)
        curve = new ECCurve(SecP256r1.p, SecP256r1.a, SecP256r1.b, SecP256r1.G, SecP256r1.r, SecP256r1.k, rm);
        memoryInfoOffset = snapshotAvailableMemory((short) 3, memoryInfo, memoryInfoOffset);

        memoryInfoOffset = snapshotAvailableMemory((short) 5, memoryInfo, memoryInfoOffset);
        point1 = new ECPoint(curve);
        memoryInfoOffset = snapshotAvailableMemory((short) 6, memoryInfo, memoryInfoOffset);
        point2 = new ECPoint(curve);

        // Testing BigNat objects used in tests
        memoryInfoOffset = snapshotAvailableMemory((short) 7, memoryInfo, memoryInfoOffset);
        byte memoryType = JCSystem.MEMORY_TYPE_TRANSIENT_RESET;
        bn1 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        memoryInfoOffset = snapshotAvailableMemory((short) 8, memoryInfo, memoryInfoOffset);
        bn2 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);
        bn3 = new BigNat(rm.MAX_BIGNAT_SIZE, memoryType, rm);

        short intLen = 4;
        int1 = new Integer(intLen, rm);
        int2 = new Integer(intLen, rm);

        randomDataDivision = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        ramDivisionData = JCSystem.makeTransientByteArray(ramDivisionDataSize, JCSystem.CLEAR_ON_DESELECT);

        initialized = true;
    }

    public static void install(byte[] ignoredArray, short ignoredOffset, byte ignoredLength) {
        new SPAUnitTests().register();
    }

    public boolean select() {
        if (initialized) {
            updateAfterReset();
        }
        return true;
    }

    public void process(APDU apdu) {
        if (selectingApplet()) {
            return;
        }

        byte[] apduBuffer = apdu.getBuffer();

        if (apduBuffer[ISO7816.OFFSET_CLA] != CLA_OC_UT) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        // Process Input
        short dataLen = apdu.setIncomingAndReceive(); // returns length of data field

        try {
            if(!initialized) {
                initialize();
            }

            switch (apduBuffer[ISO7816.OFFSET_INS]) {
                case INS_CLEANUP:
                    rm.unlockAll();
                    break;
                case INS_FREE_MEMORY:
                    if (CARD_TYPE != OperationSupport.SIMULATOR) {
                        JCSystem.requestObjectDeletion();
                    }
                    break;
                case INS_GET_ALLOCATOR_STATS:
                    short offset = 0;
                    Util.setShort(apduBuffer, offset, rm.memAlloc.getAllocatedInRAM());
                    offset += 2;
                    Util.setShort(apduBuffer, offset, rm.memAlloc.getAllocatedInEEPROM());
                    offset += 2;
                    for (short i = 0; i < (short) memoryInfo.length; i++) {
                        Util.setShort(apduBuffer, offset, memoryInfo[i]);
                        offset += 2;
                    }
                    apdu.setOutgoingAndSend((short) 0, offset);
                    break;
                case INS_GET_PROFILE_LOCKS:
                    Util.arrayCopyNonAtomic(rm.locker.profileLockedObjects, (short) 0, apduBuffer, (short) 0, (short) rm.locker.profileLockedObjects.length);
                    apdu.setOutgoingAndSend((short) 0, (short) rm.locker.profileLockedObjects.length);
                    break;

                case INS_BN_FROMARRAY:
                    testBnFromByteArray(apdu, dataLen);
                    break;
                case INS_BN_TOARRAY:
                    testBnToByteArray(apdu, dataLen);
                    break;
                case INS_BN_ADD:
                    testBnAdd(apdu, dataLen);
                    break;
                case INS_BN_SUB:
                    testBnSub(apdu, dataLen);
                    break;
                case INS_BN_MUL:
                    testBnMul(apdu, dataLen);
                    break;
                case INS_BN_SHIFT_RIGHT:
                    testBnShiftRight(apdu, dataLen);
                    break;
                case INS_BN_SHIFT_LEFT:
                    testBnShiftLeft(apdu, dataLen);
                    break;
                case INS_BN_MUL_SCHOOL:
                    testBnMulSchool(apdu, dataLen);
                    break;
                case INS_BN_SET_VALUE:
                    testBnSetValue(apdu, dataLen);
                    break;

                case INS_BN_LESSER:
                    testBnLesser(apdu, dataLen);
                    break;
                case INS_BN_EQUAL:
                    testBnEquals(apdu, dataLen);
                    break;
                case INS_BN_RESIZE:
                    testBnResize(apdu, dataLen);
                    break;
                case INS_BN_PREPEND:
                    testBnPrepend(apdu, dataLen);
                    break;
                case INS_BN_CP:
                    testBnCp(apdu, dataLen);
                    break;
                case INS_BN_SHRINK:
                    testBnShrink(apdu, dataLen);
                    break;
                case INS_BN_CLONE:
                    testBnClone(apdu, dataLen);
                    break;
                case INS_BN_ZERO:
                    testBnZero(apdu, dataLen);
                    break;
                case INS_BN_ONE:
                    testBnOne(apdu, dataLen);
                    break;
                case INS_BN_INC:
                    testBnIncrement(apdu, dataLen);
                    break;
                case INS_BN_DEC:
                    testBnDecrement(apdu, dataLen);
                    break;
                default:
                    ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
            }
        } catch (ISOException e) {
            throw e; // Our exception from code, just re-emit
        } catch (ArrayIndexOutOfBoundsException e) {
            ISOException.throwIt(SW_ArrayIndexOutOfBoundsException);
        } catch (ArithmeticException e) {
            ISOException.throwIt(SW_ArithmeticException);
        } catch (ArrayStoreException e) {
            ISOException.throwIt(SW_ArrayStoreException);
        } catch (NullPointerException e) {
            ISOException.throwIt(SW_NullPointerException);
        } catch (NegativeArraySizeException e) {
            ISOException.throwIt(SW_NegativeArraySizeException);
        } catch (CryptoException e) {
            ISOException.throwIt((short) (SW_CryptoException_prefix | e.getReason()));
        } catch (SystemException e) {
            ISOException.throwIt((short) (SW_SystemException_prefix | e.getReason()));
        } catch (PINException e) {
            ISOException.throwIt((short) (SW_PINException_prefix | e.getReason()));
        } catch (TransactionException e) {
            ISOException.throwIt((short) (SW_TransactionException_prefix | e.getReason()));
        } catch (CardRuntimeException e) {
            ISOException.throwIt((short) (SW_CardRuntimeException_prefix | e.getReason()));
        } catch (Exception e) {
            ISOException.throwIt(SW_Exception);
        }
    }

    /* API for SPA trace detection */
    private void beginDivision() {
         for (short i = 0; i < pauseOuterCycles; i++) { }
        randomDataDivision.nextBytes(ramDivisionData, (short) 0, (short) 128);
    }

    private void middleDivision() {
        randomDataDivision.nextBytes(ramDivisionData, (short) 0, (short) 128);
        for (short i = 0; i < pauseInnerCycles; i++) { }
        randomDataDivision.nextBytes(ramDivisionData,  (short) 0, (short) 128);
    }

    private void endDivision() {
        randomDataDivision.nextBytes(ramDivisionData,  (short) 0, (short) 128);
        for (short i = 0; i < pauseInnerCycles; i++) { }
        randomDataDivision.nextBytes(ramDivisionData, (short) 0, (short) 128);
        for (short i = 0; i < pauseInnerCycles; i++) { }
        randomDataDivision.nextBytes(ramDivisionData,  (short) 0, (short) 128);
        for (short i = 0; i < pauseOuterCycles; i++) { }
    }

    /* JCMathLib functions */

    final short snapshotAvailableMemory(short tag, short[] buffer, short bufferOffset) {
        buffer[bufferOffset] = tag;
        buffer[(short) (bufferOffset + 1)] = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
        buffer[(short) (bufferOffset + 2)] = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
        return (short) (bufferOffset + 3);
    }

    void updateAfterReset() {
        if (curve != null) {
            curve.updateAfterReset();
        }
        if (rm != null) {
            rm.refreshAfterReset();
            rm.unlockAll();
        }
    }

    void testBnFromByteArray(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, dataLen);
        beginDivision();
        bn1.ctCopyToByteArray(apduBuffer, (short) 0);
        middleDivision();
        short len = bn1.ctCopyToByteArray(apduBuffer, (short) 0);
        endDivision();
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnToByteArray(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, dataLen);
        beginDivision();
        bn1.ctCopyToByteArray(apduBuffer, (short) 0);
        middleDivision();
        short len = bn1.ctCopyToByteArray(apduBuffer, (short) 0);
        endDivision();
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnAdd(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), (short) (dataLen - p1));
        bn3.setSize((short) (p1 + 1));
        bn3.copy(bn1);

        beginDivision();
        bn3.ctAdd(bn2);
        middleDivision();
        bn3.copy(bn1);
        beginDivision();
        bn3.ctAdd(bn2);
        endDivision();

        short len = bn3.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnSub(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), (short) (dataLen - p1));
        bn3.setSize((short) (p1 + 1));
        bn3.copy(bn1);
        beginDivision();
        bn3.ctSubtract(bn2);
        middleDivision();
        bn3.copy(bn1);
        beginDivision();
        bn3.ctSubtract(bn2);
        endDivision();

        short len = bn3.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnMul(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), (short) (dataLen - p1));
        bn3.clone(bn1);

        beginDivision();
        bn3.mult(bn2);
        middleDivision();
        bn3.mult(bn2);
        endDivision();

        short len = bn3.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnShiftRight(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, dataLen);

        beginDivision();
        bn1.ctShiftRightBits(p1);
        middleDivision();
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, dataLen);
        beginDivision();
        bn1.ctShiftRightBits(p1);
        endDivision();

        short len = bn1.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnShiftLeft(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, dataLen);

        beginDivision();
        bn1.ctShiftLeft(p1);
        middleDivision();
        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, dataLen);
        beginDivision();
        bn1.ctShiftLeft(p1);
        endDivision();

        short len = bn1.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnMulSchool(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), (short) (dataLen - p1));
        short previous = OperationSupport.getInstance().RSA_SQ;
        OperationSupport.getInstance().RSA_SQ = 0x0000;
        bn3.clone(bn1);

        beginDivision();
        bn3.ctMult(bn2);
        middleDivision();
        bn3.clone(bn1);
        beginDivision();
        bn3.ctMult(bn2);
        endDivision();

        OperationSupport.getInstance().RSA_SQ = previous;
        short len = bn3.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnSetValue(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short len = 0;
        if (dataLen % 2 > 0) {
            short b = apduBuffer[ISO7816.OFFSET_CDATA];
            bn1.setSize((short) 1);
            beginDivision();
            bn1.setValue(b);
            middleDivision();
            bn1.setValue(b);
            endDivision();
            len += bn1.copyToByteArray(apduBuffer, len);
        }
        if (dataLen % 4 > 1) {
            short s = Util.makeShort(apduBuffer[(short) (ISO7816.OFFSET_CDATA + 1)], apduBuffer[(short) (ISO7816.OFFSET_CDATA + 2)]);
            bn2.setSize((short) 2);
            bn1.setValue(s);
            middleDivision();
            bn1.setValue(s);
            endDivision();
            len += bn2.copyToByteArray(apduBuffer, len);
        }
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnLesser(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), (short) (dataLen - p1));

        short lesser = bn1.ctIsLesser(bn2, (short) 0, (short) 0);
        apdu.setOutgoingAndSend((short) 0, lesser);
    }

    void testBnEquals(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), (short) (dataLen - p1));

        short isEqual = bn1.ctEquals(bn2);
        apdu.setOutgoingAndSend((short) 0, isEqual);
    }

    void testBnResize(APDU apdu, short ignoredDataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        byte newSize = apduBuffer[(short) (ISO7816.OFFSET_CDATA + p1)];

        bn1.ctResize(newSize);
        apdu.setOutgoingAndSend((short) 0, (short) 0);
    }

    void testBnPrepend(APDU apdu, short ignoredDataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        byte[] arrayABuffer = rm.ARRAY_A;
        rm.lock(arrayABuffer);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        byte newSize = apduBuffer[(short) (ISO7816.OFFSET_CDATA + p1)];
        bn1.ctPrependZeros(newSize, arrayABuffer, (short) 0);

        rm.unlock(arrayABuffer);

        apdu.setOutgoingAndSend((short) 0, (short) 0);
    }
    void testBnShrink(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, dataLen);
        bn1.ctShrink();

        apdu.setOutgoingAndSend((short) 0, (short) 0);
    }

    void testBnCp(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), (short) (dataLen - p1));
        bn1.ctCopy(bn2);
        short len = bn1.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnClone(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), (short) (dataLen - p1));
        bn1.ctClone(bn2);
        short len = bn1.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnZero(APDU apdu, short ignoredDataLength) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.ctIsZero();
        apdu.setOutgoingAndSend((short) 0, (short) 0);
    }

    void testBnOne(APDU apdu, short ignoredDataLength) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn1.isOne();
        apdu.setOutgoingAndSend((short) 0, (short) 0);
    }

    void testBnIncrement(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, dataLen);
        bn1.ctIncrement();
        apdu.setOutgoingAndSend((short) 0, (short) 0);
    }

    void testBnDecrement(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, dataLen);
        bn1.ctDecrement();
        apdu.setOutgoingAndSend((short) 0, (short) 0);
    }
}
