package com.geekzjj.oneplustweaks.SubModules;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class OPFeatures {
    public static void init(final XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("android.util.OpFeatures", lpparam.classLoader, "isSupport",int[].class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                int[] features = (int[])param.args[0];
                if(features.length!=1){
                    XposedBridge.log(lpparam.packageName+": OpFeatures, "+features);
                }
                if(features[0]==1){
                    //OP_FEATURE_SKU_GLOBAL
                    param.setResult(false);
                } else if(features[0]==0){
                    //OP_FEATURE_SKU_CHINA
                    param.setResult(true);
                }
            }
        });
    }
}
