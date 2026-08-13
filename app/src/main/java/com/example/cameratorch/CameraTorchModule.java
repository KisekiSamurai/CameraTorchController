package com.example.cameratorch;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.Looper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import android.app.AndroidAppHelper;

public class CameraTorchModule implements IXposedHookLoadPackage {

    private static CameraManager cameraManager;
    private static String currentCameraId;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        // 只Hook目标相机应用
        if (!isTargetCameraApp(lpparam.packageName)) {
            return;
        }

        XposedBridge.log(Constants.TAG + " Hooking package: " + lpparam.packageName);

        try {
            hookCameraOpen(lpparam);
        } catch (Exception e) {
            XposedBridge.log(Constants.TAG + " Failed to hook: " + e.getMessage());
        }
    }

    private boolean isTargetCameraApp(String packageName) {
        for (String app : Constants.TARGET_CAMERA_APPS) {
            if (packageName.equals(app)) {
                return true;
            }
        }
        return false;
    }

    private void hookCameraOpen(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
            "android.hardware.camera2.CameraManager",
            lpparam.classLoader,
            "openCamera",
            String.class,
            android.hardware.camera2.CameraDevice.StateCallback.class,
            android.os.Handler.class,
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        // 检查模块是否启用
                        if (!isModuleEnabled()) {
                            XposedBridge.log(Constants.TAG + " Module disabled");
                            return;
                        }

                        // 获取CameraManager实例
                        cameraManager = (CameraManager) param.thisObject;
                        currentCameraId = (String) param.args[0];

                        XposedBridge.log(Constants.TAG + " Camera opened: " + currentCameraId);

                        // 在主线程中执行手电筒操作
                        new Handler(Looper.getMainLooper()).post(() -> {
                            try {
                                // 开启手电筒
                                if (enableFlashlight()) {
                                    // 显示通知
                                    showNotification();
                                    XposedBridge.log(Constants.TAG + " Flashlight enabled successfully");
                                }
                            } catch (Exception e) {
                                XposedBridge.log(Constants.TAG + " Error enabling flashlight: " + e.getMessage());
                            }
                        });

                    } catch (Exception e) {
                        XposedBridge.log(Constants.TAG + " Error in hook: " + e.getMessage());
                    }
                }
            }
        );
    }

    private boolean enableFlashlight() {
        try {
            if (cameraManager != null && currentCameraId != null) {
                // 检查闪光灯是否支持
                if (isFlashlightSupported()) {
                    cameraManager.setTorchMode(currentCameraId, true);
                    return true;
                } else {
                    XposedBridge.log(Constants.TAG + " Flashlight not supported for camera: " + currentCameraId);
                }
            }
        } catch (CameraAccessException e) {
            XposedBridge.log(Constants.TAG + " Camera access error: " + e.getMessage());
        } catch (SecurityException e) {
            XposedBridge.log(Constants.TAG + " Security exception: " + e.getMessage());
        }
        return false;
    }

    public static void disableFlashlight() {
        try {
            if (cameraManager != null && currentCameraId != null) {
                cameraManager.setTorchMode(currentCameraId, false);
                XposedBridge.log(Constants.TAG + " Flashlight disabled");
            }
        } catch (CameraAccessException e) {
            XposedBridge.log(Constants.TAG + " Error disabling flashlight: " + e.getMessage());
        }
    }

    private boolean isFlashlightSupported() {
        try {
            if (cameraManager != null && currentCameraId != null) {
                CameraManager.CameraCharacteristics characteristics =
                    cameraManager.getCameraCharacteristics(currentCameraId);
                Boolean flashAvailable = characteristics.get(
                    android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE);
                return flashAvailable != null && flashAvailable;
            }
        } catch (CameraAccessException e) {
            XposedBridge.log(Constants.TAG + " Error checking flashlight support: " + e.getMessage());
        }
        return false;
    }

    private boolean isModuleEnabled() {
        try {
            Context context = AndroidAppHelper.currentApplication();
            if (context != null) {
                SharedPreferences prefs = context.getSharedPreferences(
                    Constants.PREFS_NAME, Context.MODE_PRIVATE);
                return prefs.getBoolean(Constants.KEY_ENABLED, true);
            }
        } catch (Exception e) {
            XposedBridge.log(Constants.TAG + " Error checking module status: " + e.getMessage());
        }
        return true; // 默认启用
    }

    private void showNotification() {
        try {
            Context context = AndroidAppHelper.currentApplication();
            if (context != null) {
                FlashlightNotificationManager.showNotification(context);
            }
        } catch (Exception e) {
            XposedBridge.log(Constants.TAG + " Error showing notification: " + e.getMessage());
        }
    }

    public static Context getModuleContext() {
        try {
            return AndroidAppHelper.currentApplication();
        } catch (Exception e) {
            return null;
        }
    }
}
