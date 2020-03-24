package com.geekzjj.oneplustweaks.SubModules;

import com.geekzjj.oneplustweaks.Common;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class FixOPScreenShotCrash {

    public static void init(final ClassLoader classLoader) {

        final Class GlobalScreenshotClass = XposedHelpers.findClass("com.oneplus.screenshot.GlobalScreenshot", classLoader);
        final Class SurfaceControlClass = XposedHelpers.findClass("android.view.SurfaceControl", classLoader);
        XposedHelpers.findAndHookMethod(GlobalScreenshotClass, "takeScreenshot",
                Runnable.class, boolean.class, boolean.class, Rect.class, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {

                        Runnable finisher = (Runnable) param.args[0];
                        boolean statusBarVisible = (boolean)param.args[1];
                        boolean navBarVisible = (boolean)param.args[2];
                        Rect crop = (Rect) param.args[3];

                        DisplayMetrics mDisplayMetrics = (DisplayMetrics) XposedHelpers.getObjectField(param.thisObject,"mDisplayMetrics");
                        int cropWidth2 = crop.width();
                        int cropHeight2 = crop.height();
                        if (cropWidth2 == mDisplayMetrics.widthPixels && cropHeight2 == mDisplayMetrics.heightPixels) {
                            return XposedBridge.invokeOriginalMethod(param.method,param.thisObject,param.args);
                        }

                        Rect rect = new Rect(0, 0, mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels);

                        XposedHelpers.setStaticBooleanField(GlobalScreenshotClass,"mIsBusy",true);
                        XposedHelpers.setBooleanField(param.thisObject,"mNavBarVisible",navBarVisible);
                        XposedHelpers.setBooleanField(param.thisObject,"mStatusBarVisible",statusBarVisible);

                        int rot = (int)(XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject,"mDisplay"),"getRotation"));
                        Bitmap mFullScreenBitmap = (Bitmap) XposedHelpers.callStaticMethod(SurfaceControlClass,"screenshot",rect, rect.width(), rect.height(), rot);
                        Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject,"mContext");
                        if (mFullScreenBitmap == null) {
                            NotificationManager mNotificationManager = (NotificationManager) XposedHelpers.getObjectField(param.thisObject,"mNotificationManager");
                            XposedHelpers.callStaticMethod(GlobalScreenshotClass,"notifyScreenshotError",mContext, mNotificationManager,
                                    mContext.getResources().getIdentifier("screenshot_failed_to_capture_text", "string", Common.PACKAGE_ONEPLUS_SCREENSHOT));
                            finisher.run();
                            XposedBridge.log("Longshot.GlobalScreenshot  mScreenBitmap = null");
                            return null;
                        }

                        Bitmap cropped = Bitmap.createBitmap(mFullScreenBitmap, crop.left, crop.top, cropWidth2, cropHeight2);
                        XposedHelpers.setObjectField(param.thisObject,"mScreenBitmap",cropped);

                        Bitmap mScreenBitmap = (Bitmap) XposedHelpers.getObjectField(param.thisObject,"mScreenBitmap");
                        mScreenBitmap.setHasAlpha(false);
                        mScreenBitmap.prepareToDraw();

                        XposedHelpers.callMethod(param.thisObject,"startAnimation",finisher, mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels, statusBarVisible, navBarVisible);
                        return null;
                    }
                });
    }
}