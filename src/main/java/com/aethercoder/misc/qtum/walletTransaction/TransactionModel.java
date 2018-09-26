package com.aethercoder.misc.qtum.walletTransaction;

import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 0000000000000000000000000000000000000000000000000000000000000060  192 -3
 * 00000000000000000000000000000000000000000000000000000000000000c0  384 -6
 * 0000000000000000000000000000000000000000000000000000000000000100  521 -8
 * 0000000000000000000000000000000000000000000000000000000000000028  40chars
 * 3263343036613537663263313261643163336232363261356163633964303333  data domain
 * 3364633830336638000000000000000000000000000000000000000000000000  data domain
 * 0000000000000000000000000000000000000000000000000000000000000008  8chars
 * 3933656235376531000000000000000000000000000000000000000000000000  data domain
 * 0000000000000000000000000000000000000000000000000000000000000008  8chars
 * 6162636465666768000000000000000000000000000000000000000000000000  data domain
 * <p>
 * 2c406a57f2c12ad1c3b262a5acc9d0333dc803f8
 * 93eb57e1
 * abcdefgh
 */

public class TransactionModel {
    // define constransts
    private final static int HEX_INSTRUCTION_SIZE = 64;

    public String address;
    public ExecutionResult executionResult;
    public TransactionReceipt transactionReceipt;
    public String hash;

    public TransactionModel(HashMap map, String param) {
        address = (String) map.get("address");
        executionResult = new ExecutionResult((HashMap) map.get("executionResult"));
        transactionReceipt = new TransactionReceipt((HashMap) map.get("transactionReceipt"));
        hash = param;
    }

    public class ExecutionResult {
        public int gasUsed;
        public String excepted;
        public String newAddress;
        public String output;
        public int codeDeposit;
        public int gasRefunded;
        public int depositSize;
        public int gasForDeposit;
        public List<String> out;

        public ExecutionResult(HashMap map) {
            gasUsed = (int) map.get("gasUsed");
            excepted = (String) map.get("excepted");
            newAddress = (String) map.get("newAddress");
            output = (String) map.get("output");
            codeDeposit = (int) map.get("codeDeposit");
            gasRefunded = (int) map.get("gasRefunded");
            depositSize = (int) map.get("depositSize");
            gasForDeposit = (int) map.get("gasForDeposit");
            out = formatOutput(output);
            System.out.println("out:" + out.size());
        }


        private List<String> formatOutput(String output) {
            List<String> results = new ArrayList<>();
            List<String> instructs = CommonUtility.getStrList(output, 64);
            int returnSize = getReturnNumbers(instructs);
            for (int i = 0; i < returnSize; i++) {
                int offset = Integer.parseInt(instructs.get(i), 16) * 2;//position offset
                int charLength = Integer.parseInt(output.substring(offset, offset + 64), 16);
                int base = offset + 64;
                String data = output.substring(base, base + charLength * 2);
                results.add(new String(Hex.decode(data.getBytes())));
            }
            return results;
        }

    }

    public class TransactionReceipt {
        public String stateRoot;
        public int gasUsed;
        public String bloom;

        public TransactionReceipt(HashMap map) {
            stateRoot = (String) map.get("stateRoot");
            gasUsed = (int) map.get("gasUsed");
            bloom = (String) map.get("bloom");
        }

    }

    /**
     * 获取返回值个数
     *
     * @param stringList
     * @return
     */
    private int getReturnNumbers(List<String> stringList) {
        int num = 0;
        for (String s : stringList) {
            if (s.startsWith("0")) {
                num++;
                continue;
            }
            break;
        }
        return num > 0 ? --num : 0;
    }

}
