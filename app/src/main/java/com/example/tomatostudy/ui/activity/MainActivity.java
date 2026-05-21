package com.example.tomatostudy.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.tomatostudy.R;
import com.example.tomatostudy.ui.fragment.LockFocusFragment;
import com.example.tomatostudy.ui.fragment.MineFragment;
import com.example.tomatostudy.ui.fragment.StatisticsFragment;
import com.example.tomatostudy.ui.fragment.TaskCollectionFragment;
import com.example.tomatostudy.ui.fragment.TaskFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_task) {
                switchMainFragment(new TaskFragment());
                return true;
            } else if (itemId == R.id.nav_collection) {
                switchMainFragment(new TaskCollectionFragment());
                return true;
            } else if (itemId == R.id.nav_lock_focus) {
                switchMainFragment(new LockFocusFragment());
                return true;
            } else if (itemId == R.id.nav_statistics) {
                switchMainFragment(new StatisticsFragment());
                return true;
            } else if (itemId == R.id.nav_mine) {
                switchMainFragment(new MineFragment());
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_task);
        }
    }

    private void switchMainFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragmentContainer, fragment)
                .commit();
    }
}
