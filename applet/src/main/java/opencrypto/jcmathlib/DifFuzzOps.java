package opencrypto.jcmathlib;

public class DifFuzzOps {
    /* Supported methods for fuzzing */
    public final static byte INS_BN_STR = (byte) 0x01;
    public final static byte INS_BN_PREPEND = (byte) 0x02;
    public final static byte INS_BN_INC = (byte) 0x03;
    public final static byte INS_BN_LESSER = (byte) 0x04;
    public final static byte INS_BN_EQUAL = (byte) 0x05;
    public final static byte INS_BN_ADD = (byte) 0x06;
    public final static byte INS_BN_DIV = (byte) 0x07;
    public final static byte INS_BN_MULT = (byte) 0x08;
    public final static byte INS_BN_ZERO = (byte) 0x09;
    public final static byte INS_BN_SHRINK = (byte) 0x40;
    public final static byte INS_BN_SUB = (byte) 0x41;
    public final static byte INS_BN_CLONE = (byte) 0x42;
    public final static byte INS_BN_COPY = (byte) 0x43;

    /* BigNat class */
    public final static byte INS_BN_GCD  = (byte) 0x10;
    public final static byte INS_BN_MODNEG  = (byte) 0x11;
    public final static byte INS_BN_MODADD  = (byte) 0x12;
    public final static byte INS_BN_MODEXP = (byte) 0x13;
    public final static byte INS_BN_MODSQ = (byte) 0x14;
    public final static byte INS_BN_QUAD = (byte) 0x15;
    public final static byte INS_BN_MODMULT = (byte) 0x16;
    public final static byte INS_BN_MODINV = (byte) 0x17;
    public final static byte INS_BN_COP = (byte) 0x18;
    public final static byte INS_BN_MODSUB = (byte) 0x19;

    /* Integer Class */
    public final static byte INS_INT_LES  = (byte) 0x20;
    public final static byte INS_INT_ADD  = (byte) 0x21;
    public final static byte INS_INT_SUB  = (byte) 0x22;

    /* ECPoint Class */
    public final static byte INS_EC_NEG = (byte) 0x30;
    public final static byte INS_EC_EQ = (byte) 0x31;
    public final static byte INS_EC_MUL = (byte) 0x32;

}
