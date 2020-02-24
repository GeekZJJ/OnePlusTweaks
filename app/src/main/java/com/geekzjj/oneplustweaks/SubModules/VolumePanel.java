/*
 * Copyright (C) 2019 Peter Gregus for GravityBox Project (C3C076@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.geekzjj.oneplustweaks.SubModules;

import android.media.AudioManager;
import android.view.Gravity;
import android.view.WindowManager;

import com.geekzjj.oneplustweaks.PreferenceUtils;

import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class VolumePanel {

    private static final String TAG = "VolumePanel ";
    public static final String PACKAGE_NAME = "com.android.systemui";
    private static final String CLASS_VOLUME_PANEL = "com.android.systemui.volume.VolumeDialogImpl";
    private static final String CLASS_VOLUME_ROW = CLASS_VOLUME_PANEL + ".VolumeRow";
    private static final boolean DEBUG = false;

    private static Object mVolumePanel;

    private static boolean mVolumePanelExpanded;
    private static Set<String> mVolumePanelExpandedStreams;
    private static int mTimeout;
    private static int mVolumePanelLocation = 0;

    private static void log(String message) {
        XposedBridge.log(TAG + ": " + message);
    }

    public static void init(final ClassLoader classLoader) {
        try {
            final Class<?> classVolumePanel = XposedHelpers.findClass(CLASS_VOLUME_PANEL, classLoader);


            mVolumePanelExpanded = PreferenceUtils.getVolumePanelExpanded();
            mVolumePanelExpandedStreams = PreferenceUtils.getVolumePanelItems();
            mVolumePanelLocation = PreferenceUtils.getVolumePanelLocation();

            if (DEBUG) log("mVolumePanelExpanded="+mVolumePanelExpanded);
            if (DEBUG) log("mVolumePanelExpandedStreams="+mVolumePanelExpandedStreams.toString());

            mTimeout = 0;

            XposedBridge.hookAllConstructors(classVolumePanel, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) {
                        mVolumePanel = param.thisObject;
                    }
                });


            XposedHelpers.findAndHookMethod(classVolumePanel, "initDialog", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) {
//                    XposedBridge.log("initDialog,"+mVolumePanelLocation);
                    Object mWindow = XposedHelpers.getObjectField(param.thisObject,"mWindow");
                    WindowManager.LayoutParams lp = (WindowManager.LayoutParams) XposedHelpers.callMethod(mWindow,"getAttributes");
                    lp.gravity = ( mVolumePanelLocation==0 ? Gravity.LEFT : Gravity.RIGHT ) | Gravity.CENTER_VERTICAL;
                    XposedHelpers.callMethod(mWindow,"setAttributes",lp);
                }
            });

            XC_MethodHook shouldBeVisibleHook = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) {
                    int streamType = XposedHelpers.getIntField(param.args[0], "stream");
                    if (mVolumePanelExpanded && (
                            streamType == AudioManager.STREAM_MUSIC ||
                            streamType == AudioManager.STREAM_RING ||
                            streamType == AudioManager.STREAM_ALARM ||
                            streamType == AudioManager.STREAM_VOICE_CALL ||
                            streamType == 6 /* BLUETOOTH_SCO */ ||
                            streamType == AudioManager.STREAM_SYSTEM)) {
                        param.setResult(mVolumePanelExpandedStreams.contains(String.valueOf(streamType)));
                    }

                }
            };
            XposedHelpers.findAndHookMethod(classVolumePanel, "shouldBeVisibleH",
                    CLASS_VOLUME_ROW, CLASS_VOLUME_ROW, shouldBeVisibleHook);

            XposedHelpers.findAndHookMethod(classVolumePanel, "computeTimeoutH", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) {
                    if (mTimeout != 0) {
                        param.setResult(mTimeout);
                    }
                }
            });
        } catch (Throwable t) {
        }
    }

    public static void setVolumePanelExpandedStreams(HashSet<String> streams){
        mVolumePanelExpandedStreams = streams;
    }

    public static void setmVolumePanelExpanded(boolean b) {
        mVolumePanelExpanded = b;
    }
    public static void setVolumePanelLocation(int location) {
        mVolumePanelLocation = location;
        Object mWindow = XposedHelpers.getObjectField(mVolumePanel,"mWindow");
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) XposedHelpers.callMethod(mWindow,"getAttributes");
        lp.gravity = ( mVolumePanelLocation==0 ? Gravity.LEFT : Gravity.RIGHT ) | Gravity.CENTER_VERTICAL;
        XposedHelpers.callMethod(mWindow,"setAttributes",lp);
    }
}
