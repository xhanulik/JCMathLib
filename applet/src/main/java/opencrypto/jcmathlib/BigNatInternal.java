package opencrypto.jcmathlib;

import javacard.framework.ISOException;
import javacard.framework.Util;

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
     * Get size of this BigNat in bytes.
     *
     * @return size in bytes
     */
    public short length() {
        return size;
    }

    /**
     * Sets the size of this BigNat in bytes.
     *
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
     * Resize this BigNat value to given size in bytes. May result in truncation.
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
     * Prepend zeros to reach the defined byte length and store the result in an output buffer.
     *
     * @param targetLength required length including prepended zeroes
     * @param outBuffer    output buffer for value with prepended zeroes
     * @param outOffset    start offset inside outBuffer for write
     */
    public void prependZeros(short targetLength, byte[] outBuffer, short outOffset) {
        short start = (short) (targetLength - size);
        if (start > 0) {
            Util.arrayFillNonAtomic(outBuffer, outOffset, start, (byte) 0);
        }
        Util.arrayCopyNonAtomic(value, offset, outBuffer, (short) (outOffset + start), size);
    }

    /**
     * Remove leading zeroes from this BigNat and decrease its byte size accordingly.
     */
    public void shrink() {
        short i;
        short newSize = (short) value.length;
        boolean foundNonZero = false;
        for (i = 0; i < value.length; i++) { // Compute size of non-zero part
            foundNonZero = (value[i] != 0) || foundNonZero;
            short value = foundNonZero ? (short) 0 : (short) 1;
            newSize -= value;
        }

        if (newSize < 0) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_INVALIDRESIZE);
        }
        resize(newSize);
    }

    /**
     * Set this BigNat value to zero. Previous size is kept.
     */
    public void zero() {
        Util.arrayFillNonAtomic(value, offset, size, (byte) 0);
    }

    /**
     * Erase the internal array of this BigNat.
     */
    public void erase() {
        Util.arrayFillNonAtomic(value, (short) 0, (short) value.length, (byte) 0);
    }

    /**
     * Set this BigNat to a given value. Previous size is kept.
     */
    public void setValue(byte newValue) {
        zero();
        value[(short) (value.length - 1)] = (byte) (newValue & DIGIT_MASK);
    }

    /**
     * Set this BigNat to a given value. Previous size is kept.
     */
    public void setValue(short newValue) {
        zero();
        value[(short) (value.length - 1)] = (byte) (newValue & DIGIT_MASK);
        value[(short) (value.length - 2)] = (byte) ((short) (newValue >> 8) & DIGIT_MASK);
    }

    /**
     * Copies a BigNat into this without changing size. May throw an exception if this is too small.
     */
    public void copy(BigNatInternal other) {
        short diff = (short) (size - other.size);
        short thisStart = diff >= 0 ? (short) (diff + offset) : offset;
        short otherStart = diff >= 0 ? other.offset : (short) (other.offset - diff);
        short len = diff >= 0 ? other.size : size;
        boolean problem = false;

        // Verify here that other have leading zeroes up to otherStart
        for (short i = 0; i < other.value.length; i++) {
            byte validIndex = (byte) (i < otherStart ? 1 : 0);
            byte nonZero = (byte) (other.value[i] != 0 ? 1 : 0);
            problem = ((nonZero & validIndex) != 0) || problem;
        }

        if (problem) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_INVALIDCOPYOTHER);
        }

        Util.arrayFillNonAtomic(value, (short) 0, (short) value.length, (byte) 0);
        Util.arrayCopyNonAtomic(other.value, otherStart, value, thisStart, len);
    }

    /**
     * Copies a BigNat into this including its size. May require reallocation.
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

     * Test equality with zero.
     */
    public boolean isZero() {
        return isZero((short) 0, (short) value.length);
    }

    /**
     * Test quality with zero for given part of number.
     *
     * @param offset offset in the byte array, starting index
     * @param end    ending index
     */
    private boolean isZero(short offset, short end) {
        short nonZero = 0;
        for (short i = offset; i < end; i++) {
            short nonZeroValue = (short) (value[i] != 0 ? 1 : 0);
            nonZero = (short) (nonZeroValue | nonZero);
        }
        return nonZero == 0;
    }

    /**
     * Test equality with one.
     */
    public boolean isOne() {
        boolean upperZero = isZero((short) 0, (short) ((short) value.length - 1));
        return (value[(short) (value.length - 1)] == (byte) 0x01) && upperZero;
    }

    /**

     * Check if stored BigNat is odd.
     */
    public boolean isOdd() {
        return (byte) (value[(short) (value.length - 1)] & (byte) 1) != (byte) 0;
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
        // index, where the byte positions in other corresponding to the positions in this
        // (after shifting, starting from start index)
        short j = (short) (other.size + shift - size + start + other.offset);

        short otherBigger = 0;
        // check the bytes by which other is longer than this
        // if they are non-zero, then other is strictly greater than this
        for (short i = 0; i < other.value.length; ++i) {
            short validIndex1 = (short) (i >= (short) (start + other.offset) ? 1 : 0);
            short validIndex2 = (short) (i < j ? 1 : 0);
            short nonZeroValue = (short) (other.value[i] != 0 ? 1 : 0);
            otherBigger = (short) ((validIndex1 & validIndex2 & nonZeroValue) | otherBigger);
        }

        short thisLesser = 0;
        short lesserSet = 1;
        // check all bytes at positions that correspond to the number other in this
        for (short i = 0; i < (short) value.length; i++) {
            short thisValue = (short) (value[i] & DIGIT_MASK);
            short otherValue = (j >= other.offset && j < (short) other.value.length) ? (short) (other.value[j] & DIGIT_MASK) : (short) 0;
            short validIndex = (short) (i >= (short) (start + offset) ? 1 : 0);
            short thisSmaller = (short) (thisValue < otherValue ? 1 : 0);
            short thisBigger = (short) (thisValue > otherValue ? 1 : 0);

            // this is lesser, no previous bytes in other were lesser
            thisLesser = (lesserSet & validIndex & thisSmaller) != 0 ? 1 : thisLesser;
            // first lesser byte seen, do not take next bytes into account
            lesserSet = (lesserSet & validIndex & thisSmaller) != 0 ? 0 : lesserSet;
            // larger bytes in this observed before any smaller byte, this cannot be smaller than other
            lesserSet = (lesserSet & validIndex & thisBigger) != 0 ? 0 : lesserSet;
            j += validIndex;
        }

        return (otherBigger | thisLesser) != 0;
    }

    /**
     * Value equality check.
     *
     * @param other BigNat to compare
     * @return true if this and other have the same value, false otherwise.
     */
    public boolean equals(BigNatInternal other) {
        short diff = (short) (size - other.size);
        short thisStart = offset;
        short otherStart = other.offset;;
        short length = size;

        if (diff == 0) {
            thisStart = offset;
            otherStart = other.offset;
            length = size;
        }

        if (diff < 0) {
            thisStart = offset;
            otherStart = (short) (other.offset - diff);
            length = size;
        }

        if (diff > 0) {
            thisStart = (short) (offset + diff);
            otherStart = other.offset;
            length = other.size;
        }

        short nonZeroPrefixOther = 0;
        for (short i = 0; i < other.value.length; ++i) {
            short validIndex1 = (short) (i >= other.offset ? 1 : 0);
            short validIndex2 = (short) (i < (short) (other.offset - diff) ? 1 : 0);
            short nonZero = (short) (other.value[i] != (byte) 0 ? 1 : 0);
            nonZeroPrefixOther = (short) ((validIndex1 & validIndex2 & nonZero) | nonZeroPrefixOther);
        }

        short nonZeroPrefixThis = 0;
        for (short i = (short) 0; i < value.length; ++i) {
            short validIndex1 = (short) (i >= offset ? 1 : 0);
            short validIndex2 = (short) (i < (short) (offset + diff) ? 1 : 0);
            short nonZero = (short) (value[i] != (byte) 0 ? 1 : 0);
            nonZeroPrefixThis = (short) ((validIndex1 & validIndex2 & nonZero) | nonZeroPrefixThis);
        }

        boolean result = true;
        if (diff < 0) {
            result = nonZeroPrefixOther == (short) 0;
        }
        if (diff > 0) {
            result = nonZeroPrefixThis == (short) 0;
        }
        return Util.arrayCompare(value, thisStart, other.value, otherStart, length) == 0 && result;
    }
    public boolean equals2(BigNatInternal other) {
        // Compute difference in lengths in order to prepend zeroes
        short diff = (short) (size - other.size);
        // Compare using hash of values
        byte[] arrayABuffer = rm.ARRAY_A;
        byte[] arrayBBuffer = rm.ARRAY_B;
        rm.lock(arrayABuffer);
        rm.lock(arrayBBuffer);
        short len;

        if (diff == 0) {
            // Same length, hash the values directly
            rm.hashEngine.doFinal(this.value, this.offset, this.length(), arrayABuffer, (short) 0);
            len = rm.hashEngine.doFinal(other.value, other.offset, other.length(), arrayBBuffer, (short) 0);
        } else {
            if (diff < 0) {
                // Other is longer, prepend zeroes to this
                this.prependZeros(other.length(), arrayABuffer, (short) 0);
                rm.hashEngine.doFinal(arrayABuffer, (short) 0, other.length(), arrayABuffer, (short) 0);
                len = rm.hashEngine.doFinal(other.value, other.offset, other.length(), arrayBBuffer, (short) 0);
            } else {
                // This is longer, prepend zeroes to this
                other.prependZeros(this.length(), arrayBBuffer, (short) 0);
                rm.hashEngine.doFinal(this.value, this.offset, this.length(), arrayABuffer, (short) 0);
                len = rm.hashEngine.doFinal(arrayBBuffer, (short) 0, this.length(), arrayBBuffer, (short) 0);
            }
        }
        boolean bResult = Util.arrayCompare(arrayABuffer, (short) 0, arrayBBuffer, (short) 0, len) == 0;
        rm.unlock(arrayABuffer);
        rm.unlock(arrayBBuffer);

        return bResult;
    }

    /**
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

    /**
     * Increment this BigNat.
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

    /**
     * Decrement this BigNat.
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

    /**
     * Adds other to this. Outputs carry bit.
     *
     * @param other BigNat to add
     * @return true if carry occurs, false otherwise
     */
    public byte add(BigNatInternal other) {
        return add(other, (short) 0, (short) 1);
    }

    /**
     * Computes other * multiplier, shifts the results by shift and adds it to this.
     * Multiplier must be in range [0; 2^8 - 1].
     * This must be large enough to fit the results.
     */
    private byte add(BigNatInternal other, short shift, short multiplier) {
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
     * Subtract provided other BigNat from this BigNat.
     *
     * @param other BigNat to be subtracted from this
     */
    public void subtract(BigNatInternal other) {
        subtract(other, (short) 0, (short) 1);
    }

    /**
     * Computes other * multiplier, shifts the results by shift and subtract it from this.
     * Multiplier must be in range [0; 2^8 - 1].
     */
    private void subtract(BigNatInternal other, short shift, short multiplier) {
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
     * Multiplies this and other using software multiplications and stores results into this.
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
     * Right bit shift with carry
     *
     * @param bits number of bits to shift by
     * @param carry ORed into the highest byte
     */
    protected void shiftRight(short bits, short carry) {
        // assumes 0 <= bits < 8
        short mask = (short) ((short) (1 << bits) - 1); // lowest `bits` bits set to 1
        for (short i = offset; i < (short) value.length; i++) {
            short current = (short) (value[i] & 0xff);
            short previous = current;
            current >>= bits;
            value[i] = (byte) (current | carry);
            carry = (short) (previous & mask);
            carry <<= (short) (8 - bits);
        }
    }

    /**
     * Right bit shift
     *
     * @param bits number of bits to shift by
     */
    public void shiftRight(short bits) {
        shiftRight(bits, (short) 0);
    }

    /**
     * Left bit shift with carry
     *
     * @param bits number of bits to shift by
     * @param carry ORed into the lowest byte
     */
    protected void shiftLeft(short bits, short carry) {
        // assumes 0 <= bits < 8
        short mask = (short) ((-1 << (8 - bits)) & 0xff); // highest `bits` bits set to 1
        for (short i = (short) (value.length - 1); i >= offset; --i) {
            short current = (short) (value[i] & 0xff);
            short previous = current;
            current <<= bits;
            value[i] = (byte) (current | carry);
            carry = (short) (previous & mask);
            carry >>= (8 - bits);
        }

        if (carry != 0) {
            setSize((short) (size + 1));
            value[offset] = (byte) carry;
        }
    }

    /**
     * Right bit shift
     *
     * @param bits number of bits to shift by
     */
    public void shiftLeft(short bits) {
        shiftLeft(bits, (short) 0);
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
            quotient.zero();
        }

        short divisorIndex = divisor.offset;
        while (divisor.value[divisorIndex] == 0) {
            divisorIndex++;
        }

        short divisorShift = (short) (size - divisor.size + divisorIndex - divisor.offset);
        short divisionRound = 0;
        short firstDivisorDigit = (short) (divisor.value[divisorIndex] & DIGIT_MASK);
        short divisorBitShift = (short) (highestOneBit((short) (firstDivisorDigit + 1)) - 1);
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
    }

    /**
     * Get the index of the highest bit set to 1. Used in remainderDivide.
     */
    private static short highestOneBit(short x) {
        for (short i = 0; i < DOUBLE_DIGIT_LEN; ++i) {
            if (x < 0) {
                return i;
            }
            x <<= 1;
        }
        return DOUBLE_DIGIT_LEN;
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
    private static short shiftBits(short high, byte middle, byte low, short shift) {
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
        bits = (short) ((((short) (low & mask) & DIGIT_MASK) >> DOUBLE_DIGIT_LEN - shift));
        high |= bits;

        return high;
    }

    /// [DependencyBegin:ObjectLocker]
    private boolean ERASE_ON_LOCK = false;
    private boolean ERASE_ON_UNLOCK = false;
    private boolean locked = false; // Logical flag to store info if this BigNat is currently used for some operation. Used as a prevention of unintentional parallel use of same temporary pre-allocated BigNat.

    /**
     * Lock/reserve this BigNat for subsequent use.
     * Used to protect corruption of pre-allocated temporary BigNat used in different,
     * potentially nested operations. Must be unlocked by unlock() later on.
     */
    public void lock() {
        if (locked) {
            ISOException.throwIt(ReturnCodes.SW_LOCK_ALREADYLOCKED);
        }
        locked = true;
        if (ERASE_ON_LOCK) {
            erase();
        }
    }

    /**
     * Unlock/release this BigNat from use. Used to protect corruption
     * of pre-allocated temporary BigNat used in different nested operations.
     * Must be locked before.
     */
    public void unlock() {
        if (!locked) {
            ISOException.throwIt(ReturnCodes.SW_LOCK_NOTLOCKED);
        }
        locked = false;
        if (ERASE_ON_UNLOCK) {
            erase();
        }
    }

    /**
     * Return current state of logical lock of this object
     *
     * @return true if object is logically locked (reserved), false otherwise
     */
    public boolean isLocked() {
        return locked;
    }
    /// [DependencyEnd:ObjectLocker]
}
