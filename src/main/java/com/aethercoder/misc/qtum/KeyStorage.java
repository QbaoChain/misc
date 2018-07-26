package com.aethercoder.misc.qtum;

import com.google.common.collect.ImmutableList;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDUtils;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class KeyStorage implements Serializable {

    private static KeyStorage sKeyStorage;
    private List<DeterministicKey> mDeterministicKeyList;
    private List<String> mAddressesList;
    private Wallet sWallet = null;
    private int sCurrentKeyPosition = 0;
    private File mFile;
    private String seedString;
    private int addressCount;
//    public static final int ADDRESSES_COUNT = 1000;

    public static void setUpKeyStorage(KeyStorage kStorage) {
        sKeyStorage = kStorage;
    }

    public static KeyStorage getInstance(String seedString) {
        if (sKeyStorage == null || !sKeyStorage.getSeedString().equals(seedString)) {
            sKeyStorage = new KeyStorage(seedString);
        }
        return sKeyStorage;
    }

    private KeyStorage(String seedString) {
        this.seedString = seedString;
    }

    public void setWallet(Wallet wallet) {
        this.sWallet = wallet;
    }

    public void clearKeyStorage() {
        sKeyStorage = null;
    }


    public void importWallet() {
        if (sWallet != null) {
            return;
        }
        String passphrase = "";
        DeterministicSeed seed = null;
        try {
            seed = new DeterministicSeed(seedString, null, passphrase, DeterministicHierarchy.BIP32_STANDARDISATION_TIME_SECS);

        } catch (UnreadableWalletException e) {
            e.printStackTrace();
        }
        if (seed != null) {
            sWallet = Wallet.fromSeed(CurrentNetParams.getNetParams(), seed);
        }
    }

    public String getAddress(Integer index) {
        List<ChildNumber> pathParent = new ArrayList<>();
        pathParent.add(new ChildNumber(88, true));
        pathParent.add(new ChildNumber(0, true));
        ImmutableList<ChildNumber> path = HDUtils.append(pathParent, new ChildNumber(index, true));
        DeterministicKey k = sWallet.getActiveKeyChain().getKeyByPath(path, true);
        return k.toAddress(CurrentNetParams.getNetParams()).toString();
    }

    public List<DeterministicKey> getKeyList(int numberOfKeys) {
        if (mDeterministicKeyList == null || mDeterministicKeyList.size() == 0) {
            mDeterministicKeyList = new ArrayList<>(numberOfKeys);
            mAddressesList = new ArrayList<>();
            List<ChildNumber> pathParent = new ArrayList<>();
            pathParent.add(new ChildNumber(88, true));
            pathParent.add(new ChildNumber(0, true));
            mAddressesList.clear();
            for (int i = 0; i < numberOfKeys; i++) {
                ImmutableList<ChildNumber> path = HDUtils.append(pathParent, new ChildNumber(i, true));
                if (sWallet != null) {
                    DeterministicKey k = sWallet.getActiveKeyChain().getKeyByPath(path, true);
                    mDeterministicKeyList.add(k);
                    mAddressesList.add(k.toAddress(CurrentNetParams.getNetParams()).toString());
                }
            }
        }
        return mDeterministicKeyList;
    }

    public String getCurrentAddress() {
        if (getKeyList(addressCount).size() > 0) {
            return getKeyList(addressCount).get(sCurrentKeyPosition).toAddress(CurrentNetParams.getNetParams()).toString();
        }
        return null;
    }

    public List<String> getAddresses() {
        return mAddressesList;
    }

    public DeterministicKey getCurrentKey() {
        return getKeyList(addressCount).get(sCurrentKeyPosition);
    }

    public int getCurrentKeyPosition() {
        return sCurrentKeyPosition;
    }

    public String getSeedString() {
        return seedString;
    }

    public int getAddressCount() {
        return addressCount;
    }

    public void setAddressCount(int addressCount) {
        this.addressCount = addressCount;
    }

    public static void main(String... args) {
        String seed = "risk tap lovely sustain rapidly murder philosophy transmission bit suitable lake imagination";
        KeyStorage keyStorage = new KeyStorage(seed);
        keyStorage.importWallet();
        for(int i = 0; i < 10; i++) {
            System.out.println(keyStorage.getAddress(i));
        }
    }
}