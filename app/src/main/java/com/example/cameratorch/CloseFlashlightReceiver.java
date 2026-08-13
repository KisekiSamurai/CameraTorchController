package com.example.cameratorch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

public class CloseFlashlightReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constants.ACTION_CLOSE_FLASHLIGHT.equals(intent.getAction())) {
            // 在新线程中执行关闭操作，避免阻塞主线程
            new Thread(() -> {
                try {
                    // 关闭手电筒
                    CameraTorchModule.disableFlashlight();

                    // 在主线程中取消通知
                    new Handler(Looper.getMainLooper()).post(() -> {
                        FlashlightNotificationManager.cancelNotification(context);
                    });

                    // 检查是否需要同时关闭相机
                    SharedPreferences prefs = context.getSharedPreferences(
                        Constants.PREFS_NAME, Context.MODE_PRIVATE);
                    boolean autoCloseCamera = prefs.getBoolean(Constants.KEY_AUTO_CLOSE_CAMERA, false);

                    if (autoCloseCamera) {
                        // 延迟关闭相机，确保手电筒已关闭
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            closeCameraApp(context);
                        }, 500);
                    }

                } catch (Exception e) {
                    // 静默处理异常
                }
            }).start();
        }
    }

    private void closeCameraApp(Context context) {
        try {
            // 获取当前前台应用
            String packageName = getForegroundPackageName(context);
            if (packageName != null && isCameraApp(packageName)) {
                Intent mainIntent = new Intent(Intent.ACTION_MAIN);
                mainIntent.addCategory(Intent.CATEGORY_HOME);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mainIntent);
            }
        } catch (Exception e) {
            // 静默处理异常
        }
    }

    private String getForegroundPackageName(Context context) {
        // 简化实现 - 实际项目中需要更复杂的逻辑
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object currentActivityThread = activityThread.getMethod("currentActivityThread").invoke(null);
            Object activities = activityThread.getMethod("mActivities").get(currentActivityThread);

            if (activities != null) {
                java.lang.reflect.Field field = activities.getClass().getDeclaredField("mActivities");
                field.setAccessible(true);
                java.util.ArrayMap<?, ?> activityMap = (java.util.ArrayMap<?, ?>) field.get(activities);

                for (int i = activityMap.size() - 1; i >= 0; i--) {
                    Object record = activityMap.valueAt(i);
                    if (record != null) {
                        java.lang.reflect.Field pausedField = record.getClass().getDeclaredField("paused");
                        pausedField.setAccessible(true);
                        boolean paused = pausedField.getBoolean(record);

                        if (!paused) {
                            java.lang.reflect.Field intentField = record.getClass().getDeclaredField("intent");
                            intentField.setAccessible(true);
                            Intent intent = (Intent) intentField.get(record);
                            if (intent != null && intent.getComponent() != null) {
                                return intent.getComponent().getPackageName();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 静默处理异常
        }
        return null;
    }

    private boolean isCameraApp(String packageName) {
        for (String app : Constants.TARGET_CAMERA_APPS) {
            if (packageName.equals(app)) {
                return true;
            }
        }
        return false;
    }
}
