package opencrypto.jcmathlib;

public class CTUtil {

    /**
     * Copies an array from the specified source array, beginning at the specified position, to the specified position of the destination array (non-atomically).
     * @param src source byte array
     * @param srcOff offset within source byte array to start copy from
     * @param dest destination byte array
     * @param destOff offset within destination byte array to start copy into
     * @param length byte length to be copied
     * @return  destOff+length
     * @implNote If srcOff or destOff or length parameter is negative an ArrayIndexOutOfBoundsException exception is thrown.
     * @implNote If srcOff+length is greater than src.length, the length of the src array a ArrayIndexOutOfBoundsException exception is thrown and no copy is performed.
     * @implNote If destOff+length is greater than dest.length, the length of the dest array an ArrayIndexOutOfBoundsException exception is thrown and no copy is performed.
     */
    public static short ctArrayCopyNonAtomic(byte[] src, short srcOff, byte[] dest, short destOff, short length) {
        if (srcOff < 0 || destOff < 0 || length < 0
                || (short) (srcOff + length) > src.length || (short) (destOff + length) > dest.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        short srcIndex = srcOff;
        for (short destIndex = 0; destIndex < dest.length; destIndex++) {
            short validDestRange = ConstantTime.ctGreaterOrEqual(destIndex, destOff); // after destination offset and before end of copied value
            validDestRange &= ConstantTime.ctLessThan(destIndex, (short) (destOff + length));
            short validSrcRange = ConstantTime.ctLessThan(srcIndex, (short) src.length); // before end of copied value
            short validSrcIndex = ConstantTime.ctSelect(validSrcRange, srcIndex, (short) 0);

            byte destValue = dest[destIndex]; // destination value can be read always
            byte srcValue = src[validSrcIndex];

            dest[destIndex] = ConstantTime.ctSelect((short) (validDestRange & validSrcRange), srcValue, destValue);
            srcIndex += ConstantTime.ctSelect(validDestRange, (short) 1, (short) 0);
        }
        return (short) (destOff + length);
    }

    /**
     * Copies an array from the specified source array, beginning at the specified position, to the specified position of the destination array (non-atomically)
     * if non-blocked.
     * Serves mainly for blinding the actual operation for constant-time purpose
     * @param src source byte array
     * @param srcOff offset within source byte array to start copy from
     * @param dest destination byte array
     * @param destOff offset within destination byte array to start copy into
     * @param length byte length to be copied
     * @return  destOff+length
     * @implNote If srcOff or destOff or length parameter is negative an ArrayIndexOutOfBoundsException exception is thrown.
     * @implNote If srcOff+length is greater than src.length, the length of the src array a ArrayIndexOutOfBoundsException exception is thrown and no copy is performed.
     * @implNote If destOff+length is greater than dest.length, the length of the dest array an ArrayIndexOutOfBoundsException exception is thrown and no copy is performed.
     */
    // TODO make use of return value reflecting problem
    public static short ctArrayCopyNonAtomic(byte[] src, short srcOff, byte[] dest, short destOff, short length, short blind) {
        short srcOffNonNegative = ConstantTime.ctIsNonNegative(srcOff);
        short destOffNonNegative = ConstantTime.ctIsNonNegative(destOff);
        short lengthNonNegative = ConstantTime.ctIsNonNegative(length);
        short srcValidLength = (short) ~ConstantTime.ctGreater((short) (srcOff + length), (short) src.length);
        short destValidLength = (short) ~ConstantTime.ctGreater((short) (destOff + length), (short) dest.length);
        short exceptionProblem = (short) (~srcOffNonNegative | ~destOffNonNegative | ~lengthNonNegative | ~srcValidLength | ~destValidLength);

        short srcIndex = srcOff;
        for (short destIndex = 0; destIndex < dest.length; destIndex++) {
            short validDestRange = ConstantTime.ctGreaterOrEqual(destIndex, destOff); // after destination offset and before end of copied value
            validDestRange &= ConstantTime.ctLessThan(destIndex, (short) (destOff + length));
            short validSrcRange = (short) (ConstantTime.ctLessThan(srcIndex, (short) src.length) & srcOffNonNegative); // before end of copied value
            short validSrcIndex = ConstantTime.ctSelect(validSrcRange, srcIndex, (short) 0);

            byte destValue = dest[destIndex]; // destination value can be read always
            byte srcValue = src[validSrcIndex];

            dest[destIndex] = ConstantTime.ctSelect((short) (validDestRange & validSrcRange & ~blind), srcValue, destValue);
            srcIndex += ConstantTime.ctSelect(validDestRange, (short) 1, (short) 0);
        }
        return ConstantTime.ctSelect(exceptionProblem, (short) 0, (short) (destOff + length));
    }

    /**
     * Fills the byte array (non-atomically) beginning at the specified position, for the specified length with the specified byte value.
     *
     * @param bArray the byte array
     * @param bOff offset within byte array to start filling bValue into
     * @param bLen byte length to be filled
     * @param bValue the value to fill the byte array with
     * @return bOff+bLen
     * @implNote If bOff or bLen parameter is negative an ArrayIndexOutOfBoundsException exception is thrown.
     * @implNote If bOff+bLen is greater than bArray.length, the length of the bArray array an ArrayIndexOutOfBoundsException exception is thrown.
     * @implNote If bArray parameter is null a NullPointerException exception is thrown.
     */
    public static short ctArrayFillNonAtomic(byte[] bArray, short bOff, short bLen, byte bValue) {
        if (bArray == null)
            throw new NullPointerException();
        if (bOff < 0 || bLen < 0 || (short) (bOff + bLen) > bArray.length)
            throw new ArrayIndexOutOfBoundsException();
        for (short index = 0; index < bArray.length; index++) {
            short validIndex = ConstantTime.ctGreaterOrEqual(index, bOff);
            validIndex &= ConstantTime.ctLessThan(index, (short) (bOff + bLen));
            byte value = bArray[index];
            bArray[index] = ConstantTime.ctSelect(validIndex, bValue, value);
        }
        return (short) (bOff + bLen);
    }

    /**
     * Fills the byte array (non-atomically) beginning at the specified position, for the specified length with the specified byte value.
     * Serves mainly for blinding the actual operation for constant-time purpose
     * @param bArray the byte array
     * @param bOff offset within byte array to start filling bValue into
     * @param bLen byte length to be filled
     * @param bValue the value to fill the byte array with
     * @return bOff+bLen
     */
    public static short ctArrayFillNonAtomic(byte[] bArray, short bOff, short bLen, byte bValue, short blind) {
        if (bArray == null)
            throw new NullPointerException();
        if (bOff < 0 || bLen < 0 || (short) (bOff + bLen) > bArray.length)
            throw new ArrayIndexOutOfBoundsException();
        short bOffNonNegative = ConstantTime.ctIsNonNegative(bOff);
        short bLenNonNegative = ConstantTime.ctIsNonNegative(bLen);
        short validLength = ConstantTime.ctGreater((short) (bOff + bLen), (short) bArray.length);
        short exceptionProblem = (short) (~bOffNonNegative | ~bLenNonNegative | ~validLength);

        for (short index = 0; index < bArray.length; index++) {
            short validIndex = ConstantTime.ctGreaterOrEqual(index, bOff);
            validIndex &= ConstantTime.ctLessThan(index, (short) (bOff + bLen));
            byte value = bArray[index];
            bArray[index] = ConstantTime.ctSelect((short) (validIndex & blind & exceptionProblem), bValue, value);
        }
        return ConstantTime.ctSelect(exceptionProblem, (short) 0, (short) (bOff + bLen));
    }
}
