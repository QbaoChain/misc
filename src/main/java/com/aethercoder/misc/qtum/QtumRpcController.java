package com.aethercoder.misc.qtum;

import com.aethercoder.basic.utils.BeanUtils;
import com.aethercoder.misc.qtum.walletTransaction.GambleGameWithdraw;
import io.swagger.annotations.Api;
import org.bitcoinj.core.Address;
import org.bitcoinj.params.QtumMainNetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping( value = "qtumRPC", produces = "application/json")
@Api( tags = "qtumRPC", description = "qtum后台接口" )
public class QtumRpcController {
    private static Logger logger = LoggerFactory.getLogger(QtumRpcController.class);

    @Autowired
    private QtumService qtumService;

    /**
     * 获取当前区块数量
     * @return 区块数
     */
    @RequestMapping( value = "/getBlockCount", method = RequestMethod.GET)
    public Integer getBlockCount() {
        logger.info("getBlockCount");
        return qtumService.getBlockCount();
    }

    /**
     * 获取地址的UTXO
     * @param addresses 地址集合
     * @return 地址的UTXO返回结果
     */
    @RequestMapping( value = "/outputs/unspent", method = RequestMethod.GET)
    public List getUnspent(@RequestParam String addresses) {
        logger.info("outputs/unspent");
        long time0 = System.currentTimeMillis();
        List<String> list = BeanUtils.jsonToList(addresses, String.class);
        List lists = qtumService.getUnspentOutputs(list);
        System.out.println("getUnspent: " + (System.currentTimeMillis() - time0));
        return lists;
    }

    /**
     * 调用合约（不广播）
     * @param param 参数内容
     * @return 合约返回结果
     * @throws Exception 异常
     */
    @RequestMapping( value = "/contract/call", method = RequestMethod.POST, consumes = "application/json" )
    public Map<String, List> callContract(@RequestBody Map param) throws Exception{
        logger.info("contract/call");
        Map<String, List> map = new HashMap<>();
        String contractAddress = (String)param.get("contractAddress");
        map.put("result", qtumService.callContract(contractAddress, (List)param.get("param")));
        return map;
    }

    /**
     * 发起交易请求
     * @param param 参数内容
     * @return 交易hash
     * @throws Exception 异常
     */
    @RequestMapping( value = "/sendRawTransaction", method = RequestMethod.POST, consumes = "application/json" )
    public Map sendRawTransaction(@RequestBody Map param) throws Exception{
        logger.info("sendRawTransaction");
        String result = qtumService.sendRawTransaction((String)param.get("param"));
        Map map = new HashMap();
        map.put("result", result);
        return map;
    }

    /**
     * 获取区块交易费用
     * @param nBlocks 区块数
     * @return 交易费用
     * @throws Exception 异常
     */
    @RequestMapping( value = "/estimateFee", method = RequestMethod.GET)
    public Map estimateFee(@RequestParam Integer nBlocks) throws Exception{
        logger.info("estimateFee");
        Double result = qtumService.estimateFee(nBlocks);
        Map map = new HashMap();
        map.put("result", result);
        return map;
    }

    /**
     * 依据交易hash值获取交易内容
     * @param txHash 交易hash
     * @return 交易费用
     * @throws Exception 异常
     */
    @RequestMapping( value = "/getTransaction", method = RequestMethod.GET)
    public Map getTransaction(@RequestParam String txHash) throws Exception{
        logger.info("getTransaction");
        Map map = new HashMap();
        map.put("result", qtumService.getTransaction(txHash));
        return map;
    }

    /**
     * 获取地址的SHA160值
     * @param addressStr 地址
     * @return 地址的SHA160值
     * @throws Exception 异常
     */
    @RequestMapping( value = "/sha160", method = RequestMethod.GET)
    public Map sha160(String addressStr) throws Exception{
        logger.info("sha160");
        org.bitcoinj.core.Address address = Address.fromBase58(QtumMainNetParams.get(), addressStr);
        String result = Hex.toHexString(address.getHash160());
        Map map = new HashMap();
        map.put("result", result);
        return map;
    }

    /**
     * 获取dgp信息
     */
    @RequestMapping( value = "/blockchain/dgpinfo", method = RequestMethod.GET)
    public Map getDgpInfo() throws Exception{
        logger.info("blockchain/dgpinfo");
        return qtumService.getDGPInfo();
    }

    /**
     * 获取Qtum 16进制的地址信息
     */
    @RequestMapping( value = "/getHexAddress", method = RequestMethod.GET)
    public String getHexAddress(String address) throws Exception{
        logger.info("getHexAddress");
        return qtumService.getHexAddress(address);
    }

    /**
     * 开始QbaoChain博彩时时猜
     */
    @RequestMapping( value = "/qbaochain/start", method = RequestMethod.POST)
    public void qbaoChainStart() throws Exception{
        logger.info("qbaoChainStart");
        GambleGameThread qbaoChainStart = new GambleGameThread(qtumService);
        qbaoChainStart.run();
    }

    /**
     * QbaoChain博彩时时猜开奖
     */
    @RequestMapping( value = "/qbaochain/withdraw", method = RequestMethod.POST)
    public void gambleGameWithdraw(String flag) throws Exception{
        logger.info("qbaoChainWithDraw");
        GambleGameWithdraw gambleGameWithdraw = new GambleGameWithdraw(qtumService,flag);
        gambleGameWithdraw.withdraw();
    }
}
