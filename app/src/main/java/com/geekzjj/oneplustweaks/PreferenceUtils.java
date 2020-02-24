package com.geekzjj.oneplustweaks;

import android.os.Environment;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import de.robv.android.xposed.XSharedPreferences;

public class PreferenceUtils {

    private static XSharedPreferences instance = null;

    public synchronized static XSharedPreferences getInstance(){
        if (instance == null){
            String packageName = Module.class.getPackage().getName();
            String prefFileName = Module.class.getPackage().getName();
            File prefFile = new File(Environment.getDataDirectory(), "user_de/0/" + packageName + "/shared_prefs/" + prefFileName + "_preferences.xml");
            instance = new XSharedPreferences(prefFile);
            instance.makeWorldReadable();
        }else {
            instance.reload();
        }
        return instance;
    }

    public static boolean getHotspotHiddenSSID(){
        return getBooleanPreference(Common.KEY_HOTSPOT_HIDDEN_SSID,Common.DEFAULT_HOTSPOT_HIDDEN_SSID);
    }

    public static int getHotspot2GChannel(){
        return getIntPreference(Common.KEY_HOTSPOT_2G_CHANNEL,Common.DEFAULT_HOTSPOT_2G_CHANNEL);
    }

    public static int getHotspot5GChannel(){
        return getIntPreference(Common.KEY_HOTSPOT_5G_CHANNEL,Common.DEFAULT_HOTSPOT_5G_CHANNEL);
    }

    public static boolean getVolumePanelExpanded(){
        return getBooleanPreference(Common.KEY_VOLUME_PANEL_EXPANDED,Common.DEFAULT_VOLUME_PANEL_EXPANDED);
    }

    public static int getClearRecentsLocation(){
        return getIntPreference(Common.KEY_CLEAR_RECENTS_LOCATION,Common.DEFAULT_CLEAR_RECENTS_LOCATION);
    }

    public static boolean getRecentsAddClearBtn(){
        return getBooleanPreference(Common.KEY_RECENTS_ADD_CLEAR_BTN,Common.DEFAULT_RECENTS_ADD_CLEAR_BTN);
    }

    public static int getVolumePanelLocation(){
        return getIntPreference(Common.KEY_VOLUME_PANEL_LOCATION,Common.DEFAULT_VOLUME_PANEL_LOCATION);
    }

    public static HashSet<String> getVolumePanelItems(){
        return (HashSet<String>) getInstance().getStringSet(Common.KEY_VOLUME_EXPAND_STREAMS,new HashSet<>(Arrays.asList(Common.DEFAULT_VOLUME_EXPAND_STREAMS)));
    }

    private static int getIntPreference(String key,int defaultVal){
        return Integer.parseInt(getInstance().getString(key,Integer.toString(defaultVal)));
    }

    private static boolean getBooleanPreference(String key,boolean defaultVal){
        return getInstance().getBoolean(key,defaultVal);
    }
}
