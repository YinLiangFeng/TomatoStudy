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

public class RegisterFragment extends Fragment {

    private TextInputEditText usernameEditText;
    private TextInputEditText nicknameEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private LoginViewModel loginViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usernameEditText = view.findViewById(R.id.registerUsernameEditText);
        nicknameEditText = view.findViewById(R.id.registerNicknameEditText);
        passwordEditText = view.findViewById(R.id.registerPasswordEditText);
        confirmPasswordEditText = view.findViewById(R.id.registerConfirmPasswordEditText);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        MaterialButton registerButton = view.findViewById(R.id.registerButton);
        TextView loginText = view.findViewById(R.id.goLoginText);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegisterClick();
            }
        });

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof LoginActivity) {
                    ((LoginActivity) getActivity()).showLoginFragment();
                }
            }
        });
    }

    private void handleRegisterClick() {
        String username = getText(usernameEditText);
        String password = getText(passwordEditText);
        String confirmPassword = getText(confirmPasswordEditText);
        if (username.length() == 0 || password.length() == 0 || confirmPassword.length() == 0) {
            Toast.makeText(requireContext(), R.string.register_empty_tip, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(requireContext(), R.string.password_not_same_tip, Toast.LENGTH_SHORT).show();
            return;
        }

        String nickname = getText(nicknameEditText);
        boolean success = loginViewModel.register(username, password, nickname);
        if (success) {
            Toast.makeText(requireContext(), R.string.register_success_tip, Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof LoginActivity) {
                ((LoginActivity) getActivity()).showLoginFragment();
            }
        } else {
            Toast.makeText(requireContext(), R.string.register_fail_tip, Toast.LENGTH_SHORT).show();
        }
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}
