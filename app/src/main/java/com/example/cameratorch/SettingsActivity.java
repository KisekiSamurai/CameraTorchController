package com.example.cameratorch;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置标题
        setTitle("相机手电筒控制器设置");

        // 加载偏好设置Fragment
        getFragmentManager().beginTransaction()
            .replace(android.R.id.content, new SettingsFragment())
            .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        private SwitchPreference enabledPref;
        private SwitchPreference autoClosePref;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            // 获取偏好设置项
            enabledPref = (SwitchPreference) findPreference(Constants.KEY_ENABLED);
            autoClosePref = (SwitchPreference) findPreference(Constants.KEY_AUTO_CLOSE_CAMERA);

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
