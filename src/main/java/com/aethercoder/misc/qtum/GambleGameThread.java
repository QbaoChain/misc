package com.aethercoder.misc.qtum;

import com.aethercoder.basic.utils.BeanUtils;
import com.aethercoder.misc.qtum.walletTransaction.CommonUtility;
import com.aethercoder.misc.qtum.walletTransaction.UnspentOutput;
import com.google.common.collect.ImmutableList;
import jnr.ffi.Struct;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDUtils;
import org.bitcoinj.params.QtumMainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.web3j.abi.datatypes.Int;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;

public class GambleGameThread implements Runnable{
    private static String GAMBLE_WITHDRAW = "3ccfd60b";
    private static String GAMBLE_RESTART = "f4374f06";

    private QtumService qtumService;

    public GambleGameThread(QtumService qtumService){
        this.qtumService = qtumService;
    }

    /**
     * 执行方法
     */
    @Override
    public void run() {

        try{
            String seedCode = "rapid accident album driving blink complain attention participate vehicle hopefully protest crisis";
            List<String> addressList = qtumService.getOwnerAdderss(seedCode);

            while (true){
                String contractAddress = getContractAddress();
                gambleRestart(seedCode, contractAddress, addressList);

                Thread.sleep(1000 * 360);
                System.out.println("GAMBLE_WITHDRAW is now!");
                qtumService.callContract(seedCode, "",contractAddress, "0.3", GAMBLE_WITHDRAW, addressList);
                Thread.sleep(1000 * 240);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void gambleRestart(String seedCode, String contractAddress, List<String> addressList) throws Exception{
        List<String> paramList = new ArrayList<String>();
        paramList.add("f13a1506");
        LinkedHashMap timeMap = (LinkedHashMap) qtumService.callContract(contractAddress, paramList).get(0).get("executionResult");

        Integer deadTime = Integer.parseInt((String)timeMap.get("output"), 16);
        Integer nowTime = Integer.valueOf(String.valueOf(System.currentTimeMillis()).substring(0, 10));
        if(deadTime < nowTime){
            System.out.println("GAMBLE_RESTART is now!");
            qtumService.callContract(seedCode, "", contractAddress, "0.21", GAMBLE_RESTART, addressList);
        }
        else{
            Thread.sleep(30000);
            gambleRestart(seedCode, contractAddress, addressList);
        }
    }

    private String getContractAddress() throws Exception {
        String address = "";

        File file = new File("/Users/wangkai/qbao-misc/contractAddress.conf");
        FileReader reader = new FileReader(file);//定义一个fileReader对象，用来初始化BufferedReader
        BufferedReader bReader = new BufferedReader(reader);//new一个BufferedReader对象，将文件内容读取到缓存
        StringBuilder sb = new StringBuilder();
        String s = "";
        while ((s = bReader.readLine()) != null) {
            sb.append(s);//将读取的字符串添加换行符后累加存放在缓存中
        }
        bReader.close();
        address = sb.toString();
        return address;
    }
}
