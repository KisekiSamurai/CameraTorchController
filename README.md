# CameraTorchController

一个Lsposed模块，实现相机启动时自动开启手电筒，通知栏一键关闭功能。

## ✨ 功能特性

- **自动开启手电筒**：当用户启动相机应用时，自动开启手电筒并保持常亮
- **通知栏控制**：显示持久通知，包含"关闭手电筒"按钮
- **一键关闭**：用户点击通知按钮即可关闭手电筒
- **自动清理**：手电筒关闭后通知自动消失
- **可配置设置**：支持启用/禁用模块、自动关闭相机等选项

## 📱 支持的相机应用

- 原生相机 (com.android.camera)
- 小米相机 (com.xiaomi.camera)
- 三星相机 (com.samsung.android.camera)
- Google Camera (com.google.android.GoogleCamera)
- OPPO相机 (com.oppo.camera)
- vivo相机 (com.vivo.camera)
- 华为相机 (com.huawei.camera)

## 🛠️ 开发环境

- Android Studio Arctic Fox (2020.3.1) 或更高版本
- JDK 8 或更高版本
- Android SDK 33
- Lsposed框架（安装在测试设备上）

## 📦 安装说明

1. 下载APK文件
2. 安装到设备
3. 打开Lsposed Manager
4. 进入 `模块` → 找到 `CameraTorchController`
5. 启用模块并选择作用域（勾选目标相机应用）
6. 重启设备或重启目标应用

## 🔧 构建APK

```bash
# 克隆项目
git clone https://github.com/yourusername/CameraTorchController.git

# 进入项目目录
cd CameraTorchController

# 构建APK
./gradlew assembleDebug

# APK输出路径
# app/build/outputs/apk/debug/app-debug.apk
```

## ⚙️ 配置选项

- **启用模块**：开启后相机启动时自动开启手电筒
- **关闭手电筒时同时关闭相机**：点击通知关闭手电筒时，同时退出相机应用
- **手电筒模式**：常亮模式、闪烁模式、定时模式
- **自动关闭延迟**：设置手电筒自动关闭的延迟时间（秒）

## 🐛 调试

```bash
# 查看模块日志
adb logcat | grep CameraTorchModule

# 查看Xposed相关日志
adb logcat | grep Xposed
```

## 📄 许可证

GPL-3.0 License

## 🔗 相关链接

- [Lsposed官方文档](https://lsposed.org/)
- [Xposed框架文档](https://api.xposed.info/)
- [Android Camera2 API文档](https://developer.android.com/media/camera/camera2)
