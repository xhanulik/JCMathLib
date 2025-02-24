import com.licel.jcardsim.bouncycastle.util.encoders.Hex;
import com.licel.jcardsim.smartcardio.CardSimulator;
import com.licel.jcardsim.utils.AIDUtil;
import javacard.framework.AID;
import opencrypto.jcmathlib.DifFuzzApplet;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.smartcardio.CommandAPDU;

public class DifFuzzDriver {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        int n = 3;
        int maxM= 64;
        System.out.println("maxM=" + maxM);

        byte[] value;

        // Read all inputs.
        List<Byte> values = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(args[0])) {
            byte[] bytes = new byte[1];
            while ((fis.read(bytes) != -1) ) {//&& (i < maxM * n)
                values.add(bytes[0]);
            }
        } catch (IOException e) {
            System.err.println("Error reading input...");
            e.printStackTrace();
            return;
        }

        if (values.size() < n) {
            throw new RuntimeException("Too Less Data...");
        }

        int m = values.size() / n;
        System.out.println("m=" + m);

        // copy into one array to be passed to applet
        value = new byte[values.size()];
        for (int i = 0; i < values.size(); i++) {
            value[i] = values.get(i);
        }

        // print inputs
        System.out.println("value=" + Arrays.toString(value));

        // prepare simulator
        CardSimulator simulator = new CardSimulator();
        AID appletAID = AIDUtil.create("DifFuzzApplet".getBytes());
        simulator.installApplet(appletAID, DifFuzzApplet.class);
        simulator.selectApplet(appletAID);
        byte p1 = (byte) (value.length / n);

        // send data
        System.out.println(Hex.toHexString(simulator.transmitCommand(new CommandAPDU(0, 0, p1, 0, value)).getData()));
        System.out.println("Done.");
    }
}
