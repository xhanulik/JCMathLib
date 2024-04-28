package opencrypto.jcmathlib;

import javacard.framework.JCSystem;

/**
 * @author Vasilios Mavroudis and Petr Svenda
 */
public class Integer {
    private ResourceManager rm;
    private BigNat magnitude;
    private byte sign;

    /**
     * Allocates integer with provided length and sets to zero.
     *
     * @param size Integer size
     * @param rm ResourceManager with all supporting objects
     */
    public Integer(short size, ResourceManager rm) {
        allocate(size, (byte) 0, null, (byte) -1, rm);
    }

    /**
     * Allocates integer from provided buffer and initialize by provided value.
     * Sign is expected as first byte of value.
     *
     * @param value       array with initial value
     * @param valueOffset start offset within   value
     * @param length      length of array
     * @param rm          ResourceManager with all supporting objects
     */
    public Integer(byte[] value, short valueOffset, short length, ResourceManager rm) {
        allocate(length, (value[valueOffset] == (byte) 0x00) ? (byte) 0 : (byte) 1, value, (short) (valueOffset + 1), rm);
    }

    /**
     * Allocates integer from provided array with explicit sign. No sign is expected in provided array.
     *
     * @param sign  sign of integer
     * @param value array with initial value
     * @param rm    ResourceManager with all supporting objects
     */
    public Integer(byte sign, byte[] value, ResourceManager rm) {
        allocate((short) value.length, sign, value, (short) 0, rm);
    }

    /**
     * Copy constructor of integer from other already existing value
     *
     * @param other integer to copy from
     */
    public Integer(Integer other) {
        rm.lock(rm.ARRAY_A);
        short len = magnitude.copyToByteArray(rm.ARRAY_A, (short) 0);
        allocate(len, other.getSign(), rm.ARRAY_A, (short) 0, other.rm);
        rm.unlock(rm.ARRAY_A);
    }

    /**
     * Creates integer from existing Bignat and provided sign. If required,
     * copy is performed, otherwise BigNat is used as magnitude.
     *
     * @param sign      sign of integer
     * @param magnitude initial magnitude
     * @param copy      if true, magnitude is directly used (no copy). If false, new storage array is allocated.
     */
    public Integer(byte sign, BigNat magnitude, boolean copy, ResourceManager rm) {
        if (copy) {
            // Copy from provided BigNat
            rm.lock(rm.ARRAY_A);
            short len = magnitude.copyToByteArray(rm.ARRAY_A, (short) 0);
            allocate(len, sign, rm.ARRAY_A, (short) 0, rm);
            rm.unlock(rm.ARRAY_A);
        } else {
            // Use directly provided BigNat as storage - no allocation
            initialize(sign, magnitude, rm);
        }
    }

    /**
     * Initialize integer object with provided sign and already allocated Bignat
     * as magnitude
     *
     * @param sign      sign of integer
     * @param bnStorage magnitude (object is directly used, no copy is performed)
     */
    private void initialize(byte sign, BigNat bnStorage, ResourceManager rm) {
        this.sign = sign;
        this.magnitude = bnStorage;
        this.rm = rm;
    }

    /**
     * Allocates and initializes Integer.
     *
     * @param size            length of integer
     * @param sign            sign of integer
     * @param fromArray       input array with initial value (copy of value is
     *                        performed)
     * @param fromArrayOffset start offset within fromArray
     */
    private void allocate(short size, byte sign, byte[] fromArray, short fromArrayOffset, ResourceManager rm) {
        this.rm = rm;
        BigNat mag = new BigNat(size, JCSystem.MEMORY_TYPE_TRANSIENT_RESET, this.rm);
        if (fromArray != null) {
            mag.fromByteArray(fromArray, fromArrayOffset, size);
        }
        initialize(sign, mag, this.rm);
    }

    private void ctAllocate(short size, byte sign, byte[] fromArray, short fromArrayOffset, ResourceManager rm) {
        this.rm = rm;
        BigNat mag = new BigNat(size, JCSystem.MEMORY_TYPE_TRANSIENT_RESET, this.rm);
        if (fromArray != null) {
            mag.ctFromByteArray(fromArray, fromArrayOffset, size);
        }
        initialize(sign, mag, this.rm);
    }

    /**
     * Clone value into this Integer from other Integer. Updates size of integer.
     *
     * @param other other integer to copy from
     */
    public void clone(Integer other) {
        this.sign = other.getSign();
        this.magnitude.copy(other.getMagnitude());
    }

    public void ctClone(Integer other) {
        this.sign = other.getSign();
        this.magnitude.ctCopy(other.getMagnitude());
    }

    /**
     * set this integer to zero
     */
    public void zero() {
        this.sign = (short) 0;
        this.magnitude.zero();
    }

    public void ctZero() {
        this.sign = (short) 0;
        this.magnitude.ctZero();
    }

    /**
     * Return sign of this integer
     *
     * @return current sign
     */
    public byte getSign() {
        return this.sign;
    }

    /**
     * Set sign of this integer
     *
     * @param s new sign
     */
    public void setSign(byte s) {
        this.sign = s;
    }

    /**
     * Return length (in bytes) of this integer
     *
     * @return length of this integer
     */
    public short getSize() {
        return this.magnitude.length();
    }

    /**
     * Set length of this integer
     *
     * @param newSize new length
     */
    public void setSize(short newSize) {
        this.magnitude.setSize(newSize);
    }

    /**
     * Compute negation of this integer
     */
    public void negate() {
        if (this.isPositive()) {
            this.setSign((byte) 1);
        } else if (this.isNegative()) {
            this.setSign((byte) 0);
        }
    }

    public void ctNegate() {
        short positive = this.ctIsPositive();
        short negative = this.ctIsNegative();
        byte oldSign = this.sign;
        byte newSign = (byte) ConstantTime.ctSelect(positive, (short) 1, oldSign);
        newSign = (byte) ConstantTime.ctSelect(negative, (short) 0, newSign);
        this.setSign(newSign);
    }

    /**
     * Returns magnitude as Bignat. No copy is performed so change of Bignat also changes this integer
     *
     * @return Bignat representing magnitude
     */
    public BigNat getMagnitude() {
        return this.magnitude;
    }

    /**
     * Set magnitude of this integer from other one. Will not change this integer length.
     * No sign is copied from other.
     *
     * @param other other integer to copy from
     */
    public void setMagnitude(Integer other) {
        this.magnitude.copy(other.getMagnitude());
    }

    public void ctSetMagnitude(Integer other) {
        this.magnitude.ctCopy(other.getMagnitude());
    }

    /**
     * Serializes this integer value into array. Sign is serialized as first byte
     *
     * @param outBuffer       output array
     * @param outBufferOffset start offset within output array
     * @return length of resulting serialized number including sign (number of bytes)
     */
    public short toByteArray(byte[] outBuffer, short outBufferOffset) {
        //Store sign
        outBuffer[outBufferOffset] = sign;
        //Store magnitude
        magnitude.copyToByteArray(outBuffer, (short) (outBufferOffset + 1));
        return (short) (this.getSize() + 1);
    }

    public short ctToByteArray(byte[] outBuffer, short outBufferOffset) {
        //Store sign
        outBuffer[outBufferOffset] = sign;
        //Store magnitude
        magnitude.ctCopyToByteArray(outBuffer, (short) (outBufferOffset + 1));
        return (short) (this.getSize() + 1);
    }

    /**
     * Deserialize value of this integer from provided array including sign.
     * Sign is expected to be as first byte
     *
     * @param value       array with value
     * @param valueOffset start offset within value
     * @param valueLength length of value
     */
    public void fromByteArray(byte[] value, short valueOffset, short valueLength) {
        //Store sign
        this.sign = value[valueOffset];
        //Store magnitude
        this.magnitude.fromByteArray(value, (short) (valueOffset + 1), (short) (valueLength - 1));
    }

    public void ctFromByteArray(byte[] value, short valueOffset, short valueLength) {
        //Store sign
        this.sign = value[valueOffset];
        //Store magnitude
        this.magnitude.ctFromByteArray(value, (short) (valueOffset + 1), (short) (valueLength - 1));
    }

    /**
     * Return true if integer is negative.
     *
     * @return true if integer is negative, false otherwise
     */
    public boolean isNegative() {
        return this.sign == 1;
    }

    public short ctIsNegative() {
        return ConstantTime.ctEqual(this.sign, (short) 1);
    }

    /**
     * Return true if integer is positive.
     *
     * @return true if integer is positive, false otherwise
     */
    public boolean isPositive() {
        return this.sign == 0;
    }

    public short ctIsPositive() {
        return ConstantTime.ctEqual(this.sign, (short) 0);
    }

    /**
     * Compares two integers. Return true, if this is smaller than other.
     *
     * @param other other integer to compare
     * @return true, if this is strictly smaller than other. False otherwise.
     */
    public boolean lesser(Integer other) {
        if (this.sign == 1 && other.sign == 0) {
            return true;
        } else if (this.sign == 0 && other.sign == 1) {
            return false;
        } else if ((this.sign == 0 && other.sign == 0)) {
            return this.magnitude.isLesser(other.magnitude);
        } else { //if ((this.sign == 1 && other.sign==1))
            return (!this.magnitude.isLesser(other.magnitude));
        }
    }

    public short ctLesser(Integer other) {
        short thisNegativeOtherPositive = (short) (this.ctIsNegative() & other.ctIsPositive());
        short thisPositiveOtherNegative = (short) (this.ctIsPositive() & other.ctIsNegative());
        short bothPositive = (short) (this.ctIsPositive() & other.ctIsPositive());
        short bothNegative = (short) (this.ctIsNegative() & other.ctIsNegative());
        short isLesser = this.magnitude.ctIsLesser(other.magnitude);
        short result = ConstantTime.ctSelect(thisNegativeOtherPositive, (short) 0xffff, (short) 0);
        result = ConstantTime.ctSelect(thisPositiveOtherNegative, (short) 0, result);
        result = ConstantTime.ctSelect(bothPositive, isLesser, result);
        result = ConstantTime.ctSelect(bothNegative,  (short) (~isLesser), result);
        return result;
    }

    /**
     * Add other integer to this and store result into this.
     *
     * @param other other integer to add
     */
    public void add(Integer other) {
        BigNat tmp = rm.BN_A;

        if (this.isPositive() && other.isPositive()) { //this and other are (+)
            this.sign = 0;
            this.magnitude.add(other.magnitude);
        } else if (this.isNegative() && other.isNegative()) { //this and other are (-)
            this.sign = 1;
            this.magnitude.add(other.magnitude);
        } else {
            if (this.isPositive() && other.getMagnitude().isLesser(this.getMagnitude())) { //this(+) is larger than other(-)
                this.sign = 0;
                this.magnitude.subtract(other.magnitude, (short) 0, (short) 1);
            } else if (this.isNegative() && other.getMagnitude().isLesser(this.getMagnitude())) {    //this(-) has larger magnitude than other(+)
                this.sign = 1;
                this.magnitude.subtract(other.magnitude, (short) 0, (short) 1);
            } else if (this.isPositive() && this.getMagnitude().isLesser(other.getMagnitude())) { //this(+) has smaller magnitude than other(-)
                this.sign = 1;
                tmp.lock();
                tmp.clone(other.getMagnitude());
                tmp.subtract(this.magnitude, (short) 0, (short) 1);
                this.magnitude.copy(tmp);
                tmp.unlock();
            } else if (this.isNegative() && this.getMagnitude().isLesser(other.getMagnitude())) {  //this(-) has larger magnitude than other(+)
                this.sign = 0;
                tmp.lock();
                tmp.clone(other.getMagnitude());
                tmp.subtract(this.magnitude, (short) 0, (short) 1);
                this.magnitude.copy(tmp);
                tmp.unlock();
            } else if (this.getMagnitude().equals(other.getMagnitude())) {  //this has opposite sign than other, and the same magnitude
                this.sign = 0;
                this.zero();
            }
        }
    }

    public void ctAdd(Integer other) {
        BigNat tmp = rm.BN_A;

        short thisNegativeOtherPositive = (short) (this.ctIsNegative() & other.ctIsPositive()); // true
        short thisPositiveOtherNegative = (short) (this.ctIsPositive() & other.ctIsNegative()); // false
        short bothPositive = (short) (this.ctIsPositive() & other.ctIsPositive());
        short bothNegative = (short) (this.ctIsNegative() & other.ctIsNegative());
        short otherLesser = other.getMagnitude().ctIsLesser(this.getMagnitude());
        short thisLesser = this.getMagnitude().ctIsLesser(other.getMagnitude());

        /* Both positive */
        short newSign = ConstantTime.ctSelect(bothPositive, (short) 0, this.sign);
        this.magnitude.ctAdd(other.magnitude, (short) (~bothPositive));
        /* Both negative */
        newSign = ConstantTime.ctSelect(bothNegative, (short) 1,newSign);
        this.magnitude.ctAdd(other.magnitude, (short) (~bothNegative));
        /* this(+) is larger than other(-) */
        short thisPositiveLargerThanOtherNegative = (short) (thisPositiveOtherNegative & otherLesser);
        newSign = ConstantTime.ctSelect(thisPositiveLargerThanOtherNegative, (short) 0, newSign);
        this.magnitude.ctSubtract(other.magnitude, (short) (~thisPositiveLargerThanOtherNegative));
        /* this(-) has larger magnitude than other(+) */
        short thisNegativeLargerThanOtherPositive = (short) (thisNegativeOtherPositive & otherLesser);
        newSign = ConstantTime.ctSelect(thisNegativeLargerThanOtherPositive, (short) 1, newSign);
        this.magnitude.ctSubtract(other.magnitude, (short) (~thisNegativeLargerThanOtherPositive));
        /* this(+) has smaller magnitude than other(-) */
        /* this(+) has smaller magnitude than other(-) */
        /* this(-) has larger magnitude than other(+) */
        /* this has opposite sign than other, and the same magnitude */
//        } else {
//            if (this.isPositive() && other.getMagnitude().isLesser(this.getMagnitude())) { //this(+) is larger than other(-)
//                this.sign = 0;
//                this.magnitude.subtract(other.magnitude, (short) 0, (short) 1);
//            } else if (this.isNegative() && other.getMagnitude().isLesser(this.getMagnitude())) {    //this(-) has larger magnitude than other(+)
//                this.sign = 1;
//                this.magnitude.subtract(other.magnitude, (short) 0, (short) 1);
//            } else if (this.isPositive() && this.getMagnitude().isLesser(other.getMagnitude())) { //this(+) has smaller magnitude than other(-)
//                this.sign = 1;
//                tmp.lock();
//                tmp.clone(other.getMagnitude());
//                tmp.subtract(this.magnitude, (short) 0, (short) 1);
//                this.magnitude.copy(tmp);
//                tmp.unlock();
//            } else if (this.isNegative() && this.getMagnitude().isLesser(other.getMagnitude())) {  //this(-) has larger magnitude than other(+)
//                this.sign = 0;
//                tmp.lock();
//                tmp.clone(other.getMagnitude());
//                tmp.subtract(this.magnitude, (short) 0, (short) 1);
//                this.magnitude.copy(tmp);
//                tmp.unlock();
//            } else if (this.getMagnitude().equals(other.getMagnitude())) {  //this has opposite sign than other, and the same magnitude
//                this.sign = 0;
//                this.zero();
//            }
//        }
    }

    /**
     * Substract other integer from this and store result into this.
     *
     * @param other other integer to substract
     */
    public void subtract(Integer other) {
        other.negate(); // Potentially problematic - failure and exception in subsequent function will cause other to stay negated
        this.add(other);
        // Restore original sign for other
        other.negate();
    }

    /**
     * Multiply this and other integer and store result into this.
     *
     * @param other other integer to multiply
     */
    public void multiply(Integer other) {
        BigNat tmp = rm.BN_B;

        if (this.isPositive() && other.isNegative()) {
            this.setSign((byte) 1);
        } else if (this.isNegative() && other.isPositive()) {
            this.setSign((byte) 1);
        } else {
            this.setSign((byte) 0);
        }

        tmp.lock();
        tmp.clone(this.magnitude);
        tmp.mult(other.getMagnitude());
        this.magnitude.copy(tmp);
        tmp.unlock();
    }

    /**
     * Divide this by other integer and store result into this.
     *
     * @param other divisor
     */
    public void divide(Integer other) {
        BigNat tmp = rm.BN_A;

        if (this.isPositive() && other.isNegative()) {
            this.setSign((byte) 1);
        } else if (this.isNegative() && other.isPositive()) {
            this.setSign((byte) 1);
        } else {
            this.setSign((byte) 0);
        }

        tmp.lock();
        tmp.clone(this.magnitude);
        tmp.remainderDivide(other.getMagnitude(), this.magnitude);
        tmp.unlock();
    }

    /**
     * Computes modulo of this by other integer and store result into this.
     *
     * @param other modulus
     */
    public void modulo(Integer other) {
        this.magnitude.mod(other.getMagnitude());
    }
}