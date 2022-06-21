package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.json.JSONException;

public class NavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
    }

    public void onClickPersonal(View view)
    {
        try {
            String my_email = AppData.UserData.getString("user_email");
            PersonalActivity.enterPersonalByEmail(my_email, this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onClickUserInfo(View view)
    {
        Intent intent = new Intent(NavigationActivity.this, UserInfoActivity.class);
        startActivity(intent);
    }

    public void onClickPostActive(View view)
    {
        Intent intent = new Intent(NavigationActivity.this, PostActiveActivity.class);
        startActivity(intent);
    }

    public void onClickShowAllActive(View view)
    {
        Intent intent = new Intent(NavigationActivity.this, AllActiveActivity.class);
        startActivity(intent);
    }

}