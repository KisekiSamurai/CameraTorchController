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
import android.content.Intent;

public class CameraTorchModule implements IXposedHookLoadPackage {

    private static CameraManager cameraManager;
    private static String currentCameraId;
    private static boolean torchEnabled = false;
    private static boolean hookedOpenCamera = false;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log(Constants.TAG + " ========== Module loaded ==========");
        XposedBridge.log(Constants.TAG + " Package: " + lpparam.packageName);

        // 只Hook目标相机应用
        if (!isTargetCameraApp(lpparam.packageName)) {
            XposedBridge.log(Constants.TAG + " Not a target camera app, skipping");
            return;
        }

        XposedBridge.log(Constants.TAG + " ✓ Target camera app detected, hooking...");

        try {
            // 方法1: Hook CameraManager.openCamera
            hookCameraManagerOpenCamera(lpparam);
        } catch (Exception e) {
            XposedBridge.log(Constants.TAG + " Failed to hook openCamera: " + e.getMessage());
        }

        try {
            // 方法2: Hook Activity onResume
            hookActivityOnResume(lpparam);
        } catch (Exception e) {
            XposedBridge.log(Constants.TAG + " Failed to hook onResume: " + e.getMessage());
        }

        try {
            // 方法3: Hook Intent (相机启动)
            hookCameraIntent(lpparam);
        } catch (Exception e) {
            XposedBridge.log(Constants.TAG + " Failed to hook Intent: " + e.getMessage());
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

    // 方法1: Hook CameraManager.openCamera
    private void hookCameraManagerOpenCamera(XC_LoadPackage.LoadPackageParam lpparam) {
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
                    XposedBridge.log(Constants.TAG + " ★ openCamera called!");
                    String cameraId = (String) param.args[0];
                    handleCameraOpened(param.thisObject, cameraId);
                }
            }
        );
        XposedBridge.log(Constants.TAG + " ✓ Hooked CameraManager.openCamera");
    }

    // 方法2: Hook Activity onResume
    private void hookActivityOnResume(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
            "android.app.Activity",
            lpparam.classLoader,
            "onResume",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Activity activity = (Activity) param.thisObject;
                    String className = activity.getClass().getName();

                    // 只处理相机相关的Activity
                    if (className.contains("Camera") || className.contains("camera")) {
                        XposedBridge.log(Constants.TAG + " ★ Camera Activity resumed: " + className);
                        handleCameraActivityResumed();
                    }
                }
            }
        );
        XposedBridge.log(Constants.TAG + " ✓ Hooked Activity.onResume");
    }

    // 方法3: Hook Intent
    private void hookCameraIntent(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
            "android.app.Instrumentation",
            lpparam.classLoader,
            "execStartActivity",
            android.content.Context.class,
            android.os.IBinder.class,
            android.os.IBinder.class,
            android.app.Activity.class,
            android.content.Intent.class,
            android.os.Bundle.class,
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Intent intent = (Intent) param.args[4];
                    if (intent != null) {
                        String action = intent.getAction();
                        if ("android.media.action.IMAGE_CAPTURE".equals(action) ||
                            "android.media.action.STILL_IMAGE_CAMERA".equals(action) ||
                            "android.media.action.VIDEO_CAMERA".equals(action)) {
                            XposedBridge.log(Constants.TAG + " ★ Camera Intent detected: " + action);
                            handleCameraActivityResumed();
                        }
                    }
                }
            }
        );
        XposedBridge.log(Constants.TAG + " ✓ Hooked Instrumentation.execStartActivity");
    }

    // 处理相机打开事件
    private void handleCameraOpened(Object thisObject, String cameraId) {
        try {
            if (!isModuleEnabled()) {
                XposedBridge.log(Constants.TAG + " Module disabled");
                return;
            }

            XposedBridge.log(Constants.TAG + " Camera opened: " + cameraId);

            // 保存CameraManager实例
            cameraManager = (CameraManager) thisObject;
            currentCameraId = cameraId;

            // 延迟开启手电筒
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                enableTorch();
            }, 300);

        } catch (Exception e) {
            XposedBridge.log(Constants.TAG + " Error in handleCameraOpened: " + e.getMessage());
        }
    }

    // 处理相机Activity恢复事件
    private void handleCameraActivityResumed() {
        try {
            if (!isModuleEnabled()) {
                XposedBridge.log(Constants.TAG + " Module disabled");
                return;
            }

            XposedBridge.log(Constants.TAG + " Enabling flashlight...");

            // 延迟执行，确保相机已初始化
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                enableTorch();
            }, 500);

        } catch (Exception e) {
            XposedBridge.log(Constants.TAG + " Error: " + e.getMessage());
        }
    }

    // 开启手电筒
    private void enableTorch() {
        try {
            Context context = AndroidAppHelper.currentApplication();
            if (context == null) {
                XposedBridge.log(Constants.TAG + " Context is null");
                return;
            }

            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager == null) {
                XposedBridge.log(Constants.TAG + " CameraManager is null");
                return;
            }

            // 获取后置相机ID
            String[] cameraIds = cameraManager.getCameraIdList();
            XposedBridge.log(Constants.TAG + " Found " + cameraIds.length + " cameras");

            for (String id : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    currentCameraId = id;
                    Boolean flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);

                    XposedBridge.log(Constants.TAG + " Back camera: " + id + ", Flash: " + flashAvailable);

                    if (flashAvailable != null && flashAvailable) {
                        cameraManager.setTorchMode(currentCameraId, true);
                        torchEnabled = true;
                        XposedBridge.log(Constants.TAG + " ★★★ TORCH ENABLED ★★★");
                        showNotification();
                        return;
                    }
                }
            }

            XposedBridge.log(Constants.TAG + " No back camera with flash found");
        } catch (CameraAccessException e) {
            XposedBridge.log(Constants.TAG + " Camera access error: " + e.getMessage());
        } catch (SecurityException e) {
            XposedBridge.log(Constants.TAG + " Security error: " + e.getMessage());
        } catch (Exception e) {
            XposedBridge.log(Constants.TAG + " Error: " + e.getMessage());
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
            XposedBridge.log(Constants.TAG + " Error checking module: " + e.getMessage());
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
}
