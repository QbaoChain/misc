package com.aethercoder.misc.qtum.walletTransaction;

import com.aethercoder.misc.qtum.sha3.utils.HexUtils;
import org.spongycastle.util.encoders.Hex;

import java.util.HashMap;

public class TransactionModel {

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

    public class ExecutionResult{
        public int gasUsed;
        public String excepted;
        public String newAddress;
        public String output;
        public int codeDeposit;
        public int gasRefunded;
        public int depositSize;
        public int gasForDeposit;
        public String out;

        public ExecutionResult(HashMap map){
            gasUsed = (int) map.get("gasUsed");
            excepted = (String) map.get("excepted");
            newAddress = (String) map.get("newAddress");
            output = (String) map.get("output");
            codeDeposit = (int) map.get("codeDeposit");
            gasRefunded = (int) map.get("gasRefunded");
            depositSize = (int) map.get("depositSize");
            gasForDeposit = (int) map.get("gasForDeposit");
            out = formatOutput(output);
            System.out.println("out:" + out);
        }


        private String formatOutput(String output){
            int hexLength = Integer.parseInt(output.substring(0, 64), 16);
            int chaLength = Integer.parseInt(output.substring(64, 128), 16);
            System.out.println("chaLength:" + chaLength);
            System.out.println("hexLength:" + hexLength);
            int base = 64 + hexLength * 2;
            String data = output.substring(base, base + chaLength *2);
            return new String(Hex.decode(data.getBytes()));
        }

    }

    public class TransactionReceipt{
        public String stateRoot;
        public int gasUsed;
        public String bloom;

        public TransactionReceipt(HashMap map){
            stateRoot = (String) map.get("stateRoot");
            gasUsed = (int) map.get("gasUsed");
            bloom = (String) map.get("bloom");
        }

    }

}
