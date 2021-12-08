package com.example.settings;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

public class WifiUtils {
    private static WifiUtils utils = null;

    public WifiUtils(Context context) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public static WifiUtils getInstance(Context context) {
        if (utils == null) {
            synchronized (WifiUtils.class) {
                if (utils == null) {
                    utils = new WifiUtils(context);
                }
            }
        }
        return utils;
    }

    private WifiManager wifiManager;

    /**
     * wifi是否打开
     *
     * @return
     */
    public boolean isWifiEnable() {
        boolean isEnable = false;
        if (wifiManager != null) {
            if (wifiManager.isWifiEnabled()) {
                isEnable = true;
            }
        }
        return isEnable;
    }

    /**
     * 打开WiFi
     */
    public void openWifi() {
        if (wifiManager != null && !isWifiEnable()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭WiFi
     */
    public void closeWifi() {
        if (wifiManager != null && isWifiEnable()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 扫描Wifi
     * 1.有的ssid为""，也就是ssid !＝ null,获取不到ssid
     * 设置中并没有多余wifi，但这个热点点其它信息可以获取到，说明这个热点是存在的，
     *      是该热点隐藏了，所以获取不到。这也就是手机设置中为什么会有添加网路的按钮了
     * 2  wifi列表中有许多同名的wifi热点，也就是扫描的结果中有重合部分，并不是有多个同名的wifi热点
     *  当附近wifi热点比较少时不会出现此问题，当附近wifi网络比较多时会出现此问题。这就需要将同
     *  名的热点进行删除，但是如果真有两个ssid名相同的wifi，那就可以通过capabilities去区分吧，
     *  如果capabilities也相同就没办法了，系统设置里面也不显示同名的
     *
     */
    public void startScan(){
        //开始扫描
        wifiManager.startScan();
    }

    /**
     * 获取WiFi列表
     *
     * @return
     */
    public List<ScanResult> getWifiList() {
        List<ScanResult> resultList = new ArrayList<>();
        if (wifiManager != null && isWifiEnable()) {
            resultList.addAll(wifiManager.getScanResults());
        }
        return resultList;
    }

    /**
     * 有密码连接
     *
     * @param ssid
     * @param pws
     */
    public void connectWifiPws(String ssid, String pws) {
        wifiManager.disableNetwork(wifiManager.getConnectionInfo().getNetworkId());
        int netId = wifiManager.addNetwork(getWifiConfig(ssid, pws, true));
        wifiManager.enableNetwork(netId, true);
    }

    /**
     * 无密码连接
     *
     * @param ssid
     */
    public void connectWifiNoPws(String ssid) {
        wifiManager.disableNetwork(wifiManager.getConnectionInfo().getNetworkId());
        int netId = wifiManager.addNetwork(getWifiConfig(ssid, "", false));
        wifiManager.enableNetwork(netId, true);
    }

    /**
     * wifi设置
     *
     * @param ssid
     * @param pws
     * @param isHasPws
     */
    private WifiConfiguration getWifiConfig(String ssid, String pws, boolean isHasPws) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";

        WifiConfiguration tempConfig = isExist(ssid);
        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }
        if (isHasPws) {
            config.preSharedKey = "\"" + pws + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        return config;
    }


    private WifiConfiguration isExist(String ssid) {
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\""+ssid+"\"")) {
                return config;
            }
        }
        return null;
    }
}
