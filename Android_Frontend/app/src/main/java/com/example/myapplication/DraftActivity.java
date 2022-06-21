package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class DraftActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft);
    }
    @Override
    public void onResume() {

        super.onResume();
        initDraftList();
    }

    public void postActiveByDraftId(String draftId)
    {
        Intent intent = new Intent(DraftActivity.this, PostActiveActivity.class);
        intent.putExtra("draft_id", draftId);
        startActivity(intent);
    }

    public void initDraftList()
    {
        try {
            LinearLayout draftContainer = findViewById(R.id.draft_layout_container);
            draftContainer.removeAllViews();
            JSONObject draftDataDict = AppData.UserData.getJSONObject("draft_dict");
            Iterator<String> iter = draftDataDict.keys();
            while(iter.hasNext()) {
                String key = iter.next();
                JSONObject draftData = draftDataDict.getJSONObject(key);
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View newDraftView = inflater.inflate(R.layout.draft_template, null, false);
                TextView textDraftTitle = newDraftView.findViewById(R.id.text_draft_title);
                TextView textDraftId = newDraftView.findViewById(R.id.text_draft_id);
                Button buttonEditDraft = newDraftView.findViewById(R.id.button_edit_draft);
                textDraftTitle.setText(draftData.getString("title"));
                final String strDraftId = draftData.getString("draft_id");
                textDraftId.setText(strDraftId);
                buttonEditDraft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        postActiveByDraftId(strDraftId);
                    }
                });
                draftContainer.addView(newDraftView);
            }
        }catch (JSONException e)
        {
            e.printStackTrace();
        }


    }
}