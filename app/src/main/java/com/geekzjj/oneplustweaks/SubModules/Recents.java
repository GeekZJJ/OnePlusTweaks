package com.geekzjj.oneplustweaks.SubModules;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import com.geekzjj.oneplustweaks.R;
import com.geekzjj.oneplustweaks.Module;
import com.geekzjj.oneplustweaks.PreferenceUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
    static int clearRecentsLocation = 5;
    static boolean recentsAddClearBtn = false;

    public final static Set<Object> sLockedTasks = new HashSet<>();
    public static Drawable mLightLockedDrawable;
    public static Drawable mLightUnlockedDrawable;
    public static Drawable mDarkLockedDrawable;
    public static Drawable mDarkUnlockedDrawable;
    public static Object mTask;

    public static int OVERVIEW_DISMISS_ALL = 357;
    public static int DismissSourceHeaderButton = 2;

    public static void setRecentsAddClearBtn(boolean recentsAddClearBtn) {
        Recents.recentsAddClearBtn = recentsAddClearBtn;
    }

    public static void setClearRecentsLocation(int clearRecentsLocation) {
        Recents.clearRecentsLocation = clearRecentsLocation;
    }

    public static void init(final ClassLoader classLoader) {

        clearRecentsLocation = PreferenceUtils.getClearRecentsLocation();
        recentsAddClearBtn = PreferenceUtils.getRecentsAddClearBtn();
        final Class<?> recentActivityClass = XposedHelpers.findClass("com.android.systemui.recents.RecentsActivity", classLoader);
        final Class<?> eventBusClazz = XposedHelpers.findClass("com.android.systemui.recents.events.EventBus",classLoader);
        final Class<?> dismissAllTaskViewsEventClazz = XposedHelpers.findClass("com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent",classLoader);
        final Class<?> deleteTaskDataEventClazz = XposedHelpers.findClass("com.android.systemui.recents.events.ui.DeleteTaskDataEvent",classLoader);
        final Class<?> taskViewClazz = XposedHelpers.findClass("com.android.systemui.recents.views.TaskView",classLoader);
        final Class<?> metricsLoggerClazz = XposedHelpers.findClass("com.android.internal.logging.MetricsLogger",classLoader);
        final Class<?> interpolatorsClazz = XposedHelpers.findClass("com.android.systemui.Interpolators",classLoader);
        final Class<?> taskStackClazz = XposedHelpers.findClass("com.android.systemui.shared.recents.model.TaskStack",classLoader);
        final Class<?> utilitiesClazz = XposedHelpers.findClass("com.android.systemui.shared.recents.utilities.Utilities",classLoader);
        final Class<?> animationPropsClazz = XposedHelpers.findClass("com.android.systemui.shared.recents.utilities.AnimationProps",classLoader);
        final Class<?> taskClazz = XposedHelpers.findClass("com.android.systemui.shared.recents.model.Task",classLoader);

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
        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.RecentsView", classLoader, "updateStack",taskStackClazz,boolean.class,
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

        XposedHelpers.findAndHookMethod("com.android.systemui.shared.recents.model.TaskStack",classLoader, "removeTask",
                taskClazz, animationPropsClazz, boolean.class,boolean.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(final MethodHookParam param) {
//                        XposedBridge.log("beforeHookedMethod removeTask");
                        // Update the top level view's visibilities
                        Object t = param.args[0];
                        if (sLockedTasks.contains(t)) {
                            sLockedTasks.remove(t);
                        }
                    }
                });

        XposedHelpers.findAndHookMethod("com.android.systemui.shared.recents.model.TaskStack",classLoader, "removeAllTasks",boolean.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                XposedBridge.log("replace removeAllTasks");
                boolean notifyStackChanges = (boolean) methodHookParam.args[0];
                Object TaskStackObject = methodHookParam.thisObject;
                Object mStackTaskList = XposedHelpers.getObjectField(TaskStackObject,"mStackTaskList");
                Object mRawTaskList = XposedHelpers.getObjectField(TaskStackObject,"mRawTaskList");
                Object mCb = XposedHelpers.getObjectField(TaskStackObject,"mCb");
                ArrayList<?> tasks = (ArrayList<?>) XposedHelpers.callMethod(mStackTaskList,"getTasks");
                for (int i = tasks.size() - 1; i >= 0; i--) {
                    Object t = tasks.get(i);
                    if (sLockedTasks.contains(t)) {
                        continue;
                    }
                    XposedHelpers.callMethod(mStackTaskList,"remove",t);
                    XposedHelpers.callMethod(mRawTaskList,"remove",t);
                }
                if (mCb != null && notifyStackChanges) {
                    // Notify that all tasks have been removed
                    XposedHelpers.callMethod(mCb,"onStackTasksRemoved",TaskStackObject);
                }
                return null;
            }
        });

        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.TaskStackView",classLoader, "onBusEvent",dismissAllTaskViewsEventClazz, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                XposedBridge.log("replace onBusEvent");
                Object event = methodHookParam.args[0];
                final Object TaskStackViewObject = methodHookParam.thisObject;
                final Context context = (Context) XposedHelpers.callMethod(TaskStackViewObject,"getContext");
                final Object mStack = XposedHelpers.getObjectField(TaskStackViewObject,"mStack");
                // Keep track of the tasks which will have their data removed
                final ArrayList tasks = new ArrayList<>((ArrayList<Object>) XposedHelpers.callMethod(mStack,"getTasks"));
                ArrayList deletedTasks = new ArrayList<>();
                ArrayList taskViews = new ArrayList<>((ArrayList<Object>) XposedHelpers.callMethod(TaskStackViewObject,"getTaskViews"));
                for (Object t : taskViews) {
                    if (sLockedTasks.contains(XposedHelpers.callMethod(t,"getTask"))) {
                        deletedTasks.add(t);
                    }
                }
                taskViews.removeAll(deletedTasks);
                Object mAnimationHelper = XposedHelpers.getObjectField(TaskStackViewObject,"mAnimationHelper");
                boolean useGridLayout = (boolean)XposedHelpers.callMethod(TaskStackViewObject,"useGridLayout");
                XposedHelpers.callMethod(mAnimationHelper,"startDeleteAllTasksAnimation",taskViews,useGridLayout,XposedHelpers.callMethod(event,"getAnimationTrigger"));
                XposedHelpers.callMethod(event,"addPostAnimationCallback",new Runnable() {
                    @Override
                    public void run() {
                        XposedHelpers.callMethod(TaskStackViewObject,"announceForAccessibility",context.getResources().getString(context.getResources().getIdentifier("accessibility_recents_all_items_dismissed", "string", "com.android.systemui")));

                        // Remove all tasks and delete the task data for all tasks
                        XposedHelpers.callMethod(mStack,"removeAllTasks",true);
                        for (int i = tasks.size() - 1; i >= 0; i--) {
                            Object t = tasks.get(i);
                            if (sLockedTasks.contains(t)) continue;
                            Object EventBusObject = XposedHelpers.callStaticMethod(eventBusClazz,"getDefault");
                            XposedHelpers.callMethod(EventBusObject,"send",XposedHelpers.newInstance(deleteTaskDataEventClazz,t));
                        }
                        XposedHelpers.callStaticMethod(metricsLoggerClazz,"action",context,OVERVIEW_DISMISS_ALL);
                    }
                });
                return null;
            }
        });

        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.TaskStackViewTouchHandler",classLoader, "getChildAtPosition", MotionEvent.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                XposedBridge.log("replace getChildAtPosition");
                MotionEvent ev = (MotionEvent) methodHookParam.args[0];
                Object tv = XposedHelpers.callMethod(methodHookParam.thisObject,"findViewAtPoint",(int) ev.getX(), (int) ev.getY());
                if (tv != null && ((boolean)XposedHelpers.callMethod(methodHookParam.thisObject,"canChildBeDismissed",tv) || sLockedTasks.contains(  XposedHelpers.callMethod(tv,"getTask")  ))) {
                    return tv;
                }
                return null;
            }
        });

        XposedHelpers.findAndHookConstructor("com.android.systemui.recents.views.TaskViewHeader", classLoader, Context.class, AttributeSet.class, int.class, int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                XposedBridge.log("TaskViewHeader Constructor");

                mGbContext = getGbContext((Context)param.args[0]);

                View view = (View) param.thisObject;
                ViewGroup vg = (ViewGroup) view.getRootView();
//                LayoutInflater mInflater = LayoutInflater.from(mGbContext);
//                mInflater.inflate(R.layout.lock_task,vg,true);

//                final String title = (String) XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.thisObject,"mTask"),"title");
//                XposedBridge.log("Constructor: "+" size="+mLockTaskButtons.size());
//                ImageView lockBtn = view.findViewById(R.id.lock_task);

                if(mLightLockedDrawable==null){
                    mLightLockedDrawable = mGbContext.getDrawable(R.drawable.recents_locked_light);
                    mLightUnlockedDrawable = mGbContext.getDrawable(R.drawable.recents_unlocked_light);
                    mDarkLockedDrawable = mGbContext.getDrawable(R.drawable.recents_locked_dark);
                    mDarkUnlockedDrawable = mGbContext.getDrawable(R.drawable.recents_unlocked_dark);
                }

                ImageView lockBtn = new ImageView(vg.getContext());
                lockBtn.setImageDrawable(mLightLockedDrawable);
                int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, view.getResources().getDisplayMetrics());
                lockBtn.setPadding(padding,padding,padding,padding);
                FrameLayout.LayoutParams lParams = new FrameLayout.LayoutParams(WRAP_CONTENT,WRAP_CONTENT,Gravity.CENTER_VERTICAL|Gravity.END);
                lockBtn.setLayoutParams(lParams);
                lockBtn.setAlpha(1f);
                lockBtn.setVisibility(View.GONE);
                vg.addView(lockBtn);

                XposedHelpers.setAdditionalInstanceField(param.thisObject,"mLockTaskButton",lockBtn);
            }
        });

//        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.TaskViewHeader", classLoader,"onFinishInflate", new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
////                XposedBridge.log("onFinishInflate");
//                View view = (View) param.thisObject;
//                XposedHelpers.setAdditionalInstanceField(param.thisObject,"mLockTaskButton",view.findViewById(R.id.lock_task));
//            }
//        });

        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.TaskViewHeader", classLoader,"updateLayoutParams",View.class, View.class, View.class, View.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        XposedBridge.log("updateLayoutParams");

                        View view = (View) param.thisObject;
                        View mAppOverlayView = (View) XposedHelpers.getObjectField(view,"mAppOverlayView");
                        int mHeaderBarHeight = XposedHelpers.getIntField(view,"mHeaderBarHeight");
                        int mHeaderButtonPadding = XposedHelpers.getIntField(view,"mHeaderButtonPadding");
                        if (mAppOverlayView == null) {
                            ImageView btn = getLockTaskButton(param.thisObject);
                            if (btn!=null) {
                                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(mHeaderBarHeight, mHeaderBarHeight, Gravity.END);
                                lp.setMarginEnd(mHeaderBarHeight);
                                btn.setLayoutParams(lp);
                                btn.setPadding(mHeaderButtonPadding, mHeaderButtonPadding,mHeaderButtonPadding, mHeaderButtonPadding);
                            } else {
                                XposedBridge.log("mLockTaskButton==null");
                            }
                        }
                    }
                });

        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.TaskViewHeader", classLoader,"onTaskViewSizeChanged",int.class,int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        XposedBridge.log("onTaskViewSizeChanged");
                        ImageView btn = getLockTaskButton(param.thisObject);
                        if(btn!=null){
                            btn.setVisibility(View.VISIBLE);
                        } else {
                            XposedBridge.log("onTaskViewSizeChanged: btn==null");
                        }
                    }
                });

        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.TaskViewHeader", classLoader,"bindToTask",taskClazz,boolean.class,boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        XposedBridge.log("bindToTask");
                        mTask = XposedHelpers.getObjectField(param.thisObject, "mTask");
                        updateLockTaskDrawable(getLockTaskButton(param.thisObject));
                        getLockTaskButton(param.thisObject).setOnClickListener((View.OnClickListener) param.thisObject);
                        getLockTaskButton(param.thisObject).setClickable(false);
                        getLockTaskButton(param.thisObject).setAlpha(0f);
                        XposedHelpers.callMethod(getLockTaskButton(param.thisObject).getBackground(),"setForceSoftware",true);
                    }
                });

//        final Class<?> taskViewDismissedEventClazz = XposedHelpers.findClass("com.android.systemui.recents.events.ui.TaskViewDismissedEvent",classLoader);
//        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.TaskStackView", classLoader,"onBusEvent",taskViewDismissedEventClazz,
//                new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        XposedBridge.log("onBusEvent TaskViewDismissedEvent");
//                    }
//                });

        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.TaskView", classLoader,"dismissTask",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        XposedBridge.log("dismissTask,remove mLockTaskButton");
                        XposedHelpers.removeAdditionalInstanceField(XposedHelpers.getObjectField(param.thisObject, "mHeaderView"),"mLockTaskButton");
                    }
                });

        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.TaskViewHeader", classLoader,"startNoUserInteractionAnimation",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        String title = (String) XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.thisObject,"mTask"),"title");
//                        XposedBridge.log("startNoUserInteractionAnimation: "+title);
                        View view = (View) param.thisObject;
                        int duration = view.getResources().getInteger(view.getResources().getIdentifier("recents_task_enter_from_app_duration", "integer", "com.android.systemui"));
                        getLockTaskButton(param.thisObject).setVisibility(View.VISIBLE);
                        getLockTaskButton(param.thisObject).setClickable(true);
                        if (getLockTaskButton(param.thisObject).getVisibility() == View.VISIBLE) {
                            getLockTaskButton(param.thisObject).animate()
                                    .alpha(1f)
                                    .setInterpolator((TimeInterpolator) XposedHelpers.getStaticObjectField(interpolatorsClazz,"FAST_OUT_LINEAR_IN"))
                                    .setDuration(duration)
                                    .start();
                        } else {
                            getLockTaskButton(param.thisObject).setAlpha(1f);
                            XposedBridge.log("startNoUserInteractionAnimation: not VISIBLE");
                        }
                    }
                });
        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.TaskViewHeader", classLoader,"setNoUserInteractionState",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        XposedBridge.log("setNoUserInteractionState");
                        getLockTaskButton(param.thisObject).setVisibility(View.VISIBLE);
                        getLockTaskButton(param.thisObject).animate().cancel();
                        getLockTaskButton(param.thisObject).setAlpha(1f);
                        getLockTaskButton(param.thisObject).setClickable(true);
                    }
                });

        XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.TaskViewHeader", classLoader, "onClick", View.class,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
//                        XposedBridge.log("onClick");
                        View v = (View) param.args[0];
                        ImageView mDismissButton = (ImageView)XposedHelpers.getObjectField(param.thisObject,"mDismissButton");
                        mTask = XposedHelpers.getObjectField(param.thisObject, "mTask");
                        if (v == mDismissButton) {
                            Object tv = XposedHelpers.callStaticMethod(utilitiesClazz,"findParent",param.thisObject,taskViewClazz);
                            if (!sLockedTasks.contains(XposedHelpers.callMethod(tv,"getTask"))) {
                                XposedHelpers.callMethod(tv,"dismissTask");
                                // Keep track of deletions by the dismiss button
                                XposedHelpers.callStaticMethod(metricsLoggerClazz,"histogram",mGbContext,"overview_task_dismissed_source",DismissSourceHeaderButton);
                            }
                            return null;
                        } else if (v == getLockTaskButton(param.thisObject)) {
                            if (sLockedTasks.contains(mTask)) {
                                sLockedTasks.remove(mTask);
                            } else {
                                sLockedTasks.add(mTask);
                            }
                            updateLockTaskDrawable(getLockTaskButton(param.thisObject));
                            return null;
                        } else {
                            return XposedBridge.invokeOriginalMethod(param.method,param.thisObject,param.args);
                        }
                    }
                });
    }

    private static void updateLockTaskDrawable(ImageView btn) {
        boolean useLightOnPrimaryColor = XposedHelpers.getBooleanField(mTask,"useLightOnPrimaryColor");
        String title = (String) XposedHelpers.getObjectField(mTask,"title");
//        XposedBridge.log("useLightOnPrimaryColor="+useLightOnPrimaryColor+",title="+title);
        if (sLockedTasks.contains(mTask)) {
            btn.setImageDrawable(useLightOnPrimaryColor ? mLightLockedDrawable : mDarkLockedDrawable);
            btn.setContentDescription(mGbContext.getResources().getString(R.string.accessibility_unlock_task, title));
        } else {
            btn.setImageDrawable(useLightOnPrimaryColor ? mLightUnlockedDrawable : mDarkUnlockedDrawable);
            btn.setContentDescription(mGbContext.getResources().getString(R.string.accessibility_lock_task, title));
        }
        ((AnimatedVectorDrawable) btn.getDrawable()).start();
    }

    private static ImageView getLockTaskButton(Object obj){
        return (ImageView) XposedHelpers.getAdditionalInstanceField(obj,"mLockTaskButton");
    }

    public static synchronized Context getGbContext(Context context) throws Throwable {
        if (mGbContext == null) {
            mGbContext = context.createPackageContext(Module.class.getPackage().getName(),Context.CONTEXT_IGNORE_SECURITY);
            mGbContext = mGbContext.createDeviceProtectedStorageContext();
        }
        return mGbContext;
    }
}
