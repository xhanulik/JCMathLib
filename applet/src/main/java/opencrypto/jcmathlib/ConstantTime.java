package opencrypto.jcmathlib;

import javacard.framework.JCSystem;

public class ConstantTime {
    private static byte[] isNegativeTable = null;
    private static byte[] lessThanTable = null;

    public static void initializeLookUpTables() {
        if (isNegativeTable == null) {
            // TODO: use JCMathLib object allocator instead of standalone allocation
            isNegativeTable = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_RESET);
            for (short i = 0; i < 256; i++) {
                byte value = (byte) i;
                isNegativeTable[i] = (byte) ((value < 0) ? 0xFF : 0x00);
            }
            lessThanTable = JCSystem.makeTransientByteArray((short) 511, JCSystem.CLEAR_ON_RESET);
            for (short d = -255; d <= 255; d++) {
                lessThanTable[(short) (d + 255)] = (byte) ((d < 0) ? 0xFF : 0x00);
            }
        }
    }

    /**
     * Returns the given byte value with the MSB copied to all the other bits.
     *
     * @param  a    value to be checked
     * @return      0 or (byte) 255
     */
    public static byte ctMsb(byte a) {
        return (byte) ((-((a >> 7) & 0x01)) & (byte) 0xff);
    }

    /**
     * Returns the given short value with the MSB copied to all the other bits.
     *
     * @param  a    value to be checked
     * @return      0 or (short) 65535
     */
    public static short ctMsb(short a) {
        return (short) ((-((a >> 15) & 0x01)) & (short) 0xffff);
    }

    /**
     * Constant time check for zero byte value.
     *
     * @param  a    value to be checked
     * @return      (byte) 255 if zero, 0 otherwise
     */
    public static byte ctIsZero(byte a) {
        return ctMsb((byte) (~a & ((a & 0xff) - 1)));
    }

    /**
     * Constant time check for zero short value.
     *
     * @param  a    value to be checked
     * @return      (short) 65535 if zero, 0 otherwise
     */
    public static short ctIsZero(short a) {
        return ctMsb((short) (~a & ((a & (short) 0xffff) - 1)));
    }

    /**
     * Constant time check for non-zero byte value.
     *
     * @param  a    value to be checked
     * @return      (byte) 255 if zero, 0 otherwise
     */
    public static byte ctIsNonZero(byte a){
        return (byte) ~ctMsb((byte) (~a & ((0xff & a) - 1)));
    }

    /**
     * Constant time check for non-zero short value.
     *
     * @param  a    value to be checked
     * @return      (short) 65535 if zero, 0 otherwise
     */
    public static short ctIsNonZero(short a){
        return (short) ~ctMsb((short) (~a & (((short) 0xffff & a) - 1)));
    }

    /**
     * Compares two byte values for the first value being less than the second value.
     * Non-negative values only.
     *
     * @param a the first byte value to compare
     * @param b the second byte value to compare
     * @return 0xff if the first byte is less than the second byte, 0 otherwise
     */
    public static byte ctLessThan(byte a, byte b) {
        return ctMsb((byte) (a ^ ((a ^ b) | ((a - b) ^ b))));
    }

    public static byte ctLessThanLookUp(byte a, byte b) {
        return lessThanTable[(short) ((a & 0xFF) - (b & 0xFF) + 255)];
    }

    /**
     * Compares two short values for the first value being less than the second value.
     * Non-negative values only.
     *
     * @param a the first short value to compare
     * @param b the second short value to compare
     * @return 0xffff if the first short is less than the second short, 0 otherwise
     */
    public static short ctLessThan(short a, short b) {
        return ctMsb((short) (a ^ ((a ^ b) | ((a - b) ^ b))));
    }

    /**
     * Compares two byte values for the first value being greater or equal to the second value.
     * Non-negative values only.
     *
     * @param a the first byte value to compare
     * @param b the second byte value to compare
     * @return 0xff if the first short is greater or equal to the second short, 0 otherwise
     */
    public static byte ctGreaterOrEqual(byte a, byte b) {
        return (byte) (~ctLessThan(a, b) & 0xff);
    }

    /**
     * Compares two short values for the first value being greater or equal to the second value.
     * Non-negative values only.
     *
     * @param a the first short value to compare
     * @param b the second short value to compare
     * @return 0xffff if the first short is greater or equal to the second short, 0 otherwise
     */
    public static short ctGreaterOrEqual(short a, short b) {
        return (short) (~ctLessThan(a, b) & (short) 0xffff);
    }

    /**
     * Compares two byte values for the first value being greater to the second value.
     * Non-negative values only.
     *
     * @param a the first byte value to compare
     * @param b the second byte value to compare
     * @return 0xff if the first short is greater to the second short, 0 otherwise
     */
    public static byte ctGreater(byte a, byte b) {
        return (byte) (ctLessThan(b, a) & 0xff);
    }

    /**
     * Compares two short values for the first value being greater to the second value.
     * Non-negative values only.
     *
     * @param a the first short value to compare
     * @param b the second short value to compare
     * @return 0xffff if the first short is greater to the second short, 0 otherwise
     */
    public static short ctGreater(short a, short b) {
        return (short) (ctLessThan(b, a) & (short) 0xffff);
    }

    /**
     * Compares two byte values for the first value being equal to the second value.
     *
     * @param a the first byte value to compare
     * @param b the second byte value to compare
     * @return 0xff if the first byte is equal to the second byte, 0 otherwise
     */
    public static byte ctEqual(byte a, byte b) {
        return ctIsZero((byte) (a ^ b));
    }

    /**
     * Compares two short values for the first value being equal to the second value.
     *
     * @param a the first short value to compare
     * @param b the second short value to compare
     * @return 0xffff if the first short is equal to the second short, 0 otherwise
     */
    public static short ctEqual(short a, short b) {
        return ctIsZero((short) (a ^ b));
    }

    /**
     * Compares two byte values for the first value being not equal to the second value.
     *
     * @param a the first byte value to compare
     * @param b the second byte value to compare
     * @return 0xff if the first byte is not equal to the second byte, 0 otherwise
     */
    public static byte ctNotEqual(byte a, byte b) {
        return (byte) ~ctEqual(a, b);
    }

    /**
     * Compares two short values for the first value being not equal to the second value.
     *
     * @param a the first short value to compare
     * @param b the second short value to compare
     * @return 0xffff if the first short is not equal to the second short, 0 otherwise
     */
    public static short ctNotEqual(short a, short b) {
        return (short) ~ctEqual(a, b);
    }

    /**
     * Returns value according to provided mask.
     *
     * @param mask value for selecting a or b
     * @param a value to return if mask is 0xff
     * @param b value to return if mask is 0x00
     * @return value a or b
     */
    public static byte ctSelect(byte mask, byte a, byte b) {
        return (byte) ((mask & a) | (~mask & b));
    }
    public static byte ctSelect(short mask, byte a, byte b) {
        return (byte) ((mask & a) | (~mask & b));
    }
    public static short ctSelect(short mask, short a, short b) {
        return (short) ((mask & a) | (~mask & b));
    }
    public static short ctSelect(byte mask, short a, short b) {
        return (short) ((mask & a) | (~mask & b));
    }

    /**
     * Check whether the given number is positive
     * [1, 32767]
     *
     * @param a value to check for positivity
     * @return 0xff if a is positive, 0 otherwise
     */
    public static byte ctIsPositive(byte a) {
        return (byte) (~ctMsb(a) & ~ctIsZero(a));
    }

    public static short ctIsPositive(short a) {
        return (short) (~ctMsb(a) & ~ctIsZero(a));
    }

    /**
     * Check whether the given number is negative
     * [-32768, -1]
     *
     * @param a value to check for negativity
     * @return 0xff if a is positive, 0 otherwise
     */
    public static byte ctIsNegative(byte a) {
        return ctMsb(a);
    }

    public static byte ctIsNegativeLookUp(byte a) {
        return isNegativeTable[a & 0xff];
    }

    public static short ctIsNegative(short a) {
        return ctMsb(a);
    }

    /**
     * Check whether the given number is non-negative
     * [0, 32767]
     *
     * @param a value to check for non-negativity
     * @return 0xff if a is positive, 0 otherwise
     */
    public static byte ctIsNonNegative(byte a) {
        return (byte) ~ctMsb(a);
    }

    public static short ctIsNonNegative(short a) {
        return (short) ~ctMsb(a);
    }

    /**
     * Check whether the given number is non-positive
     * [-32768, 0]
     *
     * @param a value to check for non-positivity
     * @return 0xff if a is positive, 0 otherwise
     */
    public static byte ctIsNonPositive(byte a) {
        return (byte) (ctMsb(a) | ctIsZero(a));
    }

    public static short ctIsNonPositive(short a) {
        return (short) (ctMsb(a) | ctIsZero(a));
    }
}