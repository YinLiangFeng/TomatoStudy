package com.example.tomatostudy.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tomatostudy.R;
import com.example.tomatostudy.ui.activity.LoginActivity;
import com.example.tomatostudy.viewmodel.LoginViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginFragment extends Fragment {

    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private LoginViewModel loginViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usernameEditText = view.findViewById(R.id.loginUsernameEditText);
        passwordEditText = view.findViewById(R.id.loginPasswordEditText);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        MaterialButton loginButton = view.findViewById(R.id.loginButton);
        TextView registerText = view.findViewById(R.id.goRegisterText);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLoginClick();
            }
        });

        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof LoginActivity) {
                    ((LoginActivity) getActivity()).showRegisterFragment();
                }
            }
        });
    }

    private void handleLoginClick() {
        String username = getText(usernameEditText);
        String password = getText(passwordEditText);
        if (username.length() == 0 || password.length() == 0) {
            Toast.makeText(requireContext(), R.string.login_empty_tip, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = loginViewModel.login(username, password);
        if (success) {
            Toast.makeText(requireContext(), R.string.login_success_tip, Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof LoginActivity) {
                ((LoginActivity) getActivity()).goToMainActivity();
            }
        } else {
            Toast.makeText(requireContext(), R.string.login_fail_tip, Toast.LENGTH_SHORT).show();
        }
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}
