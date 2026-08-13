package com.example.cameratorch;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置标题
        setTitle("相机手电筒控制器设置");

        // 加载偏好设置Fragment
        getSupportFragmentManager().beginTransaction()
            .replace(android.R.id.content, new SettingsFragment())
            .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private SwitchPreferenceCompat enabledPref;
        private SwitchPreferenceCompat autoClosePref;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            // 获取偏好设置项
            enabledPref = findPreference(Constants.KEY_ENABLED);
            autoClosePref = findPreference(Constants.KEY_AUTO_CLOSE_CAMERA);

            // 设置监听器
            setupPreferenceListeners();
        }

        private void setupPreferenceListeners() {
            enabledPref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean enabled = (Boolean) newValue;
                // 更新摘要
                enabledPref.setSummary(enabled ? "已启用" : "已禁用");
                return true;
            });

            autoClosePref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean autoClose = (Boolean) newValue;
                // 更新摘要
                autoClosePref.setSummary(autoClose ? "关闭手电筒时将同时退出相机" : "仅关闭手电筒");
                return true;
            });

            // 初始化摘要
            updateSummaries();
        }

        private void updateSummaries() {
            enabledPref.setSummary(enabledPref.isChecked() ? "已启用" : "已禁用");
            autoClosePref.setSummary(autoClosePref.isChecked() ?
                "关闭手电筒时将同时退出相机" : "仅关闭手电筒");
        }
    }
}
