package opencrypto.jcmathlib;


import javacard.framework.*;
import javacard.security.CryptoException;


public class ConstantTimeUnitTests extends Applet {

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

    public final static byte INS_BN_TOARRAY = (byte) 0x20;
    public final static byte INS_BN_ADD = (byte) 0x21;
    public final static byte INS_BN_SUB = (byte) 0x22;
    public final static byte INS_BN_MUL = (byte) 0x23;
    public final static byte INS_BN_SHIFT_RIGHT = (byte) 0x24;
    public final static byte INS_BN_MOD = (byte) 0x25;
    public final static byte INS_BN_SQ = (byte) 0x26;
    public final static byte INS_BN_MUL_SCHOOL = (byte) 0x27;
    public final static byte INS_BN_SET_VALUE = (byte) 0x28;
    public final static byte INS_BN_SHIFT_LEFT = (byte) 0x29;


    public final static byte INS_BN_ADD_MOD = (byte) 0x30;
    public final static byte INS_BN_SUB_MOD = (byte) 0x31;
    public final static byte INS_BN_MUL_MOD = (byte) 0x32;
    public final static byte INS_BN_EXP_MOD = (byte) 0x33;
    public final static byte INS_BN_INV_MOD = (byte) 0x34;
    public final static byte INS_BN_SQ_MOD = (byte) 0x35;
    public final static byte INS_BN_SQRT_MOD = (byte) 0x36;

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
    public final static byte INS_BN_INC = (byte) 0x5A;
    public final static byte INS_BN_DEC = (byte) 0x5B;
    public final static byte INS_BN_DIV = (byte) 0x5C;
    public final static byte INS_BN_NEG_MOD = (byte) 0x5D;
    public final static byte INS_BN_FROMARRAY = (byte) 0x5E;

    /* Util tests */
    public final static byte INS_BN_GET_BIT = (byte) 0x60;
    public final static byte INS_BN_SET_BIT = (byte) 0x61;
    public final static byte INS_BN_APPEND_ZEROES = (byte) 0x62;
    public final static byte INS_BN_NEG_BYTE = (byte) 0x63;
    public final static byte INS_BN_LESS_THAN_BYTE = (byte) 0x64;

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

    public ConstantTimeUnitTests() {
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
        initialized = true;
    }


    public static void install(byte[] ignoredArray, short ignoredOffset, byte ignoredLength) {
        new ConstantTimeUnitTests().register();
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
                /* Mainatiner steps */
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

                    /* BigNumInternal tests */
                case INS_BN_TOARRAY:
                    testBnToArray(apdu, dataLen);
                    break;
                case INS_BN_FROMARRAY:
                    testBnFromArray(apdu, dataLen);
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
                case INS_BN_SQ:
                    testBnSq(apdu, dataLen);
                    break;
                case INS_BN_MOD:
                    testBnMod(apdu, dataLen);
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
                case INS_BN_DIV:
                    testBnDiv(apdu, dataLen);
                    break;
                case INS_BN_GET_BIT:
                    testBnGetBit(apdu, dataLen);
                    break;
                case INS_BN_SET_BIT:
                    testBnSetBit(apdu, dataLen);
                    break;
                case INS_BN_APPEND_ZEROES:
                    testBnAppendZeroes(apdu, dataLen);
                    break;
                case INS_BN_NEG_BYTE:
                    testNegByte(apdu, dataLen);
                    break;
                case INS_BN_LESS_THAN_BYTE:
                    testLessThanByte(apdu, dataLen);
                    break;

                /* BigNat tests */
                case INS_BN_ADD_MOD:
                    testBnAddMod(apdu, dataLen);
                    break;
                case INS_BN_SUB_MOD:
                    testBnSubMod(apdu, dataLen);
                    break;
                case INS_BN_MUL_MOD:
                    testBnMulMod(apdu, dataLen);
                    break;
                case INS_BN_EXP_MOD:
                    testBnExpMod(apdu, dataLen);
                    break;
                case INS_BN_SQ_MOD:
                    testBnSqMod(apdu, dataLen);
                    break;
                case INS_BN_INV_MOD:
                    testBnInvMod(apdu, dataLen);
                    break;
                case INS_BN_SQRT_MOD:
                    testBnModSqrt(apdu, dataLen);
                    break;
                case INS_BN_NEG_MOD:
                    testBnNegMod(apdu, dataLen);
                    break;
                default:
                    ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
                    break;
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

    void testBnToArray(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, dataLen);
        short len = bn1.ctCopyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnFromArray(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();

        bn1.ctFromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, dataLen);
        short len = bn1.copyToByteArray(apduBuffer, (short) 0);

    }

    void testBnAdd(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);


        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), (short) (dataLen - p1));
        bn3.setSize((short) (p1 + 1));
        bn3.copy(bn1);
        bn3.ctAdd(bn2);
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
        bn3.ctSubtract(bn2);
        short len = bn3.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnMul(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), (short) (dataLen - p1));
        bn3.clone(bn1);
        bn3.ctMult(bn2);
        short len = bn3.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnSq(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, dataLen);
        bn1.sq();
        short len = bn1.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnShiftRight(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, dataLen);
        bn1.ctShiftRightBits(p1);
        short len = bn1.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnShiftLeft(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, dataLen);
        bn1.ctShiftLeftBits(p1);
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
        bn3.ctMult(bn2);
        OperationSupport.getInstance().RSA_SQ = previous;
        short len = bn3.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnMod(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        short p2 = (short) (apduBuffer[ISO7816.OFFSET_P2] & 0x00FF);
        bn3.setSize((short) 64);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p2);
        bn1.ctMod(bn2, bn3);
        apdu.setOutgoingAndSend((short) 0, (short) 0);
    }

    void testBnSetValue(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short len = 0;
        if (dataLen % 2 > 0) {
            short b = apduBuffer[ISO7816.OFFSET_CDATA];
            bn1.setSize((short) 1);
            bn1.setValue(b);
            len += bn1.copyToByteArray(apduBuffer, len);
        }
        if (dataLen % 4 > 1) {
            short s = Util.makeShort(apduBuffer[(short) (ISO7816.OFFSET_CDATA + 1)], apduBuffer[(short) (ISO7816.OFFSET_CDATA + 2)]);
            bn2.setSize((short) 2);
            bn2.setValue(s);
            len += bn2.copyToByteArray(apduBuffer, len);
        }
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnAddMod(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        short p2 = (short) (apduBuffer[ISO7816.OFFSET_P2] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p2);
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1 + p2), (short) (dataLen - p1 - p2));
        bn1.modAdd(bn2, bn3);
        short len = bn1.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnSubMod(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        short p2 = (short) (apduBuffer[ISO7816.OFFSET_P2] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p2);
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1 + p2), (short) (dataLen - p1 - p2));
        bn1.modSub(bn2, bn3);
        short len = bn1.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnMulMod(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        short p2 = (short) (apduBuffer[ISO7816.OFFSET_P2] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p2);
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1 + p2), (short) (dataLen - p1 - p2));
        bn1.modMult(bn2, bn3);
        short len = bn1.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnExpMod(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        short p2 = (short) (apduBuffer[ISO7816.OFFSET_P2] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), p2);
        bn3.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1 + p2), (short) (dataLen - p1 - p2));
        bn1.modExp(bn2, bn3);
        short len = bn1.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnSqMod(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), (short) (dataLen - p1));
        bn1.modSq(bn2);
        short len = bn1.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnInvMod(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), (short) (dataLen - p1));
        bn1.modInv(bn2);
        short len = bn1.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    void testBnModSqrt(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), (short) (dataLen - p1));
        bn1.modSqrt(bn2);
        short len = bn1.copyToByteArray(apduBuffer, (short) 0);
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

    void testBnDiv(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), (short) (dataLen - p1));
        bn1.remainderDivide(bn2, null);
        apdu.setOutgoingAndSend((short) 0, (short) 0);
    }

    void testBnGetBit(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short bitNum = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        CTUtil.ctGetBit(apduBuffer, (short) apduBuffer.length, bitNum);
    }

    void testBnSetBit(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short bitNum = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);
        CTUtil.ctSetBit(apduBuffer, (short) apduBuffer.length, (byte) 0, bitNum);
    }

    void testBnAppendZeroes(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short targetLength = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, dataLen);
        bn1.appendZeros(targetLength, apduBuffer, (short) 0);
    }

    void testNegByte(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        byte value = apduBuffer[ISO7816.OFFSET_CDATA];
        byte result = 0;
        ConstantTime.initializeLookUpTables();
        for (short i = 0; i < 100; i++) {
            result = ConstantTime.ctIsNegative(value);
        }
        for (short i = 0; i < 100; i++) {
            result = ConstantTime.ctIsNegativeLookUp(value);
        }
    }

    void testLessThanByte(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        byte a = apduBuffer[ISO7816.OFFSET_CDATA];
        byte b = apduBuffer[ISO7816.OFFSET_CDATA + 1];
        byte result = 0;
        ConstantTime.initializeLookUpTables();
        for (short i = 0; i < 100; i++) {
            result = ConstantTime.ctLessThan(a, b);
        }
        for (short i = 0; i < 100; i++) {
            result = ConstantTime.ctLessThan(a, b);
        }
    }

    void testBnNegMod(APDU apdu, short dataLen) {
        byte[] apduBuffer = apdu.getBuffer();
        short p1 = (short) (apduBuffer[ISO7816.OFFSET_P1] & 0x00FF);

        bn1.fromByteArray(apduBuffer, ISO7816.OFFSET_CDATA, p1);
        bn2.fromByteArray(apduBuffer, (short) (ISO7816.OFFSET_CDATA + p1), (short) (dataLen - p1));
        bn1.modNegate(bn2);
        short len = bn1.copyToByteArray(apduBuffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, len);
    }
}
