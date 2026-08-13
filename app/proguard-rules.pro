# 保留Xposed相关类
-keep class de.robv.android.xposed.** { *; }
-keep class * extends de.robv.android.xposed.IXposedHookLoadPackage { *; }
-keep class * extends de.robv.android.xposed.IXposedHookZygoteInit { *; }
-keep class * extends de.robv.android.xposed.IXposedHookInitPackageContent { *; }

# 保留模块类
-keep class com.example.cameratorch.** { *; }

# 保留反射调用的类
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# 移除日志（发布时）
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
