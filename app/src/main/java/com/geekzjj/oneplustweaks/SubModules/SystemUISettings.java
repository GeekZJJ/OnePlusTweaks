package com.geekzjj.oneplustweaks.SubModules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.geekzjj.oneplustweaks.Common;

import java.util.HashSet;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class SystemUISettings {

    private static Context appContext;

    public static void init(final ClassLoader classLoader) {
        try{

            Class<?> ContextClass = XposedHelpers.findClass("android.content.ContextWrapper", classLoader);
            findAndHookMethod(ContextClass, "getApplicationContext", new XC_MethodHook() {
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
                        XposedBridge.log("appContext=null" );
                    }
                }
            });
        } catch (Throwable t) {
        }
    }

    private static BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (action.equals(Common.ACTION_SETTINGS_CHANGED)) {
                    if (intent.hasExtra(Common.KEY_VOLUME_PANEL_EXPANDED)) {
                        Boolean volumeExpand = intent.getBooleanExtra(Common.KEY_VOLUME_PANEL_EXPANDED, Common.DEFAULT_VOLUME_PANEL_EXPANDED);
                        VolumePanel.setmVolumePanelExpanded(volumeExpand);
                    }
                    if (intent.hasExtra(Common.KEY_VOLUME_EXPAND_STREAMS)) {
                        HashSet<String> streams = (HashSet<String>) intent.getSerializableExtra(Common.KEY_VOLUME_EXPAND_STREAMS);
                        VolumePanel.setVolumePanelExpandedStreams(streams);
                    }
                    if (intent.hasExtra(Common.KEY_RECENTS_ADD_CLEAR_BTN)) {
                        Boolean add = intent.getBooleanExtra(Common.KEY_RECENTS_ADD_CLEAR_BTN, Common.DEFAULT_RECENTS_ADD_CLEAR_BTN);
                        Recents.setRecentsAddClearBtn(add);
                    }
                    if (intent.hasExtra(Common.KEY_CLEAR_RECENTS_LOCATION)) {
                        int location = intent.getIntExtra(Common.KEY_CLEAR_RECENTS_LOCATION, Common.DEFAULT_CLEAR_RECENTS_LOCATION);
                        Recents.setClearRecentsLocation(location);
                    }
                }
            }catch (Exception e) {
                XposedBridge.log("onReceive failed: "+ e.toString());
            }
        }
    };
}
