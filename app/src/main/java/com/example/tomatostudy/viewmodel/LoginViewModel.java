package com.example.tomatostudy.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.tomatostudy.database.model.User;
import com.example.tomatostudy.repository.UserRepository;

public class LoginViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public boolean register(String username, String password, String nickname) {
        // 注册时先交给 Repository 检查用户名是否重复，再把新用户保存到本地 SQLite。
        return userRepository.register(username, password, nickname);
    }

    public boolean login(String username, String password) {
        // 登录时根据用户名和密码查询本地用户，成功后由 Repository 保存当前登录状态。
        return userRepository.login(username, password);
    }

    public void logout() {
        userRepository.logout();
    }

    public User getCurrentUser() {
        return userRepository.getCurrentUser();
    }
}
