package com.geekzjj.oneplustweaks.SubModules;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;

import com.geekzjj.oneplustweaks.Module;
import com.geekzjj.oneplustweaks.PreferenceUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.geekzjj.oneplustweaks.R;

public class Recents {
    public static final String TAG = "Recents ";

    public static Context mGbContext;
    public static Context mContext;

    public static Activity mRecentsActivity;

    static View mFloatingButtonLayout;
    static View mFloatingButton;

    //0:右上
    //1:左上
    //2:中上
    //3:右下
    //4:左下
    //5:中下
    static int clearRecentsLocation = PreferenceUtils.getClearRecentsLocation();
    static boolean recentsAddClearBtn = PreferenceUtils.getRecentsAddClearBtn();

    public static void setRecentsAddClearBtn(boolean recentsAddClearBtn) {
        Recents.recentsAddClearBtn = recentsAddClearBtn;
    }

    public static void setClearRecentsLocation(int clearRecentsLocation) {
        Recents.clearRecentsLocation = clearRecentsLocation;
    }

    public static void init(final ClassLoader classLoader) {

        Class<?> recentActivityClass = XposedHelpers.findClass("com.android.systemui.recents.RecentsActivity", classLoader);
        final Class<?> eventBusClazz = XposedHelpers.findClass("com.android.systemui.recents.events.EventBus",classLoader);
        final Class<?> dismissAllTaskViewsEventClazz = XposedHelpers.findClass("com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent",classLoader);

        XposedHelpers.findAndHookMethod(recentActivityClass, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                mRecentsActivity = (Activity) param.thisObject;
                mGbContext = getGbContext(mRecentsActivity);
            }
        });

        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.RecentsView", classLoader, "onAttachedToWindow",
        new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) {
                View view = (View) param.thisObject;
                ViewGroup vg = (ViewGroup) view.getRootView();
//                Resources res = view.getResources();
//                //最近任务布局
//                View recentsView = view.findViewById(res.getIdentifier("recents_view", "id", "com.android.systemui"));
//                ViewGroup vg = (ViewGroup) recentsView.getParent();
//                // create and inject new ImageView and set onClick listener to handle action
//                mRecentsClearButton = new ImageButton(vg.getContext());
//                mRecentsClearButton.setBackgroundDrawable(mGbContext.getDrawable(R.drawable.floating_action_button));
//                mClearAllRecentsSizePx = (int) TypedValue.applyDimension(
//                        TypedValue.COMPLEX_UNIT_DIP, 50, res.getDisplayMetrics());
//                FrameLayout.LayoutParams lParams = new FrameLayout.LayoutParams(
//                        mClearAllRecentsSizePx, mClearAllRecentsSizePx);
//                mRecentsClearButton.setLayoutParams(lParams);
//                mRecentsClearButton.setScaleType(ScaleType.CENTER);
//                mRecentsClearButton.setClickable(true);
//                mRecentsClearButton.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        Toast.makeText(mGbContext,"hello",Toast.LENGTH_SHORT).show();
//                    }
//                });
//                mRecentsClearButton.setVisibility(View.VISIBLE);
//                vg.addView(mRecentsClearButton);
//                mRecentsClearButton.performClick();
//                mFloatingButtonLayout = new FrameLayout(vg.getContext());

                LayoutInflater mInflater = LayoutInflater.from(mGbContext);
                vg = (ViewGroup) mInflater.inflate(R.layout.floating_action_button,vg);

                mFloatingButtonLayout=vg.findViewById(R.id.floating_action_button_layout);

                mFloatingButton=mFloatingButtonLayout.findViewById(R.id.clear_recents_btn);
                mFloatingButton.setVisibility(View.VISIBLE);
                mFloatingButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Object EventBusObject = XposedHelpers.callStaticMethod(eventBusClazz,"getDefault");
                        XposedHelpers.callMethod(EventBusObject,"send",XposedHelpers.newInstance(dismissAllTaskViewsEventClazz));
                    }
                });
            }
        });

        XposedHelpers.findAndHookConstructor("com.android.systemui.recents.views.RecentsView", classLoader, Context.class, AttributeSet.class, int.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        mContext = (Context) XposedHelpers.getObjectField(param.thisObject,"mContext");
                        if(mContext==null) {
                            XposedBridge.log("RecentsView Constructor: mContext==null");
                        }
                    }
                });

        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.RecentsView", classLoader, "onMeasure",int.class,int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) {
                        if (mFloatingButtonLayout != null && recentsAddClearBtn) {
                            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mFloatingButtonLayout.getLayoutParams();
                            boolean isLandscape = mContext.getResources().getConfiguration().orientation
                                    == Configuration.ORIENTATION_LANDSCAPE;
                            if (isLandscape) {
                                params.topMargin = mContext.getResources().
                                        getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_height", "dimen", "com.android.systemui"));
                            } else {
                                params.topMargin = 2 * mContext.getResources().
                                        getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_height", "dimen", "com.android.systemui"));
                            }
                            switch (clearRecentsLocation) {
                                case 0:
                                    params.gravity = Gravity.TOP | Gravity.RIGHT;
                                    break;
                                case 1:
                                    params.gravity = Gravity.TOP | Gravity.LEFT;
                                    break;
                                case 2:
                                    params.gravity = Gravity.TOP | Gravity.CENTER;
                                    break;
                                case 3:
                                default:
                                    params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                                    break;
                                case 4:
                                    params.gravity = Gravity.BOTTOM | Gravity.LEFT;
                                    break;
                                case 5:
                                    params.gravity = Gravity.BOTTOM | Gravity.CENTER;
                                    break;
                            }
                            mFloatingButtonLayout.setLayoutParams(params);
                            mFloatingButtonLayout.setVisibility(View.VISIBLE);
                        } else {
                            mFloatingButtonLayout.setVisibility(View.GONE);
                        }
                    }
                });
        Class<?> TaskStackClazz = XposedHelpers.findClass("com.android.systemui.shared.recents.model.TaskStack",classLoader);
        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.RecentsView", classLoader, "updateStack",TaskStackClazz,boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) {
                        // Update the top level view's visibilities
                        Object stack = param.args[0];
                        int taskCount = (int)XposedHelpers.callMethod(stack,"getTaskCount");
                        if (taskCount > 0) {
                            if (mFloatingButton != null) {
                                mFloatingButton.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (mFloatingButton != null) {
                                mFloatingButton.setVisibility(View.GONE);
                            }
                        }
                    }
                });

        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.RecentsView",classLoader, "showStackActionButton",int.class,boolean.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                if (recentsAddClearBtn) {
                    return null;
                }
                return XposedBridge.invokeOriginalMethod(methodHookParam.method,methodHookParam.thisObject,methodHookParam.args);
            }
        });

    }

    public static synchronized Context getGbContext(Context context) throws Throwable {
        if (mGbContext == null) {
            mGbContext = context.createPackageContext(Module.class.getPackage().getName(),Context.CONTEXT_IGNORE_SECURITY);
            mGbContext = mGbContext.createDeviceProtectedStorageContext();
        }
        return mGbContext;
    }
}
