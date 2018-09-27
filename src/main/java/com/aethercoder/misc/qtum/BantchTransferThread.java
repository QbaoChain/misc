package com.aethercoder.misc.qtum;

public class BantchTransferThread implements Runnable{

    private QtumService qtumService;

    private Integer peerNum;

    public BantchTransferThread(QtumService qtumService,Integer peerNum){
        this.qtumService = qtumService;
        this.peerNum = peerNum;
    }

    /**
     * 执行方法
     */
    @Override
    public void run() {
        try {
            String seedString = "rapid accident album driving blink complain attention participate vehicle hopefully protest crisis";
            long time = System.currentTimeMillis();
            String toAddress = "QPYtQeus6iQDHoncktXUHJp79JdE4ncnLb";
            String fromAddress = "QRVTEo2tKqje1cDc72amhCodeGvHfz1yKE";

            for (int i = 0; i < 10000; i++){
                qtumService.transferByPeerNum(seedString, toAddress, "0.05", "0.04", fromAddress, peerNum);
            }

            System.out.println("TRANSFER time is" + (System.currentTimeMillis() - time));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
