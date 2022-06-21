package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 开启后台服务接受通知
        startService(new Intent(MainActivity.this, BackgroundService.class));
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    1
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.MakeToast("Enter Main Activity", this);
        if (AppData.UserData != null)
        {
            GotoNaviActivity();
        }
        else
        {
            GotoLoginActivity();
        }
    }

    public void GotoNaviActivity()
    {
        Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
        startActivity(intent);
    }

    public void GotoLoginActivity()
    {
        Intent intent = new Intent(MainActivity.this, LogIn.class);
        startActivity(intent);
    }

    public void launchLogInActivity(View view){
        GotoLoginActivity();
    }
}