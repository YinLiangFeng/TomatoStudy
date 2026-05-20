package com.example.tomatostudy.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tomatostudy.R;
import com.example.tomatostudy.database.model.User;
import com.example.tomatostudy.repository.UserRepository;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MILLIS = 600L;

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        userRepository = new UserRepository(this);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                checkLoginStatus();
            }
        }, SPLASH_DELAY_MILLIS);
    }

    private void checkLoginStatus() {
        // 启动页负责读取本地登录状态，避免用户每次打开 App 都重新登录。
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            // 没有已登录用户时进入登录页，后续第 8 步会在 LoginActivity 中承载登录和注册 Fragment。
            goToLogin();
        } else {
            // 找到 is_login=1 的用户时直接进入主页，让用户回到上次登录后的主流程。
            goToMain();
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
