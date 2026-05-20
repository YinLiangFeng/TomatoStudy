package com.example.tomatostudy.repository;

import android.content.Context;

import com.example.tomatostudy.database.dao.SettingsDao;
import com.example.tomatostudy.database.model.Settings;

public class SettingsRepository {

    private final SettingsDao settingsDao;

    public SettingsRepository(Context context) {
        settingsDao = new SettingsDao(context);
    }

    public Settings loadSettings(int userId) {
        // 读取番茄钟设置时，如果用户还没有设置记录，就创建一条默认设置，保证页面总能显示可用配置。
        Settings settings = settingsDao.findByUserId(userId);
        if (settings == null) {
            settings = new Settings();
            settings.setUserId(userId);
            settingsDao.insertSettings(settings);
        }
        return settings;
    }

    public void saveSettings(Settings settings) {
        // 保存设置时由 DAO 决定新增还是更新，页面层不需要关心数据库中是否已有记录。
        settingsDao.saveSettings(settings);
    }

    public void savePomodoroSetting(int userId, int focusMinutes, int restMinutes) {
        Settings settings = loadSettings(userId);
        settings.setDefaultFocusMinutes(focusMinutes);
        settings.setDefaultRestMinutes(restMinutes);
        saveSettings(settings);
    }
}
