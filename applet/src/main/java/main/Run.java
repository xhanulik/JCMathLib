package main;

import com.licel.jcardsim.smartcardio.CardSimulator;
import com.licel.jcardsim.utils.AIDUtil;
import javacard.framework.AID;
import opencrypto.jcmathlib.ConstantTimeUnitTests;
import org.bouncycastle.util.encoders.Hex;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.Arrays;

public class Run {
    public static void main(String[] args) throws Exception {
        CardSimulator simulator = new CardSimulator();

        AID appletAID = AIDUtil.create("Example".getBytes());
        simulator.installApplet(appletAID, ConstantTimeUnitTests.class);

        simulator.selectApplet(appletAID);

        CommandAPDU commandAPDU = new CommandAPDU(0xB0, 0x21, 0x05, 0x00, new byte[]{0x01, 0x02, 0x03, 0x04,0x05});
        ResponseAPDU response = simulator.transmitCommand(commandAPDU);

        System.out.printf("INS: %x, P1: %x, P2: %x, DATA: %s\n", commandAPDU.getINS(), commandAPDU.getP1(), commandAPDU.getP2(), Arrays.toString(commandAPDU.getData()));
        System.out.printf("RES: %2x %2x, DATA: %s\n", response.getSW1(), response.getSW2(), new String(response.getData()));
    }
}