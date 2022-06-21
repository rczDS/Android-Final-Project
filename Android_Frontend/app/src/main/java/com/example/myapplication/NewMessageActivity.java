package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

public class NewMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);
    }
    @Override
    public  void onResume()
    {
        super.onResume();
        LinearLayout layout = findViewById(R.id.new_message_layout);
        layout.removeAllViews();
        try {
            JSONArray newMessages = AppData.UserData.getJSONArray("messages");
            int size = newMessages.length();
            for(int i = 0; i < size; i ++)
            {
                String newMessageStr = newMessages.getString(size - 1 - i);
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View newView = inflater.inflate(R.layout.new_message_template, null, false);
                TextView newMessageView = newView.findViewById(R.id.text_new_message_display);
                newMessageView.setText(newMessageStr);
                layout.addView(newView);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}