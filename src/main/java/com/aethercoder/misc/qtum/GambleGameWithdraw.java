package com.aethercoder.misc.qtum;

import com.aethercoder.misc.qtum.ContractBuilder;
import com.aethercoder.misc.qtum.CurrentNetParams;
import com.aethercoder.misc.qtum.KeyStorage;
import com.aethercoder.misc.qtum.QtumService;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GambleGameWithdraw {
    private static String GAMBLE_WITHDRAW = "3ccfd60b";
    private static String GAMBLE_OWNER_TRANSFER = "1815c2080000000000000000000000003bfa17bfc29e5902e5e02953a3dd875c4d052503";

//    private static String GAMBLE_WITHDRAW = "a9059cbb00000000000000000000000020525ee699f75419bef71ee1237396fbfd88cedd00000000000000000000000000000000000000000000000000000002540be400";
//    private static String GAMBLE_OWNER_TRANSFER = "1815c2080000000000000000000000003bfa17bfc29e5902e5e02953a3dd875c4d052503";

    private QtumService qtumService;
    private String flag;

    public GambleGameWithdraw(QtumService qtumService, String flag){
        this.qtumService = qtumService;
        this.flag = flag;
    }

    /**
     * 执行方法
     */
    public void withdraw() {

        try{
            String seed = "rapid accident album driving blink complain attention participate vehicle hopefully protest crisis";
            List<String> addressList = qtumService.getOwnerAdderss(seed);
            String contractAddress = getContractAddress();

            if (flag.equalsIgnoreCase("withdraw")){
                System.out.println("GAMBLE_WITHDRAW is now!");
                qtumService.callContract(seed, "",contractAddress, "0.3", GAMBLE_WITHDRAW, addressList);
            }
            else if(flag.equalsIgnoreCase("ownerTransfer")){
                System.out.println("GAMBLE_OWNER_TRANSFER is now!");
                qtumService.callContract(seed, "",contractAddress, "0.12", GAMBLE_OWNER_TRANSFER, addressList);
            }
        }
        catch (Exception e){
            e.printStackTrace();
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
