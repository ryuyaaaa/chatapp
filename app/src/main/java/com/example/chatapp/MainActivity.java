package com.example.chatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            transaction = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_friends:
                    Fragment friendsFragment = new FriendsFragment();
                    transaction.replace(R.id.main_fragment, friendsFragment);
//                    transaction.addToBackStack(null);
                    transaction.commit();
                    return true;
                case R.id.navigation_talks:
                    Fragment talksFragment = new TalksFragment();
                    transaction.replace(R.id.main_fragment, talksFragment);
//                    transaction.addToBackStack(null);
                    transaction.commit();
                    return true;
                case R.id.navigation_settings:
                    Fragment settingsFragment = new SettingsFragment();
                    transaction.replace(R.id.main_fragment, settingsFragment);
                    transaction.commit();
                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences("UserInfo", MODE_PRIVATE);
        System.out.println("Uid = " + pref.getString("Uid", "0"));

        if (pref.getString("Uid", "0").equals("0")) {
            loadLoginActivity();
        } else {
            System.out.println("ログインに成功");
        }

        fragmentManager = getSupportFragmentManager();

        // Fragmentを作成
        FriendsFragment friendsFragment = new FriendsFragment();

        // Fragmentの追加や削除といった変更を行う際は、Transactionを利用
        transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.main_fragment, friendsFragment);
        transaction.commit();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    public void loadLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
