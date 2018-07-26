package com.aethercoder.misc.eth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jiawei.tao
 * @Description:
 * @Date: Created in 2018/5/16
 * @modified By:
 */
@Component
public class Web3jEthUtil {

    @Value( "${web3j.url}" )
    private String url;

//    @Autowired
    private RestTemplate restTemplateOrigin;


    /**
     * 获取币币交易的比率
     * 1个ETH交易为EOS的交易比率:
     *
     * @param src    src address(ETH token地址): 0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
     * @param dest   dest address(EOS token地址): 0x86Fa049857E0209aa7D9e616F7eb3b3B78ECfdb0
     * @param srcQty 1
     * @return
     */
    private Function getExpectedRate(String src, String dest, BigInteger srcQty) {

        ArrayList param1 = new ArrayList();
        param1.add(new Address(src));
        param1.add(new Address(dest));
        param1.add(new Uint256(srcQty));

        ArrayList param2 = new ArrayList();

        param2.add(new TypeReference<Uint256>() {
        });
        param2.add(new TypeReference<Uint256>() {
        });
        Function function = new Function("getExpectedRate", param1, param2);
        return function;
    }

    public HashMap callEthGetTransactionReceipt(String txHash) {
        StringBuilder sb = new StringBuilder();
        sb.append("module=proxy&action=eth_getTransactionReceipt");
        sb.append("&txHash=");
        sb.append(txHash);
        sb.append("&apikey=pcklQV2c6OtnxNKVgWl9");

        return (HashMap)callEtherScanService(sb.toString());
    }

    /**
     * @return example:{"status":"1","message":"OK","result":"3105434"}
     */
    public HashMap callEthBlance(String address) {
        StringBuilder sb = new StringBuilder();
        sb.append("module=proxy&action=balance");
        sb.append("&address=");
        sb.append(address);
        sb.append("&tag=latest&apikey=pcklQV2c6OtnxNKVgWl9");

        // return example
        return (HashMap)callEtherScanService(sb.toString());
    }

    /**
     * @return example:{"status":"1","message":"OK","result":"3105434"}
     */
    public String callEthTokenBlance(String address,String contractAddress) {
        StringBuilder sb = new StringBuilder();
        sb.append("module=account&action=tokenbalance");
        sb.append("&contractaddress=");
        sb.append(contractAddress);
        sb.append("&address=");
        sb.append(address);
        sb.append("&tag=latest&apikey=pcklQV2c6OtnxNKVgWl9");

        // return example
        return (String)callEtherScanService(sb.toString());
    }

    public BigInteger getTransactionCount(String address) {
        StringBuilder sb = new StringBuilder();
        sb.append("module=proxy&action=eth_getTransactionCount");
        sb.append("&address=");
        sb.append(address);
        sb.append("&tag=latest&apikey=pcklQV2c6OtnxNKVgWl9");

        // return example
        String countStrHex = (String)callEtherScanService(sb.toString());
        String countStr = countStrHex.substring(2);
        BigInteger count = new BigInteger(countStr, 16);
        return count;
    }

    public BigInteger getGasPrice() {
        StringBuilder sb = new StringBuilder();
        sb.append("module=proxy&action=eth_gasPrice");
        sb.append("&apikey=pcklQV2c6OtnxNKVgWl9");

        // return example
        String countStrHex = (String)callEtherScanService(sb.toString());
        String countStr = countStrHex.substring(2);
        BigInteger count = new BigInteger(countStr, 16);
        return count;
    }

    public String sendRawTransaction(String rawTx) {
        StringBuilder sb = new StringBuilder();
        sb.append("module=proxy&action=eth_sendRawTransaction");
        sb.append("&hex=");
        sb.append(rawTx);
        sb.append("&apikey=pcklQV2c6OtnxNKVgWl9");

        // return example
        return (String)callEtherScanService(sb.toString());
    }

    private Object callEtherScanService(String params) {
        Map<String, String> header = new HashMap<>();
        HashMap result = restTemplateOrigin.getForObject(url + "?" + params, HashMap.class);
        return result.get("result");
    }
}
