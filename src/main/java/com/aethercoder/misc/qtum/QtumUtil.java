package com.aethercoder.misc.qtum;

import com.aethercoder.basic.utils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by hepengfei on 22/11/2017.
 */
@Component
public class QtumUtil {

    @Value( "${qtum.url}" )
    private String url;

    @Value( "${qtum.url1}" )
    private String url1;

    @Value( "${qtum.url2}" )
    private String url2;

    @Value( "${qtum.httpUrl}" )
    private String httpUrl;

    @Value( "${qtum.username}" )
    private String username;

    @Value( "${qtum.password}" )
    private String password;

    @Autowired
    private RestTemplate restTemplateOrigin;

    public Integer getBlockCount(){
        List<Map<String, Object>> paramList = new ArrayList<>();
        Integer blockCount = (Integer) callQtumService("getblockcount",paramList);
        return blockCount;
    }

    public List getUnspentByAddresses(List<String> addresses) {
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("addresses", addresses);

        List<Map<String, Object>> paramList = new ArrayList<>();
        paramList.add(addressMap);

        List list = (List) callQtumService("getaddressutxos", paramList);

        List mempool = getMempool(addresses);
        List<String> prevtxidList = new ArrayList<>();
        if (mempool != null && !mempool.isEmpty()) {
            for (Object obj : mempool) {
                try {
                    HashMap map = (HashMap) obj;
                    if (map.get("prevtxid") == null) {
                        Integer index = (Integer) map.get("index");
                        HashMap transaction = this.getTransaction((String) map.get("txid"));
                        if (transaction != null) {
                            List voutList = (List) transaction.get("vout");
                            Map vout = (Map) voutList.get(index);
                            Map scriptPubKey = (Map) vout.get("scriptPubKey");
                            map.put("script", scriptPubKey.get("hex"));
                            map.put("outputIndex", index);
                            list.add(map);
                        }
                    } else {
                        prevtxidList.add((String) map.get("prevtxid"));
                    }
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
//                    logger.error("getUnspentByAddresses Error:" + ex.getMessage() + ex.getStackTrace());
                }
            }
        }

        HashMap info = (HashMap) callQtumService("getinfo", null);
        Integer blocks = (Integer) info.get("blocks");

        for (Iterator it = list.iterator(); it.hasNext(); ) {
            HashMap map = (HashMap) it.next();
            String txid = (String) map.get("txid");
            if (prevtxidList.contains(txid)) {
                it.remove();
                continue;
            }

            if (map.get("height") != null && (Integer) map.get("height") >= 0) {
                map.put("confirmations", blocks - (Integer) map.get("height") + 1);
            } else {
                map.put("confirmations", 0);
                map.put("height", -1);
            }
        }

        return list;
    }

    public List getConfirmUnspentByAddresses(List<String> addressList, Integer peersNumber) {
        List<Object> paramList = new ArrayList<>();
        paramList.add(0);
        paramList.add(100000000);
        paramList.add(addressList);

        long time1 = System.currentTimeMillis();

        List list = (List) callQtumService("listunspent", paramList, peersNumber);

        System.out.println("listunspent time: " + (System.currentTimeMillis() - time1));

        return list;
    }

    public HashMap getInfo() {
        return (HashMap) callQtumService("getinfo", null);
    }

    public List getUnspentByAddresses(String addressesJson) {
        List<String> addresses = BeanUtils.jsonToList(addressesJson, String.class);
        return this.getUnspentByAddresses(addresses);
    }

    public HashMap getUtxo(String utxoId, Integer outputIndex) {
        List<Object> list = new ArrayList<>();
        list.add(utxoId);
        list.add(outputIndex);

        return (HashMap) callQtumService("gettxout", list);
    }

    public List<HashMap> callContract(String contract, List<String> params) {
        List<HashMap> result = new ArrayList<>();
        for (String param : params) {
            List<Object> list = new ArrayList<>();
            list.add(contract);
            list.add(param);
            HashMap map = (HashMap) callQtumService("callcontract", list);
            map.put("hash", param);
            result.add(map);
        }

        return result;
    }

    public Double estimateFee(Integer nBlocks) {
        List<Object> list = new ArrayList<>();
        list.add(nBlocks);

        Double d = new Double(callQtumService("estimatefee", list).toString());
        if (d < 0.004) {
            d = 0.0045;
        }
        return d;
    }

    public String sendRawTransaction(String rawTransaction) {
        List<Object> list = new ArrayList<>();
        list.add(rawTransaction);

        return (String) callQtumService("sendrawtransaction", list);
    }

    public HashMap getTransaction(String txhash) {
        List txidParamList = new ArrayList();
        txidParamList.add(txhash);
        txidParamList.add(true);
        HashMap rawTraction = (HashMap) callQtumService("getrawtransaction", txidParamList);
        return rawTraction;
    }

    public List getEventLog(Long blockStart, Long blockEnd, List<String> contractAddrList) {
        Map<String, List> addressMap = new HashMap<>();
        addressMap.put("addresses", contractAddrList);

        List<Object> paramsList = new ArrayList<>();
        paramsList.add(blockStart);
        paramsList.add(blockEnd);
        paramsList.add(addressMap);
        List result = (List) this.callQtumService("searchlogs", paramsList);
        return result;
    }

    public String getHexAddress(String address){
        List txidParamList = new ArrayList();
        txidParamList.add(address);
        Object object = (String)callQtumService("gethexaddress", txidParamList);
        return (String)object;
    }

    public HashMap getDGPInfo() {
        return (HashMap) callQtumService("getdgpinfo", null);

    }

    public List<HashMap> getMempool(List<String> addresses) {
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("addresses", addresses);

        List<Map<String, Object>> paramList = new ArrayList<>();
        paramList.add(addressMap);

        List list = (List) callQtumService("getaddressmempool", paramList);
        return list;
    }

    public Object callQtumService(String method, Object params) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("method", method);
        if (params != null) {
            bodyMap.put("params", params);
        }

        HttpHeaders headers = new HttpHeaders();
//        NetworkUtil.addAuth(username, password, header);

        String plainCreds = username + ":" + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        String encodedText = Base64.getEncoder().encodeToString(plainCredsBytes);
        headers.set("Authorization", "Basic " + encodedText);

        HttpEntity<String> request = new HttpEntity<>(BeanUtils.objectToJson(bodyMap), headers);

        HashMap result = null;
        try {
            result = restTemplateOrigin.postForObject(url, request, HashMap.class);
        } catch (HttpServerErrorException e) {
            throw new RuntimeException(e.getResponseBodyAsString());
        }
        return result.get("result");
    }

    public Object callQtumService(String method, Object params, Integer flag) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("method", method);
        if (params != null) {
            bodyMap.put("params", params);
        }

        HttpHeaders headers = new HttpHeaders();
        String plainCreds = username + ":" + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        String encodedText = Base64.getEncoder().encodeToString(plainCredsBytes);
        headers.set("Authorization", "Basic " + encodedText);

        HttpEntity<String> request = new HttpEntity<>(BeanUtils.objectToJson(bodyMap), headers);
        HashMap result = null;
        try {
            if (flag == 2){
                result = restTemplateOrigin.postForObject(url2, request, HashMap.class);
            }
            if (flag == 1){
                result = restTemplateOrigin.postForObject(url1, request, HashMap.class);
            }
            else{
                result = restTemplateOrigin.postForObject(url, request, HashMap.class);
            }
        } catch (HttpServerErrorException e) {
            throw new RuntimeException(e.getResponseBodyAsString());
        }
        return result.get("result");
    }

    public BigDecimal convertQtumAmount(BigDecimal satoshis) {
        return satoshis.divide(new BigDecimal(10).pow(8));
    }

    public BigDecimal covertTokenAmount(BigDecimal amount, Integer decimal) {
        return amount.divide(new BigDecimal(10).pow(decimal), 6, BigDecimal.ROUND_DOWN);
    }

    public BigDecimal covertDecimalAmount(BigDecimal amount, Integer decimal) {
        return amount.multiply(new BigDecimal(10).pow(decimal));
    }

    public Integer getPeersNumber(){
        if (url != url2 && url != url1)
            return 3;
        else if (url1 == url2 && url1 == url2){
            return 1;
        }
        else{
            return 2;
        }
    }
}
