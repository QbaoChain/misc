package com.aethercoder.misc.qtum;

import com.aethercoder.misc.qtum.sha3.sha.Keccak;
import com.aethercoder.misc.qtum.sha3.sha.Parameters;
import com.aethercoder.misc.qtum.walletTransaction.*;
import com.google.common.collect.ImmutableList;
import org.apache.tomcat.util.buf.HexUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDUtils;
import org.bitcoinj.params.QtumMainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by hepengfei on 2018/7/26.
 */
@Service
public class QtumService {
    private static Logger logger = LoggerFactory.getLogger(QtumRpcController.class);

    private static int GAS_LIMIT = 250000;
    private static int GAS_PRICE = 40;

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

    public Integer getBlockCount() {
        return qtumUtil.getBlockCount();
    }

    public int getTransactionConfirmation(String txHash) {
        Map txMap = qtumUtil.getTransaction(txHash);
        if (txMap.get("confirmations") == null) {
            return 0;
        }
        int confirmations = (Integer) txMap.get("confirmations");
        return confirmations;
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

        List<String> list = new ArrayList<>();
        list.add(fromAddress);
        List<UnspentOutput> unspentOutputs = getUnspentOutputs(list);
        SendRawTransactionResponse sendRawTransactionResponse = sendTx(contractBuilder.createTransactionHash(keyStorage, null, script, gasLimitInt, gasPrice,
                decimalFeePerKb, unspentOutputs));
        return sendRawTransactionResponse;
    }

    private Script createMethodScript(final String abiParams, final String contractAddress, int gasLimitInt, int gasPrice) {
        ContractBuilder contractBuilder = new ContractBuilder();
        Script script = contractBuilder.createMethodScript(abiParams, gasLimitInt, gasPrice, contractAddress);
        return script;
    }

    /*
     * 先将地址importaddress导入节点，再使用listaddressgroupings获取用户余额
     */
    public List<UnspentOutput> getUnspentOutputs(List<String> addressList) {
        long time1 = System.currentTimeMillis();
        // 获取所有地址的UTXO
        List<List<Object>> list = getConfirmUnspentByAddresses(addressList);

        Integer blockCount = getBlockCount();
        List<UnspentOutput> unspentOutputList = new ArrayList<>();
        List<String> uniqueList = new ArrayList<String>();
        String uniqueStr = "";
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).size(); j++) {
                Map map = (Map) list.get(i).get(j);
                uniqueStr = map.get("txid").toString() + "_" + map.get("vout").toString();
                if (uniqueList.contains(uniqueStr)) {
                    continue;
                }

                uniqueList.add(uniqueStr);
                UnspentOutput unspentOutput = new UnspentOutput();
                unspentOutput.setAddress((String) map.get("address"));
                unspentOutput.setSatoshis(qtumUtil.covertDecimalAmount(new BigDecimal(map.get("amount").toString()), 8));
                unspentOutput.setTxid((String) map.get("txid"));
                unspentOutput.setScript((String) map.get("scriptPubKey"));
                unspentOutput.setOutputIndex(new BigDecimal(map.get("vout").toString()));
                unspentOutput.setConfirmations(new BigDecimal(map.get("confirmations").toString()));
                unspentOutput.setHeight(new BigDecimal((blockCount - (Integer) map.get("confirmations"))));
                unspentOutputList.add(unspentOutput);
            }
        }

        Collections.sort(unspentOutputList, (unspentOutput, t1) ->
                unspentOutput.getSatoshis().doubleValue() < t1.getSatoshis().doubleValue() ? 1 : unspentOutput.getSatoshis().doubleValue() > t1.getSatoshis().doubleValue() ? -1 : 0);
        return unspentOutputList;
    }

    private List getConfirmUnspentByAddresses(List<String> addressList) {
        Integer peersNumber = qtumUtil.getPeersNumber();

        List resultList = new Vector<>();
        try {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 200, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<Runnable>(20));
            CountDownLatch count = new CountDownLatch(addressList.size());
            for (int i = 0; i < addressList.size(); i++) {
                List<String> addrList = new ArrayList<String>();
                addrList.add(addressList.get(i));
                executor.execute(new ImportAddressThread2(qtumUtil, addrList, i % peersNumber, null, count));
            }
            count.await();

            if (peersNumber > addressList.size()) {
                peersNumber = addressList.size();
            }
            CountDownLatch count1 = new CountDownLatch(peersNumber);
            ThreadPoolExecutor executor1 = new ThreadPoolExecutor(10, 10, 200, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<Runnable>(20));
            for (int i = 0; i < peersNumber; i++) {
                List addrList = new ArrayList();
                for (int j = 0; j < addressList.size(); j++) {
                    if (i == j % peersNumber) {
                        addrList.add(addressList.get(j));
                    }
                }

                executor1.execute(new ImportAddressThread(qtumUtil, addrList, i % peersNumber, resultList, count1));
            }
            count1.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }

    private SendRawTransactionResponse sendTx(String txHex) {
        String txhash = qtumUtil.sendRawTransaction(txHex);
        SendRawTransactionResponse sendRawTransactionResponse = new SendRawTransactionResponse();
//       sendRawTransactionResponse.setResult();
        sendRawTransactionResponse.setTxid(txhash);
        return sendRawTransactionResponse;
    }

    public List<HashMap> callContract(String contract, List<String> params) {
        return qtumUtil.callContract(contract, params);
    }

    public List<TransactionModel> callContractModel(String contract, List<String> params) {
        return qtumUtil.callContractModel(contract, params);
    }

    public String sendRawTransaction(String rawTransaction) {
        return (String) qtumUtil.sendRawTransaction(rawTransaction);
    }

    public Double estimateFee(Integer nBlocks) {
        return qtumUtil.estimateFee(nBlocks);
    }

    public HashMap getTransaction(String txHash) {
        return qtumUtil.getTransaction(txHash);
    }

    public HashMap getDGPInfo() {
        HashMap hashMap = new HashMap();
        hashMap.put("maxblocksize", 2000000);
        hashMap.put("mingasprice", 40);
        hashMap.put("blockgaslimit", 40000000);
        return hashMap;
    }

    public String getHexAddress(String address) {
        return (String) qtumUtil.getHexAddress(address);
    }

    public Integer getRawMemPool() {
        return qtumUtil.getRawMemPool();
    }

    public Integer getBlockTxs(Integer blockCount) {
        return qtumUtil.getBlockTxs(blockCount);
    }

    public List<String> getOwnerAdderss(String seedCode) throws Exception {
        List<String> addressList = new ArrayList<String>();
        DeterministicSeed seed = new DeterministicSeed(seedCode, null, "",
                DeterministicHierarchy.BIP32_STANDARDISATION_TIME_SECS);
        if (null == seed) {
            System.out.println("Seed is null!");
            return addressList;
        }

        NetworkParameters params = QtumMainNetParams.get();
        Wallet wallet = Wallet.fromSeed(params, seed);

        List<DeterministicKey> deterministicKeyLists = new ArrayList();
        List<ChildNumber> pathParent = new ArrayList<ChildNumber>();
        pathParent.add(new ChildNumber(88, true));
        pathParent.add(new ChildNumber(0, true));
        ImmutableList<ChildNumber> path = HDUtils.append(pathParent, new ChildNumber(0, true));
        if (wallet != null) {
            DeterministicKey k = wallet.getActiveKeyChain().getKeyByPath(path, true);
            deterministicKeyLists.add(k);
            addressList.add(k.toAddress(params).toString());
        }

        return addressList;
    }

    /**
     * 调用合约 或者 发起代币转账
     */
    public void callContract(String seed, String amount, String contractAddress, String fee, String functionId, List<String> addressList) throws Exception{
        Double result = estimateFee(2);
        List<UnspentOutput> unspentOutputs = getUnspentOutputs(addressList);

        ContractBuilder contractBuilder = new ContractBuilder();
        Script script = contractBuilder.createMethodScript(functionId, GAS_LIMIT, GAS_PRICE, contractAddress);
        if (script == null) {
            throw new RuntimeException("Incorrect address!!!");
        }

        KeyStorage keyStorage = KeyStorage.getInstance(seed);
        keyStorage.setAddressCount(10);
        keyStorage.importWallet();


        String txHex = contractBuilder.createTransactionHash(keyStorage, amount, script, new BigDecimal(fee), GAS_LIMIT, GAS_PRICE, new BigDecimal(result), unspentOutputs);

        sendRawTransaction(txHex);
    }

    /**
     * 平台币转账
     */
    public void transfer(String seedString, String toAddress, String amountString, String feeString, String fromQtumAddress) throws Exception{
        sendRawTransaction(getTransferRaw(seedString, toAddress, amountString, feeString, fromQtumAddress));
    }

    private String getTransferRaw(String seedString, String toAddress, String amountString, String feeString, String fromQtumAddress) throws Exception{
        String mTransactionRaw = "";
        Transaction transaction = new Transaction(CurrentNetParams.getNetParams());
        Address addressToSend = null;
        BigDecimal bitcoin = new BigDecimal(100000000);
        try {
            addressToSend = Address.fromBase58(CurrentNetParams.getNetParams(), toAddress);
        } catch (AddressFormatException a) {
            throw new Exception();
        }
        BigDecimal amount = new BigDecimal(amountString);
        BigDecimal mOldFee = new BigDecimal(feeString);
        if (mOldFee.doubleValue() <= 0) {
            mOldFee = new BigDecimal("0.005");
        }
        BigDecimal overFlow = new BigDecimal("0.0");
        transaction.addOutput(Coin.valueOf((amount.multiply(bitcoin).longValue())), addressToSend);

        amount = amount.add(mOldFee);
        List<String> addressList = getOwnerAdderss(seedString);
        List<UnspentOutput> unspentOutputs = getUnspentOutputs(addressList);

        for (UnspentOutput unspent : unspentOutputs) {
            overFlow = overFlow.add(unspent.getSatoshis().divide(bitcoin));
        }

        BigDecimal mDelivery = overFlow.subtract(amount);
        KeyStorage keyStorage = KeyStorage.getInstance(seedString);
        keyStorage.setAddressCount(10);
        keyStorage.importWallet();
        ECKey currentKey = keyStorage.getCurrentKey();
        String qtumAddress = addressList.get(0);

        if (mDelivery.doubleValue() != 0.0) {
            transaction.addOutput(Coin.valueOf((mDelivery.multiply(bitcoin).longValue())), currentKey.toAddress(CurrentNetParams.getNetParams()));
        }

        List<UnspentOutput> mFromUtxo = new ArrayList<>();
        for (UnspentOutput unspentOutput : unspentOutputs) {
            if (unspentOutput.getSatoshis().doubleValue() != 0.0) {
                for (String address : addressList) {
                    if (address.equals(unspentOutput.getAddress())) {
                        Sha256Hash sha256Hash = new Sha256Hash(Utils.parseAsHexOrBase58(unspentOutput.getTxid()));
                        TransactionOutPoint outPoint = new TransactionOutPoint(CurrentNetParams.getNetParams(), unspentOutput.getOutputIndex().longValue(), sha256Hash);
                        Script script = new Script(Utils.parseAsHexOrBase58(unspentOutput.getScript()));


                        DeterministicKey deterministicKey = keyStorage.getCurrentKey();
                        transaction.addSignedInput(outPoint, script, deterministicKey, Transaction.SigHash.ALL, true);
                        mFromUtxo.add(unspentOutput);
                    }
                }
            }
        }

        transaction.getConfidence().setSource(TransactionConfidence.Source.SELF);
        transaction.setPurpose(Transaction.Purpose.USER_PAYMENT);

        byte[] bytes = transaction.unsafeBitcoinSerialize();
        int txSizeInkB = (int) Math.ceil(bytes.length / 1024.);

        mTransactionRaw = HexUtils.toHexString(bytes);
        return mTransactionRaw;
    }

    public void transferByPeerNum(String seedString, String toAddress, String amountString, String feeString, String fromQtumAddress, Integer peerNum) throws Exception{
        sendRawTransaction(getTransferRaw(seedString, toAddress, amountString, feeString, fromQtumAddress), peerNum);
    }

    public String sendRawTransaction(String rawTransaction, Integer peerNum) {
        return (String) qtumUtil.sendRawTransaction(rawTransaction, peerNum);
    }
}
