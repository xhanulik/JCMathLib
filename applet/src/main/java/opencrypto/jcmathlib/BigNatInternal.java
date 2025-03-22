package opencrypto.jcmathlib;


import javacard.framework.ISOException;
import javacard.framework.Util;

import static opencrypto.jcmathlib.ConstantTime.*;

/**
 * Based on BigNat library from <a href="https://ovchip.cs.ru.nl/OV-chip_2.0">OV-chip project.</a> by Radboud University Nijmegen
 *
 * @author Vasilios Mavroudis and Petr Svenda
 */
public class BigNatInternal {
    protected final ResourceManager rm;
    private static final short DIGIT_MASK = 0xff, DIGIT_LEN = 8, DOUBLE_DIGIT_LEN = 16, POSITIVE_DOUBLE_DIGIT_MASK = 0x7fff;

    private byte[] value;
    private short size; // The current size of internal representation in bytes.
    private short offset;

    public static final short MAX_LEN = 64;
    public static final short INTERVAL_NUM = 4;
    public static final byte MAX_DIV_CYCLES = 15;

    /**
     * Construct a BigNat of at least a given size in bytes.
     */
    public BigNatInternal(short size, byte allocatorType, ResourceManager rm) {
        this.rm = rm;
        this.offset = 1;
        this.size = size;
        this.value = rm.memAlloc.allocateByteArray((short) (size + 1), allocatorType);
    }

    /**
     * Get length of allocated value of this.
     *
     * @return number of allocated bytes fot this
     */
    public short getValueLength() {
        return (short) value.length;
    }

    /**
     * Get position of first bit of specified value in this number.
     *
     * @param bit a bit value to find
     * @return Bit index of first bit of specified value in this number. If input parameter has invalid value, return out of size position.
     */
    public short ctGetFirstBitPosition(byte bit) {
        short position = (short) (size * 8); // bogus value out of size - maximal bit in number
        for (short byteIndex = (short) (value.length - 1); byteIndex >= 0; byteIndex--) {
            for (short bitIndex = 0; bitIndex < 8; bitIndex++) {
                short validIndex = ConstantTime.ctGreaterOrEqual(byteIndex, offset);
                byte bitValue = this.value[byteIndex];
                bitValue >>>= bitIndex;
                bitValue &= (byte) 0x01;
                short bitFound = ConstantTime.ctEqual(bit, bitValue);
                short newPosition = (short) ((short) (value.length - 1 - byteIndex) * 8 + bitIndex);
                short savePosition = ConstantTime.ctLessThan(newPosition, position);
                position = ConstantTime.ctSelect((short) (savePosition & validIndex & bitFound), newPosition, position);
            }
        }
        return position;
    }

    /**
     * Set value of this from a byte array representation.
     *
     * @param source the byte array
     * @param sourceOffset offset in the byte array
     * @param length length of the value representation
     * @return number of bytes read
     */
    public short fromByteArray(byte[] source, short sourceOffset, short length) {
        short read = length <= (short) value.length ? length : (short) value.length;
        setSize(read);
        Util.arrayCopyNonAtomic(source, sourceOffset, value, offset, size);
        return size;
    }

    /**
     * Set value of this from a byte array representation.
     *
     * @param source the byte array
     * @param sourceOffset offset in the byte array
     * @param length length of the value representation
     * @param blind blind the operation
     * @return number of bytes read
     * @exception ArrayIndexOutOfBoundsException when empty source array
     */
    public short ctFromByteArray(byte[] source, short sourceOffset, short length, short blind) {
        short lengthFit = ConstantTime.ctGreaterOrEqual((short) value.length, length);
        short read = ConstantTime.ctSelect(lengthFit, length, (short) value.length);
        ctSetSize(read, blind);
        CTUtil.ctArrayCopyNonAtomic(source, sourceOffset, value, offset, size, blind);
        return size;
    }

    /**
     * Set value of this from a byte array representation.
     *
     * @param source the byte array
     * @param sourceOffset offset in the byte array
     * @param length length of the value representation
     * @return number of bytes read
     * @exception ArrayIndexOutOfBoundsException when empty source array
     */
    public short ctFromByteArray(byte[] source, short sourceOffset, short length) {
        return ctFromByteArray(source, sourceOffset, length, (short) 0x00);
    }

    /**
     * Serialize this BigNat value into a provided byte array.
     *
     * @param dst the byte array
     * @param dstOffset offset in the byte array
     * @return number of bytes written
     */
    public short copyToByteArray(byte[] dst, short dstOffset) {
        Util.arrayCopyNonAtomic(value, offset, dst, dstOffset, size);
        return size;
    }

    /**
     * Serialize this BigNat value into a provided byte array.
     * Constant time implementation regarding the length of this.value
     *
     * @param dst the byte array
     * @param dstOffset offset in the byte array
     * @param blind blind the operation
     * @return number of bytes written
     */
    public short ctCopyToByteArray(byte[] dst, short dstOffset, short blind) {
        CTUtil.ctArrayCopyNonAtomic(value, offset, dst, dstOffset, size, blind);
        return size;
    }

    /**
     * Serialize this BigNat value into a provided byte array.
     * Constant time implementation regarding the length of this.value
     *
     * @param dst the byte array
     * @param dstOffset offset in the byte array
     * @return number of bytes written
     */
    public short ctCopyToByteArray(byte[] dst, short dstOffset) {
        return ctCopyToByteArray(dst, dstOffset, (short) 0x00);
    }

    /**
     * Get size of this BigNat in bytes.
     *
     * @return size in bytes
     */
    public short length() {
        return size;
    }

    /**
     * Sets the size of this BigNat in bytes.
     * Previous value is kept so value is either non-destructively trimmed or enlarged.
     *
     * @param newSize the new size
     */
    public void setSize(short newSize) {
        if (newSize < 0 || newSize > value.length) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_RESIZETOLONGER);
        }
        size = newSize;
        offset = (short) (value.length - size);
    }

    /**
     * Sets the size of this BigNat in bytes.
     * Previous value is kept so value is either non-destructively trimmed or enlarged.
     *
     * @param newSize the new size
     * @param blind blind operation to it does not have any effect
     * @return 0xffff if error occurs, 0 otherwise
     */
    public short ctSetSizeReturnError(short newSize, short blind) {
        short error = (short) (ConstantTime.ctIsNegative(newSize) | ConstantTime.ctGreater(newSize, (short) value.length));
        size = ConstantTime.ctSelect((short) (blind | error), size, newSize);
        short newOffset = (short) (value.length - size);
        offset = ConstantTime.ctSelect((short) (blind | error), offset, newOffset);
        return error;
    }

    /**
     * Sets the size of this BigNat in bytes.
     * Previous value is kept so value is either non-destructively trimmed or enlarged.
     *
     * @param newSize the new size
     * @param blind blind operation to it does not have any effect
     */
    public void ctSetSize(short newSize, short blind) {
        blind |= (short) (ConstantTime.ctIsNegative(newSize) | ConstantTime.ctGreater(newSize, (short) value.length));
        size = ConstantTime.ctSelect(blind, size, newSize);
        short newOffset = (short) (value.length - size);
        offset = ConstantTime.ctSelect(blind, offset, newOffset);
    }

    public void ctSetSize(short newSize) {
        ctSetSize(newSize, (short) 0x00);
    }

    /**
     * Set size of this BigNat to the maximum size given during object creation.
     *
     * @param erase flag indicating whether to set internal representation to zero
     */
    public void setSizeToMax(boolean erase) {
        setSize((short) value.length);
        if (erase) {
            erase();
        }
    }

    /**
     * Set size of this BigNat to the maximum size given during object creation.
     *
     * @param erase flag indicating whether to set internal representation to zero
     */
    public void ctSetSizeToMax(boolean erase, short blind) {
        ctSetSize((short) value.length, blind);
        if (erase) {
            ctErase(blind);
        }
    }

    /**
     * Resize this BigNat value to given size in bytes. May result in truncation.
     * When value is truncated, difference is filled with zeroes.
     *
     * @param newSize new size in bytes
     */
    public void resize(short newSize) {
        if (newSize > (short) value.length) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_REALLOCATIONNOTALLOWED);
        }

        short diff = (short) (newSize - size);
        setSize(newSize);
        if (diff > 0) {
            Util.arrayFillNonAtomic(value, offset, diff, (byte) 0);
        }
    }

    public void ctResize(short newSize, short blind) {
        if (newSize > (short) value.length) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_REALLOCATIONNOTALLOWED);
        }

        short diff = (short) (newSize - size);
        ctSetSize(newSize, blind);
        short fillZeros = ConstantTime.ctIsNonPositive(diff); // whether to fill with zeroes
        short length = ConstantTime.ctSelect(fillZeros, (short) 0, diff);
        CTUtil.ctArrayFillNonAtomic(value, offset, length, (byte) 0, (short) (fillZeros | blind));
    }

    public void ctResize(short newSize) {
        ctResize(newSize, (short) 0x00);
    }

    /**
     * Append zeros to reach the defined byte length and store the result in an output buffer.
     *
     * @param targetLength required length including appended zeroes
     * @param outBuffer    output buffer for value with appended zeroes
     * @param outOffset    start offset inside outBuffer for write
     */
    public void appendZeros(short targetLength, byte[] outBuffer, short outOffset) {
        Util.arrayCopyNonAtomic(value, offset, outBuffer, outOffset, size);
        Util.arrayFillNonAtomic(outBuffer, (short) (outOffset + size), (short) (targetLength - size), (byte) 0);
    }

     /**
     * Append zeros to reach the defined byte length and store the result in an output buffer.
     * Constant-time implementation, dependent on the length of output buffer
     *
     * @param targetLength required length including appended zeroes
     * @param outBuffer    output buffer for value with appended zeroes
     * @param outOffset    start offset inside outBuffer for write
     * @implNote not checking whether target length suites into output buffer
     */
    public void ctAppendZeros(short targetLength, byte[] outBuffer, short outOffset) {
        short j = 0;
        for (short i = 0; i < outBuffer.length; i++) {
            short beforeOutRange = ConstantTime.ctLessThan(i, outOffset);
            short afterOutRange = ConstantTime.ctGreaterOrEqual(i, (short) (outOffset + size));
            short zeroPaddingRange = (short) (afterOutRange & ConstantTime.ctLessThan(i, (short) (outOffset + targetLength)));
            short validOutRange = (short) (~beforeOutRange & ~afterOutRange & ~zeroPaddingRange);

            short validThisRange = ctLessThan(j, size);
            short thisIndex = ConstantTime.ctSelect(validThisRange, j, (short) 0);
            byte thisValue = value[(short) (offset + thisIndex)];

            /* Copy bytes from this value */
            byte outBufferValue = outBuffer[i];
            outBufferValue = ConstantTime.ctSelect((short) (validOutRange & validThisRange), thisValue, outBufferValue);
            /* Append zeroes after value to get target length */
            outBuffer[i] = ConstantTime.ctSelect(zeroPaddingRange, (byte) 0, outBufferValue);
            j += ConstantTime.ctSelect(validOutRange, (short) 1, (short) 0);
        }
    }

    /**
     * Prepend zeros to reach the defined byte length and store the result in an output buffer.
     *
     * @param targetLength required length including prepended zeroes
     * @param outBuffer    output buffer for value with prepended zeroes
     * @param outOffset    start offset inside outBuffer for write
     * @implNote if targetLength < size, then start is negative, undefined behaviours
     * @implNote not checking whether target length suites into output buffer
     */
    public void prependZeros(short targetLength, byte[] outBuffer, short outOffset) {
        short start = (short) (targetLength - size);
        if (start > 0) {
            Util.arrayFillNonAtomic(outBuffer, outOffset, start, (byte) 0);
        }
        Util.arrayCopyNonAtomic(value, offset, outBuffer, (short) (outOffset + start), size);
    }

    /**
     * @implNote if targetLength < size, then start is negative, undefined behaviours
     * @implNote not checking whether target length suites into output buffer
     */
    public void ctPrependZeros(short targetLength, byte[] outBuffer, short outOffset) {
        short start = (short) (targetLength - size);
        short j = 0;
        for (short i = 0; i < outBuffer.length; i++) {
            short before = ConstantTime.ctLessThan(i, outOffset);
            short after = ConstantTime.ctGreaterOrEqual(i, (short) (outOffset + targetLength));
            short zeroes = (short) (ConstantTime.ctGreaterOrEqual(i, outOffset) & ConstantTime.ctLessThan(i, (short) (outOffset + start)));
            short validOutRange = (short) (~before & ~after & ~zeroes);

            short thisIndex = ConstantTime.ctSelect(ctLessThan(j, size), j, (short) 0);
            byte thisValue = value[(short) (offset + thisIndex)];

            /* Copy bytes from this value */
            byte outBufferValue = outBuffer[i];
            outBufferValue = ConstantTime.ctSelect(validOutRange, thisValue, outBufferValue);
            /* Append zeroes after value to get target length */
            outBuffer[i] = ConstantTime.ctSelect(zeroes, (byte) 0, outBufferValue);
            j += ConstantTime.ctSelect(validOutRange, (short) 1, (short) 0);
        }
    }

    /**
     * Remove leading zeroes from this BigNat and decrease its byte size accordingly.
     */
    public void shrink() {
        short i;
        for (i = offset; i < value.length; i++) { // Find first non-zero byte
            if (value[i] != 0) {
                break;
            }
        }
        short newSize = (short) (value.length - i);
        if (newSize < 0) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_INVALIDRESIZE);
        }
        resize(newSize);
    }

    /**
     * Refactored, not leaking offset position.
     */
    public void ctShrink(short blind) {
        short i;
        short newSize = (short) value.length;
        byte foundNonZero = 0x00;
        for (i = 0; i < value.length; i++) { // Compute size of non-zero part
            short validRange = ConstantTime.ctGreaterOrEqual(i, offset);
            byte isNonZeroValue = (byte) ~ConstantTime.ctIsZero(value[i]);
            foundNonZero = (byte) ((isNonZeroValue | foundNonZero) & validRange);
            newSize -= ConstantTime.ctSelect(foundNonZero, (short) 0, (short) 1);;
        }
        ctResize(newSize, blind);
    }

    public void ctShrink() {
        ctShrink((short) 0x00);
    }

    /**
     * Set this BigNat value to zero. Previous size is kept.
     */
    public void zero() {
        Util.arrayFillNonAtomic(value, offset, size, (byte) 0);
    }

    /**
     * Keep values before offset
     */
    public void ctZero() {
        Util.arrayFillNonAtomic(value, (short) 0, (short) value.length, (byte) 0);
    }

    public void ctZero(short blind) {
        for (short i = 0; i < value.length; i++) {
            short validIndex = ConstantTime.ctGreaterOrEqual(i, offset);
            byte thisValue = value[i];
            value[i] = ConstantTime.ctSelect((short) (validIndex & ~blind), (byte) 0, thisValue);
        }
    }

//    public void ctZero() {
//        ctZero((short) 0x00);
//    }

    /**
     * Erase the internal array of this BigNat.
     */
    public void erase() {
        Util.arrayFillNonAtomic(value, (short) 0, (short) value.length, (byte) 0);
    }

    public void ctErase(short blind) {
        // faster to zero all array than use CTUtil
        for (short i = 0; i < value.length; i++) {
            byte current = value[i];
            value[i] = ConstantTime.ctSelect(blind, current, (byte) 0);
        }
    }

//    public void ctErase() {
//        ctErase((short) 0x00);
//    }

    /**
     * Set this BigNat to a given value. Previous size is kept.
     */
    public void setValue(byte newValue) {
        zero();
        value[(short) (value.length - 1)] = (byte) (newValue & DIGIT_MASK);
    }

//    public void ctSetValue(byte newValue) {
//        ctZero();
//        value[(short) (value.length - 1)] = (byte) (newValue & DIGIT_MASK);
//    }

    /**
     * Set this BigNat to a given value. Previous size is kept.
     */
    public void setValue(short newValue) {
        zero();
        value[(short) (value.length - 1)] = (byte) (newValue & DIGIT_MASK);
        value[(short) (value.length - 2)] = (byte) ((short) (newValue >> 8) & DIGIT_MASK);
    }

//    public void ctSetValue(short newValue) {
//        ctZero();
//        value[(short) (value.length - 1)] = (byte) (newValue & DIGIT_MASK);
//        value[(short) (value.length - 2)] = (byte) ((short) (newValue >> 8) & DIGIT_MASK);
//    }

    /**
     * Copies a BigNat into this without changing size. May throw an exception if this is too small.
     *
     * @param other number to be copied
     */
    public void copy(BigNatInternal other) {
        short thisStart, otherStart, len;
        short diff = (short) (size - other.size);
        if (diff >= 0) {
            thisStart = (short) (diff + offset);
            otherStart = other.offset;
            len = other.size;
            if (diff > 0) {
                Util.arrayFillNonAtomic(value, offset, diff, (byte) 0);
            }
        } else {
            thisStart = offset;
            otherStart = (short) (other.offset - diff);
            len = size;
            // Verify here that other have leading zeroes up to otherStart
            for (short i = other.offset; i < otherStart; i++) {
                if (other.value[i] != 0) {
                    ISOException.throwIt(ReturnCodes.SW_BIGNAT_INVALIDCOPYOTHER);
                }
            }
        }
        Util.arrayCopyNonAtomic(other.value, otherStart, value, thisStart, len);
    }

    /**
     * Copies a BigNat into this without changing size. May throw an exception if this is too small.
     *
     * @param other number to be copied
     * @param blind blind the whole operation
     */
    public void ctCopy(BigNatInternal other, short blind) {
        short diff = (short) (size - other.size);
        short thisStart = ConstantTime.ctSelect(ConstantTime.ctIsNonNegative(diff), (short) (diff + offset), offset);
        short otherStart = ConstantTime.ctSelect(ConstantTime.ctIsNonNegative(diff), other.offset, (short) (other.offset - diff));
        short len = ConstantTime.ctSelect(ConstantTime.ctIsNonNegative(diff), other.size, size);
        short problem = 0;

        // Verify here that other have leading zeroes up to otherStart to report possible problem and not change result later
        for (short i = 0; i < other.value.length; i++) {
            problem = (short) (((short) ~ConstantTime.ctIsZero((short) other.value[i]) // non-zero value
                    & ConstantTime.ctLessThan(i, otherStart) // valid index in this
                    & ConstantTime.ctIsNegative(diff) // other value longer
                    & ~blind) | problem);
        }

        short copiedBytes = 0;
        for (short thisIndex = 0; thisIndex < value.length; thisIndex++) {
            /* Check whether index is in this area for copied bytes */
            short isInThisValue = ConstantTime.ctGreaterOrEqual(thisIndex, thisStart);
            isInThisValue = (short) (ConstantTime.ctLessThan(copiedBytes, len) & isInThisValue & ~problem);
            /* Read bytes from other array */
            byte otherValue = other.value[ConstantTime.ctSelect(ctLessThan(otherStart, (short) other.value.length), otherStart, (short)0)];
            byte thisValue = this.value[thisIndex];
            thisValue = ConstantTime.ctSelect((byte) (problem | blind), thisValue, (byte) 0);
            /* Store byte into index */
            value[thisIndex] = ConstantTime.ctSelect((short) (isInThisValue & ~blind & ~problem), otherValue, thisValue);
            /* Increment index in other */
            otherStart += ConstantTime.ctSelect((byte) isInThisValue, (byte) 1, (byte) 0);
        }

        if ((problem & (short) 0xffff) == (short) 0xffff) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_INVALIDCOPYOTHER);
        }
    }

    public void ctCopy(BigNatInternal other) {
        ctCopy(other, (short) 0x00);
    }

    /**
     * Copies a BigNat into this including its size. May require reallocation, which is not supported yet.
     *
     * @param other number to be cloned
     */
    public void clone(BigNatInternal other) {
        if (other.size > (short) value.length) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_REALLOCATIONNOTALLOWED);
        }

        short diff = (short) ((short) value.length - other.size);
        other.copyToByteArray(value, diff);
        if (diff > 0) {
            Util.arrayFillNonAtomic(value, (short) 0, diff, (byte) 0);
        }
        setSize(other.size);
    }

    /**
     * Copies a BigNat into this including its size. May require reallocation, which is not supported yet.
     *
     * @param other number to be cloned
     * @implNote
     */
    public void ctClone(BigNatInternal other, short blind) {
        if (other.size > (short) value.length) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_REALLOCATIONNOTALLOWED);
        }
        if (other.length() == 0) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_INVALIDCLONE);
        }

        short diff = (short) ((short) value.length - other.size);
        ctZero(blind);
        other.ctCopyToByteArray(value, diff, blind);
        ctSetSize(other.size, blind);
    }

    public void ctClone(BigNatInternal other) {
        ctClone(other, (short) 0x00);
    }

    /**
     * Test equality with zero.
     */
    public boolean isZero() {
        for (short i = offset; i < value.length; i++) {
            if (value[i] != 0) {
                return false; // CTO
            }
        }
        return true;
    }

    public short ctIsZero() {
        return ctIsZero(offset, (short) value.length);
    }

    /**
     * Test quality with zero for given part of number.
     *
     * @param offset offset in the byte array, starting index
     * @param end    ending index
     */
    public short ctIsZero(short offset, short end) {
        byte good = (byte) 0xff;
        for (short i = 0; i < value.length; i++) {
            byte validIndex = (byte) ((ctGreaterOrEqual(i, offset) & ctLessThan(i, end)) & 0xff);
            good &= (ConstantTime.ctIsZero(value[i]) & validIndex) | ~validIndex;
        }
        return good;
    }

    /**
     * Test equality with one.
     */
    public boolean isOne() {
        for (short i = offset; i < (short) (value.length - 1); i++) {
            if (value[i] != 0) {
                return false; // CTO
            }
        }
        return value[(short) (value.length - 1)] == (byte) 0x01;
    }

    public short ctIsOne() {
        short upperZero = ctIsZero((short) 0, (short) ((short) value.length - 1));
        short lowerByte = value[(short) (value.length - 1)];
        return (short) (ConstantTime.ctEqual(lowerByte, (short) 0x01) & upperZero);
    }

    /**

    /**
     * Test equality with two.
     */
    public boolean isTwo() {
        for (short i = offset; i < (short) (value.length - 1); i++) {
            if (value[i] != 0) {
                return false; // CTO
            }
        }
        return value[(short) (value.length - 1)] == (byte) 0x02;
    }

    /**

     * Check if stored BigNat is odd.
     */
    public boolean isOdd() {
        return (byte) (value[(short) (value.length - 1)] & (byte) 1) != (byte) 0;
    }

    public short ctIsOdd() {
        return (short) (value[(short) (value.length - 1)] & (byte) 1);
    }

    /**
     * Returns true if this BigNat is lesser than the other.
     * @param other Bignat to compare to
     */
    public boolean isLesser(BigNatInternal other) {
        return isLesser(other, (short) 0, (short) 0);
    }

    /**
     * Returns true if this is lesser than other shifted by a given number of digits.
     * @param other Bignat to compare to
     * @param shift left shift of other before the comparison
     * @param start digits to skip at the beginning
     * @return  true if this number is strictly less than the shifted other, false otherwise.
     */
    public boolean isLesser(BigNatInternal other, short shift, short start) {
        short j = (short) (other.size + shift - size + start + other.offset);

        for (short i = (short) (start + other.offset); i < j; ++i) {
            if (other.value[i] != 0) {
                return true;
            }
        }

        for (short i = (short) (start + offset); i < (short) value.length; i++, j++) {
            short thisValue = (short) (value[i] & DIGIT_MASK);
            short otherValue = (j >= other.offset && j < (short) other.value.length) ? (short) (other.value[j] & DIGIT_MASK) : (short) 0;
            if (thisValue < otherValue) {
                return true; // CTO
            }
            if (thisValue > otherValue) {
                return false;
            }
        }
        return false;
    }

    public short ctIsLesser(BigNatInternal other) {
        return ctIsLesser(other, (short) 0, (short) 0);
    }

    public short ctIsLesser(BigNatInternal other, short shift, short start) {
        ConstantTime.initializeLookUpTables();
        // index, where the byte positions in other corresponding to the positions in this
        // (after shifting, starting from start index)
        // j can be negative
        byte j = (byte) (other.size + shift - size + start + other.offset);

        byte otherBigger = 0;
        // check the bytes by which other is longer than this
        // if they are non-zero, then other is strictly greater than this
        for (byte i = 0; i < other.value.length; ++i) {
            byte nonZeroValue = ConstantTime.ctGreaterOrEqualLookUp(i, (byte) (start + other.offset)); // lower index in range
            nonZeroValue &= (byte) (ConstantTime.ctLessThanLookUp(i, j) & ConstantTime.ctIsNonNegativeLookUp(j)); // upper index in range
            nonZeroValue &= ConstantTime.ctIsNonZeroUnwrap(other.value[i]); // non-zero value
            otherBigger = (byte) (nonZeroValue | otherBigger);
        }

        byte thisLesser = 0x00;
        byte lesserNotSeenYet = (byte) 0xff;
        // check all bytes at positions that correspond to the number other in this
        for (byte i = 0; i < (byte) value.length; i++) {
            byte thisValue = (byte) (value[i] & DIGIT_MASK);
            byte validThisIndex = ConstantTime.ctGreaterOrEqualLookUp(i, (byte) (start + offset));

            byte validOtherIndex = (byte) (ConstantTime.ctGreaterOrEqualLookUp(j, (byte) other.offset) // lower bound
                    & ConstantTime.ctLessThanLookUp(j, (byte) other.value.length) // upper bound
                    & ConstantTime.ctIsNonNegativeLookUp(j)); // upper
            byte otherIndex = (byte) (validOtherIndex & j); // substitute bogus index when negative
            byte otherValue = (byte) (validOtherIndex & (byte) (other.value[otherIndex] & DIGIT_MASK));

            // we already checked for longer other
            // when thi range is valid, compare with other, if other is invalid, it is zero anyway
            byte thisSmaller = (byte) (ConstantTime.ctLessThanLookUp(thisValue, otherValue) & validThisIndex);
            byte thisBigger = (byte) (ConstantTime.ctLessThanLookUp(otherValue, thisValue) & validThisIndex);

            // this is lesser, no previous bytes in other were lesser
            thisLesser = ConstantTime.ctSelect((byte) (lesserNotSeenYet & thisSmaller), (byte) 0xff, thisLesser);
            // first lesser byte seen, do not take next bytes into account
            lesserNotSeenYet = (byte) ((byte) ~(lesserNotSeenYet & thisSmaller) & lesserNotSeenYet);
            // larger bytes in this observed before any smaller byte, this cannot be smaller than other
            lesserNotSeenYet = (byte) (~(lesserNotSeenYet & thisBigger) & lesserNotSeenYet);
            j += validThisIndex & 1;
        }

        return (short) (otherBigger | thisLesser);
    }

    /**
     * Value equality check.
     *
     * @param other BigNat to compare
     * @return true if this and other have the same value, false otherwise.
     */
    public boolean equals(BigNatInternal other) {
        short diff = (short) (size - other.size);

        if (diff == 0) {
            return Util.arrayCompare(value, offset, other.value, other.offset, size) == 0;
        }


        if (diff < 0) {
            short end = (short) (other.offset - diff);
            for (short i = other.offset; i < end; ++i) {
                if (other.value[i] != (byte) 0) {
                    return false;
                }
            }
            return Util.arrayCompare(value, offset, other.value, end, size) == 0;
        }

        short end = (short) (offset + diff);
        for (short i = offset; i < end; ++i) {
            if (value[i] != (byte) 0) {
                return false;
            }
        }
        short r = Util.arrayCompare(value, end, other.value, other.offset, other.size);
        return r == 0;
    }

    public short ctEquals(BigNatInternal other) {
        short diff = (short) (size - other.size);
        short newThisOffset = (short) (offset + diff);
        short newOtherOffset = (short) (other.offset - diff);
        short thisStart = ConstantTime.ctSelect(ConstantTime.ctIsPositive(diff), newThisOffset, offset);
        short otherStart = ConstantTime.ctSelect(ConstantTime.ctIsNegative(diff), newOtherOffset, other.offset);

        // If other is longer, check that there are only zeroes
        short nonZeroPrefixOther = 0;
        for (short i = 0; i < other.value.length; ++i) {
            short nonZero = ConstantTime.ctGreaterOrEqual(i, other.offset); // valid lower bound
            nonZero &= ConstantTime.ctLessThan (i, newOtherOffset); // valid upper bound
            nonZero &= ConstantTime.ctIsNonZero(other.value[i]);
            nonZeroPrefixOther = (short) (nonZero | nonZeroPrefixOther);
        }

        // If this is longer, check that there are only zeroes
        short nonZeroPrefixThis = 0;
        for (short i = (short) 0; i < value.length; ++i) {
            short nonZero = ConstantTime.ctGreaterOrEqual(i, offset); // valid lower bound
            nonZero &= ConstantTime.ctLessThan (i, newThisOffset); // valid upper bound
            nonZero &= ConstantTime.ctIsNonZero(value[i]);
            nonZeroPrefixThis = (short) (nonZero | nonZeroPrefixThis);
        }

        short result = ConstantTime.ctSelect(ConstantTime.ctIsNegative(diff), (short) ~nonZeroPrefixOther, (short) 0xffff);
        result = ConstantTime.ctSelect(ConstantTime.ctIsPositive(diff), (short) ~nonZeroPrefixThis, result);

        // Check corresponding parts
        short j = otherStart;
        for (short i = 0; i < value.length; i++) {
            short validThisIndex = ConstantTime.ctGreaterOrEqual(i, thisStart);
            short equals = ConstantTime.ctEqual(value[i], other.value[j]);
            result &= ConstantTime.ctSelect(validThisIndex, equals, (short) 0xffff);
            j += ConstantTime.ctSelect(validThisIndex, (short) 1, (short) 0);
        }
        return result;
    }

    /**
<<<<<<< HEAD
     * Test equality with a byte.
     */
    public boolean equals(byte b) {
        for (short i = offset; i < (short) (value.length - 1); i++) {
            if (value[i] != 0) {
                return false; // CTO
            }
        }
        return value[(short) (value.length - 1)] == b;
    }

    public short ctEquals(byte b) {
        short result = this.ctIsZero(offset, (short) (value.length - 1));
        return (short) (result & ConstantTime.ctEqual(value[(short) (value.length - 1)], b));
    }

    /**
     * Increment this BigNat.
     * @apiNote Does not increase size.
     */
    public void increment() {
        for (short i = (short) (value.length - 1); i >= offset; i--) {
            short tmp = (short) (value[i] & 0xff);
            value[i] = (byte) (tmp + 1);
            if (tmp < 255) {
                break; // CTO
            }
        }
    }

    public void ctIncrement() {
        byte incrementByte = (byte) 0xff;
        for (short i = (short) (value.length - 1); i >= 0; i--) {
            byte tmp = value[i];
            short validIndex = ConstantTime.ctGreaterOrEqual(i, offset);
            byte newValue = (byte) (tmp + 1);
            value[i] = ConstantTime.ctSelect((short) (validIndex & incrementByte), newValue, tmp);
            incrementByte = ConstantTime.ctEqual(tmp, (byte) 0xff);
        }
    }

    /**
     * Decrement this BigNat.
     * @apiNote Does not decrease size.
     */
    public void decrement() {
        short tmp;
        for (short i = (short) (value.length - 1); i >= offset; i--) {
            tmp = (short) (value[i] & 0xff);
            value[i] = (byte) (tmp - 1);
            if (tmp != 0) {
                break; // CTO
            }
        }
    }

    public void ctDecrement() {
        byte decrementByte = (byte) 0xff;
        for (short i = (short) (value.length - 1); i >= 0; i--) {
            byte tmp = value[i];
            short validIndex = ConstantTime.ctGreaterOrEqual(i, offset);
            byte newValue = (byte) (tmp - 1);
            value[i] = ConstantTime.ctSelect((short) (validIndex & decrementByte), newValue, tmp);
            decrementByte = ConstantTime.ctEqual(tmp, (byte) 0x00);
        }
    }

    /**
     * Add short value to this BigNat
     *
     * @param other short value to add
     */
    public byte add(short other) {
        rm.BN_WORD.lock();
        rm.BN_WORD.setValue(other);
        byte carry = add(rm.BN_WORD);
        rm.BN_WORD.unlock();
        return carry;
    }

    public byte ctAdd(short other) {
        rm.BN_WORD.lock();
        rm.BN_WORD.setValue(other);
        byte carry = ctAdd(rm.BN_WORD);
        rm.BN_WORD.unlock();
        return carry;
    }

    /**
     * Computes other * multiplier, shifts the results by shift and adds it to this.
     * Multiplier must be in range [0; 2^8 - 1].
     * Size of this must be large enough to fit the results.
     * Original implementation. Leaking data size-offset.
     */
    public byte add(BigNatInternal other) {
        return add(other, (short) 0, (short) 1);
    }

    public byte add(BigNatInternal other, short shift, short multiplier) {
        short acc = 0;
        short i = (short) (other.size - 1 + other.offset);
        short j = (short) (size - 1 - shift + offset);
        for (; i >= other.offset && j >= offset; i--, j--) {
            acc += (short) ((short) (value[j] & DIGIT_MASK) + (short) (multiplier * (other.value[i] & DIGIT_MASK)));

            value[j] = (byte) (acc & DIGIT_MASK);
            acc = (short) ((acc >> DIGIT_LEN) & DIGIT_MASK);
        }

        for (; acc > 0 && j >= offset; --j) {
            acc += (short) (value[j] & DIGIT_MASK);
            value[j] = (byte) (acc & DIGIT_MASK);
            acc = (short) ((acc >> DIGIT_LEN) & DIGIT_MASK);
        }

        // output carry bit if present
        return (byte) (((byte) (((short) (acc | -acc) & (short) 0xFFFF) >>> 15) & 0x01) << 7);
    }

    /**
     * Adds other to this. Outputs carry bit.
     * Size of this must be large enough to fit the results.
     *
     * @param other BigNat to add
     * @param blind do not process the operation, used for bogus operations
     * @return outputs carry bit if present
     */
    public byte ctAdd(BigNatInternal other, short blind) {
        short acc = 0;
        short otherIndex = (short) (other.value.length - 1);

        short length = ConstantTime.ctSelect(ConstantTime.ctIsZero(MAX_LEN), (short) this.value.length, MAX_LEN);
        length = ConstantTime.ctSelect(ConstantTime.ctLessThan(MAX_LEN, (short) this.value.length), length, (short) this.value.length);

        for (short thisIndex = (short) (length - 1); thisIndex >= 0; thisIndex--, otherIndex--) {
            // index must be in range of size of this number
            short thisValidRange = ConstantTime.ctGreaterOrEqual(thisIndex, offset);
            // index in other should be in bounds of other.value
            short otherValidRange = (short) (ConstantTime.ctGreaterOrEqual(otherIndex, other.offset) & ConstantTime.ctIsNonNegative(otherIndex));
            // prepare index for other - valid or bogus (just for some reading)
            short newOtherIndex = ConstantTime.ctSelect(otherValidRange, otherIndex, (short) 0);
            // always read something from other
            short otherBogusValue = (short) (other.value[newOtherIndex] & DIGIT_MASK);
            // get value from other - if out of other bounds, use 0
            short otherValue = ConstantTime.ctSelect(otherValidRange, otherBogusValue, (short) 0);
            // compute new value
            short thisValue = (short) (value[thisIndex] & DIGIT_MASK);
            // if we are out of size for this, add only 0
            acc += ConstantTime.ctSelect(thisValidRange, (short) (thisValue + otherValue), (short) 0);
            // set new value into this if in valid range
            short tmp = (byte) (acc & DIGIT_MASK);
            this.value[thisIndex] = ConstantTime.ctSelect((short) (thisValidRange & ~blind), (byte) tmp, (byte) thisValue);
            // preserve acc from last valid byte in this
            tmp = (short) ((acc >> DIGIT_LEN) & DIGIT_MASK);
            acc = ConstantTime.ctSelect(thisValidRange, tmp, acc);
        }
        // output carry bit if present
        return (byte) (((byte) (((short) (acc | -acc) & (short) 0xFFFF) >>> 15) & 0x01) << 7);
    }

    public byte ctAddPartitioned(BigNatInternal other, short blind) {
        short acc = 0;
        short otherIndex = (short) (other.value.length - 1);

        short partSize = (short) (this.value.length / INTERVAL_NUM);
        short length = 0;
        for (short i = 1; i <= INTERVAL_NUM; i++) {
            if (offset < (short) (i * partSize)) {
                length += partSize;
            }
        }
        if (length >= this.value.length)
            length = (short) this.value.length;

        for (short thisIndex = (short) (length - 1); thisIndex >= 0; thisIndex--, otherIndex--) {
            // index must be in range of size of this number
            short thisValidRange = ConstantTime.ctGreaterOrEqual(thisIndex, offset);
            // index in other should be in bounds of other.value
            short otherValidRange = (short) (ConstantTime.ctGreaterOrEqual(otherIndex, other.offset) & ConstantTime.ctIsNonNegative(otherIndex));
            // prepare index for other - valid or bogus (just for some reading)
            short newOtherIndex = ConstantTime.ctSelect(otherValidRange, otherIndex, (short) 0);
            // always read something from other
            short otherBogusValue = (short) (other.value[newOtherIndex] & DIGIT_MASK);
            // get value from other - if out of other bounds, use 0
            short otherValue = ConstantTime.ctSelect(otherValidRange, otherBogusValue, (short) 0);
            // compute new value
            short thisValue = (short) (value[thisIndex] & DIGIT_MASK);
            // if we are out of size for this, add only 0
            acc += ConstantTime.ctSelect(thisValidRange, (short) (thisValue + otherValue), (short) 0);
            // set new value into this if in valid range
            short tmp = (byte) (acc & DIGIT_MASK);
            this.value[thisIndex] = ConstantTime.ctSelect((short) (thisValidRange & ~blind), (byte) tmp, (byte) thisValue);
            // preserve acc from last valid byte in this
            tmp = (short) ((acc >> DIGIT_LEN) & DIGIT_MASK);
            acc = ConstantTime.ctSelect(thisValidRange, tmp, acc);
        }
        // output carry bit if present
        return (byte) (((byte) (((short) (acc | -acc) & (short) 0xFFFF) >>> 15) & 0x01) << 7);
    }

    /**
     * Implementation of addition method for number with size at most 128 bytes
     *
     * @param other number to be added
     * @param blind do not process the operation, used for bogus operations
     * @return outputs carry bit if present
     * @implNote 256 byte length not supported yet, since explicit checking for overflow with minus sign would be needed
     */
    public byte ctAddOptimized(BigNatInternal other, short blind) {
        short acc = 0;
        byte otherIndex = (byte) (other.value.length - 1);

        for (byte thisIndex = (byte) (this.value.length - 1); thisIndex >= 0; thisIndex--, otherIndex--) {
            // index must be in range of size of this number
            byte thisValidRange = ConstantTime.ctGreaterOrEqualLookUp(thisIndex, (byte) offset);
            // index in other should be in bounds of other.value
            byte otherValidRange = (byte) (ConstantTime.ctGreaterOrEqualLookUp(otherIndex, (byte) other.offset)
                    & ConstantTime.ctIsNonNegativeLookUp(otherIndex));
            // prepare index for other - valid or bogus (just for some reading)
            byte newOtherIndex = (byte) (otherValidRange & otherIndex);
            // always read something from other
            short otherBogusValue = (short) (other.value[newOtherIndex] & DIGIT_MASK);
            // get value from other - if out of other bounds, use 0
            short otherValue = (short) (otherValidRange & otherBogusValue);
            // compute new value
            short thisValue = (short) (value[thisIndex] & DIGIT_MASK);
            // if we are out of size for this, add only 0
            acc += (short) (thisValidRange & (short) (thisValue + otherValue));
            // set new value into this if in valid range
            short tmp = (byte) (acc & DIGIT_MASK);
            this.value[thisIndex] = ConstantTime.ctSelect((short) (thisValidRange & ~blind), (byte) tmp, (byte) thisValue);
            // preserve acc from last valid byte in this
            tmp = (short) ((acc >> DIGIT_LEN) & DIGIT_MASK);
            acc = ConstantTime.ctSelect(thisValidRange, tmp, acc);
        }
        // output carry bit if present
        return (byte) (((byte) (((short) (acc | -acc) & (short) 0xFFFF) >>> 15) & 0x01) << 7);
    }

    public byte ctAdd(BigNatInternal other) {
        return ctAdd(other, (short) 0x00);
    }

    /**
     * Refactored method, shift and multiplier are adding complexity.
     * Using also invalid indexes outside of this and other offset.
     */
    public byte ctAddShift(BigNatInternal other, short shift, short multiplier, short blind) {
        short acc = 0;
        short otherIndex = (short) (other.value.length - 1);

        for (short i = (short) (this.value.length - 1); i >= 0; i--, otherIndex--) {
            short thisShiftedIndex = (short) (i - shift);

            // shifted index must be in range of this number
            short thisValidRange = (short) (ConstantTime.ctIsNonNegative(thisShiftedIndex) & ConstantTime.ctGreaterOrEqual(thisShiftedIndex, offset));
            short thisValidIndex = ConstantTime.ctSelect(thisValidRange, thisShiftedIndex, (short) 0);

            // index in other should be in range
            short otherValidRange = (short) (ConstantTime.ctGreaterOrEqual(otherIndex, other.offset) & ConstantTime.ctIsNonNegative(otherIndex));
            short otherValidIndex = ConstantTime.ctSelect(otherValidRange, otherIndex, (short) 0);

            // get value from other - if out of other bounds, use 0
            short otherValue = (short) (multiplier * (other.value[otherValidIndex] & DIGIT_MASK));
            otherValue = ConstantTime.ctSelect(otherValidRange, otherValue, (short) 0);

            // compute new value if in valid range
            short newValue = (short) ((short) (value[thisValidIndex] & DIGIT_MASK) + otherValue);
            acc += ConstantTime.ctSelect(thisValidRange, newValue, (short) 0);

            // set new value only when in valid range
            byte thisValue = value[thisValidIndex];
            value[thisValidIndex] = ConstantTime.ctSelect((short) (thisValidRange & ~blind), (byte) (acc & DIGIT_MASK), thisValue);

            // preserve acc from last valid byte in this
            acc = ConstantTime.ctSelect(thisValidRange, (short) ((acc >> DIGIT_LEN) & DIGIT_MASK), acc);
        }

        // output carry bit if present
        return (byte) (((byte) (((short) (acc | -acc) & (short) 0xFFFF) >>> 15) & 0x01) << 7);
    }

    public byte ctAddShift(BigNatInternal other, short shift, short multiplier) {
        return ctAddShift(other, shift, multiplier, (short) 0x00);
    }

    /**
     * Perform addition or subtraction operation given the operation mask
     * @param other number to be added or subtracted
     * @param operation 0xffff for addition, 0x00 for subtraction
     * @return carry bit if present
     */
    public byte ctAddSubtract(BigNatInternal other, short operation, short blind) {
        short acc = 0;
        short otherIndex = (short) (other.value.length - 1);

        for (short thisIndex = (byte) (this.value.length - 1); thisIndex >= 0; thisIndex--, otherIndex--) {
            // index must be in range of size of this number
            short thisValidRange = ConstantTime.ctGreaterOrEqual(thisIndex, offset);

            // index in other should be in bounds of other.value
            short otherValidRange = (short) (ConstantTime.ctGreaterOrEqual(otherIndex, other.offset) & ConstantTime.ctIsNonNegative(otherIndex));
            // prepare index for other - valid or bogus (just for some reading)
            short newOtherIndex = (short) (otherValidRange & otherIndex);

            // always read something from other
            short otherBogusValue = (short) (other.value[newOtherIndex] & DIGIT_MASK);
            // get value from other - if out of other bounds, use 0
            short otherValue = (short) (otherValidRange & thisValidRange & otherBogusValue);
            // compute new value
            short thisValue = (short) (value[thisIndex] & DIGIT_MASK);

            // combine addition and subtraction operation according to addition mask
            acc += ConstantTime.ctSelect(operation, ConstantTime.ctSelect(thisValidRange, (short) (thisValue + otherValue), (short) 0), otherValue);
            short tmp = ConstantTime.ctSelect(operation, (byte) (acc & DIGIT_MASK), (short) ((thisValue & DIGIT_MASK) - (acc & DIGIT_MASK)));
            this.value[thisIndex] = (byte) (ConstantTime.ctSelect((short) (thisValidRange & ~blind), tmp, thisValue) & DIGIT_MASK);
            acc = ConstantTime.ctSelect(thisValidRange, (short) ((acc >> DIGIT_LEN) & DIGIT_MASK), acc);
            acc += ((tmp >> 15) & 1) & operation;
        }
        // output carry bit if present
        return ConstantTime.ctSelect((byte) operation, (byte) (((byte) (((short) (acc | -acc) & (short) 0xFFFF) >>> 15) & 0x01) << 7), (byte) (acc & 0xff));
    }

    /**
     * Subtract short value to this BigNat
     *
     * @param other short value to subtract
     */
    public void subtract(short other) {
        rm.BN_WORD.lock();
        rm.BN_WORD.setValue(other);
        subtract(rm.BN_WORD);
        rm.BN_WORD.unlock();
    }

    public void ctSubtract(short other) {
        rm.BN_WORD.lock();
        rm.BN_WORD.setValue(other);
        ctSubtract(rm.BN_WORD);
        rm.BN_WORD.unlock();
    }

    public void subtract(BigNatInternal other) {
        subtract(other, (short) 0, (short) 1);
    }

    /**
     * Computes other * multiplier, shifts the results by shift and subtract it from this.
     * Multiplier must be in range [0; 2^8 - 1].
     */
    public void subtract(BigNatInternal other, short shift, short multiplier) {
        short acc = 0;
        short i = (short) (size - 1 - shift + offset);
        short j = (short) (other.size - 1 + other.offset);
        for (; i >= offset && j >= other.offset; i--, j--) {
            acc += (short) (multiplier * (other.value[j] & DIGIT_MASK));
            short tmp = (short) ((value[i] & DIGIT_MASK) - (acc & DIGIT_MASK));

            value[i] = (byte) (tmp & DIGIT_MASK);
            acc = (short) ((acc >> DIGIT_LEN) & DIGIT_MASK);
            if (tmp < 0) {
                acc++;
            }
        }

        // deal with carry as long as there are digits left in this
        for (; i >= offset && acc != 0; --i) {
            short tmp = (short) ((value[i] & DIGIT_MASK) - (acc & DIGIT_MASK));
            value[i] = (byte) (tmp & DIGIT_MASK);
            acc = (short) ((acc >> DIGIT_LEN) & DIGIT_MASK);
            if (tmp < 0) {
                acc++;
            }
        }
    }

    /**
     * Subtract provided other BigNat from this BigNat.
     * Refactored, computes over all indexes in values, without shift and multiplier.
     * All bytes before offset are assumed to be zeroes.
     *
     * @param other BigNat to be subtracted from this
     */
    public byte ctSubtract(BigNatInternal other, short blind) {
        short acc = 0;
        byte otherIndex = (byte) (other.value.length - 1);

        for (byte thisIndex = (byte) (this.value.length - 1); thisIndex >= 0; thisIndex--, otherIndex--) {
            // compute only on valid this indexes
            byte validThisIndex = ConstantTime.ctGreaterOrEqualLookUp(thisIndex, (byte) offset);

            // check non-negative other index or set to 0
            byte validOtherIndex = (byte) (ConstantTime.ctGreaterOrEqual(otherIndex, (byte) other.offset) & ConstantTime.ctIsNonNegativeLookUp(otherIndex));
            byte newOtherIndex = (byte) (validOtherIndex & otherIndex);

            // add value to acc and subtract
            short newValue = (short) (other.value[newOtherIndex] & DIGIT_MASK);
            acc += (short) (validThisIndex & validOtherIndex) & newValue;
            short thisValue = value[thisIndex];
            short tmp = (short) ((thisValue & DIGIT_MASK) - (acc & DIGIT_MASK));

            // set new value
            value[thisIndex] = (byte) (ConstantTime.ctSelect((short) (validThisIndex & ~blind), tmp, thisValue) & DIGIT_MASK);

            // update acc
            acc = (short) ((acc >> DIGIT_LEN) & DIGIT_MASK);
            acc += (tmp >> 15) & 1;
        }
        return (byte) (acc & 0xff);
    }

    public byte ctSubtract(BigNatInternal other) {
        return ctSubtract(other, (short) 0x00);
    }

    /**
     * Refactored, computes over only valid indexes inside offsets.
     * @
     */
    public void ctSubtractShift(BigNatInternal other, byte shift, short multiplier, short blind) {
        ConstantTime.initializeLookUpTables();
        short acc = 0;
        byte otherIndex = (byte) (other.size - 1 + other.offset);

        for (byte i = (byte) (this.value.length - 1); i >= 0; i--, otherIndex--) {
            byte thisShiftedIndex = (byte) (i - shift);

            // shifted index must be in range of this number
            byte thisValidRange = (byte) (ConstantTime.ctIsNonNegativeLookUp(thisShiftedIndex)
                    & ConstantTime.ctGreaterOrEqualLookUp(thisShiftedIndex, (byte) offset));
            byte thisValidIndex = (byte) (thisValidRange & thisShiftedIndex);

            // index in other should be in range
            byte otherValidRange = (byte) (ConstantTime.ctGreaterOrEqualLookUp(otherIndex, (byte) other.offset)
                    & ConstantTime.ctIsNonNegativeLookUp(otherIndex));
            byte otherValidIndex = (byte) (otherValidRange & otherIndex);

            // computation for corresponding bytes
            short otherValue = (short) (multiplier * (other.value[otherValidIndex] & DIGIT_MASK));
            otherValue = (short) (otherValidRange & otherValue);

            // compute new value if in valid range
            acc += (short) (thisValidRange & otherValidRange) & otherValue;
            short valueToSet = (short) ((value[thisValidIndex] & DIGIT_MASK) - (acc & DIGIT_MASK));

            // set new value only when in valid range
            byte thisValue = value[thisValidIndex];
            value[thisValidIndex] = (byte) ConstantTime.ctSelect((short) (thisValidRange & ~blind), valueToSet, thisValue);
            acc = (short) ((acc >> DIGIT_LEN) & DIGIT_MASK);
            acc += (short) (ConstantTime.ctIsNegative(valueToSet) & thisValidRange) & (short) 1;
        }
    }

    public void ctSubtractShift(BigNatInternal other, byte shift, short multiplier) {
        ctSubtractShift(other, shift, multiplier, (short) 0x00);
    }

    /**
     * Multiplies this and other using software multiplications and stores results into this.
     * Original version, not time-constant.
     */
    public void mult(BigNatInternal other) {
        BigNatInternal tmp = rm.BN_F;
        tmp.lock();
        tmp.clone(this);
        setSizeToMax(true);
        for (short i = (short) (other.value.length - 1); i >= other.offset; i--) {
            add(tmp, (short) (other.value.length - 1 - i), (short) (other.value[i] & DIGIT_MASK));
        }
        shrink();
        tmp.unlock();
    }

    /**
     * Multiplies this and other using software multiplications and stores results into this.
     * Refactored method, using refactored and reimplemented add2().
     * Goes through whole other.value array.
     */
    public void ctMult(BigNatInternal other, short blind) {
        BigNatInternal tmp = rm.BN_F;
        tmp.lock();
        tmp.ctClone(this, blind);
        ctSetSizeToMax(true, blind);
        for (short i = (short) (other.value.length - 1); i >= 0; i--) {
            short otherIndex = ConstantTime.ctSelect(ConstantTime.ctGreaterOrEqual(i, other.offset), i, (short) 0);
            ctAddShift(tmp, (short) (other.value.length - 1 - otherIndex), (short) (other.value[otherIndex] & DIGIT_MASK), blind);
        }
        ctShrink(blind);
        tmp.unlock();
    }

    public void ctMult(BigNatInternal other) {
        ctMult(other, (short) 0x00);
    }

    /**
     * Refactored method, cycle over all other values.
     * Adding done directly.
     */
    public void ctMultDirect(BigNatInternal other) {
        BigNatInternal tmp = rm.BN_F;
        tmp.lock();
        tmp.ctClone(this);
        setSizeToMax(true);

        short over = 0;
        short thisStart = (short) (this.value.length - 1);
        for (short otherIndex = (short) (other.value.length - 1); otherIndex >= 0; otherIndex--) {
            short multiplier = (short) (other.value[otherIndex] & DIGIT_MASK);
            short tmpIndex = (short) (tmp.value.length - 1);
            for (short i = (short) (this.value.length - 1); i >= 0; i--) {
                // check valid index in this
                short thisValidRange = ConstantTime.ctGreaterOrEqual(thisStart, i);
                short thisIndex = ConstantTime.ctSelect(ConstantTime.ctGreaterOrEqual(thisStart, i), i, (short) 0);
                short thisValue = (short) (value[thisIndex] & DIGIT_MASK);
                thisValue = ConstantTime.ctSelect(thisValidRange, thisValue, (short) 0);
                // check index in tmp, set bogus value if needed
                short tmpValidRange = ConstantTime.ctIsNonNegative(tmpIndex);
                short tmpValidIndex = ConstantTime.ctSelect(tmpValidRange, tmpIndex, (short) 0);
                short tmpValue = tmp.value[tmpValidIndex];
                tmpValue = ConstantTime.ctSelect((short) (thisValidRange & tmpValidRange), tmpValue, (short) 0);
                // compute
                over += (short) (thisValue + (short) (tmpValue & DIGIT_MASK) * multiplier);
                // store byte
                thisValue = (byte) (value[i] & DIGIT_MASK);
                value[i] = ConstantTime.ctSelect(thisValidRange, (byte) (over & DIGIT_MASK), (byte) thisValue);
                over = (short) ((over >> DIGIT_LEN) & DIGIT_MASK);
                tmpIndex -= ConstantTime.ctSelect(thisValidRange, (short) 1, (short) 0);
            }
            thisStart--;
        }
        ctShrink();
        tmp.unlock();
    }

    public void ctMultDirectOptimized(BigNatInternal other) {
        BigNatInternal tmp = rm.BN_F;
        tmp.lock();
        tmp.ctClone(this);
        setSizeToMax(true);

        short over = 0;
        short thisStart = (short) (this.value.length - 1);
        for (short otherIndex = (short) (other.value.length - 1); otherIndex >= other.offset; otherIndex--) {
            short multiplier = (short) (other.value[otherIndex] & DIGIT_MASK);
            short tmpIndex = (short) (tmp.value.length - 1);
            for (short i = (short) (this.value.length - 1); i >= this.offset; i--) {
                // check valid index in this
                short thisValidRange = ConstantTime.ctGreaterOrEqual(thisStart, i);
                short thisIndex = ConstantTime.ctSelect(ConstantTime.ctGreaterOrEqual(thisStart, i), i, (short) 0);
                short thisValue = (short) (value[thisIndex] & DIGIT_MASK);
                thisValue = ConstantTime.ctSelect(thisValidRange, thisValue, (short) 0);
                // check index in tmp, set bogus value if needed
                short tmpValidRange = ConstantTime.ctIsNonNegative(tmpIndex);
                short tmpValidIndex = ConstantTime.ctSelect(tmpValidRange, tmpIndex, (short) 0);
                short tmpValue = tmp.value[tmpValidIndex];
                tmpValue = ConstantTime.ctSelect((short) (thisValidRange & tmpValidRange), tmpValue, (short) 0);
                // compute
                over += (short) (thisValue + (short) (tmpValue & DIGIT_MASK) * multiplier);
                // store byte
                thisValue = (byte) (value[i] & DIGIT_MASK);
                value[i] = ConstantTime.ctSelect(thisValidRange, (byte) (over & DIGIT_MASK), (byte) thisValue);
                over = (short) ((over >> DIGIT_LEN) & DIGIT_MASK);
                tmpIndex -= ConstantTime.ctSelect(thisValidRange, (short) 1, (short) 0);
            }
            thisStart--;
        }
        ctShrink();
        tmp.unlock();
    }

    /**
     * Right bit shift with carry
     *
     * @param bits number of bits to shift this by
     * @param carry XORed into the highest byte
     */
    public void shiftRightBits(short bits, short carry) {
        if (bits < 0 || bits > 7) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_INVALIDSHIFT);
        }

        short mask = (short) ((short) (1 << bits) - 1); // lowest `bits` bits set to 1
        for (short i = offset; i < (short) value.length; i++) {
            short current = (short) (value[i] & DIGIT_MASK);
            short previous = current;
            current >>= bits;
            value[i] = (byte) (current | carry);
            carry = (short) (previous & mask);
            carry <<= (short) (8 - bits);
        }
    }

    public void ctShiftRightBits(short bits, short carry, short blind) {
        if (bits < 0 || bits > 7) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_INVALIDSHIFT);
        }

        short mask = (short) ((short) (1 << bits) - 1); // lowest `bits` bits set to 1
        for (short i = 0; i < (short) value.length; i++) {
            short validRange = ConstantTime.ctGreaterOrEqual(i, offset);
            short thisValue = (short) (value[i] & DIGIT_MASK);
            short current =  ConstantTime.ctSelect(validRange, thisValue, (short) 0);
            short previous = current;

            /* use carry to compute current if in valid range */
            current >>= bits;
            current = (byte) (current | carry);
            value[i] = (byte) ConstantTime.ctSelect((short) (validRange & ~blind), current, thisValue);

            /* Update carry if in valid range */
            current = (short) ((short) (previous & mask) << (short) (8 - bits));
            carry = ConstantTime.ctSelect(validRange, current, carry);
        }
    }

    public void ctShiftRightBits(short bits, short carry) {
        ctShiftRightBits(bits, carry, (short) 0x00);
    }

    /**
     * Right bit shift
     *
     * @param bits number of bits to shift by
     */
    public void shiftRightBits(short bits) {
        shiftRightBits(bits, (short) 0);
    }
    public void ctShiftRightBits(short bits) {
        ctShiftRightBits(bits, (short) 0);
    }

    /**
     * Right byte shift
     *
     * @param bytes number of bytes to shift by
     */
    public void shiftRightBytes(short bytes) {
        if (bytes < 0)
            throw new ArrayIndexOutOfBoundsException();

        Util.arrayCopyNonAtomic(value, offset, value, (short) (offset + bytes), size);
        Util.arrayFillNonAtomic(value, offset, bytes, (byte) 0);
    }

    public void ctShiftRightBytes(short bytes, short blind) {
        if (bytes < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }

        for (short index = (short) (value.length - 1); index >= 0; index--) {
            short indexFrom = (short) (index - bytes);
            short validIndexFrom = (short) (ConstantTime.ctGreaterOrEqual(index, bytes) & ConstantTime.ctGreaterOrEqual(indexFrom, offset));
            short validIndex = ConstantTime.ctGreaterOrEqual(index, offset);
            short mask = (short) (validIndexFrom & validIndex);
            byte valueFrom = value[ConstantTime.ctSelect((short) (mask & ~blind), indexFrom, index)];
            value[index] = ConstantTime.ctSelect(mask, valueFrom, (byte) 0);
        }
    }

    /**
     * Right bit shift
     *
     * @param bits number of bytes to shift by
     */
    public void shiftRight(short bits) {
        if (bits < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }

        short bytes = (short) (bits / 8);
        bits = (short) (bits - (bytes * 8));
        shiftRightBytes(bytes);
        shiftRightBits(bits);
    }


    public void ctShiftRight(short bits, short blind) {
        if (bits < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }

        short bytes = (short) (bits >>> 3); // bits / 8
        bits = (short) (bits - (bytes * 8));
        ctShiftRightBytes(bytes, blind);
        ctShiftRightBits(bits, (short) 0, blind);
    }

    /**
     * Left byte shift
     *
     * @param bytes number of bytes to shift by
     */
    public void ctShiftLeftBytes(short bytes) {
        if (bytes < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        short newOffset = offset;
        for (short index = 0; index < value.length; index++) {
            short indexFrom = (short) (index + bytes);
            short validIndexFrom = (short) (ConstantTime.ctGreaterOrEqual(indexFrom, offset) & ConstantTime.ctLessThan(indexFrom, (short) value.length));
            newOffset = ConstantTime.ctSelect((short) (validIndexFrom & ConstantTime.ctLessThan(index, newOffset)), index, newOffset);
            byte valueFrom = value[ConstantTime.ctSelect(validIndexFrom, indexFrom, (short) 0)];
            value[index] = ConstantTime.ctSelect(validIndexFrom, valueFrom, (byte) 0);
        }
        ctSetSize((short) (size + (offset - newOffset)));
    }

    /**
     * Left bit shift with carry
     *
     * @param bits number of bits to shift by
     * @param carry ORed into the lowest byte
     */
    protected void shiftLeftBits(short bits, short carry) {
        if (bits < 0 || bits > 7) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_INVALIDSHIFT);
        }

        short mask = (short) ((-1 << (8 - bits)) & 0xff); // highest `bits` bits set to 1
        for (short i = (short) (value.length - 1); i >= offset; --i) {
            short current = (short) (value[i] & 0xff);
            short previous = current;
            current <<= bits;
            value[i] = (byte) (current | carry);
            carry = (short) (previous & mask);
            carry >>>= (8 - bits);
        }

        if (carry != 0) {
            setSize((short) (size + 1));
            value[offset] = (byte) carry;
        }
    }

    public void ctShiftLeftBits(short bits, short carry) {
        if (bits < 0 || bits > 7) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_INVALIDSHIFT);
        }

        short mask = (short) ((-1 << (8 - bits)) & 0xff); // highest `bits` bits set to 1
        for (short i = (short) (value.length - 1); i >= 0; --i) {
            short validRange = ConstantTime.ctGreaterOrEqual(i, offset);
            short thisValue = (short) (value[i] & 0xff);
            short current = ConstantTime.ctSelect(validRange, thisValue, (short) 0);
            short previous = current;

            /* use carry to compute current if in valid range */
            current <<= bits;
            current = (byte) (current | carry);
            value[i] = (byte) ConstantTime.ctSelect(validRange, current, thisValue);

            /* update carry if in valid range */
            current = (short) ((short) (previous & mask) >>> (short) (8 - bits));
            carry = ConstantTime.ctSelect(validRange, current, carry);
        }

        short newSize = (short) (size + 1);
        short sizeMask = (short) (ConstantTime.ctIsNonZero(carry) & ctLessThan(newSize, (short) value.length));
        newSize = ConstantTime.ctSelect(sizeMask, newSize, size);
        setSize(newSize);
        short valueAtOffset = value[offset];
        value[offset] = (byte) ConstantTime.ctSelect(sizeMask, carry, valueAtOffset);
    }

    /**
     * Left bit shift
     *
     * @param bits number of bits to shift by
     */
    public void shiftLeftBits(short bits) {
        shiftLeftBits(bits, (short) 0);
    }

    public void ctShiftLeftBits(short bits) {
        ctShiftLeftBits(bits, (short) 0);
    }

    /**
     * Left bit shift
     *
     * @param bits number of bytes to shift by
     */
    public void ctShiftLeft(short bits) {
        if (bits < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }

        short bytes = (short) (bits >>> 3); // bits / 8
        bits = (short) (bits - (bytes * 8));
        ctShiftLeftBytes(bytes);
        ctShiftLeftBits(bits);
    }

    /**
     * Divide this by divisor and store the remained in this and quotient in quotient.
     *
     * Quadratic complexity in digit difference of this and divisor.
     *
     * @param divisor non-zero number
     * @param quotient may be null
     */
    public void remainderDivide(BigNatInternal divisor, BigNatInternal quotient) {
        if (quotient != null) {
            quotient.setSizeToMax(true);
        }

        short divisorIndex = divisor.offset;
        while (divisorIndex < (short) (divisor.value.length - 1) && divisor.value[divisorIndex] == 0) { // move to first nonzero digit
            divisorIndex++;
        }

        short divisorShift = (short) (size - divisor.size + divisorIndex - divisor.offset);
        short divisionRound = 0;
        short firstDivisorDigit = (short) (divisor.value[divisorIndex] & DIGIT_MASK); // first nonzero digit
        short divisorBitShift = (short) (highestOneBit((short) (firstDivisorDigit + 1)) - 1); // in short from left -1
        byte secondDivisorDigit = divisorIndex < (short) (divisor.value.length - 1) ? divisor.value[(short) (divisorIndex + 1)] : 0;
        byte thirdDivisorDigit = divisorIndex < (short) (divisor.value.length - 2) ? divisor.value[(short) (divisorIndex + 2)] : 0;

        while (divisorShift >= 0) {
            while (!isLesser(divisor, divisorShift, (short) (divisionRound > 0 ? divisionRound - 1 : 0))) {
                short divisionRoundOffset = (short) (divisionRound + offset);
                short dividentDigits = divisionRound == 0 ? 0 : (short) ((short) (value[(short) (divisionRoundOffset - 1)]) << DIGIT_LEN);
                dividentDigits |= (short) (value[(short) (divisionRound + offset)] & DIGIT_MASK);

                short divisorDigit;
                if (dividentDigits < 0) {
                    dividentDigits = (short) ((dividentDigits >>> 1) & POSITIVE_DOUBLE_DIGIT_MASK);
                    divisorDigit = (short) ((firstDivisorDigit >>> 1) & POSITIVE_DOUBLE_DIGIT_MASK);
                } else {
                    short dividentBitShift = (short) (highestOneBit(dividentDigits) - 1);
                    short bitShift = dividentBitShift <= divisorBitShift ? dividentBitShift : divisorBitShift;

                    dividentDigits = shiftBits(
                            dividentDigits, divisionRound < (short) (size - 1) ? value[(short) (divisionRoundOffset + 1)] : 0,
                            divisionRound < (short) (size - 2) ? value[(short) (divisionRoundOffset + 2)] : 0,
                            bitShift
                    );
                    divisorDigit = shiftBits(firstDivisorDigit, secondDivisorDigit, thirdDivisorDigit, bitShift);
                }

                short multiple = (short) (dividentDigits / (short) (divisorDigit + 1));
                if (multiple < 1) {
                    multiple = 1;
                }

                subtract(divisor, divisorShift, multiple);

                if (quotient != null) {
                    short divisorShiftOffset = (short) (divisorShift - quotient.offset);
                    short quotientDigit = (short) ((quotient.value[(short) (quotient.size - 1 - divisorShiftOffset)] & DIGIT_MASK) + multiple);
                    quotient.value[(short) (quotient.size - 1 - divisorShiftOffset)] = (byte) quotientDigit;
                }
            }
            divisionRound++;
            divisorShift--;
        }
        if (quotient != null) {
            quotient.shrink();
        }
    }

    /**
     *
     * @param divisor
     * @param quotient
     * @implNote max size of number -> 128 bytes
     */
    public void ctRemainderDivideOptimized(BigNatInternal divisor, BigNatInternal quotient) {
        ConstantTime.initializeLookUpTables();

        quotient.zero(); // make it faster with JavaCard implementation

        byte divisorIndex = (byte) divisor.offset;
        for (byte i = 0; i < divisor.value.length; i++) {// move to first nonzero digit
            byte mask = (byte) (ConstantTime.ctLessThanLookUp(divisorIndex, (byte) (divisor.value.length - 1))
                    & ConstantTime.ctIsZeroUnwrap(divisor.value[divisorIndex]));
            divisorIndex += (mask & 1);
        }

        byte divisorShift = (byte) (size - divisor.size + divisorIndex - divisor.offset);
        byte divisionRound = 0;
        short firstDivisorDigit = (short) (divisor.value[divisorIndex] & DIGIT_MASK); // first nonzero digit
        short divisorBitShift = (short) (ctHighestOneBit((short) (firstDivisorDigit + 1)) - 1); // in short from left -1
        short tmpIndex = (short) (ConstantTime.ctGreater((short) (divisor.value.length - 1), divisorIndex) & (short) (divisorIndex + 1)); // ctSelect
        byte secondDivisorDigit = (byte) (ConstantTime.ctGreaterLookUp((byte) (divisor.value.length - 1), divisorIndex) & divisor.value[tmpIndex]);  // ctSelect
        tmpIndex = (short) (ConstantTime.ctGreater((short) (divisor.value.length - 2), divisorIndex) & (short) (divisorIndex + 2)); // ctSelect
        byte thirdDivisorDigit = (byte) (ConstantTime.ctGreaterLookUp((byte) (divisor.value.length - 2), divisorIndex) & divisor.value[tmpIndex]); // ctSelect

        for (byte i = 0; i < MAX_DIV_CYCLES; i++) {
            // !isLesser branch condition
            short divisorShiftNegative = ConstantTime.ctIsNegative(divisorShift);
            short isLesserDivisor = ctIsLesser(divisor, divisorShift, (short) (ConstantTime.ctIsPositive(divisionRound) & (short) (divisionRound - 1)));
            short doSubtract = ((short) (~divisorShiftNegative & ~isLesserDivisor));
            // inside the branch
            short divisionRoundOffset = (short) (divisionRound + offset);
            tmpIndex = (short) (doSubtract & (short) (divisionRoundOffset - 1)); // ctSelect
            short newDividentDigits = (short) ((short) (value[tmpIndex]) << DIGIT_LEN);
            tmpIndex = (short) (doSubtract & (short) (divisionRound + offset));  // ctSelect
            short dividentDigits = (short) (~ConstantTime.ctIsZeroUnwrap(divisionRound) & newDividentDigits // ctSelect
                    | (short) (value[tmpIndex] & DIGIT_MASK));
            short dividentDigitsNegative = ConstantTime.ctIsNegativeUnwrap(dividentDigits);
            short dividentBitShift = (short) (ctHighestOneBit(dividentDigits) - 1);
            short bitShift = ConstantTime.ctSelect(ConstantTime.ctGreaterOrEqual(divisorBitShift, dividentBitShift), dividentBitShift, divisorBitShift);

            tmpIndex = ConstantTime.ctSelect((short) (doSubtract
                                                & ConstantTime.ctGreaterLookUp((byte) (size - 1), divisionRound)
                                                & ConstantTime.ctGreaterLookUp((byte) size, (byte) 0)),
                    (short) (divisionRoundOffset + 1), (short) 0);
            byte a = (byte) (ConstantTime.ctGreaterLookUp((byte) (size - 1), divisionRound) & value[tmpIndex]); // ctSelect
            tmpIndex = ConstantTime.ctSelect((short) (doSubtract
                                                & ConstantTime.ctGreaterLookUp((byte) (size - 2), divisionRound)
                                                & ConstantTime.ctGreaterLookUp((byte) size, (byte) 1)),
                    (short) (divisionRoundOffset + 2), (short) 0);
            byte b = (byte) (ConstantTime.ctGreaterLookUp((byte) (size - 2), divisionRound) & value[tmpIndex]);
            dividentDigits = ConstantTime.ctSelect(dividentDigitsNegative,
                    (short) ((dividentDigits >>> 1) & POSITIVE_DOUBLE_DIGIT_MASK),
                    ctShiftBits(dividentDigits,
                            a,
                            b,
                            (byte) bitShift));

            short divisorDigit = ConstantTime.ctSelect(dividentDigitsNegative,
                    (short) ((firstDivisorDigit >>> 1) & POSITIVE_DOUBLE_DIGIT_MASK),
                    ctShiftBits(firstDivisorDigit, secondDivisorDigit, thirdDivisorDigit, (byte) bitShift));
            short multiple = (short) (dividentDigits / (short) (divisorDigit + 1)); // division cannot be optimized here
            multiple = ConstantTime.ctSelect((short) (ConstantTime.ctIsZeroUnwrap(multiple)
                    | ConstantTime.ctIsNegativeUnwrap(multiple)), (short) 1, multiple);

            ctSubtractShift(divisor, (byte) (~ctIsNegative(divisorShift) & divisorShift), multiple, (short) ~doSubtract);

            short divisorShiftOffset = (short) (divisorShift - quotient.offset);
            tmpIndex = (short) (doSubtract & (short) (quotient.size - 1 - divisorShiftOffset));
            short quotientDigit = (short) ((quotient.value[tmpIndex] & DIGIT_MASK) + multiple);
            tmpIndex = (short) (doSubtract & (short) (quotient.size - 1 - divisorShiftOffset));
            quotient.value[tmpIndex] = (byte) ConstantTime.ctSelect(doSubtract, quotientDigit, quotient.value[tmpIndex]);

            divisionRound += (isLesserDivisor & ~divisorShiftNegative) & (byte) 1;
            divisorShift -= (byte) (isLesserDivisor & ~divisorShiftNegative) & (byte) 1;
        }
        quotient.ctShrink();
    }

    /*    public void ctRemainderDivideOptimized(BigNatInternal divisor, BigNatInternal quotient) {
        quotient.zero(); // make it faster with JavaCard implementation

        short divisorIndex = divisor.offset;
        for (short i = 0; i < divisor.value.length; i++) {// move to first nonzero digit
            short mask = (short) (ConstantTime.ctLessThan(divisorIndex, (short) (divisor.value.length - 1)) & ConstantTime.ctIsZero(divisor.value[divisorIndex]));
            divisorIndex += ConstantTime.ctSelect(mask, (short) 1, (short) 0);
        }

        short divisorShift = (short) (size - divisor.size + divisorIndex - divisor.offset);
        short divisionRound = 0;
        short firstDivisorDigit = (short) (divisor.value[divisorIndex] & DIGIT_MASK); // first nonzero digit
        short divisorBitShift = (short) (ctHighestOneBit((short) (firstDivisorDigit + 1)) - 1); // in short from left -1
        short index = ConstantTime.ctSelect(ConstantTime.ctGreater((short) (divisor.value.length - 1), divisorIndex), (short) (divisorIndex + 1), (short) 0);
        byte secondDivisorDigit = ConstantTime.ctSelect(ConstantTime.ctGreater((short) (divisor.value.length - 1), divisorIndex), divisor.value[index], (byte) 0);
        index = ConstantTime.ctSelect(ConstantTime.ctGreater((short) (divisor.value.length - 2), divisorIndex), (short) (divisorIndex + 2), (short) 0);
        byte thirdDivisorDigit = ConstantTime.ctSelect(ConstantTime.ctGreater((short) (divisor.value.length - 2), divisorIndex), divisor.value[index], (byte) 0);

        short MAX_CYCLES = 15;
        for (short i = 0; i < MAX_CYCLES; i++) {
            // !isLesser branch condition
            short divisorShiftNegative = ConstantTime.ctIsNegative(divisorShift);
            short isLesserStart = ConstantTime.ctSelect(ConstantTime.ctIsPositive(divisionRound), (short) (divisionRound - 1), (short) 0);
            short isLesserDivisor = ctIsLesser(divisor, divisorShift, isLesserStart);
            short doSubtract = ((short) (~divisorShiftNegative & ~isLesserDivisor));

            // inside of the branch
            short divisionRoundOffset = (short) (divisionRound + offset);
            index = ConstantTime.ctSelect(doSubtract, (short) (divisionRoundOffset - 1), (short) 0);
            short newDividentDigits = (short) ((short) (value[index]) << DIGIT_LEN);
            index = ConstantTime.ctSelect(doSubtract, (short) (divisionRound + offset), (short) 0);
            short dividentDigits = (short) (ConstantTime.ctSelect(ConstantTime.ctIsZero(divisionRound), (short) 0, newDividentDigits)
                    | (short) (value[index] & DIGIT_MASK));
            short dividentDigitsNegative = ConstantTime.ctIsNegative(dividentDigits);
            short dividentBitShift = (short) (ctHighestOneBit(dividentDigits) - 1);
            short bitShift = ConstantTime.ctSelect(ConstantTime.ctGreaterOrEqual(divisorBitShift, dividentBitShift), dividentBitShift, divisorBitShift);

            index = ConstantTime.ctSelect((short) (doSubtract
                                                & ConstantTime.ctGreater((short) (size - 1), divisionRound)
                                                & ConstantTime.ctGreater(size, (short) 0)),
                    (short) (divisionRoundOffset + 1), (short) 0);
            byte a = ConstantTime.ctSelect(ConstantTime.ctGreater((short) (size - 1), divisionRound), value[index], (byte) 0);
            index = ConstantTime.ctSelect((short) (doSubtract
                                                & ConstantTime.ctGreater((short) (size - 2), divisionRound)
                                                & ConstantTime.ctGreater(size, (short) 1)),
                    (short) (divisionRoundOffset + 2), (short) 0);
            byte b = ConstantTime.ctSelect(ConstantTime.ctGreater((short) (size - 2), divisionRound), value[index], (byte) 0);

            dividentDigits = ConstantTime.ctSelect(dividentDigitsNegative,
                    (short) ((dividentDigits >>> 1) & POSITIVE_DOUBLE_DIGIT_MASK),
                    ctShiftBits(dividentDigits, a, b, bitShift));
            short divisorDigit = ConstantTime.ctSelect(dividentDigitsNegative,
                    (short) ((firstDivisorDigit >>> 1) & POSITIVE_DOUBLE_DIGIT_MASK),
                    ctShiftBits(firstDivisorDigit, secondDivisorDigit, thirdDivisorDigit, bitShift));
            short multiple = (short) (dividentDigits / (short) (divisorDigit + 1)); // division cannot be optimized
            multiple = ConstantTime.ctSelect((short) (ConstantTime.ctIsZero(multiple) | ConstantTime.ctIsNegative(multiple)), (short) 1, multiple);

            ctSubtractShift(divisor, ConstantTime.ctSelect(ConstantTime.ctIsNegative(divisorShift), (short) 0, divisorShift), multiple, (short) ~doSubtract);

            short divisorShiftOffset = (short) (divisorShift - quotient.offset);
            index = ConstantTime.ctSelect(doSubtract, (short) (quotient.size - 1 - divisorShiftOffset), (short) 0);
            short quotientDigit = (short) ((quotient.value[index] & DIGIT_MASK) + multiple);
            index = ConstantTime.ctSelect(doSubtract, (short) (quotient.size - 1 - divisorShiftOffset), (short) 0);
            quotient.value[index] = (byte) ConstantTime.ctSelect(doSubtract, quotientDigit, quotient.value[index]);

            divisionRound += ConstantTime.ctSelect((short) (isLesserDivisor & ~divisorShiftNegative), (byte) 1, (byte) 0);
            divisorShift -= ConstantTime.ctSelect((short) (isLesserDivisor & ~divisorShiftNegative), (byte) 1, (byte) 0);
        }
    }
     */

    /**
     * Get the index of the highest bit set to 1. Used in remainderDivide.
     */
    public static short highestOneBit(short x) {
        for (short i = 0; i < DOUBLE_DIGIT_LEN; ++i) {
            if (x < 0) {
                return i;
            }
            x <<= 1;
        }
        return DOUBLE_DIGIT_LEN;
    }

    public static short ctHighestOneBit(short x) {
        short index = 0;
        for (short i = 0; i < DOUBLE_DIGIT_LEN; ++i) {
            short isNonZero = ConstantTime.ctIsNonZero(x);
            index += isNonZero & 1;
            x >>>= isNonZero & 1;
        }
        return (short) (DOUBLE_DIGIT_LEN - index);
    }

    /**
     * Shift to the left and fill. Used in remainderDivide.
     *
     * @param high most significant 16 bits
     * @param middle middle 8 bits
     * @param low least significant 8 bits
     * @param shift the left shift
     * @return most significant 16 bits as short
     */
    public static short shiftBits(short high, byte middle, byte low, short shift) {
        // shift high
        high <<= shift;

        // merge middle bits
        byte mask = (byte) (DIGIT_MASK << (shift >= DIGIT_LEN ? 0 : DIGIT_LEN - shift));
        short bits = (short) ((short) (middle & mask) & DIGIT_MASK);
        if (shift > DIGIT_LEN) {
            bits <<= shift - DIGIT_LEN;
        } else {
            bits >>>= DIGIT_LEN - shift;
        }
        high |= bits;

        if (shift <= DIGIT_LEN) {
            return high;
        }

        // merge low bits
        mask = (byte) (DIGIT_MASK << DOUBLE_DIGIT_LEN - shift);
        bits = (short) ((((short) (low & mask) & DIGIT_MASK) >>> DOUBLE_DIGIT_LEN - shift));
        high |= bits;

        return high;
    }

    public static short ctShiftBits(short high, byte middle, byte low, byte shift) {
        ConstantTime.initializeLookUpTables();
        // shift high
        high <<= shift;

        // merge middle bits
        byte mask = (byte) (DIGIT_MASK << (ConstantTime.ctLessThanLookUp(shift, (byte) 8) & ((byte) 8 - shift)));
        short bits = (short) ((short) (middle & mask) & DIGIT_MASK);
        bits <<= ConstantTime.ctLessThanLookUp((byte) 8, shift) & (short) (shift - (byte) 8);
        bits >>>= ConstantTime.ctGreaterOrEqualLookUp((byte) 8, shift) & (short) ((byte) 8 - shift);
        high |= bits;

        // merge low bits
        mask = (byte) (DIGIT_MASK << DOUBLE_DIGIT_LEN - shift);
        bits = (short) ((((short) (low & mask) & DIGIT_MASK) >>> DOUBLE_DIGIT_LEN - shift));

        high |= ConstantTime.ctLessThanLookUp((byte) 8, shift) & bits;

        return high;
    }

    public void ctMod(BigNatInternal modulus, BigNatInternal tmp, short blindResult) {
        short newModulusSize = modulus.length() % 8 == 0 ? modulus.length() : (short) (((modulus.length() >>> 3 /* / 8*/) + 1) * 8);
        short newThisSize = this.length() % 8 == 0 ? this.length() : (short) (((this.length() >>> 3) + 1) * 8);

        short newSize = (short) (newThisSize + newModulusSize);
        if (newSize > value.length || newSize > modulus.value.length || newSize > tmp.value.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        this.ctSetSize(newSize);
        modulus.ctSetSize(newSize);
        tmp.ctSetSize(newSize);
        short index = (short) (modulus.value.length - newModulusSize);
        // move modulus to the left
        for (short i = 0; i < modulus.value.length; i++) {
            short validI = (short) (ConstantTime.ctGreaterOrEqual(i, modulus.offset)
                    & ConstantTime.ctLessThan(i, (short) (modulus.offset + newSize)));
            short validIndex = ConstantTime.ctLessThan(index, (short) modulus.value.length);
            byte modulusValue = modulus.value[ConstantTime.ctSelect(validIndex, index, (short) 0)];
            modulus.value[i] = ConstantTime.ctSelect((short) (validI & validIndex), modulusValue, (byte) 0);
            index += ConstantTime.ctSelect(validI, (short) 1, (short) 0);
        }

        for (short i = 0; i < (short) (this.value.length * 8); i++) {
            // we care only about bits int thisSize
            short invalidBit = ConstantTime.ctGreaterOrEqual(i, (short) (newThisSize * 8));
            // shift right
            modulus.ctShiftRightBits((short) 1, (short) 0, invalidBit);
            //subtract with borrow
            tmp.ctCopy(this);
            byte borrow = tmp.ctSubtract(modulus);
            // update this
            short blind = (short) (ConstantTime.ctIsNonZero(borrow) | blindResult);
            this.ctCopy(tmp, blind);
        }
        ctShrink();
    }

    public void ctMod(BigNatInternal modulus, BigNatInternal tmp) {
        ctMod(modulus, tmp, (short) 0x00);
    }

    public void ctRemainderDivide(BigNatInternal divisor, BigNatInternal quotient, BigNatInternal remainder, short blind) {
        // nominator N = this
        // denominator D = divisor
        // quotient Q = result
        // remainder R
        // https://en.wikipedia.org/wiki/Division_algorithm#Integer_division_(unsigned)_with_remainder

        /* if D = 0 then error(DivisionByZeroException) end */
        /* Q := 0 */
        quotient.ctZero();
        /* R := 0 */
        remainder.ctZero();
        /* for i := n - 1 .. 0 do (number of bits in N) */
        for (short i = (short) (this.value.length * 8 - 1); i >= 0; i--) {
            /* R := R << 1 */
            remainder.ctShiftLeftBits((short) 1, blind);
            /* R(0) := N(i) */
            byte bitValue =  CTUtil.ctGetBit(this.value, (short) this.value.length, i);
            CTUtil.ctSetBit(remainder.value, (short) remainder.value.length, bitValue, (short) 0);
            /* if R ≥ D then */
            short blindSubtraction = (short) (remainder.ctIsLesser(divisor) | blind);
            /* R := R - D */
            remainder.ctSubtract(divisor, blindSubtraction);
            /* Q(i) := 1 */
            byte quotientBit = CTUtil.ctGetBit(quotient.value, (short) quotient.value.length, i);
            quotientBit = ConstantTime.ctSelect(blindSubtraction, quotientBit, (byte) 1);
            CTUtil.ctSetBit(quotient.value, (short) quotient.value.length, quotientBit, i, blind);
        }
        quotient.ctShrink();
        remainder.ctShrink();
    }

    public void ctRemainderDivide(BigNatInternal divisor, BigNatInternal quotient, BigNatInternal remainder) {
        ctRemainderDivide(divisor, quotient, remainder, (short) 0x00);
    }

    public short ctShiftRightByTrailingZeroes(short blind) {
        byte firstNonZeroBit = 0x00;
        short result = 0;
        for (short index = (short) (value.length - 1); index >= 0; index--) {
            short validIndex = ConstantTime.ctGreaterOrEqual(index, offset);
            byte thisValue = value[index];
            // xxxx xxx1, xxxx xx10, xxxx x100, xxxx 1000, xxx1 0000, xx10 0000, x100 0000, 1000 0000, 0000 0000
            byte zero = (byte) (ConstantTime.ctIsZeroUnwrap(thisValue) & 8);
            byte second = (byte) (ConstantTime.ctEqual((byte) (0b00000011 & thisValue), (byte) 0b00000010) & 1);
            byte third = (byte) (ConstantTime.ctEqual((byte) (0b00000111 & thisValue), (byte) 0b00000100) & 2);
            byte fourth = (byte) (ConstantTime.ctEqual((byte) (0b00001111 & thisValue), (byte) 0b00001000) & 3);
            byte fifth = (byte) (ConstantTime.ctEqual((byte) (0b00011111 & thisValue), (byte) 0b00010000) & 4);
            byte sixth = (byte) (ConstantTime.ctEqual((byte) (0b00111111 & thisValue), (byte) 0b00100000) & 5);
            byte seventh = (byte) (ConstantTime.ctEqual((byte) (0b01111111 & thisValue), (byte) 0b01000000) & 6);
            byte eighth = (byte) (ConstantTime.ctEqual((byte) (0b11111111 & thisValue), (byte) 0b10000000) & 7);
            short addition = ((short) (second + third + fourth + fifth + sixth + seventh + eighth));
            result += ConstantTime.ctSelect((byte) (firstNonZeroBit | ~validIndex), (byte) 0, (byte) (addition + zero));
            firstNonZeroBit |= ConstantTime.ctSelect(
                    (byte) (ConstantTime.ctIsZero(firstNonZeroBit) & validIndex & (ctIsPositive(addition) | ctEqual((byte) (0b00000001 & thisValue), (byte) 0b00000001))),
                    (byte) 0xff, (byte) 0);
        }
        ctShiftRight(result, blind);
        return result;
    }


    /// [DependencyBegin:ObjectLocker]
    private boolean ERASE_ON_LOCK = false;
    private boolean ERASE_ON_UNLOCK = false;
    private short locked = 0x0000; // Logical flag to store info if this BigNat is currently used for some operation. Used as a prevention of unintentional parallel use of same temporary pre-allocated BigNat.

    /**
     * Lock/reserve this BigNat for subsequent use.
     * Used to protect corruption of pre-allocated temporary BigNat used in different,
     * potentially nested operations. Must be unlocked by unlock() later on.
     */
    public void lock() {
        if (locked == (short) 0xffff) {
            ISOException.throwIt(ReturnCodes.SW_LOCK_ALREADYLOCKED);
        }
        locked = (short) 0xffff;
        if (ERASE_ON_LOCK) {
            erase();
        }
    }

    public void ctLock(short blind) {
        if (locked == (short) 0xffff) {
            ISOException.throwIt(ReturnCodes.SW_LOCK_ALREADYLOCKED);
        }
        locked = (short) 0xffff;
        if (ERASE_ON_LOCK) {
            ctErase(blind);
        }
    }

    /**
     * Unlock/release this BigNat from use. Used to protect corruption
     * of pre-allocated temporary BigNat used in different nested operations.
     * Must be locked before.
     */
    public void unlock() {
        if (locked != (short) 0xffff) {
            ISOException.throwIt(ReturnCodes.SW_LOCK_NOTLOCKED);
        }
        locked = (short) 0x0000;
        if (ERASE_ON_UNLOCK) {
            erase();
        }
    }

    public void ctUnlock(short blind) {
        if (locked != (short) 0xffff) {
            ISOException.throwIt(ReturnCodes.SW_LOCK_NOTLOCKED);
        }
        locked = (short) 0x0000;
        if (ERASE_ON_UNLOCK) {
            ctErase(blind);
        }
    }

    /**
     * Return current state of logical lock of this object
     *
     * @return true if object is logically locked (reserved), false otherwise
     */
    public boolean isLocked() {
        return locked == (short) 0xffff;
    }
    /// [DependencyEnd:ObjectLocker]
}
