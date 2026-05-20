package com.example.tomatostudy.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.tomatostudy.R;
import com.example.tomatostudy.ui.fragment.LoginFragment;
import com.example.tomatostudy.ui.fragment.RegisterFragment;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            showLoginFragment();
        }
        initBackPressHandler();
    }

    public void showLoginFragment() {
        replaceFragment(new LoginFragment());
    }

    public void showRegisterFragment() {
        replaceFragment(new RegisterFragment());
    }

    public void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.loginFragmentContainer, fragment)
                .commit();
    }

//    当用户按返回键时：
//
//    当前页面 = loginFragmentContainer 里正在显示的 Fragment
//
//    如果当前页面是 RegisterFragment：
//    切换回 LoginFragment
//
//    否则：
//    关闭自定义返回监听
//            执行系统默认返回
    private void initBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.loginFragmentContainer);
                if (currentFragment instanceof RegisterFragment) {
                    showLoginFragment();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
}
