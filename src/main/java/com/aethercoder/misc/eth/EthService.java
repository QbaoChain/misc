package com.aethercoder.misc.eth;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by hepengfei on 2018/7/14.
 */
@Service
public class EthService {

    @Autowired
    private Web3jEthUtil web3jEthUtil;

    public Function transfer(String toAddress, BigInteger tokenAmount) {
        List<Type> params = Arrays.asList(new Address(toAddress), new Uint256(tokenAmount));
        List<TypeReference<?>> returnTypes = Collections.singletonList(new TypeReference<Bool>() {
        });
        Function function = new Function("transfer", params, returnTypes);
        return function;
    }

//    public Function approve(String address, BigInteger tokenAmount) {
//        List<Type> params = Arrays.asList(new Address(toAddress), new Uint256(tokenAmount));
//        List<TypeReference<?>> returnTypes = Collections.singletonList(new TypeReference<Bool>() {
//        });
//        Function function = new Function("transfer", params, returnTypes);
//        return function;
//    }

    public String getTokenBalance(String contractAddress, String address) {
        String tokenBalance = web3jEthUtil.callEthTokenBlance(address, contractAddress);
        return tokenBalance;
    }

    public String sendRawTransaction(String fromAddress, String toAddress, String amount, String gasLimit, String seed){
        String rawTx = createRawTransaction(fromAddress, toAddress, amount, gasLimit, "", seed);
        System.out.println(rawTx);
        return web3jEthUtil.sendRawTransaction(rawTx);
    }

    public String sendTokenRawTransaction(String fromAddress, String toAddress, String contractAddress, String amount, String gasLimit, String seed){
        String rawTx = createTokenTransferRawTransaction(fromAddress, toAddress, contractAddress, amount, gasLimit, seed);
        return web3jEthUtil.sendRawTransaction(rawTx);
    }

    public String createTokenTransferRawTransaction(String fromAddress, String toAddress, String contractAddress, String amount, String gasLimit, String seed) {
        BigDecimal amountDecimal = new BigDecimal(amount);
//        BigDecimal decimal = new BigDecimal(10).pow(18);
//        amountDecimal = amountDecimal.multiply(decimal);

        Function function = transfer(toAddress, amountDecimal.toBigInteger());
        String data = FunctionEncoder.encode(function);
        return createRawTransaction(fromAddress, contractAddress, "0", gasLimit, data, seed);
    }

    public String createRawTransaction(String fromAddress, String toAddress, String amount, String gasLimit, String data, String seed) {
//        BigDecimal bigIntegerGweiPrice = Convert.toWei(gasPrice, Convert.Unit.GWEI);
//        BigInteger gasPrice = web3jEthUtil.getGasPrice();
        BigInteger gasPrice = Convert.toWei("32", Convert.Unit.GWEI).toBigInteger();
        BigInteger value = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
//        BigInteger value = new BigInteger(amount);
//        Web3j web3 = Web3jManager.getInstance().getWeb3j();
//        if(web3 == null){
//            web3 = Web3j.build(new HttpService(url));
//        }
//        EthGetTransactionCount ethGetTransactionCount2;
//        BigInteger nonce = new;
//        if (nonceBigInteger.intValue() == 0) {
//        ethGetTransactionCount2 = web3.ethGetTransactionCount(
//                    fromAddress, DefaultBlockParameterName.PENDING).sendAsync().get();
        BigInteger nonce = web3jEthUtil.getTransactionCount(fromAddress);
//            logger.info("Server Nonce = " + nonce);
//            int localNonce = CommonUtility.SharedPreferencesUtility.getInt(mContext, Constants.ETH_NONCE, nonce.intValue());
//            logger.info("Local Nonce = " + localNonce);
            //本地Nonce  大于 服务器返回的Nonce
//            if (localNonce > nonce.intValue()) {
//                nonce = new BigInteger(String.valueOf(localNonce));
//            }
//        }

        //生成Transaction
        RawTransaction tx = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                new BigInteger(gasLimit),
                toAddress,
                value,
                data
        );

        final Credentials keys = getCredentialsBySeed(seed);
        byte[] signed = TransactionEncoder.signMessage(tx, keys);
        String raw = "0x" + Hex.toHexString(signed);
        return raw;
    }

    public Credentials getCredentialsBySeed(String strSeed) {
        DeterministicSeed deterministicSeed;
        try {
            deterministicSeed = new DeterministicSeed(strSeed, null, "", 0);
            byte[] seedBytes = deterministicSeed.getSeedBytes();

            DeterministicKey key = HDKeyDerivation.createMasterPrivateKey(seedBytes);
            key = HDKeyDerivation.deriveChildKey(key, new ChildNumber(44, true));
            key = HDKeyDerivation.deriveChildKey(key, new ChildNumber(60, true));
            key = HDKeyDerivation.deriveChildKey(key, new ChildNumber(0, true));
            key = HDKeyDerivation.deriveChildKey(key, new ChildNumber(0, false));
            key = HDKeyDerivation.deriveChildKey(key, new ChildNumber(0, false));
            ECKeyPair ecKeyPair = ECKeyPair.create(key.getPrivKeyBytes());
            Credentials credentials = Credentials.create(ecKeyPair);
            return credentials;
        } catch (UnreadableWalletException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void main(String... args) {
        System.out.println(Convert.toWei("30", Convert.Unit.GWEI));
        System.out.println(Convert.toWei("5", Convert.Unit.ETHER).toBigInteger());
    }

}
