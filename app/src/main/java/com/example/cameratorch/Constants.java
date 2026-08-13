package com.example.cameratorch;

public class Constants {
    // 通知相关
    public static final String CHANNEL_ID = "camera_torch_channel";
    public static final String CHANNEL_NAME = "相机手电筒控制";
    public static final int NOTIFICATION_ID = 1001;

    // 广播动作
    public static final String ACTION_CLOSE_FLASHLIGHT = "ACTION_CLOSE_FLASHLIGHT";

    // 偏好设置键
    public static final String PREFS_NAME = "camera_torch_prefs";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_AUTO_CLOSE_CAMERA = "auto_close_camera";

    // 目标相机应用包名
    public static final String[] TARGET_CAMERA_APPS = {
        "com.android.camera",
        "com.xiaomi.camera",
        "com.xiaomi.camera.experimental",
        "com.miui.camera",
        "com.samsung.android.camera",
        "com.google.android.GoogleCamera",
        "com.oppo.camera",
        "com.vivo.camera",
        "com.huawei.camera",
        "com.android.camera2"
    };

    // 日志标签
    public static final String TAG = "CameraTorchModule";
}
