package com.aethercoder.misc;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.QtumMainNetParams;


public class CurrentNetParams {

    public CurrentNetParams(){}

    public static NetworkParameters getNetParams(){
        return QtumMainNetParams.get();
    }
}

