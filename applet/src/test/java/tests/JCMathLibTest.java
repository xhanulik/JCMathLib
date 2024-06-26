package tests;

import cz.muni.fi.crocs.rcard.client.CardManager;
import cz.muni.fi.crocs.rcard.client.CardType;
import cz.muni.fi.crocs.rcard.client.Util;
import javacard.framework.ISO7816;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import opencrypto.jcmathlib.*;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Integer;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * JCMathLib Unit Tests
 *
 * @author Petr Svenda and Antonin Dufka
 */
public class JCMathLibTest extends BaseTest {
    public static byte[] APDU_CLEANUP = {UnitTests.CLA_OC_UT, UnitTests.INS_CLEANUP, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    public static int BIGNAT_BIT_LENGTH = 256;
    public static Map<String, Long> perfMap = new HashMap<>();
    public static String atr;

    public static ECParameterSpec CURVE_SPEC = null;
    public static byte[] CURVE_P = SecP256r1.p;
    public static byte[] CURVE_R = SecP256r1.r;
    public static byte[] CURVE_A = SecP256r1.a;
    public static byte[] CURVE_B = SecP256r1.b;
    public static byte[] CURVE_G = SecP256r1.G;
    public static short CURVE_K = SecP256r1.k;

    public JCMathLibTest() throws Exception {
        this.setCardType(UnitTests.CARD_TYPE == OperationSupport.SIMULATOR ? CardType.JCARDSIMLOCAL : CardType.PHYSICAL);
        this.setSimulateStateful(true);
        ECCurve curve = new ECCurve.Fp(new BigInteger(1, CURVE_P), new BigInteger(1, CURVE_A), new BigInteger(1, CURVE_B));
        BigInteger x = new BigInteger(1, Arrays.copyOfRange(CURVE_G, 1, CURVE_G.length / 2 + 1));
        BigInteger y = new BigInteger(1, Arrays.copyOfRange(CURVE_G, 1 + CURVE_G.length / 2, CURVE_G.length));
        CURVE_SPEC = new ECParameterSpec(curve, curve.createPoint(x, y), new BigInteger(1, CURVE_R), BigInteger.valueOf(CURVE_K));

        statefulCard = connect();
    }

    @Test
    public void allocationInfo() throws Exception {
        // Obtain allocated bytes in RAM and EEPROM
        CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_GET_ALLOCATOR_STATS, 0, 0, new byte[1]);
        ResponseAPDU response = statefulCard.transmit(cmd);
        Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, response.getSW());
        byte[] data = response.getData();
        System.out.printf("Data allocator: RAM = %d, EEPROM = %d%n", Util.getShort(data, (short) 0), Util.getShort(data, (short) 2));
        // Print memory snapshots from allocation
        for (int offset = 4; offset < data.length; offset += 6) {
            System.out.printf("Tag '%d': RAM = %d, EEPROM = %d%n", Util.getShort(data, offset), Util.getShort(data, (short) (offset + 2)), Util.getShort(data, (short) (offset + 4)));
        }
    }

    @Nested
    class ECCTest {
        @Test
        public void eccGen() throws Exception {
            perfMap.put("eccGen/INS_EC_GEN", new Long(-1));
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_EC_GEN, 0, 0, new byte[1]);
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("eccGen/INS_EC_GEN", statefulCard.getLastTransmitTime());
            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void eccAdd() throws Exception {
            perfMap.put("eccAdd/INS_EC_ADD", new Long(-1));
            ECPoint point1 = randECPoint();
            ECPoint point2 = randECPoint();
            ECPoint sum = point1.add(point2);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_EC_ADD, 0, 0, Util.concat(point1.getEncoded(false), point2.getEncoded(false)));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("eccAdd/INS_EC_ADD", statefulCard.getLastTransmitTime());
            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertArrayEquals(sum.getEncoded(false), resp.getData());
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void eccNegation() throws Exception {
            perfMap.put("eccNegation/INS_EC_NEG", new Long(-1));
            CardManager cardMngr = connect();
            ECPoint point = randECPoint();
            ECPoint negated = point.negate();
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_EC_NEG, point.getEncoded(false).length, 0, point.getEncoded(false));
            ResponseAPDU resp = cardMngr.transmit(cmd);
            perfMap.put("eccNegation/INS_EC_NEG", statefulCard.getLastTransmitTime());
            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertArrayEquals(negated.getEncoded(false), resp.getData());
            cardMngr.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void eccMultiplyGenerator() throws Exception {
            perfMap.put("eccMultiplyGenerator/INS_EC_MUL", new Long(-1));
            ECPoint point = CURVE_SPEC.getG();
            BigInteger scalar = randomBigNat(BIGNAT_BIT_LENGTH);
            ECPoint result = point.multiply(scalar);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_EC_MUL, scalar.toByteArray().length, 0, Util.concat(scalar.toByteArray(), point.getEncoded(false)));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("eccMultiplyGenerator/INS_EC_MUL", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertArrayEquals(result.getEncoded(false), resp.getData());
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void eccMultiplyRandom() throws Exception {
            perfMap.put("eccMultiplyRandom/INS_EC_MUL", new Long(-1));
            ECPoint point = randECPoint();
            BigInteger scalar = randomBigNat(BIGNAT_BIT_LENGTH);
            ECPoint result = point.multiply(scalar);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_EC_MUL, scalar.toByteArray().length, 0, Util.concat(scalar.toByteArray(), point.getEncoded(false)));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("eccMultiplyRandom/INS_EC_MUL", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertArrayEquals(result.getEncoded(false), resp.getData());
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void eccIsEqual() throws Exception {
            perfMap.put("eccIsEqual/INS_EC_COMPARE", new Long(-1));
            ECPoint point1 = randECPoint();
            ECPoint point2 = randECPoint();
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_EC_COMPARE, point1.getEncoded(false).length, point2.getEncoded(false).length, Util.concat(point1.getEncoded(false), point2.getEncoded(false)));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("eccIsEqual/INS_EC_COMPARE", statefulCard.getLastTransmitTime());
            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void eccDoubleGenerator() throws Exception {
            perfMap.put("eccDoubleGenerator/INS_EC_DBL", new Long(-1));
            ECPoint point = CURVE_SPEC.getG();
            ECPoint doubled = point.add(point);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_EC_DBL, 0, 0, point.getEncoded(false));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("eccDoubleGenerator/INS_EC_DBL", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertArrayEquals(doubled.getEncoded(false), resp.getData());
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void eccDoubleRandom() throws Exception {
            perfMap.put("eccDoubleRandom/INS_EC_DBL", new Long(-1));
            ECPoint point = randECPoint();
            ECPoint doubled = point.add(point);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_EC_DBL, 0, 0, point.getEncoded(false));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("eccDoubleRandom/INS_EC_DBL", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertArrayEquals(doubled.getEncoded(false), resp.getData());
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void eccFromX() throws Exception {
            perfMap.put("eccFromX/INS_EC_FROM_X", new Long(-1));
            CardManager cardMngr = connect();
            ECPoint point = randECPoint();
            ECPoint negated = point.negate();
            byte[] xCoord = point.getXCoord().getEncoded();
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_EC_FROM_X, xCoord.length, 0, xCoord);
            ResponseAPDU resp = cardMngr.transmit(cmd);
            perfMap.put("eccFromX/INS_EC_FROM_X", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertTrue(Arrays.equals(point.getEncoded(false), resp.getData()) || Arrays.equals(negated.getEncoded(false), resp.getData()));
            cardMngr.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void eccIsYEven() throws Exception {
            perfMap.put("eccIsYEven/INS_EC_IS_Y_EVEN", new Long(-1));
            CardManager cardMngr = connect();
            ECPoint point = randECPoint();
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_EC_IS_Y_EVEN, point.getEncoded(false).length, 0, point.getEncoded(false));
            ResponseAPDU resp = cardMngr.transmit(cmd);
            perfMap.put("eccIsYEven/INS_EC_IS_Y_EVEN", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(point.getYCoord().toBigInteger().mod(BigInteger.valueOf(2)).intValue() == 0 ? 1 : 0, resp.getData()[0]);
            cardMngr.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void eccMultRandomAndAdd() throws Exception {
            perfMap.put("eccMultRandomAndAdd/INS_EC_MUL_ADD", new Long(-1));
            ECPoint point1 = randECPoint();
            ECPoint point2 = randECPoint();
            BigInteger scalar = randomBigNat(BIGNAT_BIT_LENGTH);
            ECPoint result = point1.multiply(scalar).add(point2);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_EC_MUL_ADD, scalar.toByteArray().length, 0, Util.concat(Util.concat(scalar.toByteArray(), point1.getEncoded(false)), point2.getEncoded(false)));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("eccMultRandomAndAdd/INS_EC_MUL_ADD", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertArrayEquals(result.getEncoded(false), resp.getData());
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }


        @Test
        public void eccEncode() throws Exception {
            perfMap.put("eccEncode(uncompressed_in_out)/INS_EC_ENCODE", new Long(-1));
            perfMap.put("eccEncode(compressed_out)/INS_EC_ENCODE", new Long(-1));
            perfMap.put("eccEncode(compressed_in)/INS_EC_ENCODE", new Long(-1));
            perfMap.put("eccEncode(compressed_in_out)/INS_EC_ENCODE", new Long(-1));
            CardManager cardMngr = connect();
            ECPoint point = randECPoint();

            for (int i = 0; i < 2; ++i) {
                // Test both uncompressed
                CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_EC_ENCODE, point.getEncoded(false).length, 0, point.getEncoded(false));
                ResponseAPDU resp = cardMngr.transmit(cmd);
                perfMap.put("eccEncode(uncompressed_in_out)/INS_EC_ENCODE", statefulCard.getLastTransmitTime());

                Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
                Assertions.assertArrayEquals(point.getEncoded(false), resp.getData());

                // Test compressed output
                cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_EC_ENCODE, point.getEncoded(false).length, 1, point.getEncoded(false));
                resp = cardMngr.transmit(cmd);
                perfMap.put("eccEncode(compressed_out)/INS_EC_ENCODE", statefulCard.getLastTransmitTime());

                Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
                Assertions.assertArrayEquals(point.getEncoded(true), resp.getData());

                // Test compressed input
                cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_EC_ENCODE, point.getEncoded(true).length, 0, point.getEncoded(true));
                resp = cardMngr.transmit(cmd);
                perfMap.put("eccEncode(compressed_in)/INS_EC_ENCODE", statefulCard.getLastTransmitTime());

                Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
                Assertions.assertArrayEquals(point.getEncoded(false), resp.getData());

                // Test both compressed
                cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_EC_ENCODE, point.getEncoded(true).length, 1, point.getEncoded(true));
                resp = cardMngr.transmit(cmd);
                perfMap.put("eccEncode(compressed_in_out)/INS_EC_ENCODE", statefulCard.getLastTransmitTime());

                Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
                Assertions.assertArrayEquals(point.getEncoded(true), resp.getData());

                // Test with negated point
                point = point.negate();
            }

            cardMngr.transmit(new CommandAPDU(APDU_CLEANUP));
        }
    }

    @Nested
    class BigNatTest {
        @Test
        public void bigNatStorage() throws Exception {
            perfMap.put("bigNatStorage/INS_BN_STR", new Long(-1));
            BigInteger num = randomBigNat(BIGNAT_BIT_LENGTH);
            byte[] data = Util.concat(new byte[]{}, num.toByteArray());
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_STR, 0, 0, data);
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("bigNatStorage/INS_BN_STR", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertArrayEquals(data, resp.getData());
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatAddition() throws Exception {
            perfMap.put("bigNatAddition/INS_BN_ADD", new Long(-1));
            BigInteger num1 = randomBigNat(BIGNAT_BIT_LENGTH - 1);
            BigInteger num2 = randomBigNat(BIGNAT_BIT_LENGTH - 1);
            BigInteger result = num1.add(num2);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_ADD, num1.toByteArray().length, 0, Util.concat(num1.toByteArray(), num2.toByteArray()));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("bigNatAddition/INS_BN_ADD", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, new BigInteger(1, resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatSubtraction() throws Exception {
            perfMap.put("bigNatSubtraction/INS_BN_SUB", new Long(-1));
            BigInteger num1 = randomBigNat(BIGNAT_BIT_LENGTH - 1);
            BigInteger num2 = randomBigNat(BIGNAT_BIT_LENGTH - 1);
            BigInteger result = num1.subtract(num2);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_SUB, num1.toByteArray().length, 0, Util.concat(num1.toByteArray(), num2.toByteArray()));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("bigNatSubtraction/INS_BN_SUB", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, new BigInteger(resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatMultiplication() throws Exception {
            perfMap.put("bigNatMultiplication/INS_BN_MUL", new Long(-1));
            BigInteger num1 = randomBigNat(BIGNAT_BIT_LENGTH);
            BigInteger num2 = randomBigNat(BIGNAT_BIT_LENGTH);
            BigInteger result = num1.multiply(num2);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_MUL, num1.toByteArray().length, 0, Util.concat(num1.toByteArray(), num2.toByteArray()));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("bigNatMultiplication/INS_BN_MUL", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, new BigInteger(1, resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatSq() throws Exception {
            perfMap.put("bigNatSq/INS_BN_SQ", new Long(-1));
            BigInteger num1 = randomBigNat(BIGNAT_BIT_LENGTH);
            BigInteger result = num1.multiply(num1);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_SQ, 0, 0, num1.toByteArray());
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("bigNatSq/INS_BN_SQ", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, new BigInteger(1, resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatShiftRight() throws Exception {
            perfMap.put("bigNatShiftRight(8b)/INS_BN_SHIFT_RIGHT", new Long(-1));
            for (int bits = 0; bits < 8; ++bits) {
                BigInteger num1 = randomBigNat(BIGNAT_BIT_LENGTH);
                BigInteger result = num1.shiftRight(bits);
                CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_SHIFT_RIGHT, bits, 0, num1.toByteArray());
                ResponseAPDU resp = statefulCard.transmit(cmd);
                perfMap.put("bigNatShiftRight(8b)/INS_BN_SHIFT_RIGHT", statefulCard.getLastTransmitTime());

                Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
                Assertions.assertEquals(result, new BigInteger(1, resp.getData()));
            }
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatShiftLeft() throws Exception {
            perfMap.put("bigNatShiftLeft(8b)/INS_BN_SHIFT_LEFT", (long) -1);
            for (int bits = 0; bits < 8; ++bits) {
                BigInteger num1 = randomBigNat(BIGNAT_BIT_LENGTH);
                BigInteger result = num1.shiftLeft(bits);
                CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_SHIFT_LEFT, bits, 0, num1.toByteArray());
                ResponseAPDU resp = statefulCard.transmit(cmd);
                perfMap.put("bigNatShiftLeft(8b)/INS_BN_SHIFT_LEFT", statefulCard.getLastTransmitTime());

                Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
                Assertions.assertEquals(result, new BigInteger(1, resp.getData()));
            }
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatMultiplicationSlow() throws Exception {
            perfMap.put("bigNatMultiplicationSlow/INS_BN_MUL_SCHOOL", new Long(-1));
            BigInteger num1 = randomBigNat(BIGNAT_BIT_LENGTH);
            BigInteger num2 = randomBigNat(BIGNAT_BIT_LENGTH);
            BigInteger result = num1.multiply(num2);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_MUL_SCHOOL, num1.toByteArray().length, 0, Util.concat(num1.toByteArray(), num2.toByteArray()));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("bigNatMultiplicationSlow/INS_BN_MUL_SCHOOL", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, new BigInteger(1, resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatMod() throws Exception {
            perfMap.put("bigNatMod/INS_BN_MOD", new Long(-1));
            BigInteger num1 = randomBigNat(BIGNAT_BIT_LENGTH);
            BigInteger num2 = randomBigNat(BIGNAT_BIT_LENGTH - 1);
            BigInteger result = num1.mod(num2);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_MOD, (num1.toByteArray()).length, 0, Util.concat((num1.toByteArray()), (num2.toByteArray())));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("bigNatMod/INS_BN_MOD", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, new BigInteger(1, resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatSetValue() throws Exception {
            perfMap.put("bigNatSetValue/INS_BN_SET_VALUE", new Long(-1));
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_SET_VALUE, 0, 0, new byte[]{0x12, 0x34, 0x56});
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("bigNatSetValue/INS_BN_SET_VALUE", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(0x12, resp.getData()[0]);
            Assertions.assertEquals(0x3456, Util.getShort(resp.getData(), 1));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatModSqrt() throws Exception {
            perfMap.put("bigNatModSqrt/INS_BN_SQRT_MOD", new Long(-1));
            BigInteger num = randomBigNat(BIGNAT_BIT_LENGTH);
            BigInteger mod = new BigInteger(1, CURVE_P);
            // Sample num until we get a quadratic residue
            while (!num.modPow(mod.subtract(BigInteger.valueOf(1)).divide(BigInteger.valueOf(2)), mod).equals(BigInteger.valueOf(1))) {
                num = randomBigNat(BIGNAT_BIT_LENGTH);
            }
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_SQRT_MOD, Util.trimLeadingZeroes(num.toByteArray()).length, 0, Util.concat(Util.trimLeadingZeroes(num.toByteArray()), Util.trimLeadingZeroes(mod.toByteArray())));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("bigNatModSqrt/INS_BN_SQRT_MOD", statefulCard.getLastTransmitTime());

            BigInteger receivedResult = new BigInteger(1, resp.getData());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(receivedResult.modPow(BigInteger.valueOf(2), mod), num);
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatModAdd() throws Exception {
            perfMap.put("bigNatModAdd/INS_BN_ADD_MOD", new Long(-1));
            BigInteger num1 = randomBigNat(BIGNAT_BIT_LENGTH);
            BigInteger num2 = randomBigNat(BIGNAT_BIT_LENGTH);
            BigInteger num3 = new BigInteger(1, CURVE_R);

            BigInteger result = (num1.add(num2)).mod(num3);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_ADD_MOD, Util.trimLeadingZeroes(num1.toByteArray()).length, Util.trimLeadingZeroes(num2.toByteArray()).length, Util.concat(Util.trimLeadingZeroes(num1.toByteArray()), Util.trimLeadingZeroes(num2.toByteArray()), Util.trimLeadingZeroes(num3.toByteArray())));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("bigNatModAdd/INS_BN_ADD_MOD", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, new BigInteger(1, resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatModSub() throws Exception {
            perfMap.put("bigNatModSub/INS_BN_SUB_MOD", new Long(-1));
            BigInteger num1 = randomBigNat(BIGNAT_BIT_LENGTH);
            BigInteger num2 = randomBigNat(BIGNAT_BIT_LENGTH);
            BigInteger num3 = new BigInteger(1, CURVE_R);
            BigInteger result = (num1.subtract(num2)).mod(num3);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_SUB_MOD, Util.trimLeadingZeroes(num1.toByteArray()).length, Util.trimLeadingZeroes(num2.toByteArray()).length, Util.concat(Util.trimLeadingZeroes(num1.toByteArray()), Util.trimLeadingZeroes(num2.toByteArray()), Util.trimLeadingZeroes(num3.toByteArray())));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("bigNatModSub/INS_BN_SUB_MOD", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, new BigInteger(1, resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatModMult() throws Exception {
            perfMap.put("bigNatModMult/INS_BN_MUL_MOD", new Long(-1));
            BigInteger num1 = randomBigNat(BIGNAT_BIT_LENGTH);
            BigInteger num2 = randomBigNat(BIGNAT_BIT_LENGTH);
            BigInteger num3 = new BigInteger(1, CURVE_R);
            BigInteger result = (num1.multiply(num2)).mod(num3);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_MUL_MOD, Util.trimLeadingZeroes(num1.toByteArray()).length, Util.trimLeadingZeroes(num2.toByteArray()).length, Util.concat(Util.trimLeadingZeroes(num1.toByteArray()), Util.trimLeadingZeroes(num2.toByteArray()), Util.trimLeadingZeroes(num3.toByteArray())));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("bigNatModMult/INS_BN_MUL_MOD", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, new BigInteger(1, resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatModExp() throws Exception {
            perfMap.put("bigNatModExp/INS_BN_EXP_MOD", new Long(-1));
            // Test multiple configurations (to check for OperationSupport.RSA_KEY_REFRESH)
            for (int i = 0; i < 3; ++i) {
                BigInteger base = randomBigNat(BIGNAT_BIT_LENGTH);
                BigInteger exp = randomBigNat(BIGNAT_BIT_LENGTH);
                BigInteger mod = new BigInteger(1, CURVE_R);
                BigInteger result = (base.modPow(exp, mod));
                CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_EXP_MOD, Util.trimLeadingZeroes(base.toByteArray()).length, Util.trimLeadingZeroes(exp.toByteArray()).length, Util.concat(Util.trimLeadingZeroes(base.toByteArray()), Util.trimLeadingZeroes(exp.toByteArray()), Util.trimLeadingZeroes(mod.toByteArray())));
                ResponseAPDU resp = statefulCard.transmit(cmd);
                perfMap.put("bigNatModExp/INS_BN_EXP_MOD", statefulCard.getLastTransmitTime());

                Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
                Assertions.assertEquals(result, new BigInteger(1, resp.getData()));
            }
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatModSq() throws Exception {
            perfMap.put("bigNatModSq/INS_BN_SQ_MOD", new Long(-1));
            BigInteger base = randomBigNat(BIGNAT_BIT_LENGTH);
            BigInteger exp = BigInteger.valueOf(2);
            BigInteger mod = new BigInteger(1, CURVE_R);
            BigInteger result = (base.modPow(exp, mod));
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_SQ_MOD, Util.trimLeadingZeroes(base.toByteArray()).length, (short) 0, Util.concat(Util.trimLeadingZeroes(base.toByteArray()), Util.trimLeadingZeroes(mod.toByteArray())));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("bigNatModSq/INS_BN_SQ_MOD", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, new BigInteger(1, resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void bigNatModInv() throws Exception {
            perfMap.put("bigNatModInv/INS_BN_INV_MOD", new Long(-1));
            BigInteger base = randomBigNat(BIGNAT_BIT_LENGTH);
            BigInteger mod = new BigInteger(1, CURVE_R);
            BigInteger result = base.modInverse(mod);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_BN_INV_MOD, Util.trimLeadingZeroes(base.toByteArray()).length, 0, Util.concat(Util.trimLeadingZeroes(base.toByteArray()), Util.trimLeadingZeroes(mod.toByteArray())));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("bigNatModInv/INS_BN_INV_MOD", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, new BigInteger(1, resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }
    }

    @Nested
    class IntegerTest {
        @Test
        public void integerStorage() throws Exception {
            perfMap.put("integerStorage/INS_INT_STR", new Long(-1));
            int num = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_INT_STR, 0, 0, intToBytes(num));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("integerStorage/INS_INT_STR", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void integerAddition() throws Exception {
            perfMap.put("integerAddition/INS_INT_ADD", new Long(-1));
            int num1 = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
            int num2 = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
            int result = num1 + num2;
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_INT_ADD, intToBytes(num1).length, 0, Util.concat(intToBytes(num1), intToBytes(num2)));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("integerAddition/INS_INT_ADD", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, bytesToInt(resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void integerSubtraction() throws Exception {
            perfMap.put("integerSubtraction/INS_INT_SUB", new Long(-1));
            int num1 = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
            int num2 = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
            int result = num1 - num2;
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_INT_SUB, intToBytes(num1).length, 0, Util.concat(intToBytes(num1), intToBytes(num2)));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("integerSubtraction/INS_INT_SUB", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, bytesToInt(resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void integerMultiplication() throws Exception {
            perfMap.put("integerMultiplication/INS_INT_MUL", new Long(-1));
            int num1 = ThreadLocalRandom.current().nextInt(0, (int) (Math.sqrt(Integer.MAX_VALUE)));
            int num2 = ThreadLocalRandom.current().nextInt(0, (int) (Math.sqrt(Integer.MAX_VALUE)));
            int result = num1 * num2;
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_INT_MUL, intToBytes(num1).length, 0, Util.concat(intToBytes(num1), intToBytes(num2)));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("integerMultiplication/INS_INT_MUL", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, bytesToInt(resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void integerDivision() throws Exception {
            perfMap.put("integerDivision/INS_INT_DIV", new Long(-1));
            int num1 = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
            int num2 = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
            int result = num1 / num2;
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_INT_DIV, intToBytes(num1).length, 0, Util.concat(intToBytes(num1), intToBytes(num2)));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("integerDivision/INS_INT_DIV", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, bytesToInt(resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }

        @Test
        public void integerModulo() throws Exception {
            perfMap.put("integerModulo/INS_INT_MOD", new Long(-1));
            int num1 = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
            int num2 = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
            int result = num1 % num2;
            CommandAPDU cmd = new CommandAPDU(UnitTests.CLA_OC_UT, UnitTests.INS_INT_MOD, intToBytes(num1).length, 0, Util.concat(intToBytes(num1), intToBytes(num2)));
            ResponseAPDU resp = statefulCard.transmit(cmd);
            perfMap.put("integerModulo/INS_INT_MOD", statefulCard.getLastTransmitTime());

            Assertions.assertEquals(ISO7816.SW_NO_ERROR & 0xffff, resp.getSW());
            Assertions.assertEquals(result, bytesToInt(resp.getData()));
            statefulCard.transmit(new CommandAPDU(APDU_CLEANUP));
        }
    }

    public static BigInteger randomBigNat(int maxNumBitLength) {
        Random rnd = new Random();
        BigInteger aRandomBigInt;
        while (true) {
            do {
                aRandomBigInt = new BigInteger(maxNumBitLength, rnd);

            } while (aRandomBigInt.compareTo(new BigInteger("1")) < 1);

            if ((Util.trimLeadingZeroes(aRandomBigInt.toByteArray()).length != maxNumBitLength / 8) || (aRandomBigInt.toByteArray()).length != maxNumBitLength / 8) {
                // After serialization, number is longer or shorter - generate new one
            } else {
                // We have proper number
                return aRandomBigInt;
            }
        }
    }

    public static byte[] intToBytes(int val) {
        byte[] data = new byte[5];
        if (val < 0) {
            data[0] = 0x01;
        } else {
            data[0] = 0x00;
        }

        int unsigned = Math.abs(val);
        data[1] = (byte) (unsigned >>> 24);
        data[2] = (byte) (unsigned >>> 16);
        data[3] = (byte) (unsigned >>> 8);
        data[4] = (byte) unsigned;

        return data;
    }

    public static int bytesToInt(byte[] data) {
        int val = (data[1] << 24)
                | ((data[2] & 0xFF) << 16)
                | ((data[3] & 0xFF) << 8)
                | (data[4] & 0xFF);

        if (data[0] == 0x01) {
            val = val * -1;
        }

        return val;
    }

    /**
     * Utility function which will generate random valid ECPoint
     *
     * @return ECPoint
     */
    public static ECPoint randECPoint() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDSA", "BC");
        kpg.initialize(CURVE_SPEC);
        KeyPair apair = kpg.generateKeyPair();
        ECPublicKey apub = (ECPublicKey) apair.getPublic();
        return apub.getQ();
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
        // Iterating over the HashMap
        TreeMap<String, Long> sortedMap = new TreeMap<>(perfMap);
        StringBuilder contentBuilder = new StringBuilder();
        //contentBuilder.append(String.format("\n\nPERFORMANCE SUMMARY (ATR=%s)\n", atr));
        //System.out.printf("| Operation (time in ms) | %s |\n", atr);
        contentBuilder.append(String.format("| Operation (time in ms) | %s |\n", atr));

        //System.out.println("| --- | --- |");
        contentBuilder.append("| --- | --- |\n");
        //System.out.println("-----------------------------------------------------------------");
        for (Map.Entry<String, Long> entry : sortedMap.entrySet()) {
            String key = entry.getKey();
            Long value = entry.getValue();
            //System.out.printf("| %-50s | %s |%n", key, value);
            contentBuilder.append(String.format("| %-50s | %s |\n", key, value));
        }
        System.out.printf("\n\nPERFORMANCE SUMMARY (ATR=%s)\n", atr);
        System.out.println(contentBuilder.toString());
        System.out.println("-----------------------------------------------------------------");

        // Save the formatted content to a file
        String filePath = atr + ".csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(contentBuilder.toString().replace("|", ",|,"));
        } catch (IOException e) {
            System.out.println("An error occurred while saving the file: " + e.getMessage());
        }
    }

    @BeforeEach
    public void setUpMethod() {
    }

    @AfterEach
    public void tearDownMethod() throws Exception {
        if (statefulCard != null) {
            statefulCard.disconnect(true);
        }
        this.setCardType(UnitTests.CARD_TYPE == OperationSupport.SIMULATOR ? CardType.JCARDSIMLOCAL : CardType.PHYSICAL);
        this.setSimulateStateful(true);
        statefulCard = connect();
        atr = Util.toHex(statefulCard.atr().getBytes());
    }
}
