package com.geekzjj.oneplustweaks;


import com.geekzjj.oneplustweaks.SubModules.FixOPScreenShotCrash;
import com.geekzjj.oneplustweaks.SubModules.OPFeatures;
import com.geekzjj.oneplustweaks.SubModules.Recents;
import com.geekzjj.oneplustweaks.SubModules.SystemUISettings;
import com.geekzjj.oneplustweaks.SubModules.VolumePanel;
import com.geekzjj.oneplustweaks.SubModules.Hotspot;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Module implements IXposedHookLoadPackage {

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals(Common.PACKAGE_ONEPLUS_SCREENSHOT)) {
            FixOPScreenShotCrash.init(lpparam.classLoader);
        } else if (lpparam.packageName.equals(Common.PACKAGE_ANDROID)) {
            Hotspot.init(lpparam.classLoader);
        } else if (lpparam.packageName.equals(Common.PACKAGE_SYSTEMUI)) {
            SystemUISettings.init(lpparam.classLoader);
            Recents.init(lpparam.classLoader);
            VolumePanel.init(lpparam.classLoader);
        } else if (lpparam.packageName.equals(Common.PACKAGE_ONEPLUS_MMS)) {
            OPFeatures.init(lpparam);
        } else if (lpparam.packageName.equals(Common.PACKAGE_ONEPLUS_DIALER)) {
            OPFeatures.init(lpparam);
        } else if (lpparam.packageName.equals(Common.PACKAGE_ONEPLUS_DESKCLOCK)) {
            OPFeatures.init(lpparam);
        } else if (lpparam.packageName.equals(Common.PACKAGE_ONEPLUS_CONFIG)) {
            OPFeatures.init(lpparam);
        }/* else if (lpparam.packageName.equals(Common.PACKAGE_ANCROID_PHONE)) {
            OPFeatures.init(lpparam);
        }*/
    }
}