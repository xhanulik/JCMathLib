/*
 * Portions of this file are licensed under the Apache License, Version 2.0, derived from
 * https://github.com/isstac/diffuzz/blob/a408988f7a2a3dcc000f310015338838dbcb5528/evaluation/themis_jdk_safe/src/MessageDigest_FuzzDriver.java#L38.
 * Copyright (c) diffuzz (https://github.com/isstac/diffuzz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * See LICENSES/APACHE-LICENSE-2.0.
 *
 * Rest of this file are licensed under the MIT License.
 * Modifications made by Veronika Hanulíková.
 *
 */


import com.licel.jcardsim.bouncycastle.util.encoders.Hex;
import com.licel.jcardsim.smartcardio.CardSimulator;
import com.licel.jcardsim.utils.AIDUtil;
import javacard.framework.AID;
import opencrypto.jcmathlib.DifFuzzApplet;
import opencrypto.jcmathlib.DifFuzzOps;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.smartcardio.CommandAPDU;

public class DifFuzzDriver {
    /* Choose fuzzed method here */
    static byte fuzzedOp = DifFuzzOps.INS_BN_PREPEND;
    /* Settings of fuzzing driver, do not overwrite manually */
    static int MAX_LEN = 64;
    static int numberOfPartitions = 3;
    static byte p1 = 0;
    static byte p2 = 0;
    public static void main(String[] args) {
        /* BEGIN: Adapted from diffuzz (Apache License 2.0.) */
        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        /* Read all input bytes */
        List<Byte> values = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(args[0])) {
            byte[] bytes = new byte[1];
            while ((fis.read(bytes) != -1) ) {
                values.add(bytes[0]);
            }
        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            return;
        }
        /* END: Adapted from diffuzz (Apache License 2.0.) */

        /* Setup APDU parameters according to chosen operation */
        switch(fuzzedOp) {
            case DifFuzzOps.INS_BN_PREPEND:
            case DifFuzzOps.INS_BN_INC:
            case DifFuzzOps.INS_BN_STR:
            case DifFuzzOps.INS_BN_ZERO:
            case DifFuzzOps.INS_BN_SHRINK:
            case DifFuzzOps.INS_BN_MODSQ:
            case DifFuzzOps.INS_BN_QUAD:
            case DifFuzzOps.INS_BN_MODINV:
            case DifFuzzOps.INS_BN_CLONE:
            case DifFuzzOps.INS_BN_COPY:
                setupAPDUParams(2, values.size());
                break;
            case DifFuzzOps.INS_BN_ADD:
            case DifFuzzOps.INS_BN_SUB:
            case DifFuzzOps.INS_BN_MULT:
            case DifFuzzOps.INS_BN_MODNEG:
            case DifFuzzOps.INS_BN_MODEXP:
            case DifFuzzOps.INS_BN_COP:
            case DifFuzzOps.INS_INT_LES:
            case DifFuzzOps.INS_INT_SUB:
                setupAPDUParams(3, values.size());
                break;
            case DifFuzzOps.INS_BN_MODADD:
            case DifFuzzOps.INS_BN_MODSUB:
            case DifFuzzOps.INS_BN_LESSER:
            case DifFuzzOps.INS_BN_DIV:
            case DifFuzzOps.INS_BN_GCD:
            case DifFuzzOps.INS_BN_MODMULT:
            case DifFuzzOps.INS_BN_EQUAL:
            case DifFuzzOps.INS_INT_ADD:
                setupAPDUParams(4, values.size());
                break;
            case DifFuzzOps.INS_EC_NEG:
            case DifFuzzOps.INS_EC_EQ:
            case DifFuzzOps.INS_EC_MUL:
                break;
            default:
                System.err.println("Invalid fuzzed method chosen");
                return;
        }

        /* BEGIN: Adapted from diffuzz (Apache License 2.0.) */
        /* At least one byte per partition */
        if (values.size() < numberOfPartitions || values.size() / numberOfPartitions > MAX_LEN) {
            throw new RuntimeException("Not enough data");
        }

        /* Copy into one byte array to be passed to applet */
        byte[] value = new byte[values.size()];
        for (int i = 0; i < values.size(); i++) {
            value[i] = values.get(i);
        }
        /* END: Adapted from diffuzz (Apache License 2.0.) */
        System.out.println("APDU data value=" + Arrays.toString(value));

        /* Specific EC point handling */
        if (fuzzedOp == DifFuzzOps.INS_EC_NEG) {
            if (value.length != 64)
                    return;
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
            byte[] point1 = ecSpec.getG().multiply(new BigInteger(1, Arrays.copyOfRange(value, 0, 32))).getEncoded(false);
            byte[] point2 = ecSpec.getG().multiply(new BigInteger(1, Arrays.copyOfRange(value, 32, 64))).getEncoded(false);
            value = concat(point1, point2);
        } else if (fuzzedOp == DifFuzzOps.INS_EC_EQ) {
            if (value.length != 96)
                return;
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
            byte[] point1 = ecSpec.getG().multiply(new BigInteger(1, Arrays.copyOfRange(value, 0, 32))).getEncoded(false);
            byte[] point2 = ecSpec.getG().multiply(new BigInteger(1, Arrays.copyOfRange(value, 32, 64))).getEncoded(false);
            byte[] point3 = ecSpec.getG().multiply(new BigInteger(1, Arrays.copyOfRange(value, 64, 96))).getEncoded(false);
            value = concat(concat(point1, point2), point3);
        } else if (fuzzedOp == DifFuzzOps.INS_EC_MUL) {
            if (value.length < 32 + 2)
                return;
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
            byte[] point1 = ecSpec.getG().multiply(new BigInteger(1, Arrays.copyOfRange(value, 0, 32))).getEncoded(false);
            p1 = (byte) ((value.length - 32) / 2);
            byte[] bn1 = Arrays.copyOfRange(value, 32, 32 + p1);
            byte[] bn2 = Arrays.copyOfRange(value, 32 + p1, 32 + 2 * p1);
            value = concat(concat(point1, bn1), bn2);
        }

        /* Prepare new simulator for each round */
        CardSimulator simulator = new CardSimulator();
        AID appletAID = AIDUtil.create("DifFuzzApplet".getBytes());
        simulator.installApplet(appletAID, DifFuzzApplet.class);
        simulator.selectApplet(appletAID);

        /* Send data to fuzzing applet */
        System.out.println(Hex.toHexString(simulator.transmitCommand(new CommandAPDU(0, fuzzedOp, p1, p2, value)).getData()));
        System.out.println("Done.");
    }

    private static void setupAPDUParams(int partitions, int valueLength) {
        numberOfPartitions = partitions;
        p1 = (byte) (valueLength / numberOfPartitions);
        p2 = 0;
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
