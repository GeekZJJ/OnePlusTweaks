package com.geekzjj.oneplustweaks.SubModules;

import android.graphics.Bitmap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class FixOPScreenShotCrash {

    static int stackTraceIndex = -1;
    public static void init(final ClassLoader classLoader) {
        try {
            Class bitmapClass = XposedHelpers.findClass("android.graphics.Bitmap", classLoader);
            findAndHookMethod(bitmapClass, "createBitmap",
                    Bitmap.class,int.class,int.class,int.class,int.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Throwable ex = new Throwable();
                            StackTraceElement[] stackElements = ex.getStackTrace();
                            if(stackElements == null){
                                XposedBridge.log("stackElements == null");
                                return;
                            }
                            if(stackTraceIndex == -1){
                                for (int i = 0; i < stackElements.length; i++) {
                                    if(stackElements[i].getMethodName().equals("takeScreenshot")){
                                        stackTraceIndex = i;
                                        break;
                                    }
                                }
                            } else {
                                XposedBridge.log("Get stackTraceIndex = "+stackTraceIndex);
                            }
                            if ((stackTraceIndex!=-1)&&(stackElements.length>stackTraceIndex)
                                    &&(stackElements[stackTraceIndex].getMethodName().equals("takeScreenshot"))) {
                                XposedBridge.log("createBitmap正在创建新的bitmap对象" );
                                Bitmap originResult = (Bitmap) param.getResult();
                                Bitmap newResult = originResult.copy(originResult.getConfig(),originResult.isMutable());
                                originResult.recycle();
                                param.setResult(newResult);
                            }else{
                                XposedBridge.log("ERROR: stackTraceIndex = "+stackTraceIndex);
                            }
                        }
                    });
        } catch (XposedHelpers.ClassNotFoundError e) {
            XposedBridge.log("找不到可以替换的方法" );
        }
    }
}
