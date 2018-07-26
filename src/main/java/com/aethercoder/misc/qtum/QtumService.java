package com.aethercoder.misc.qtum;

import com.aethercoder.misc.qtum.sha3.sha.Keccak;
import com.aethercoder.misc.qtum.sha3.sha.Parameters;
import com.aethercoder.misc.qtum.walletTransaction.*;
import org.bitcoinj.script.Script;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hepengfei on 2018/7/26.
 */
@Service
public class QtumService {

    @Autowired
    private QtumUtil qtumUtil;

    public String sendToken(String seed, String fromAddress, String toAddress, String contractAddress, String amount) {
        Integer gasLimit = 100000;
        String decimal = getTokenDecigetTokenDecimalmal(contractAddress);
        Integer gasPrice = 40;
        BigDecimal feePerKb = new BigDecimal(qtumUtil.estimateFee(25));
        SendRawTransactionResponse sendRawTransactionResponse = createAbiMethod(seed, fromAddress, toAddress, contractAddress, amount.toString(), gasLimit, decimal, gasPrice, feePerKb);
//        logger.info("用户 " + accountNo + " 提币成功后返回 sendRawTransactionResponse： " + sendRawTransactionResponse.toString());
        String txId = sendRawTransactionResponse.getTxid();
        return txId;
    }

    private String getTokenDecigetTokenDecimalmal(String contractAddress) {
        String decimal;
        String date = "";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        date = formatter.format(new Date());
        Token token = new Token(contractAddress, UUID.randomUUID().toString(), true, date, "Stub!", "name");
        ContractMethod contractMethod = new ContractMethod();
        String[] hashes = getHash("decimals");
        CallSmartContractResponse callSmartContractResponse = callSmartContract(contractAddress, Arrays.asList(hashes));
        ContractMethodParameter contractMethodParameter = new ContractMethodParameter("", "uint256");
        contractMethod.outputParams = new ArrayList<>();
        contractMethod.outputParams.add(contractMethodParameter);
        decimal = ContractManagementHelper.processResponse(contractMethod.outputParams, callSmartContractResponse.getItems().get(0).getOutput());
        return decimal;
    }

    private String[] getHash(String name) {
        Keccak keccak = new Keccak();
        String hashMethod = keccak.getHash(Hex.toHexString((name + "()").getBytes()), Parameters.KECCAK_256).substring(0, 8);
        return new String[]{hashMethod};
    }

    private CallSmartContractResponse callSmartContract(String contractAddress, List<String> hashes) {

        List<HashMap> mapList = qtumUtil.callContract(contractAddress, hashes);
        List<Item> itemList = new ArrayList<>();

        for (HashMap map : mapList) {
            Map executionResult = (Map) map.get("executionResult");
            Item item = new Item();
            item.setExcepted((String) executionResult.get("excepted"));
            item.setGas_used(executionResult.get("gasUsed").toString());
            String output = (String) executionResult.get("output");

//            item.setOutput(new BigInteger(output, 16).toString());
            item.setOutput(output);
            itemList.add(item);
            item.setHash((String) map.get("hash"));
        }

        CallSmartContractResponse callSmartContractResponse = new CallSmartContractResponse();
        callSmartContractResponse.setItems(itemList);
        return callSmartContractResponse;
    }

    private SendRawTransactionResponse createAbiMethod(String seed, String fromAddress, String toAddress, String contractAddress, String amount,
                                                       int gasLimitInt, String bigDecimal, Integer gasPrice, BigDecimal decimalFeePerKb) {
        KeyStorage keyStorage = KeyStorage.getInstance(seed);
        keyStorage.setAddressCount(10);
        keyStorage.importWallet();
       /* String resultAmount = amount;
        if (Integer.valueOf(getTokenDecimal(contractAddress)) != 0) {
            BigDecimal bigDecimal = new BigDecimal(getTokenDecimal(contractAddress));*/
        Integer decimal = Integer.parseInt(bigDecimal, 16);
        double decimalDouble = Math.pow(10, decimal.intValue());
        BigDecimal amountBigDecimal = new BigDecimal(amount);
        BigDecimal amountDecimal = amountBigDecimal.multiply(new BigDecimal(decimalDouble));
        String resultAmount = amountDecimal.toBigInteger().toString();
        ContractBuilder contractBuilder = new ContractBuilder();
        List<ContractMethodParameter> contractMethodParameterList = new ArrayList<>();
        ContractMethodParameter contractMethodParameterAddress = new ContractMethodParameter("_to", "address", toAddress);
        ContractMethodParameter contractMethodParameterAmount = new ContractMethodParameter("_value", "uint256", resultAmount);
        contractMethodParameterList.add(contractMethodParameterAddress);
        contractMethodParameterList.add(contractMethodParameterAmount);
        String abiParams = contractBuilder.createAbiMethodParams("transfer", contractMethodParameterList);

        Script script = createMethodScript(abiParams, contractAddress, gasLimitInt, gasPrice);

        List<UnspentOutput> unspentOutputs = getUnspentOutputs(fromAddress);
        SendRawTransactionResponse sendRawTransactionResponse = sendTx(contractBuilder.createTransactionHash(keyStorage, null, script, gasLimitInt, gasPrice,
                decimalFeePerKb, unspentOutputs));
        return sendRawTransactionResponse;
    }

    private Script createMethodScript(final String abiParams, final String contractAddress, int gasLimitInt, int gasPrice) {
        ContractBuilder contractBuilder = new ContractBuilder();
        Script script = contractBuilder.createMethodScript(abiParams, gasLimitInt, gasPrice, contractAddress);
        return script;
    }

    public List<UnspentOutput> getUnspentOutputs(String address) {
        List<String> addressList = new ArrayList<>();
        addressList.add(address);
        List<Map> list = qtumUtil.getConfirmUnspentByAddresses(addressList);
        List<UnspentOutput> unspentOutputList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map map = (Map) list.get(i);
            UnspentOutput unspentOutput = new UnspentOutput();
            unspentOutput.setAddress((String) map.get("address"));
            unspentOutput.setTxHash((String) map.get("txid"));
            unspentOutput.setVout((Integer) map.get("outputIndex"));
            unspentOutput.setTxoutScriptPubKey((String) map.get("script"));
            unspentOutput.setBlockHeight(Long.valueOf(map.get("height").toString()));
            unspentOutput.setStake((Boolean) map.get("isStake"));
            BigDecimal satoshis = new BigDecimal(map.get("satoshis").toString());
            unspentOutput.setAmount(qtumUtil.convertQtumAmount(satoshis));
            unspentOutput.setConfirmations((Integer) map.get("confirmations"));
            unspentOutputList.add(unspentOutput);
        }

        for (Iterator<UnspentOutput> iterator = unspentOutputList.iterator(); iterator.hasNext(); ) {
            UnspentOutput unspentOutput = iterator.next();
            //remove confirmations<500的数据
            if (!unspentOutput.isOutputAvailableToPay()) {
                iterator.remove();
            }
        }
        Collections.sort(unspentOutputList, (unspentOutput, t1) ->
                unspentOutput.getAmount().doubleValue() < t1.getAmount().doubleValue() ? 1 : unspentOutput.getAmount().doubleValue() > t1.getAmount().doubleValue() ? -1 : 0);

        return unspentOutputList;
    }

    private SendRawTransactionResponse sendTx(String txHex) {
        String txhash = qtumUtil.sendRawTransaction(txHex);
        SendRawTransactionResponse sendRawTransactionResponse = new SendRawTransactionResponse();
//       sendRawTransactionResponse.setResult();
        sendRawTransactionResponse.setTxid(txhash);
        return sendRawTransactionResponse;
    }

}
