package opencrypto.jcmathlib;

import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.security.RSAPrivateKey;
import javacard.security.RSAPublicKey;
import javacardx.crypto.Cipher;
import javacard.security.KeyBuilder;

/**
 * @author Vasilios Mavroudis and Petr Svenda and Antonin Dufka
 */
public class BigNat extends BigNatInternal {

    /**
     * Construct a BigNat of a given size in bytes.
     */
    public BigNat(short size, byte allocatorType, ResourceManager rm) {
        super(size, allocatorType, rm);
    }

    /**
     * Division of this BigNat by provided other BigNat.
     */
    public void divide(BigNat other) {
        BigNat tmp = rm.BN_E;

        tmp.lock();
        tmp.clone(this);
        tmp.remainderDivide(other, this);
        tmp.unlock();
    }

    public void ctDivide(BigNat other) {
        BigNat tmp = rm.BN_A; // rm.BN_Eis too big for ctIsLesser implementation over 128B numbers

        tmp.lock();
        tmp.ctClone( this);
        tmp.ctRemainderDivideOptimized(other, this);
        tmp.unlock();
    }

    /**
     * Greatest common divisor of this BigNat with other BigNat. Result is stored into this.
     */
    public void gcd(BigNat other) {
        BigNat tmp = rm.BN_A;
        BigNat tmpOther = rm.BN_B;

        tmp.lock();
        tmpOther.lock();

        tmpOther.clone(other);

        // TODO: optimise?
        while (!tmpOther.isZero()) {
            tmp.clone(tmpOther);
            mod(tmpOther);
            tmpOther.clone(this);
            clone(tmp);
        }

        tmp.unlock();
        tmpOther.unlock();
        shrink();
    }

    public void ctGcdMod(BigNat other) {
        BigNat tmp = rm.BN_A;
        BigNat tmpOther = rm.BN_B;

        tmp.lock();
        tmpOther.lock();

        tmpOther.ctClone(other);
        while (tmpOther.ctIsZero() != -1) {
            tmp.ctClone(tmpOther);
            ctMod(tmpOther);
            tmpOther.ctClone(this);
            ctClone(tmp);
        }

        tmp.unlock();
        tmpOther.unlock();
        ctShrink();
    }

    public void ctGcd(BigNat other) {
        BigNat tmp = rm.BN_A;
        BigNat tmpOther = rm.BN_B;

        tmp.lock();
        tmpOther.lock();

        tmpOther.clone(other);

        short thisZeros = ctShiftRightByTrailingZeroes((short) 0);
        short otherZeros = tmpOther.ctShiftRightByTrailingZeroes((short) 0);
        short done = 0;
        short count = 20;
        while(count > 0) {
            // Swap if necessary so other ≤ this
            short thisLesser = this.ctIsLesser(tmpOther);
            tmp.ctClone(this, done);
            this.ctClone(tmpOther, (short) (~thisLesser | done));
            tmpOther.ctClone(tmp, (short) (~thisLesser | done));
            // Identity 4: gcd(u, v) = gcd(u, v-u) as u ≤ v and u, v are both odd
            this.ctSubtract(tmpOther, done);
            // this is now even
            done |= this.ctIsZero();

            // Identity 3: gcd(u, 2ʲ v) = gcd(u, v) as u is odd
            this.ctShiftRightByTrailingZeroes(done);
            count--;
        }
        this.ctClone(tmpOther);
        short min = ConstantTime.ctSelect((ConstantTime.ctLessThan(thisZeros, otherZeros)), thisZeros, otherZeros);
        this.ctShiftLeft(min);
        ctShrink();
    }

    /**
     * Decides whether the arguments are co-prime or not.
     */
    public boolean isCoprime(BigNat a, BigNat b) {
        BigNat tmp = rm.BN_C;

        tmp.lock();
        tmp.clone(a);

        tmp.gcd(b);
        boolean result = tmp.isOne();
        tmp.unlock();
        return result;
    }

    public short ctIsCoprime(BigNat other) {
        BigNat tmp = rm.BN_C;

        tmp.lock();
        tmp.ctClone(this);

        tmp.ctGcd(other);
        short result = tmp.ctIsOne();
        tmp.unlock();
        return result;
    }

    /**
     * Square computation supporting base greater than MAX_BIGNAT_LENGTH.
     */
    public void sq() {
        if (OperationSupport.getInstance().RSA_SQ != (short) 0xffff) {
            BigNat tmp = rm.BN_E;
            tmp.lock();
            tmp.setSize(length());
            tmp.copy(this);
            super.mult(tmp);
            tmp.unlock();
            return;
        }
        if ((short) (rm.MAX_SQ_LENGTH - 1) < (short) (2 * length())) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_INVALIDSQ);
        }

        byte[] resultBuffer = rm.ARRAY_A;
        short offset = (short) (rm.MAX_SQ_LENGTH - length());

        rm.lock(resultBuffer);
        Util.arrayFillNonAtomic(resultBuffer, (short) 0, offset, (byte) 0x00);
        copyToByteArray(resultBuffer, offset);
        short len = rm.sqCiph.doFinal(resultBuffer, (short) 0, rm.MAX_SQ_LENGTH, resultBuffer, (short) 0);
        if (len != rm.MAX_SQ_LENGTH) {
            if (OperationSupport.getInstance().RSA_PREPEND_ZEROS == (short) 0xffff) {
                Util.arrayCopyNonAtomic(resultBuffer, (short) 0, resultBuffer, (short) (rm.MAX_SQ_LENGTH - len), len);
                Util.arrayFillNonAtomic(resultBuffer, (short) 0, (short) (rm.MAX_SQ_LENGTH - len), (byte) 0);
            } else {
                ISOException.throwIt(ReturnCodes.SW_ECPOINT_UNEXPECTED_KA_LEN);
            }
        }
        short zeroPrefix = (short) (rm.MAX_SQ_LENGTH - (short) 2 * length());
        fromByteArray(resultBuffer, zeroPrefix, (short) (rm.MAX_SQ_LENGTH - zeroPrefix));
        rm.unlock(resultBuffer);
        shrink();
    }

    /**
     * Naive square computation supporting base greater than MAX_BIGNAT_LENGTH.
     * Constant-time implementation.
     */
    public void ctSqNaive() {
        BigNat tmp = rm.BN_E;
        tmp.lock();
        tmp.setSize(length());
        tmp.ctCopy(this);
        super.ctMult(tmp);
    }

    /**
     * HW-supported square computation supporting base greater than MAX_BIGNAT_LENGTH.
     * Constant-time implementation.
     */
    public void ctHWSq() {
        if (OperationSupport.getInstance().RSA_SQ != (short) 0xffff) {
            ISOException.throwIt(ReturnCodes.SW_BIGNAT_INVALIDSQ);
        }

//        if ((short) (rm.MAX_SQ_LENGTH - 1) < (short) (2 * length())) {
//            ISOException.throwIt(ReturnCodes.SW_BIGNAT_INVALIDSQ);
//        }

        byte[] resultBuffer = rm.ARRAY_A;
        byte[] tmpBuffer = rm.ARRAY_B;
        short offset = (short) (rm.MAX_SQ_LENGTH - length());

        rm.lock(resultBuffer);
        rm.lock(tmpBuffer);
        Util.arrayFillNonAtomic(resultBuffer, (short) 0, offset, (byte) 0x00);
        Util.arrayFillNonAtomic(tmpBuffer, (short) 0, offset, (byte) 0x00);
        ctCopyToByteArray(resultBuffer, offset);
        ctCopyToByteArray(tmpBuffer, offset);
        short len = rm.sqCiph.doFinal(resultBuffer, (short) 0, rm.MAX_SQ_LENGTH, resultBuffer, (short) 0); // possible CTO
        rm.sqCiph.doFinal(tmpBuffer, (short) 0, rm.MAX_SQ_LENGTH, tmpBuffer, (short) 0);
        BigNat tmp = rm.BN_E;
        tmp.lock();
        tmp.setSize(length());

        short lenMax = ConstantTime.ctEqual(len, rm.MAX_SQ_LENGTH);
        short blind = (short) (~(~lenMax & OperationSupport.getInstance().RSA_PREPEND_ZEROS));
        CTUtil.ctArrayCopyNonAtomic(resultBuffer, (short) 0, resultBuffer, (short) (rm.MAX_SQ_LENGTH - len), len, blind);
        CTUtil.ctArrayFillNonAtomic(resultBuffer, (short) 0, (short) (rm.MAX_SQ_LENGTH - len), (byte) 0, blind);

        short zeroPrefix = (short) (rm.MAX_SQ_LENGTH - (short) 2 * length());
        ctFromByteArray(resultBuffer, zeroPrefix, (short) (rm.MAX_SQ_LENGTH - zeroPrefix));
        rm.unlock(resultBuffer);
        ctShrink();

        if ((~lenMax & ~OperationSupport.getInstance().RSA_PREPEND_ZEROS) == (short) 0xffff) {
            ISOException.throwIt(ReturnCodes.SW_ECPOINT_UNEXPECTED_KA_LEN);
        }
    }

    /**
     * Computes this * other and stores the result into this.
     */
    public void mult(BigNat other) {
        if (OperationSupport.getInstance().RSA_CHECK_ONE == (short) 0xffff && isOne()) {
            clone(other);
            return;
        }
        if (OperationSupport.getInstance().RSA_SQ != (short) 0xffff || length() <= (short) 16) {
            super.mult(other);
            return;
        }

        BigNat result = rm.BN_F;
        BigNat tmp = rm.BN_G;

        result.lock();
        result.setSize((short) ((length() > other.length() ? length() : other.length()) + 1));
        result.copy(this);
        result.add(other);
        result.sq();

        tmp.lock();
        if (isLesser(other)) {
            tmp.clone(other);
            tmp.subtract(this);
        } else {
            tmp.clone(this);
            tmp.subtract(other);
        }
        tmp.sq();

        result.subtract(tmp);
        tmp.unlock();
        result.shiftRightBits((short) 2);

        setSizeToMax(false);
        copy(result);
        shrink();
        result.unlock();
    }

//    public void ctMultNaive(BigNat other) {
//        super.mult(other);
//    }

    public void ctMultSq(BigNat other) {
        BigNat result = rm.BN_F;
        BigNat tmp = rm.BN_G;

        result.lock();
        result.ctSetSize((short) ((length() > other.length() ? length() : other.length()) + 1));
        result.ctCopy(this);
        result.ctAdd(other);
        result.sq();

        tmp.lock();
        if (isLesser(other)) {
            tmp.clone(other);
            tmp.subtract(this);
        } else {
            tmp.clone(this);
            tmp.subtract(other);
        }
        tmp.sq();

        result.subtract(tmp);
        tmp.unlock();
        result.shiftRightBits((short) 2);

        setSizeToMax(false);
        copy(result);
        shrink();
        result.unlock();
    }

    /**
     * Computes modulo and stores the result in this.
     */
    public void mod(BigNat mod) {
        remainderDivide(mod, null);
    }

    public void ctMod(BigNat mod) {
        remainderDivide(mod, null);
    }

    /**
     * Negate current BigNat modulo provided modulus.
     */
    public void modNegate(BigNat mod) {
        BigNat tmp = rm.BN_B;

        tmp.lock();
        tmp.clone(mod);
        tmp.subtract(this, (short) 0, (short) 1);
        setSize(mod.length());
        copy(tmp);
        tmp.unlock();
    }

    public void ctModNegate(BigNat mod) {
        BigNat tmp = rm.BN_B;

        tmp.lock();
        tmp.ctClone(mod);
        tmp.ctSubtract(this);
        setSize(mod.length());
        ctCopy(tmp);
        tmp.unlock();
    }

    /**
     * Modular addition of a BigNat to this.
     */
    public void modAdd(BigNat other, BigNat mod) {
        resize((short) (mod.length() + 1));
        add(other);
        if (!isLesser(mod)) {
            subtract(mod);
        }
        setSize(mod.length());
    }

//    public void ctModAdd(BigNat other, BigNat mod) {
//        resize((short) (mod.length() + 1));
//        ctAdd(other);
//        short thisIsLesser = ctIsLesser(mod);
//        ctSubtract(mod, thisIsLesser);
//        setSize(mod.length());
//    }

    /**
     * Modular subtraction of a BigNat from this.
     */
    public void modSub(BigNat other, BigNat mod) {
        resize((short) (mod.length() + 1));
        if (isLesser(other)) {
            add(mod);
        }
        subtract(other);
        setSize(mod.length());
    }

//    public void ctModSub(BigNat other, BigNat mod) {
//        resize((short) (mod.length() + 1));
//        short thisLesser = ctIsLesser(other);
//        ctAdd(mod, (short) (~thisLesser));
//        subtract(other);
//        setSize(mod.length());
//    }

    /**
     * Square this mod a modulus fixed with fixModSqMod method.
     */
    private void modSqFixed() {
        BigNat tmpMod = rm.BN_F;
        byte[] tmpBuffer = rm.ARRAY_A;
        short modLength;

        tmpMod.setSize(rm.MAX_EXP_LENGTH);
        if (OperationSupport.getInstance().RSA_RESIZE_MOD == (short) 0xffff) {
            modLength = rm.MAX_EXP_LENGTH;
        } else {
            modLength = rm.fixedMod.length();
        }

        prependZeros(modLength, tmpBuffer, (short) 0);
        short len = rm.modSqCiph.doFinal(tmpBuffer, (short) 0, modLength, tmpBuffer, (short) 0);

        if (len != rm.MAX_EXP_LENGTH) {
            if (OperationSupport.getInstance().RSA_PREPEND_ZEROS == (short) 0xffff) {
                Util.arrayCopyNonAtomic(tmpBuffer, (short) 0, tmpBuffer, (short) (rm.MAX_EXP_LENGTH - len), len);
                Util.arrayFillNonAtomic(tmpBuffer, (short) 0, (short) (rm.MAX_EXP_LENGTH - len), (byte) 0);
            } else {
                ISOException.throwIt(ReturnCodes.SW_ECPOINT_UNEXPECTED_KA_LEN);
            }
        }
        tmpMod.fromByteArray(tmpBuffer, (short) 0, rm.MAX_EXP_LENGTH);

        if (OperationSupport.getInstance().RSA_EXTRA_MOD == (short) 0xffff) {
            tmpMod.mod(rm.fixedMod);
        }
        setSize(rm.fixedMod.length());
        copy(tmpMod);
    }

//    private void ctModSqFixed() {
//        BigNat tmpMod = rm.BN_F;
//        byte[] tmpBuffer = rm.ARRAY_A;
//        short modLength;
//
//        tmpMod.setSize(rm.MAX_EXP_LENGTH);
//
//        short tmpLength = rm.fixedMod.length();
//        modLength = ConstantTime.ctSelect(OperationSupport.getInstance().RSA_RESIZE_MOD, rm.MAX_EXP_LENGTH, tmpLength);
//
//        ctPrependZeros(modLength, tmpBuffer, (short) 0);
//        short len = rm.modSqCiph.doFinal(tmpBuffer, (short) 0, modLength, tmpBuffer, (short) 0); // possible time leak
//
//        short lenShorter = ConstantTime.ctNotEqual(len, rm.MAX_EXP_LENGTH);
//        short doCopyAndFill = (short) (lenShorter & OperationSupport.getInstance().RSA_PREPEND_ZEROS);
//        short exception = (short) (lenShorter & ~OperationSupport.getInstance().RSA_PREPEND_ZEROS);
//        CTUtil.ctArrayCopyNonAtomic(tmpBuffer, (short) 0, tmpBuffer, (short) (rm.MAX_EXP_LENGTH - len), len, (short) (~doCopyAndFill));
//        CTUtil.ctArrayFillNonAtomic(tmpBuffer, (short) 0, (short) (rm.MAX_EXP_LENGTH - len), (byte) 0, (short) (~doCopyAndFill));
//        // TODO blind when exception happens
//        tmpMod.ctFromByteArray(tmpBuffer, (short) 0, rm.MAX_EXP_LENGTH);
//
//        tmpMod.ctMod(rm.fixedMod, (short) (~OperationSupport.getInstance().RSA_EXTRA_MOD));
//        setSize(rm.fixedMod.length());
//        ctCopy(tmpMod);
//
//        if (exception == (short) 0xffff)
//            ISOException.throwIt(ReturnCodes.SW_ECPOINT_UNEXPECTED_KA_LEN);
//    }

    /**
     * Computes (this ^ exp % mod) using RSA algorithm and store results into this.
     */
    public void modExp(BigNat exp, BigNat mod) {
        if (OperationSupport.getInstance().RSA_EXP != (short) 0xffff)
            ISOException.throwIt(ReturnCodes.SW_OPERATION_NOT_SUPPORTED);
        if (OperationSupport.getInstance().RSA_CHECK_EXP_ONE == (short) 0xffff && exp.isOne())
            return;
        if (OperationSupport.getInstance().RSA_SQ != (short) 0xffff && exp.isTwo()) {
            modMult(this, mod);
            return;
        }

        BigNat tmpMod = rm.BN_F; // modExp is called from modSqrt => requires BN_F not being locked when modExp is called
        byte[] tmpBuffer = rm.ARRAY_A;
        short modLength;

        tmpMod.lock();
        tmpMod.setSize(rm.MAX_EXP_LENGTH);

        if (OperationSupport.getInstance().RSA_PUB == (short) 0xffff) {
            // Verify if pre-allocated engine match the required values
            if (rm.expPub.getSize() < (short) (mod.length() * 8) || rm.expPub.getSize() < (short) (length() * 8)) {
                ISOException.throwIt(ReturnCodes.SW_BIGNAT_MODULOTOOLARGE);
            }
            if (OperationSupport.getInstance().RSA_KEY_REFRESH == (short) 0xffff) {
                // Simulator fails when reusing the original object
                rm.expPub = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, rm.MAX_EXP_BIT_LENGTH, false);
            }
            rm.lock(tmpBuffer);
            short len = exp.copyToByteArray(tmpBuffer, (short) 0);
            rm.expPub.setExponent(tmpBuffer, (short) 0, len);
            if (OperationSupport.getInstance().RSA_RESIZE_MOD == (short) 0xffff) {
                if (OperationSupport.getInstance().RSA_APPEND_MOD == (short) 0xffff) {
                    mod.appendZeros(rm.MAX_EXP_LENGTH, tmpBuffer, (short) 0);
                } else {
                    mod.prependZeros(rm.MAX_EXP_LENGTH, tmpBuffer, (short) 0);
                }
                rm.expPub.setModulus(tmpBuffer, (short) 0, rm.MAX_EXP_LENGTH);
                modLength = rm.MAX_EXP_LENGTH;
            } else {
                modLength = mod.copyToByteArray(tmpBuffer, (short) 0);
                rm.expPub.setModulus(tmpBuffer, (short) 0, modLength);
            }
            rm.expCiph.init(rm.expPub, Cipher.MODE_DECRYPT);
        } else {
            // Verify if pre-allocated engine match the required values
            if (rm.expPriv.getSize() < (short) (mod.length() * 8) || rm.expPriv.getSize() < (short) (length() * 8)) {
                ISOException.throwIt(ReturnCodes.SW_BIGNAT_MODULOTOOLARGE);
            }
            if (OperationSupport.getInstance().RSA_KEY_REFRESH == (short) 0xffff) {
                // Simulator fails when reusing the original object
                rm.expPriv = (RSAPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, rm.MAX_EXP_BIT_LENGTH, false);
            }
            rm.lock(tmpBuffer);
            short len = exp.copyToByteArray(tmpBuffer, (short) 0);
            rm.expPriv.setExponent(tmpBuffer, (short) 0, len);
            if (OperationSupport.getInstance().RSA_RESIZE_MOD == (short) 0xffff) {
                if (OperationSupport.getInstance().RSA_APPEND_MOD == (short) 0xffff) {
                    mod.appendZeros(rm.MAX_EXP_LENGTH, tmpBuffer, (short) 0);
                } else {
                    mod.prependZeros(rm.MAX_EXP_LENGTH, tmpBuffer, (short) 0);

                }
                rm.expPriv.setModulus(tmpBuffer, (short) 0, rm.MAX_EXP_LENGTH);
                modLength = rm.MAX_EXP_LENGTH;
            } else {
                modLength = mod.copyToByteArray(tmpBuffer, (short) 0);
                rm.expPriv.setModulus(tmpBuffer, (short) 0, modLength);
            }
            rm.expCiph.init(rm.expPriv, Cipher.MODE_DECRYPT);
        }

        prependZeros(modLength, tmpBuffer, (short) 0);
        short len = rm.expCiph.doFinal(tmpBuffer, (short) 0, modLength, tmpBuffer, (short) 0);

        if (len != rm.MAX_EXP_LENGTH) {
            if (OperationSupport.getInstance().RSA_PREPEND_ZEROS == (short) 0xffff) {
                // Decrypted length can be either tmp_size or less because of leading zeroes consumed by simulator engine implementation
                // Move obtained value into proper position with zeroes prepended
                Util.arrayCopyNonAtomic(tmpBuffer, (short) 0, tmpBuffer, (short) (rm.MAX_EXP_LENGTH - len), len);
                Util.arrayFillNonAtomic(tmpBuffer, (short) 0, (short) (rm.MAX_EXP_LENGTH - len), (byte) 0);
            } else {
                // real cards should keep whole length of block
                ISOException.throwIt(ReturnCodes.SW_ECPOINT_UNEXPECTED_KA_LEN);
            }
        }
        tmpMod.fromByteArray(tmpBuffer, (short) 0, rm.MAX_EXP_LENGTH);
        rm.unlock(tmpBuffer);

        if (OperationSupport.getInstance().RSA_EXTRA_MOD == (short) 0xffff) {
            tmpMod.mod(mod);
        }
        setSize(mod.length());
        copy(tmpMod);
        tmpMod.unlock();
    }

    /* Must be ensured that exp is not 2 when RSA_SQ not supported */
//    public void ctModExp(BigNat exp, BigNat mod) {
//        if (OperationSupport.getInstance().RSA_EXP != (short) 0xffff)
//            ISOException.throwIt(ReturnCodes.SW_OPERATION_NOT_SUPPORTED);
//
//        short isOne = exp.ctEquals((byte) 1);
//        short doNothing = (short) (OperationSupport.getInstance().RSA_CHECK_EXP_ONE & isOne); // nothing needs to be done
//
//        short isTwo = exp.ctEquals((byte) 2);
//        short expIsTwoAndNoRSASQ = (short) (~OperationSupport.getInstance().RSA_SQ & isTwo & ~doNothing);
//        ctModMult(this, mod, (short) (~expIsTwoAndNoRSASQ));
//        short blind = (short) (doNothing | expIsTwoAndNoRSASQ);
//
//        BigNat tmpMod = rm.BN_F; // modExp is called from modSqrt => requires BN_F not being locked when modExp is called
//        byte[] tmpBuffer = rm.ARRAY_A;
//        short modLength;
//
//        tmpMod.lock();
//        tmpMod.setSize(rm.MAX_EXP_LENGTH);
//
//        if (OperationSupport.getInstance().RSA_PUB == (short) 0xffff) {
//            // Verify if pre-allocated engine match the required values
//            short exception = ConstantTime.ctSelect(ConstantTime.ctLessThan(rm.expPub.getSize(), (short) (mod.length() * 8)),
//                    ReturnCodes.SW_BIGNAT_MODULOTOOLARGE, (short) 0);
//            exception = ConstantTime.ctSelect(ConstantTime.ctLessThan(rm.expPub.getSize(), (short) (length() * 8)),
//                    ReturnCodes.SW_BIGNAT_MODULOTOOLARGE, exception);
//            blind |= ConstantTime.ctIsNonZero(exception);
//            // TODO: rm.expPub.setModulus will not work, when blind?
//
//            if (OperationSupport.getInstance().RSA_KEY_REFRESH == (short) 0xffff) {
//                // Simulator fails when reusing the original object
//                rm.expPub = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, rm.MAX_EXP_BIT_LENGTH, false); // JavaCard: possible leak
//            }
//            rm.lock(tmpBuffer);
//            short len = exp.ctCopyToByteArray(tmpBuffer, (short) 0);
//            rm.expPub.setExponent(tmpBuffer, (short) 0, len); // JavaCard: possible leak
//            if (OperationSupport.getInstance().RSA_RESIZE_MOD == (short) 0xffff) {
//                if (OperationSupport.getInstance().RSA_APPEND_MOD == (short) 0xffff) {
//                    mod.ctAppendZeros(rm.MAX_EXP_LENGTH, tmpBuffer, (short) 0);
//                } else {
//                    mod.ctPrependZeros(rm.MAX_EXP_LENGTH, tmpBuffer, (short) 0);
//                }
//                rm.expPub.setModulus(tmpBuffer, (short) 0, rm.MAX_EXP_LENGTH);  // JavaCard: possible leak
//                modLength = rm.MAX_EXP_LENGTH;
//            } else {
//                modLength = mod.ctCopyToByteArray(tmpBuffer, (short) 0);
//                rm.expPub.setModulus(tmpBuffer, (short) 0, modLength);  // JavaCard: possible leak
//            }
//            rm.expCiph.init(rm.expPub, Cipher.MODE_DECRYPT); // JavaCard: possible leak
//        } else {
//            // Verify if pre-allocated engine match the required values
//            short exception = ConstantTime.ctSelect(ConstantTime.ctLessThan(rm.expPriv.getSize(), (short) (mod.length() * 8)),
//                    ReturnCodes.SW_BIGNAT_MODULOTOOLARGE, (short) 0);
//            exception = ConstantTime.ctSelect(ConstantTime.ctLessThan(rm.expPriv.getSize(), (short) (length() * 8)),
//                    ReturnCodes.SW_BIGNAT_MODULOTOOLARGE, exception);
//            blind |= ConstantTime.ctIsNonZero(exception);
//
//            if (OperationSupport.getInstance().RSA_KEY_REFRESH == (short) 0xffff) {
//                // Simulator fails when reusing the original object
//                rm.expPriv = (RSAPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, rm.MAX_EXP_BIT_LENGTH, false);  // JavaCard: possible leak
//            }
//            rm.lock(tmpBuffer);
//            short len = exp.ctCopyToByteArray(tmpBuffer, (short) 0);
//            rm.expPriv.setExponent(tmpBuffer, (short) 0, len);  // JavaCard: possible leak
//            if (OperationSupport.getInstance().RSA_RESIZE_MOD == (short) 0xffff) {
//                if (OperationSupport.getInstance().RSA_APPEND_MOD == (short) 0xffff) {
//                    mod.appendZeros(rm.MAX_EXP_LENGTH, tmpBuffer, (short) 0);
//                } else {
//                    mod.prependZeros(rm.MAX_EXP_LENGTH, tmpBuffer, (short) 0);
//
//                }
//                rm.expPriv.setModulus(tmpBuffer, (short) 0, rm.MAX_EXP_LENGTH); // JavaCard: possible leak
//                modLength = rm.MAX_EXP_LENGTH;
//            } else {
//                modLength = mod.ctCopyToByteArray(tmpBuffer, (short) 0);
//                rm.expPriv.setModulus(tmpBuffer, (short) 0, modLength); // JavaCard: possible leak
//            }
//            rm.expCiph.init(rm.expPriv, Cipher.MODE_DECRYPT); // JavaCard: possible leak
//        }
//
//        prependZeros(modLength, tmpBuffer, (short) 0); // TODO altering this
//        short len = rm.expCiph.doFinal(tmpBuffer, (short) 0, modLength, tmpBuffer, (short) 0); // JavaCard: possible leak
//
//        if (len != rm.MAX_EXP_LENGTH) {
//            if (OperationSupport.getInstance().RSA_PREPEND_ZEROS == (short) 0xffff) {
//                // Decrypted length can be either tmp_size or less because of leading zeroes consumed by simulator engine implementation
//                // Move obtained value into proper position with zeroes prepended
//                CTUtil.ctArrayCopyNonAtomic(tmpBuffer, (short) 0, tmpBuffer, (short) (rm.MAX_EXP_LENGTH - len), len);
//                CTUtil.ctArrayFillNonAtomic(tmpBuffer, (short) 0, (short) (rm.MAX_EXP_LENGTH - len), (byte) 0);
//            } else {
//                // real cards should keep whole length of block
//                ISOException.throwIt(ReturnCodes.SW_ECPOINT_UNEXPECTED_KA_LEN);
//            }
//        }
//        tmpMod.ctFromByteArray(tmpBuffer, (short) 0, rm.MAX_EXP_LENGTH);
//        rm.unlock(tmpBuffer);
//
//        if (OperationSupport.getInstance().RSA_EXTRA_MOD == (short) 0xffff) {
//            tmpMod.ctMod(mod);
//        }
//        ctSetSize(mod.length(), blind);
//        ctCopy(tmpMod, blind);
//        tmpMod.unlock();
//    }

    /**
     * Computes modular inversion. The result is stored into this.
     */
    public void modInv(BigNat mod) {
        BigNat tmp = rm.BN_B;
        tmp.lock();
        tmp.clone(mod);
        tmp.decrement();
        tmp.decrement();

        modExp(tmp, mod);
        tmp.unlock();
    }

//    public void ctModInv(BigNat mod) {
//        BigNat tmp = rm.BN_B;
//        tmp.lock();
//        tmp.ctClone(mod);
//        tmp.ctSubtract(ResourceManager.TWO);
//
//        ctModExp(tmp, mod);
//        tmp.unlock();
//    }

    /**
     * Multiplication of this and other modulo mod. The result is stored to this.
     */
    public void modMult(BigNat other, BigNat mod) {
        BigNat tmp = rm.BN_D;
        BigNat result = rm.BN_E;

        if (OperationSupport.getInstance().RSA_CHECK_ONE == (short) 0xffff && isOne()) {
            copy(other);
            return;
        }

        result.lock();
        if ((OperationSupport.getInstance().RSA_SQ != (short) 0xffff) || (OperationSupport.getInstance().RSA_EXTRA_MOD == (short) 0xffff)) {
            result.clone(this);
            result.mult(other);
            result.mod(mod);
        } else {
            result.setSize((short) (mod.length() + 1));
            result.copy(this);
            result.add(other);

            short carry = (byte) 0;
            if (result.isOdd()) {
                if (result.isLesser(mod)) {
                    carry = result.add(mod);
                } else {
                    result.subtract(mod);
                }
            }
            result.shiftRightBits((short) 1, carry);
            result.resize(mod.length());

            tmp.lock();
            tmp.clone(result);
            tmp.modSub(other, mod);

            result.modSq(mod);
            tmp.modSq(mod);

            result.modSub(tmp, mod);
            tmp.unlock();
        }
        setSize(mod.length());
        copy(result);
        result.unlock();
    }

//    public void ctModMult(BigNat other, BigNat mod) {
//        BigNat tmp = rm.BN_D;
//        BigNat result = rm.BN_E;
//
//        short isOne = ctEquals((byte) 1);
//        short onlyCopy = (short) (OperationSupport.getInstance().RSA_CHECK_ONE & isOne);
//
//        result.lock();
//        if ((OperationSupport.getInstance().RSA_SQ != (short) 0xffff) || (OperationSupport.getInstance().RSA_EXTRA_MOD == (short) 0xffff)) {
//            ctCopy(other, (short) (~onlyCopy));
//            result.ctClone(this);
//            result.ctMult(other);
//            result.ctMod(mod);
//        } else {
//            result.setSize((short) (mod.length() + 1));
//            result.ctCopy(this);
//            ctCopy(other, (short) (~onlyCopy));
//
//            result.ctAdd(other);
//            short isOdd = result.ctIsOdd();
//            short isLesser = result.ctIsLesser(mod);
//            short carry = result.ctAdd(mod, (short) (~(isOdd & isLesser)));
//            carry = ConstantTime.ctSelect((short) (isOdd & isLesser), carry, (short) 0);
//            result.ctSubtract(mod, (short) (~(isOdd & ~isLesser)));
//
//            result.ctShiftRightBits((short) 1, carry);
//            result.ctResize(mod.length());
//
//            tmp.lock();
//            tmp.ctClone(result);
//            tmp.ctModSub(other, mod);
//
//            result.ctModSq(mod);
//            tmp.ctModSq(mod);
//
//            result.ctModSub(tmp, mod);
//            tmp.unlock();
//        }
//        ctSetSize(mod.length(), onlyCopy);
//        ctCopy(result, onlyCopy); // do not change this if already copied in the beggining
//        result.unlock();
//    }
//
//    public void ctModMult(BigNat other, BigNat mod, short blind) {
//        BigNat tmp = rm.BN_D;
//        BigNat result = rm.BN_E;
//
//        short isOne = ctEquals((byte) 1);
//        short onlyCopy = (short) (OperationSupport.getInstance().RSA_CHECK_ONE & isOne);
//
//        result.lock();
//        if ((OperationSupport.getInstance().RSA_SQ != (short) 0xffff) || (OperationSupport.getInstance().RSA_EXTRA_MOD == (short) 0xffff)) {
//            ctCopy(other, (short) (~onlyCopy | blind));
//            result.ctClone(this);
//            result.ctMult(other);
//            result.ctMod(mod);
//        } else {
//            result.setSize((short) (mod.length() + 1));
//            result.ctCopy(this);
//            ctCopy(other, (short) (~onlyCopy | blind));
//
//            result.ctAdd(other);
//            short isOdd = result.ctIsOdd();
//            short isLesser = result.ctIsLesser(mod);
//            short carry = result.ctAdd(mod, (short) (~(isOdd & isLesser)));
//            carry = ConstantTime.ctSelect((short) (isOdd & isLesser), carry, (short) 0);
//            result.ctSubtract(mod, (short) (~(isOdd & ~isLesser)));
//
//            result.ctShiftRightBits((short) 1, carry);
//            result.ctResize(mod.length());
//
//            tmp.lock();
//            tmp.ctClone(result);
//            tmp.ctModSub(other, mod);
//
//            result.ctModSq(mod);
//            tmp.ctModSq(mod);
//
//            result.ctModSub(tmp, mod);
//            tmp.unlock();
//        }
//        ctSetSize(mod.length(), (short) (onlyCopy | blind));
//        ctCopy(result, (short) (onlyCopy | blind)); // do not change this if already copied in the beggining
//        result.unlock();
//    }

    /**
     * Computes modulo square of this BigNat.
     */
    public void modSq(BigNat mod) {
        if (OperationSupport.getInstance().RSA_SQ == (short) 0xffff) {
            if (rm.fixedMod != null && rm.fixedMod == mod) {
                modSqFixed();
            } else {
                modExp(ResourceManager.TWO, mod);
            }
        } else {
            modMult(this, mod);
        }
    }

    /** Constant-time implementation of modulo square of this BigNat.
     *
     * @param mod modulo BigNat
     */
//    public void ctModSq(BigNat mod) {
//        if (OperationSupport.getInstance().RSA_SQ == (short) 0xffff) {
//            if (rm.fixedMod != null && rm.fixedMod == mod) {
//                ctModSqFixed();
//            } else {
//                ctModExp(ResourceManager.TWO, mod);
//            }
//        } else {
//            ctModMult(this, mod);
//        }
//    }

    /**
     * Checks whether this BigNat is a quadratic residue modulo p.
     * @param p modulo
     */
    public boolean isQuadraticResidue(BigNat p) {
        BigNat tmp = rm.BN_A;
        BigNat exp = rm.BN_B;
        tmp.clone(this);
        exp.clone(p);
        exp.decrement();
        exp.shiftRight((short) 1);
        tmp.modExp(exp, p);
        return tmp.isOne();
    }

    /**
     * Computes square root of provided BigNat which MUST be prime using Tonelli Shanks Algorithm. The result (one of
     * the two roots) is stored to this.
     */
    public void modSqrt(BigNat p) {
        BigNat exp = rm.BN_G;
        BigNat p1 = rm.BN_B;
        BigNat q = rm.BN_C;
        BigNat tmp = rm.BN_D;
        BigNat z = rm.BN_A;
        BigNat t = rm.BN_B;
        BigNat b = rm.BN_C;

        // 1. Find Q and S such that p - 1 = Q * 2^S and Q is odd
        p1.lock();
        p1.clone(p);
        p1.decrement();

        q.lock();
        q.clone(p1);

        short s = 0;
        while (!q.isOdd()) {
            ++s;
            q.shiftRightBits((short) 1);
        }

        // 2. Find the first quadratic non-residue z by brute-force search
        exp.lock();
        exp.clone(p1);
        exp.shiftRightBits((short) 1);

        z.lock();
        z.setSize(p.length());
        z.setValue((byte) 1);
        tmp.lock();
        tmp.setSize(p.length());
        tmp.setValue((byte) 1);

        while (!tmp.equals(p1)) {
            z.increment();
            tmp.copy(z);
            tmp.modExp(exp, p); // Euler's criterion
        }
        p1.unlock();
        tmp.unlock();

        // 3. Compute the first candidate
        exp.clone(q);
        exp.increment();
        exp.shiftRightBits((short) 1);

        t.lock();
        t.clone(this);
        t.modExp(q, p);

        if (t.isZero()) {
            z.unlock();
            t.unlock();
            exp.unlock();
            q.unlock();
            zero();
            return;
        }

        mod(p);
        modExp(exp, p);
        exp.unlock();

        if (t.isOne()) {
            z.unlock();
            t.unlock();
            q.unlock();
            return;
        }

        // 4. Search for further candidates
        z.modExp(q, p);
        q.unlock();

        while(true) {
            tmp.lock();
            tmp.clone(t);
            short i = 0;

            do {
                tmp.modSq(p);
                ++i;
            } while (!tmp.isOne());

            tmp.unlock();

            b.lock();
            b.clone(z);
            s -= i;
            --s;

            tmp.lock();
            tmp.setSize((short) 1);
            tmp.setValue((byte) 1);
            while(s != 0) {
                tmp.shiftLeftBits((short) 1);
                --s;
            }
            b.modExp(tmp, p);
            tmp.unlock();
            s = i;
            z.clone(b);
            z.modSq(p);
            t.modMult(z, p);
            modMult(b, p);
            b.unlock();

            if(t.isZero()) {
                zero();
                break;
            }
            if(t.isOne()) {
                break;
            }
        }
        z.unlock();
        t.unlock();
    }

    /** Constant-time implementation square root of provided BigNat which MUST be prime using Tonelli Shanks Algorithm. The result (one of
     * the two roots) is stored to this.
     * TODO: Implement
     *
     * @param p prime for modulo
     */
//    public void ctModSqrt(BigNat p) {
//        BigNat exp = rm.BN_G;
//        BigNat p1 = rm.BN_B;
//        BigNat q = rm.BN_C;
//        BigNat tmp = rm.BN_D;
//        BigNat z = rm.BN_A;
//        BigNat t = rm.BN_B;
//        BigNat b = rm.BN_C;
//
//        // 1. Find Q and S such that p - 1 = Q * 2^S and Q is odd
//        p1.lock();
//        p1.ctClone(p);
//        p1.ctDecrement();
//
//        q.lock();
//        q.ctClone(p1);
//
//        short s = ctGetFirstBitPosition((byte) 0);
//        ctShiftRight(s);
//
//        // 2. Find the first quadratic non-residue z by brute-force search
//        exp.lock();
//        exp.ctClone(p1);
//        exp.ctShiftRightBits((short) 1);
//
//
//        z.lock();
//        z.setSize(p.length());
//        z.setValue((byte) 1);
//        tmp.lock();
//        tmp.setSize(p.length());
//        tmp.setValue((byte) 1);
//
//        // TODO: continue with Tonelli-Shanks Algorithm
//        while (!tmp.equals(p1)) {
//            z.increment();
//            tmp.copy(z);
//            tmp.modExp(exp, p); // Euler's criterion
//        }
//        p1.unlock();
//        tmp.unlock();
//
//        // 3. Compute the first candidate
//        exp.ctClone(q);
//        exp.ctIncrement();
//        exp.ctShiftRightBits((short) 1);
//        t.lock();
//        t.clone(this);
//        t.modExp(q, p);
//
//        if (t.equals((byte) 0)) {
//            z.unlock();
//            t.unlock();
//            exp.unlock();
//            q.unlock();
//            zero();
//            return;
//        }
//
//        mod(p);
//        modExp(exp, p);
//        exp.unlock();
//
//        if (t.equals((byte) 1)) {
//            z.unlock();
//            t.unlock();
//            q.unlock();
//            return;
//        }
//
//        // 4. Search for further candidates
//        z.modExp(q, p);
//        q.unlock();
//
//        while(true) {
//            tmp.lock();
//            tmp.clone(t);
//            short i = 0;
//
//            do {
//                tmp.modSq(p);
//                ++i;
//            } while (!tmp.equals((byte) 1));
//
//            tmp.unlock();
//
//            b.lock();
//            b.clone(z);
//            s -= i;
//            --s;
//
//            tmp.lock();
//            tmp.setSize((short) 1);
//            tmp.setValue((byte) 1);
//            while(s != 0) {
//                tmp.shiftLeftBits((short) 1);
//                --s;
//            }
//            b.modExp(tmp, p);
//            tmp.unlock();
//            s = i;
//            z.clone(b);
//            z.modSq(p);
//            t.modMult(z, p);
//            modMult(b, p);
//            b.unlock();
//
//            if(t.equals((byte) 0)) {
//                zero();
//                break;
//            }
//            if(t.equals((byte) 1)) {
//                break;
//            }
//        }
//        z.unlock();
//        t.unlock();
//    }
}
