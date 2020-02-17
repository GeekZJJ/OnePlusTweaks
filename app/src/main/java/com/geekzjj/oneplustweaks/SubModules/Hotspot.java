package com.geekzjj.oneplustweaks.SubModules;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.content.Context;

import com.geekzjj.oneplustweaks.Common;
import com.geekzjj.oneplustweaks.PreferenceUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class Hotspot {

    public static final String TAG = "ModHotspot ";

    public static final int AP_BAND_2GHZ = 0;
    public static final int AP_BAND_5GHZ = 1;

    private static final String WIFI_HOST_IFACE_ADDR = "192.168.43.1";

    private static Object halObj;
    private static Context appContext;

    private static boolean hiddenSSID;
    private static int hotspot2GChannel;
    private static int hotspot5GChannel;

    public static void init(final ClassLoader classLoader) {

        Class<?> wifiConfigurationClass = XposedHelpers.findClass("android.net.wifi.WifiConfiguration",classLoader);
        if(wifiConfigurationClass==null){
            XposedBridge.log(TAG+"wifiConfigurationClass=null!!!");
        }/* else {
            XposedBridge.log(TAG+"wifiConfigurationClass ok!");
        }*/
        Class<?> softApListenerClass = XposedHelpers.findClass("com.android.server.wifi.WifiNative.SoftApListener",classLoader);
        if(softApListenerClass==null){
            XposedBridge.log(TAG+"softApListenerClass=null!!!");
        } /*else {
            XposedBridge.log(TAG+"softApListenerClass ok!");
        }*/

        hiddenSSID = PreferenceUtils.getHotspotHiddenSSID();
        hotspot2GChannel = PreferenceUtils.getHotspot2GChannel();
        hotspot5GChannel = PreferenceUtils.getHotspot5GChannel();
        XposedBridge.log(TAG+"load preference:hotspot2GChannel="+hotspot2GChannel
                +" hotspot5GChannel="+hotspot5GChannel +" hiddenSSID="+hiddenSSID);

        XposedHelpers.findAndHookConstructor("com.android.server.wifi.HostapdHal", classLoader, Context.class,  new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
//                XposedBridge.log(TAG+"hook HostapdHal");
                halObj = param.thisObject;
            }
        });

        XposedHelpers.findAndHookMethod("com.android.server.connectivity.tethering.TetherInterfaceStateMachine", classLoader, "getRandomWifiIPv4Address",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) {
//                        XposedBridge.log(TAG+"hook getRandomWifiIPv4Address");
                        param.setResult(WIFI_HOST_IFACE_ADDR);
                    }
                });

        XposedHelpers.findAndHookMethod("com.android.server.wifi.WifiNative", classLoader, "startSoftAp",
                String.class, wifiConfigurationClass, softApListenerClass ,new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(final MethodHookParam param) {
                        if (param.args[1] != null) {
                            WifiConfiguration config = (WifiConfiguration)param.args[1];
                            int apBand = XposedHelpers.getIntField(config, "apBand");
                            switch (apBand) {
                                case AP_BAND_2GHZ:
                                    if(hotspot2GChannel!=0){
                                        setEnableAcs(false);
                                        XposedBridge.log(TAG+"AP_BAND_2GHZ,setting channel to "+hotspot2GChannel);
                                        XposedHelpers.setIntField(config, "apChannel",hotspot2GChannel);
                                    }else{
                                        setEnableAcs(true);
                                    }
                                    break;
                                case AP_BAND_5GHZ:
                                    if(hotspot5GChannel!=0){
                                        setEnableAcs(false);
                                        XposedBridge.log(TAG+"AP_BAND_5GHZ,setting channel to "+hotspot5GChannel);
                                        XposedHelpers.setIntField(config, "apChannel",hotspot5GChannel);
                                    }else{
                                        setEnableAcs(true);
                                    }
                                    break;
                                default:
                                    XposedBridge.log(TAG+"Error apBand="+apBand+". Exit!");
                                    return;
                            }
                            XposedHelpers.setBooleanField(config,"hiddenSSID",hiddenSSID);
                        }
                    }
                });
        Class<?> ContextClass = XposedHelpers.findClass("android.content.ContextWrapper", classLoader);
        XposedHelpers.findAndHookMethod(ContextClass, "getApplicationContext", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (appContext != null)
                    return;
                appContext = (Context) param.getResult();
                if(appContext !=null){
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(Common.ACTION_SETTINGS_CHANGED);
                    appContext.registerReceiver(mIntentReceiver, filter);
                }else{
                    XposedBridge.log(TAG+"appContext=null" );
                }
            }
        });
    }

    private static BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (action.equals(Common.ACTION_SETTINGS_CHANGED)) {
                    if (intent.hasExtra(Common.KEY_HOTSPOT_HIDDEN_SSID)) {
                        hiddenSSID = intent.getBooleanExtra(Common.KEY_HOTSPOT_HIDDEN_SSID, false);
//                        XposedBridge.log(TAG+"recv hiddenSSID broadcast:"+hiddenSSID);
                    } else if (intent.hasExtra(Common.KEY_HOTSPOT_2G_CHANNEL)) {
                        hotspot2GChannel = intent.getIntExtra(Common.KEY_HOTSPOT_2G_CHANNEL, Common.DEFAULT_HOTSPOT_2G_CHANNEL);
//                        XposedBridge.log(TAG+"recv hotspot_2g_channel broadcast:"+hotspot2GChannel);
                    } else if (intent.hasExtra(Common.KEY_HOTSPOT_5G_CHANNEL)) {
                        hotspot5GChannel = intent.getIntExtra(Common.KEY_HOTSPOT_5G_CHANNEL, Common.DEFAULT_HOTSPOT_5G_CHANNEL);
//                        XposedBridge.log(TAG+"recv hotspot_5g_channel broadcast:"+hotspot5GChannel);
                    }
                }
            }catch (Exception e) {
                XposedBridge.log(TAG+"onReceive failed: "+ e.toString());
            }
        }
    };

    public static void setEnableAcs(boolean acsEnabled){
        if(halObj!=null){
            XposedHelpers.setBooleanField(halObj, "mEnableAcs", acsEnabled);
        }else{
            XposedBridge.log(TAG+"halObj==null,fail to set mEnableAcs");
        }
    }
}
