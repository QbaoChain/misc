package com.aethercoder.misc.qtum;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.QtumMainNetParams;


public class CurrentNetParams {

    public CurrentNetParams(){}

    public static NetworkParameters getNetParams(){
        return QtumMainNetParams.get();
    }
}

