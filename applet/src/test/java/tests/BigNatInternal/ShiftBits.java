package tests.BigNatInternal;

import opencrypto.jcmathlib.BigNatInternal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ShiftBits {
    @Test
    public void noChange() {
        short result = BigNatInternal.ctShiftBits((short) 0b0000000000000001, (byte) 0b10000000, (byte) 0b00000000, (short) 0);
        Assertions.assertEquals((short) 0b0000000000000001, result);
    }

    @Test
    public void highestBits() {
        short result = BigNatInternal.ctShiftBits((short) 0b0000000000000001, (byte) 0b00000000, (byte) 0b00000000, (short) 3);
        Assertions.assertEquals((short) 0b0000000000001000, result);
    }

    @Test
    public void middleBits() {
        short result = BigNatInternal.ctShiftBits((short) 0b0000000000000001, (byte) 0b10100000, (byte) 0b00000000, (short) 3);
        Assertions.assertEquals((short) 0b0000000000001101, result);
    }

    @Test
    public void lowestBits() {
        short result = BigNatInternal.ctShiftBits((short) 0b0000000000000001, (byte) 0b10100000, (byte) 0b11000001, (short) 16);
        Assertions.assertEquals((short) 0b01010000011000001, result);
    }

    @Test
    public void bigShift() {
        short result = BigNatInternal.ctShiftBits((short) 0b0000000000000001, (byte) 0b10100000, (byte) 0b11000001, (short) 18);
        Assertions.assertEquals(-32768, result);
    }
}
