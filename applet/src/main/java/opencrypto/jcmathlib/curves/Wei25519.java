package opencrypto.jcmathlib;

public class Wei25519 {
    public final static short k = 8;

    public final static byte[] p = {
            (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xed
    };


    public final static byte[] a = {
            (byte) 0x2a, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa,
            (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa,
            (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa,
            (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa,
            (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa,
            (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa,
            (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0x98,
            (byte) 0x49, (byte) 0x14, (byte) 0xa1, (byte) 0x44
    };

    public final static byte[] b = {
            (byte) 0x7b, (byte) 0x42, (byte) 0x5e, (byte) 0xd0,
            (byte) 0x97, (byte) 0xb4, (byte) 0x25, (byte) 0xed,
            (byte) 0x09, (byte) 0x7b, (byte) 0x42, (byte) 0x5e,
            (byte) 0xd0, (byte) 0x97, (byte) 0xb4, (byte) 0x25,
            (byte) 0xed, (byte) 0x09, (byte) 0x7b, (byte) 0x42,
            (byte) 0x5e, (byte) 0xd0, (byte) 0x97, (byte) 0xb4,
            (byte) 0x26, (byte) 0x0b, (byte) 0x5e, (byte) 0x9c,
            (byte) 0x77, (byte) 0x10, (byte) 0xc8, (byte) 0x64
    };

    public final static byte[] G = {
            (byte) 0x04,

            (byte) 0x2a, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa,
            (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa,
            (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa,
            (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa,
            (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa,
            (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa,
            (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa,
            (byte) 0xaa, (byte) 0xad, (byte) 0x24, (byte) 0x5a,

            (byte) 0x20, (byte) 0xae, (byte) 0x19, (byte) 0xa1,
            (byte) 0xb8, (byte) 0xa0, (byte) 0x86, (byte) 0xb4,
            (byte) 0xe0, (byte) 0x1e, (byte) 0xdd, (byte) 0x2c,
            (byte) 0x77, (byte) 0x48, (byte) 0xd1, (byte) 0x4c,
            (byte) 0x92, (byte) 0x3d, (byte) 0x4d, (byte) 0x7e,
            (byte) 0x6d, (byte) 0x7c, (byte) 0x61, (byte) 0xb2,
            (byte) 0x29, (byte) 0xe9, (byte) 0xc5, (byte) 0xa2,
            (byte) 0x7e, (byte) 0xce, (byte) 0xd3, (byte) 0xd9
    };

    public final static byte[] r = {
            (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x14, (byte) 0xde, (byte) 0xf9, (byte) 0xde,
            (byte) 0xa2, (byte) 0xf7, (byte) 0x9c, (byte) 0xd6,
            (byte) 0x58, (byte) 0x12, (byte) 0x63, (byte) 0x1a,
            (byte) 0x5c, (byte) 0xf5, (byte) 0xd3, (byte) 0xed
    };
}