package com.aethercoder.misc.qtum;

import com.aethercoder.misc.qtum.walletTransaction.ContractMethodParameter;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;

public class ContractManagementHelper {
    public static String processResponse(List<ContractMethodParameter> contractMethodOutputParameterList, String output) {
        String type = contractMethodOutputParameterList.get(0).getType();
        if (type.contains("int")) {
            if (output.isEmpty()) {
                return "0";
            }
            return new BigInteger(Hex.decode(output)).toString();
        } else if (type.contains("string")) {
            int length = new BigInteger(Hex.decode(output.substring(64, 128))).intValue();
            String stringOutput = new String(Hex.decode(output.substring(128, 128 + length * 2)));
            if (stringOutput.isEmpty()) {
                stringOutput = "N/A";
            }
            return stringOutput;
        }
        return output;
    }
}
