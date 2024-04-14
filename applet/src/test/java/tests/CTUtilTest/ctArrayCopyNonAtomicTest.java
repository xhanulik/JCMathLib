package tests.CTUtilTest;

import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import opencrypto.jcmathlib.BigNat;
import opencrypto.jcmathlib.CTUtil;
import opencrypto.jcmathlib.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ctArrayCopyNonAtomicTest {
    @Test
    public void srcTooShort() {
        byte[] src = new byte[10];
        byte[] dest = new byte[12];
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> CTUtil.ctArrayCopyNonAtomic(src, (short) 0, dest, (short) 0, (short) 11));
    }

    @Test
    public void destTooShort() {
        byte[] src = new byte[10];
        byte[] dest = new byte[12];
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> CTUtil.ctArrayCopyNonAtomic(src, (short) 0, dest, (short) 0, (short) 11));
    }
}
