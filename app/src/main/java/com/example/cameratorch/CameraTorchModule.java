package com.example.cameratorch;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.Looper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import android.app.AndroidAppHelper;
import android.app.Activity;

public class CameraTorchModule implements IXposedHookLoadPackage {

    private static CameraManager cameraManager;
    private static String currentCameraId;
    private static boolean torchEnabled = false;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log(Constants.TAG + " Module loaded in package: " + lpparam.packageName);

        // 只Hook目标相机应用
        if (!isTargetCameraApp(lpparam.packageName)) {
            XposedBridge.log(Constants.TAG + " Not a target camera app, skipping");
            return;
        }

        XposedBridge.log(Constants.TAG + " Hooking package: " + lpparam.packageName);

        try {
            // Hook Camera Activity 的 onResume 方法
            hookCameraActivity(lpparam);
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

    private void hookCameraActivity(XC_LoadPackage.LoadPackageParam lpparam) {
        // 尝试Hook不同的Camera Activity类名
        String[] cameraActivityClasses = {
            "com.android.camera.Camera",
            "com.android.camera.ActivityBase",
            "android.app.Activity"
        };

        for (String className : cameraActivityClasses) {
            try {
                Class<?> cameraClass = XposedHelpers.findClass(className, lpparam.classLoader);
                XposedHelpers.findAndHookMethod(cameraClass, "onResume", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            if (!isModuleEnabled()) {
                                XposedBridge.log(Constants.TAG + " Module disabled");
                                return;
                            }

                            Activity activity = (Activity) param.thisObject;
                            XposedBridge.log(Constants.TAG + " Camera Activity resumed: " + activity.getClass().getName());

                            // 延迟执行，确保相机已初始化
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                try {
                                    enableFlashlightWithDelay();
                                } catch (Exception e) {
                                    XposedBridge.log(Constants.TAG + " Error: " + e.getMessage());
                                }
                            }, 500);

                        } catch (Exception e) {
                            XposedBridge.log(Constants.TAG + " Error in hook: " + e.getMessage());
                        }
                    }
                });
                XposedBridge.log(Constants.TAG + " Successfully hooked: " + className);
                break; // 成功Hook一个就退出
            } catch (Exception e) {
                XposedBridge.log(Constants.TAG + " Failed to hook " + className + ": " + e.getMessage());
            }
        }
    }

    private void enableFlashlightWithDelay() {
        try {
            Context context = AndroidAppHelper.currentApplication();
            if (context != null) {
                cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                if (cameraManager != null) {
                    // 获取后置相机ID
                    String[] cameraIds = cameraManager.getCameraIdList();
                    for (String id : cameraIds) {
                        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                        Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                        if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                            currentCameraId = id;
                            Boolean flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                            if (flashAvailable != null && flashAvailable) {
                                cameraManager.setTorchMode(currentCameraId, true);
                                torchEnabled = true;
                                XposedBridge.log(Constants.TAG + " Flashlight enabled on camera: " + currentCameraId);
                                showNotification();
                                return;
                            }
                        }
                    }
                    XposedBridge.log(Constants.TAG + " No camera with flash found");
                }
            }
        } catch (Exception e) {
            XposedBridge.log(Constants.TAG + " Error enabling flashlight: " + e.getMessage());
        }
    }

    public static void disableFlashlight() {
        try {
            if (cameraManager != null && currentCameraId != null && torchEnabled) {
                cameraManager.setTorchMode(currentCameraId, false);
                torchEnabled = false;
                XposedBridge.log(Constants.TAG + " Flashlight disabled");
            }
        } catch (Exception e) {
            XposedBridge.log(Constants.TAG + " Error disabling flashlight: " + e.getMessage());
        }
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
        return true;
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
