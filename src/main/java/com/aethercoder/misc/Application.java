package com.aethercoder.misc;

import com.aethercoder.misc.eth.EthService;
import com.aethercoder.misc.qtum.QtumService;
import com.aethercoder.misc.qtum.walletTransaction.UnspentOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
@RefreshScope
@EnableDiscoveryClient
@EnableScheduling
@EnableAutoConfiguration(exclude = {
		org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class,
		org.springframework.boot.actuate.autoconfigure.ManagementWebSecurityAutoConfiguration.class})
public class Application {

//	@Autowired
//	private EthService ethService;
//
//	@Autowired
//	private QtumService qtumService;

	public static Map<String, String> addrSeedMap = new HashMap<>();

//	@Bean
//	@LoadBalanced
//	public RestTemplate restTemplate() {
//		RestTemplate restTemplate = new RestTemplateCust();
//		return restTemplate;
//	}

	@Bean
	public RestTemplate restTemplateOrigin() {
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate;
	}

	public static void main(String[] args) throws Exception{

//		File file = new File("/Users/hepengfei/Downloads/ethAddress.txt");
//		BufferedReader bf = new BufferedReader(new FileReader(file));
//
//		String content = "";
//
//		while ((content = bf.readLine()) != null) {
////			content = bf.readLine();
//			String[] addrSeed = content.split("   ");
//			addrSeedMap.put(addrSeed[0], addrSeed[1]);
//		}

		SpringApplication.run(Application.class, args);

		System.out.println(addrSeedMap.size());


	}

	static LinkedBlockingQueue<String> linkedBlockingQueue = new LinkedBlockingQueue<>(2);
	static String txHash = null;
	static int sendCount = 0;

	private String lftAddress = "0xe0c8087ce1a17bdd5d6c12eb52f8d7eff7791987";

//	@Override
	public void run(String[] args) throws Exception {
//		String fromAddr = "0x71734bc551923DD49e56Bb6F98F2dFe7eF2c159F";
//		String toAddr = "0x54362e7c99148E2BDcc5e5AbAC18937652716CD9";
//		String contractAddr = "0xd7cbe7bfc7d2de0b35b93712f113cae4deff426b";
//		String amount = "0.0001";
////		String gasPrice = "100000";
//		String gasLimit = "6700000";
//		String seed = "lonely kitchen differently nutrient stroke necessary east yield fact intend peel pond";
////		ethService.sendTokenRawTransaction(fromAddr, toAddr, contractAddr, amount, gasLimit, seed);
//		ethService.sendRawTransaction(fromAddr, toAddr, amount, gasLimit, seed);
//		System.out.println("aaaaa");
//		System.exit(0);
////		System.out.println(rawTrans);
	}


//	@Scheduled(fixedRate = 3000)
//	public void sendTx() throws Exception{
//		System.out.println("start send tx");
//		Set<String> toAddrs = addrSeedMap.keySet();
//		String toAddr = null;
//		for(String str : toAddrs) {
//			toAddr = str;
//		}
////		String seed = addrSeedMap.get(toAddr);
//		String seed = "risk tap lovely sustain rapidly murder philosophy transmission bit suitable lake imagination";
//		if (toAddr != null) {
//			String fromAddr = "0x89ee1ab4162c8bf3193eb505993d375e7f4a8349";
////			String contractAddr = "0xd7cbe7bfc7d2de0b35b93712f113cae4deff426b";
//			String amount = "0.00219";
//			String gasLimit = "40000";
////			String txHash = ethService.sendTokenRawTransaction(fromAddr, toAddr, contractAddr, amount, gasLimit, seed);
//			System.out.println("fromAddr: " + fromAddr);
//			System.out.println("toAddr: " + toAddr);
//			Application.txHash = ethService.sendRawTransaction(fromAddr, toAddr, amount, gasLimit, seed);
//			System.out.println("txHash: " + Application.txHash);
//			if (Application.txHash == null) {
//				System.out.println("error sending tx: " + toAddr);
//			} else {
//				sendCount++;
//				addrSeedMap.remove(toAddr);
//
//				RandomAccessFile randomFile = new RandomAccessFile("/Users/hepengfei/Downloads/ethAddressFinished.txt", "rw");
//// 文件长度，字节数
//				long fileLength = randomFile.length();
//// 将写文件指针移到文件尾。
//				randomFile.seek(fileLength);
//				String content = toAddr;
//				randomFile.writeBytes(content+"\r\n");
//				randomFile.close();
//
//				System.out.println(sendCount + " sent. " + addrSeedMap.size() + " remaining.");
//			}
//		} else {
//			System.out.println("no address remaining. exit");
//			System.exit(0);
//		}
//		System.out.println("bbb" + toAddr);
//	}
//
//
////	@Scheduled(fixedRate = 10000)
//	public void sendToken() throws Exception{
//		System.out.println("start send token");
//		Set<String> fromAddrs = addrSeedMap.keySet();
//		String fromAddr = null;
//		for(String str : fromAddrs) {
//			fromAddr = str;
//		}
//		String seed = addrSeedMap.get(fromAddr);
////		String seed = "risk tap lovely sustain rapidly murder philosophy transmission bit suitable lake imagination";
//		if (fromAddr != null) {
//			String toAddr = "0x368550eb895cd118d65c1ac97cbf847591478d42";
////			String contractAddr = "0xd7cbe7bfc7d2de0b35b93712f113cae4deff426b";
//			String amount = ethService.getTokenBalance(lftAddress, fromAddr);
//			if (!amount.equals("0")) {
//				String gasLimit = "60000";
////			String txHash = ethService.sendTokenRawTransaction(fromAddr, toAddr, contractAddr, amount, gasLimit, seed);
//				System.out.println("fromAddr: " + fromAddr + " token: LFT balance: " + amount);
//				System.out.println("toAddr: " + toAddr);
//				Application.txHash = ethService.sendTokenRawTransaction(fromAddr, toAddr, lftAddress, amount, gasLimit, seed);
//				System.out.println("txHash: " + Application.txHash);
//				if (Application.txHash == null) {
//					System.out.println("error sending tx: " + fromAddr);
//				} else {
//					sendCount++;
//					addrSeedMap.remove(fromAddr);
//					System.out.println(sendCount + " sent. " + addrSeedMap.size() + " remaining.");
//				}
//			} else {
//				addrSeedMap.remove(fromAddr);
//				System.out.println(sendCount + " sent. " + addrSeedMap.size() + " remaining.");
//			}
//		} else {
//			System.out.println("no address remaining. exit");
//			System.exit(0);
//		}
//	}
//
//	public static String lastTxHash = null;
////	@Scheduled(fixedRate = 60000)
//	public void sentQtumToken() throws Exception {
//		String seed = "risk tap lovely sustain rapidly murder philosophy transmission bit suitable lake imagination";
//		String fromAddr = "QPZhh95Gbp67KUgLsy3XZzQUPkFUuXHCw3";
//		String toAddr = "QgMXfxJk8q93iPwh1gNe7FJTFV5Dw2pWhc";
//		String contractAddr = "09800417b097c61b9fd26b3ddde4238304a110d5";
//
//		if (lastTxHash != null) {
//			int txConfirmations = qtumService.getTransactionConfirmation(lastTxHash);
//
//			if (txConfirmations == 0) {
//				System.out.println(txHash + " not confirmed yet.");
//				return;
//			}
//		}
//
//		String amount = "0.01";
//		String txid = qtumService.sendToken(seed, fromAddr, toAddr, contractAddr, "0.01");
//		System.out.println("from: " + fromAddr + " to: " + toAddr + " contract: " + contractAddr + " amount: " + amount);
//		System.out.println(txid);
////		System.exit(0);
//	}
}
