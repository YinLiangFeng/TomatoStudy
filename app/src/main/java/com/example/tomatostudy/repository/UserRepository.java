package com.example.tomatostudy.repository;

import android.content.Context;

import com.example.tomatostudy.database.dao.SettingsDao;
import com.example.tomatostudy.database.dao.UserDao;
import com.example.tomatostudy.database.model.Settings;
import com.example.tomatostudy.database.model.User;

public class UserRepository {

    private final UserDao userDao;
    private final SettingsDao settingsDao;

    public UserRepository(Context context) {
        userDao = new UserDao(context);
        settingsDao = new SettingsDao(context);
    }

    public boolean register(String username, String password, String nickname) {
        if (isEmpty(username) || isEmpty(password)) {
            return false;
        }
        if (userDao.findByUsername(username) != null) {
            return false;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(isEmpty(nickname) ? username : nickname);
        user.setLogin(false);
        user.setCreatedTime(System.currentTimeMillis());

        // 注册时只创建本地用户，不自动登录，后续由登录流程统一保存登录状态。
        long userId = userDao.insertUser(user);
        if (userId <= 0) {
            return false;
        }

        Settings settings = new Settings();
        settings.setUserId((int) userId);
        settingsDao.insertSettings(settings);
        return true;
    }

    public boolean login(String username, String password) {
        if (isEmpty(username) || isEmpty(password)) {
            return false;
        }

        User user = userDao.findByUsernameAndPassword(username, password);
        if (user == null) {
            return false;
        }

        // 登录成功后先清除旧登录用户，再把当前用户标记为已登录，保证本地只有一个当前用户。
        saveLoginStatus(user.getId());
        return true;
    }

    public void saveLoginStatus(int userId) {
        // 保存登录状态时统一先清空，避免多个账号同时处于 is_login=1。
        userDao.clearLoginStatus();
        userDao.updateLoginStatus(userId, true);
    }

    public void logout() {
        userDao.clearLoginStatus();
    }

    public User getCurrentUser() {
        return userDao.findLoginUser();
    }

    public boolean updateProfile(User user) {
        return userDao.updateProfile(user) > 0;
    }

    private boolean isEmpty(String text) {
        return text == null || text.trim().length() == 0;
    }
}
