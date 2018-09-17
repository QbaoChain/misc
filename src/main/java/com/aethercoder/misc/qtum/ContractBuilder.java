package com.aethercoder.misc.qtum;

import com.aethercoder.basic.exception.AppException;
import com.aethercoder.misc.qtum.sha3.sha.Keccak;
import com.aethercoder.misc.qtum.sha3.sha.Parameters;
import com.aethercoder.misc.qtum.walletTransaction.CommonUtility;
import com.aethercoder.misc.qtum.walletTransaction.ContractMethod;
import com.aethercoder.misc.qtum.walletTransaction.ContractMethodParameter;
import com.aethercoder.misc.qtum.walletTransaction.UnspentOutput;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ContractBuilder {
    private static Logger logger = LoggerFactory.getLogger(ContractBuilder.class);

    private String hashPattern = "0000000000000000000000000000000000000000000000000000000000000000";

    //0000000000000000000000000000000000000000000000000000000000000001 //1 первый параметр
    //0000000000000000000000000000000000000000000000000000000000000080 //128 оффсет первой строки
    //0000000000000000000000000000000000000000000000000000000000000002 //2 третий параметр
    //00000000000000000000000000000000000000000000000000000000000000c0 //192 оффсет второй строки
    //0000000000000000000000000000000000000000000000000000000000000008 //8 длина первой строки
    //6e616d6500000000000000000000000000000000000000000000000000000000 //name первая строка
    //000000000000000000000000000000000000000000000000000000000000000c //12 длина второй строки
    //73796d626f6c0000000000000000000000000000000000000000000000000000 //symbol вторая строка

    private final int radix = 16;
    private final String TYPE_INT = "int";
    private final String TYPE_STRING = "string";
    private final String TYPE_ADDRESS = "address";
    private final String TYPE_BOOL = "bool";

    private final String ARRAY_PARAMETER_CHECK_PATTERN = ".*?\\d+\\[\\d*\\]";
    private final String ARRAY_PARAMETER_TYPE = "(.*?\\d+)\\[(\\d*)\\]";

    final int OP_PUSHDATA_1 = 1;
    final int OP_PUSHDATA_4 = 0x04;
    final int OP_PUSHDATA_8 = 8;
    final int OP_EXEC = 193;
    final int OP_EXEC_ASSIGN = 194;
    final int OP_EXEC_SPEND = 195;

    private Context mContext;

    public ContractBuilder() {

    }

    private boolean parameterIsArray(ContractMethodParameter contractMethodParameter) {
        Pattern p = Pattern.compile(ARRAY_PARAMETER_CHECK_PATTERN);
        Matcher m = p.matcher(contractMethodParameter.getType());
        return m.matches();
    }

    private List<ContractMethodParameter> mContractMethodParameterList;


    public String createAbiMethodParams(final String _methodName, final List<ContractMethodParameter> contractMethodParameterList) {
        String methodName = _methodName;
        String parameters = "";
        String abiParams = "";
        mContractMethodParameterList = contractMethodParameterList;
        if (contractMethodParameterList != null && contractMethodParameterList.size() != 0) {
            for (ContractMethodParameter parameter : contractMethodParameterList) {
                abiParams += convertParameter(parameter);
                parameters = parameters + parameter.getType() + ",";
            }
            methodName = methodName + "(" + parameters.substring(0, parameters.length() - 1) + ")";
        } else {
            methodName = methodName + "()";
        }
        Keccak keccak = new Keccak();
        String hashMethod = keccak.getHash(Hex.toHexString((methodName).getBytes()), Parameters.KECCAK_256).substring(0, 8);
        abiParams = hashMethod + abiParams;
        return abiParams;
    }

    public String createAbiMethodParamsByList(final String _methodName, final List<ContractMethodParameter> contractMethodParameterList) {
        String methodName = _methodName;
        String parameters;
        String abiParams = "";
        Keccak keccak = new Keccak();
        String hashMethod = "";
        if (contractMethodParameterList != null && contractMethodParameterList.size() != 0) {
            //type 相同
            parameters = contractMethodParameterList.get(0).getType();
            methodName = methodName + "(" + parameters + ")";
            hashMethod = keccak.getHash(Hex.toHexString((methodName).getBytes()), Parameters.KECCAK_256).substring(0, 8);
            for (ContractMethodParameter parameter : contractMethodParameterList) {
                abiParams += hashMethod + convertParameter(parameter) + ",";
            }

        } else {
            methodName = methodName + "()";
            hashMethod = keccak.getHash(Hex.toHexString((methodName).getBytes()), Parameters.KECCAK_256).substring(0, 8);
            abiParams = hashMethod;
        }
        if (abiParams.endsWith(",")) {
            abiParams = abiParams.substring(0, abiParams.length() - 1);
        }
        return abiParams;
    }

    //    public String createTransactionHash(KeyStorage keyStorage, Script script, String fee, List<QtumAddress> qtumAddressesList) {
//
//        Transaction transaction = new Transaction(CurrentNetParams.getNetParams());
//        transaction.addOutput(Coin.ZERO, script);
//
//        QtumAddress qtumAddress = null;
//        for (QtumAddress qtumAddressTmp : qtumAddressesList) {
//            if (qtumAddressTmp.getAmount().doubleValue() > Double.valueOf(fee)) {
//                qtumAddress = qtumAddressTmp;
//                break;
//            }
//        }
//
//        if (qtumAddress == null) {
//            throw new RuntimeException("You have insufficient funds for this transaction");
//        }
//
//        BigDecimal bitcoin = new BigDecimal(100000000);
//        ECKey myKey = keyStorage.getCurrentKey();
//        BigDecimal feeDecimal = new BigDecimal(new BigDecimal(fee).multiply(bitcoin).toString());
//        transaction.addOutput(Coin.valueOf((long) (qtumAddress.getAmount().multiply(bitcoin).subtract(feeDecimal).doubleValue())),
//                myKey.toAddress(CurrentNetParams.getNetParams()));
//
//        for (DeterministicKey deterministicKey : keyStorage.getKeyList(10)) {
//            if (Hex.toHexString(deterministicKey.getPubKeyHash()).equals(qtumAddress.getPubkeyHash())) {
//                Sha256Hash sha256Hash = new Sha256Hash(Utils.parseAsHexOrBase58(qtumAddress.getTxHash()));
//                TransactionOutPoint outPoint = new TransactionOutPoint(CurrentNetParams.getNetParams(), qtumAddress.getVout(), sha256Hash);
//                Script script2 = new Script(Utils.parseAsHexOrBase58(qtumAddress.getTxoutScriptPubKey()));
//                transaction.addSignedInput(outPoint, script2, deterministicKey, Transaction.SigHash.ALL, true);
//                break;
//            }
//        }
//
//        transaction.getConfidence().setSource(TransactionConfidence.Source.SELF);
//        transaction.setPurpose(Transaction.Purpose.USER_PAYMENT);
//
//        byte[] bytes = transaction.unsafeBitcoinSerialize();
//        return Hex.toHexString(bytes);
//    }
    public String createQtumTransactionHash(String toAddress, BigDecimal amount, KeyStorage keyStorage, BigDecimal feePerKb, List<UnspentOutput> unspentOutputs) throws RuntimeException {

        return createQtumTransactionHash(toAddress, amount, keyStorage, new BigDecimal("0.003"), feePerKb, unspentOutputs);
    }

    private String createQtumTransactionHash(String toAddress, BigDecimal amount, KeyStorage keyStorage, BigDecimal feeString, BigDecimal feePerKb, List<UnspentOutput> unspentOutputs) throws RuntimeException {
        BigDecimal bitCoin = new BigDecimal(100000000);
        BigDecimal totalPay = new BigDecimal(0);
        Transaction transaction = new Transaction(CurrentNetParams.getNetParams());

        Address addressToSend = Address.fromBase58(CurrentNetParams.getNetParams(), toAddress);
//        } catch (AddressFormatException a) {
//            throw new AppException(ErrorCode.NO_ADDRESS);
//        }
        // 转账 Token
//        BigDecimal amount = withdrawApply.getAmount();
        totalPay = totalPay.add(amount);
        long amountLong = bitCoin.multiply(amount).longValue();
        transaction.addOutput(Coin.valueOf(amountLong), addressToSend);

//        ECKey myKey = keyStorage.getCurrentKey();

        byte[] bytes = transaction.unsafeBitcoinSerialize();
        logger.info("startLength:" + bytes.length);
        BigDecimal totalFee = feeString;
        if (totalPay.compareTo(new BigDecimal(0)) > 0) {
            totalFee = totalFee.add(totalPay);
        }
        BigDecimal totalAmount = new BigDecimal("0");

        List<UnspentOutput> useUnspentOutputs = new ArrayList<>();
        boolean isEnough = false;
        for (UnspentOutput unspentOutput1 : unspentOutputs) {
            totalAmount = totalAmount.add(unspentOutput1.getSatoshis());
            useUnspentOutputs.add(unspentOutput1);
            if(isEnough){
                break;
            }else if (totalAmount.doubleValue() >= totalFee.doubleValue()) {
                isEnough = true;
            }
        }
        if (totalAmount.doubleValue() < totalFee.doubleValue()) {
//            callback.onError(new Throwable(mContext.getString(R.string.insufficient_transaction)));
            // 余额不足
            throw new RuntimeException("insufficient balance");
        }

        BigDecimal delivery = totalAmount.subtract(totalFee);
        long longValue = (long) (delivery.multiply(bitCoin).doubleValue());

        Address addressPayBack = Address.fromBase58(CurrentNetParams.getNetParams(), useUnspentOutputs.get(0).getAddress());
//        } catch (AddressFormatException a) {
//            throw new AppException(ErrorCode.NO_ADDRESS);
//        }
        if (delivery.doubleValue() != 0.0) {
            transaction.addOutput(Coin.valueOf(longValue), addressPayBack);
        }
        for (DeterministicKey deterministicKey : keyStorage.getKeyList(10)) {
            if (deterministicKey.toAddress(CurrentNetParams.getNetParams()).toString().equals(useUnspentOutputs.get(0).getAddress())) {

                for (UnspentOutput unspentOutput : useUnspentOutputs) {
                    if (unspentOutput.getSatoshis().doubleValue() != 0.0) {
                        Sha256Hash sha256Hash = new Sha256Hash(Utils.parseAsHexOrBase58(unspentOutput.getScript()));
                        TransactionOutPoint outPoint = new TransactionOutPoint(CurrentNetParams.getNetParams(), Long.valueOf(unspentOutput.getOutputIndex().toString()), sha256Hash);
                        Script script2 = new Script(Utils.parseAsHexOrBase58(unspentOutput.getScript()));
                        transaction.addSignedInput(outPoint, script2, deterministicKey, Transaction.SigHash.ALL, true);
                    }
                }
                break;
            }
        }

        transaction.getConfidence().setSource(TransactionConfidence.Source.SELF);
        transaction.setPurpose(Transaction.Purpose.USER_PAYMENT);

        bytes = transaction.unsafeBitcoinSerialize();
//        BigDecimal txSizeInkB = new BigDecimal(bytes.length / 1024.);
        logger.info("startLength:" + bytes.length);

        BigDecimal minimumFee = feePerKb.multiply(new BigDecimal(bytes.length).divide(new BigDecimal(1024)));
        minimumFee = minimumFee.setScale(6, BigDecimal.ROUND_CEILING);

        logger.info("minimumFee:" + minimumFee);
//        withdrawApply.setFee(feeString);
//        if (minimumFee.doubleValue() > feeString.doubleValue()) {
//            // 手续费不足
////            throw new AppException(ErrorCode.UNDER_CHARGE);
//            logger.info("手续费不足");
//            createQtumTransactionHash(keyStorage, minimumFee, feePerKb, unspentOutputs, withdrawApply);
//        }

        return Hex.toHexString(bytes);
    }

    public String createTransactionHash(KeyStorage keyStorage, String amount, Script script, int gasLimit, int gasPrice, BigDecimal feePerKb, List<UnspentOutput> unspentOutputs) throws RuntimeException {
        return createTransactionHash(keyStorage, amount, script, new BigDecimal("0.04"), gasLimit, gasPrice, feePerKb, unspentOutputs);
    }

    public String createTransactionHash(KeyStorage keyStorage, String amount, Script script, BigDecimal fee, int gasLimit, int gasPrice, BigDecimal feePerKb, List<UnspentOutput> unspentOutputs) throws RuntimeException {
        double decimalDouble = Math.pow(10, 8);
        BigDecimal bitCoin = new BigDecimal(decimalDouble);
        Transaction transaction = new Transaction(CurrentNetParams.getNetParams());
        //调用合约
        if (StringUtils.isNumeric(amount)) {
            long amountLong = (new BigDecimal(amount)).longValue();
            transaction.addOutput(Coin.valueOf(amountLong), script);
        } else {// 转账 Token
            transaction.addOutput(Coin.ZERO, script);
        }
        BigDecimal gasFee = (new BigDecimal(gasLimit)).multiply(new BigDecimal(gasPrice)).divide(new BigDecimal(100000000));

        ECKey myKey = keyStorage.getCurrentKey();
        byte[] bytes = transaction.unsafeBitcoinSerialize();
        BigDecimal totalFee = fee;
        if (StringUtils.isNotBlank(amount)) {
            totalFee = gasFee.add(new BigDecimal(amount));
        }
        BigDecimal totalAmount = new BigDecimal("0");

        boolean isEnough = false;
        List<UnspentOutput> useUnspentOutputs = new ArrayList<>();
        for (UnspentOutput unspentOutput1 : unspentOutputs) {
            totalAmount = totalAmount.add(unspentOutput1.getSatoshis().divide(new BigDecimal(100000000)));
            useUnspentOutputs.add(unspentOutput1);
            if(isEnough){
                break;
            }else if (totalAmount.doubleValue() >= totalFee.doubleValue()) {
                isEnough = true;
            }
        }

        if (totalAmount.doubleValue() < totalFee.doubleValue()) {
//            callback.onError(new Throwable(mContext.getString(R.string.insufficient_transaction)));
            // 余额不足
            throw new RuntimeException("insufficient balance");
        }

        BigDecimal delivery = totalAmount.subtract(totalFee);
        long longValue = (long) (delivery.multiply(bitCoin).doubleValue());
        Address addressPayBack = Address.fromBase58(CurrentNetParams.getNetParams(), useUnspentOutputs.get(0).getAddress());
        if (delivery.doubleValue() != 0.0) {
            transaction.addOutput(Coin.valueOf(longValue), addressPayBack);
        }
        for (DeterministicKey deterministicKey : keyStorage.getKeyList(10)) {

            logger.info("deterministicKey:" + deterministicKey.toAddress(CurrentNetParams.getNetParams()).toString());
            logger.info("unspentOutput.getAddress:" + addressPayBack);
            if (deterministicKey.toAddress(CurrentNetParams.getNetParams()).toString().equals(useUnspentOutputs.get(0).getAddress())) {
                for (UnspentOutput unspentOutput : useUnspentOutputs) {
                    if (unspentOutput.getSatoshis().doubleValue() != 0.0) {
                        Sha256Hash sha256Hash = new Sha256Hash(Utils.parseAsHexOrBase58(unspentOutput.getTxid()));
                        TransactionOutPoint outPoint = new TransactionOutPoint(CurrentNetParams.getNetParams(), Long.valueOf(unspentOutput.getOutputIndex().toString()), sha256Hash);
                        Script script2 = new Script(Utils.parseAsHexOrBase58(unspentOutput.getScript()));
                        transaction.addSignedInput(outPoint, script2, deterministicKey, Transaction.SigHash.ALL, true);
                    }
                }
                break;
            }
        }

        transaction.getConfidence().setSource(TransactionConfidence.Source.SELF);
        transaction.setPurpose(Transaction.Purpose.USER_PAYMENT);

        bytes = transaction.unsafeBitcoinSerialize();

        logger.info("startLength:" + bytes.length);
        BigDecimal minimumFee = feePerKb.multiply(new BigDecimal(bytes.length).divide(new BigDecimal(1024))).add(gasFee);
        minimumFee = minimumFee.setScale(6, BigDecimal.ROUND_CEILING);


        logger.info("minimumFee:" + minimumFee);
//        if (minimumFee.doubleValue() > fee.doubleValue()) {
//            // 手续费不足
////            throw new AppException(ErrorCode.UNDER_CHARGE);
//            return createTransactionHash(keyStorage, amount, script, minimumFee, gasLimit, gasPrice, feePerKb, unspentOutputs, withdrawApply);
//
//        }
        return Hex.toHexString(bytes);
    }

    long paramsCount;
//
//    public Observable<String> createAbiConstructParams(final List<ContractMethodParameter> contractMethodParameterList, final String uiid, Context context) {
//        mContext = context;
//        paramsCount = contractMethodParameterList.size();
//        currStringOffset = 0;
//
//        return rx.Observable.fromCallable(new Callable<String>() {
//            @Override
//            public String call() throws Exception {
//                String abiParams = "";
//                mContractMethodParameterList = contractMethodParameterList;
//                if (mContractMethodParameterList != null) {
//                    for (ContractMethodParameter parameter : mContractMethodParameterList) {
//                        abiParams += convertParameter(parameter, abiParams);
//                    }
//                }
//                abiParams = getByteCodeByUiid(uiid) + abiParams + appendStringParameters();
//                return abiParams;
//            }
//        });
//    }


    private String convertParameter(ContractMethodParameter parameter) {

        String _value = parameter.getValue();

        if (!parameterIsArray(parameter)) {
            if (parameter.getType().contains(TYPE_INT)) {
                return appendNumericPattern(convertToByteCode(new BigInteger(_value)));
            } else if (parameter.getType().contains(TYPE_STRING)) {
                return getStringOffset(parameter);
            } else if (parameter.getType().contains(TYPE_ADDRESS) && _value.length() == 34) {
                byte[] decode = Base58.decode(_value);
                String toHexString = Hex.toHexString(decode);
                String substring = toHexString.substring(2, 42);
                return appendAddressPattern(substring);
            } else if (parameter.getType().contains(TYPE_ADDRESS)) {
                return getStringOffset(parameter);
            } else if (parameter.getType().contains(TYPE_BOOL)) {
                return appendBoolean(_value);
            }
        } else {
            return getStringOffset(parameter);
        }

        return "";
    }

    private String appendArrayParameter(String type, String param) {
        if (type.contains(TYPE_INT)) {
            return appendNumericPattern(convertToByteCode(new BigInteger(param)));
        } else if (type.contains(TYPE_BOOL)) {
            return appendBoolean(param);
        } else if (type.contains(TYPE_ADDRESS)) {
            return appendAddressPattern(param);
        }
        return "";
    }

    private String appendStringParameters() {
        String stringParams = "";
        for (ContractMethodParameter parameter : mContractMethodParameterList) {
            if (parameter.getType().contains(TYPE_STRING)) {
                stringParams += appendStringPattern(convertToByteCode(parameter.getValue()));
            }
        }
        return stringParams;
    }

    private String appendBoolean(String parameter) {
        return Boolean.valueOf(parameter) ? appendNumericPattern("1") : appendNumericPattern("0");
    }

    private int getStringsChainSize(ContractMethodParameter parameter) {

        if (mContractMethodParameterList == null || mContractMethodParameterList.size() == 0) {
            return 0;
        }

        int index = mContractMethodParameterList.indexOf(parameter);

        if (index == mContractMethodParameterList.size() - 1) {
            return 1;
        }

        int chainSize = 0;

        for (int i = index; i < mContractMethodParameterList.size(); i++) {
            if (mContractMethodParameterList.get(index).getType().contains(TYPE_STRING)) {
                chainSize++;
            }
        }

        return chainSize;
    }

    private String convertToByteCode(long _value) {
        return Long.toString(_value, radix);
    }

    private String convertToByteCode(BigInteger _value) {
        return _value.toString(radix);
    }

    private static String convertToByteCode(String _value) {
        char[] chars = _value.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char aChar : chars) {
            hex.append(Integer.toHexString((int) aChar));
        }
        return hex.toString();
    }

    private String getStringOffset(String data) {
        return appendNumericPattern(convertToByteCode(data.length()));
    }

    long currStringOffset = 0;

    private String getStringOffset(ContractMethodParameter parameter) {
        long currOffset = ((paramsCount + currStringOffset) * 32);
        currStringOffset = getStringHash(parameter.getValue()).length() / hashPattern.length() + 1/*string length section*/;
        return appendNumericPattern(convertToByteCode(currOffset));
    }

    private String getStringLength(String _value) {
        return appendNumericPattern(convertToByteCode(_value.length()));
    }

    private String getStringHash(String _value) {
        if (_value.length() <= hashPattern.length()) {
            return formNotFullString(_value);
        } else {
            int ost = _value.length() % hashPattern.length();
            return _value + hashPattern.substring(0, hashPattern.length() - ost);
        }
    }

    private String appendStringPattern(String _value) {

        String fullParameter = "";
        fullParameter += getStringLength(_value);

        if (_value.length() <= hashPattern.length()) {
            fullParameter += formNotFullString(_value);
        } else {
            int ost = _value.length() % hashPattern.length();
            fullParameter += _value + hashPattern.substring(0, hashPattern.length() - ost);
        }

        return fullParameter;
    }

    private String appendAddressPattern(String _value) {
        return hashPattern.substring(_value.length()) + _value;
    }

    private String formNotFullString(String _value) {
        return _value + hashPattern.substring(_value.length());
    }

    private String appendNumericPattern(String _value) {
        return hashPattern.substring(0, hashPattern.length() - _value.length()) + _value;
    }
//
//    private String getByteCodeByUiid(String uiid) {
//        return FileStorageManager.getInstance().readByteCodeContract(mContext, uiid);
//    }

    public Script createMethodScript(String abiParams, int gasLimitInt, int gasPriceInt, String _contractAddress) throws RuntimeException {

        byte[] version = Hex.decode("04000000");

        byte[] arrayGasLimit = org.spongycastle.util.Arrays.reverse((new BigInteger(String.valueOf(gasLimitInt))).toByteArray());
        byte[] gasLimit = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        System.arraycopy(arrayGasLimit, 0, gasLimit, 0, arrayGasLimit.length);

        byte[] arrayGasPrice = org.spongycastle.util.Arrays.reverse((new BigInteger(String.valueOf(gasPriceInt))).toByteArray());
        byte[] gasPrice = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        System.arraycopy(arrayGasPrice, 0, gasPrice, 0, arrayGasPrice.length);

        byte[] data = Hex.decode(abiParams);
        byte[] contractAddress = Hex.decode(_contractAddress);
        byte[] program;

        ScriptChunk versionChunk = new ScriptChunk(OP_PUSHDATA_4, version);
        ScriptChunk gasLimitChunk = new ScriptChunk(OP_PUSHDATA_8, gasLimit);
        ScriptChunk gasPriceChunk = new ScriptChunk(OP_PUSHDATA_8, gasPrice);
        ScriptChunk dataChunk = new ScriptChunk(ScriptOpCodes.OP_PUSHDATA2, data);
        ScriptChunk contactAddressChunk = new ScriptChunk(ScriptOpCodes.OP_PUSHDATA2, contractAddress);
        ScriptChunk opExecChunk = new ScriptChunk(OP_EXEC_ASSIGN, null);
        List<ScriptChunk> chunkList = new ArrayList<>();
        chunkList.add(versionChunk);
        chunkList.add(gasLimitChunk);
        chunkList.add(gasPriceChunk);
        chunkList.add(dataChunk);
        chunkList.add(contactAddressChunk);
        chunkList.add(opExecChunk);

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            for (ScriptChunk chunk : chunkList) {
                chunk.write(bos);
            }
            program = bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Script(program);
    }


    private static String FUNCTION_TYPE = "function";
    private static String TYPE = "type";


    private static List<ContractMethod> initStandardInterface() {

        List<ContractMethod> standardInterface = new ArrayList<>();

        List<ContractMethodParameter> totalSupplyOutputParams = new ArrayList<>();
        totalSupplyOutputParams.add(new ContractMethodParameter("totalSupply", "uint"));
        ContractMethod totalSupply = new ContractMethod(true, "function", null, "totalSupply", totalSupplyOutputParams);

        List<ContractMethodParameter> balanceOfInputParams = new ArrayList<>();
        balanceOfInputParams.add(new ContractMethodParameter("_owner", "address"));
        List<ContractMethodParameter> balanceOfOutputParams = new ArrayList<>();
        balanceOfOutputParams.add(new ContractMethodParameter("balance", "uint"));
        ContractMethod balanceOfSupply = new ContractMethod(true, "function", balanceOfInputParams, "balanceOf", balanceOfOutputParams);

        List<ContractMethodParameter> transferInputParams = new ArrayList<>();
        transferInputParams.add(new ContractMethodParameter("_to", "address"));
        transferInputParams.add(new ContractMethodParameter("_value", "uint"));
        List<ContractMethodParameter> transferOutputParams = new ArrayList<>();
        transferOutputParams.add(new ContractMethodParameter("success", "bool"));
        ContractMethod transfer = new ContractMethod(false, "function", transferInputParams, "transfer", transferOutputParams);

        List<ContractMethodParameter> transferFromInputParams = new ArrayList<>();
        transferFromInputParams.add(new ContractMethodParameter("_from", "address"));
        transferFromInputParams.add(new ContractMethodParameter("_to", "address"));
        transferFromInputParams.add(new ContractMethodParameter("_value", "uint"));
        List<ContractMethodParameter> transferFromOutputParams = new ArrayList<>();
        transferFromOutputParams.add(new ContractMethodParameter("success", "bool"));
        ContractMethod transferFrom = new ContractMethod(false, "function", transferFromInputParams, "transferFrom", transferFromOutputParams);

        List<ContractMethodParameter> approveInputParams = new ArrayList<>();
        approveInputParams.add(new ContractMethodParameter("_spender", "address"));
        approveInputParams.add(new ContractMethodParameter("_value", "uint"));
        List<ContractMethodParameter> approveOutputParams = new ArrayList<>();
        approveOutputParams.add(new ContractMethodParameter("success", "bool"));
        ContractMethod approve = new ContractMethod(false, "function", approveInputParams, "approve", approveOutputParams);

        List<ContractMethodParameter> allowanceInputParams = new ArrayList<>();
        allowanceInputParams.add(new ContractMethodParameter("_owner", "address"));
        allowanceInputParams.add(new ContractMethodParameter("_spender", "address"));
        List<ContractMethodParameter> allowanceOutputParams = new ArrayList<>();
        allowanceOutputParams.add(new ContractMethodParameter("remaining", "uint"));
        ContractMethod allowance = new ContractMethod(true, "function", allowanceInputParams, "allowance", allowanceOutputParams);

        standardInterface.add(totalSupply);
        standardInterface.add(balanceOfSupply);
        standardInterface.add(transfer);
        standardInterface.add(transferFrom);
        standardInterface.add(approve);
        standardInterface.add(allowance);

        return standardInterface;
    }

    public static String generateContractAddress(String txHash) {
        char[] ca = txHash.toCharArray();
        StringBuilder sb = new StringBuilder(txHash.length());
        for (int i = 0; i < txHash.length(); i += 2) {
            sb.insert(0, ca, i, 2);
        }

        String reverse_tx_hash = sb.toString();
        reverse_tx_hash = reverse_tx_hash.concat("00000000");


        byte[] test5 = Hex.decode(reverse_tx_hash);

        SHA256Digest sha256Digest = new SHA256Digest();
        sha256Digest.update(test5, 0, test5.length);
        byte[] out = new byte[sha256Digest.getDigestSize()];
        sha256Digest.doFinal(out, 0);

        RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();
        ripemd160Digest.update(out, 0, out.length);
        byte[] out2 = new byte[ripemd160Digest.getDigestSize()];
        ripemd160Digest.doFinal(out2, 0);

        return Hex.toHexString(out2);
    }
}
