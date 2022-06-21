package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

public class AllActiveActivity extends AppCompatActivity {
    int filterType = -1;
    int sortType = -1;
    int posterType = -1;
    // 页面更新中的话就不要反复请求
    boolean updating = false;
    // 发到网络上的时候radio和值的映射关系
    TreeMap<Integer, Integer> filterTypeMap;
    TreeMap<Integer, Integer> sortTypeMap;
    TreeMap<Integer, Integer> posterTypeMap;

    void initMap()

    {
        filterTypeMap = new TreeMap<Integer, Integer>();
        sortTypeMap = new TreeMap<Integer, Integer>();
        posterTypeMap = new TreeMap<Integer, Integer>();
        filterTypeMap.put(R.id.radio_type_filter_all, -1);
        filterTypeMap.put(R.id.radio_type_filter_text, 0);
        filterTypeMap.put(R.id.radio_type_filter_image, 1);
        filterTypeMap.put(R.id.radio_type_filter_video, 2);
        filterTypeMap.put(R.id.radio_type_filter_audio, 3);
        sortTypeMap.put(R.id.radio_active_sort_time, 0);
        sortTypeMap.put(R.id.radio_active_sort_like, 1);
        posterTypeMap.put(R.id.radio_active_poster_all, 0);
        posterTypeMap.put(R.id.radio_active_poster_follow, 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_active);
        initMap();
        initHeader();
    }

    @Override
    public void onBackPressed(){
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("ddd", "destroy all active");
        LinearLayout activeContainer = findViewById(R.id.active_container_layout);
        activeContainer.removeAllViews();
        System.gc();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateContent();
    }

    class UpdateContentCallback implements Utils.JsonPostCallback{

        @Override
        public void call(JSONObject object) {
            try {
                AllActiveActivity.this.updating = false;
                if (object != null && object.getInt("result") == 0) {
                    JSONArray activesList = object.getJSONArray("actives_list");
                    AllActiveActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshActivesView(activesList);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    void refreshActivesView(JSONArray activesList)
    {
        try {
            LinearLayout activeContainer = findViewById(R.id.active_container_layout);
            activeContainer.removeAllViews();
            LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for(int i = 0; i < activesList.length(); i ++)
            {
                JSONObject activeData = activesList.getJSONObject(i);
                View newActiveView = inflater.inflate(R.layout.active_display_template, null, false);
                activeContainer.addView(newActiveView);
                ActiveUtils.fillActive(newActiveView, activeData);
            }

            ScrollView activesScrollView = findViewById(R.id.all_active_scroll_view);
            // oncreate中调用跳转到 头不生效, 得用这个post
            activesScrollView.post(new Runnable() {
                @Override
                public void run() {
                    activesScrollView.scrollTo(0, 0);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    final static String SHOW_ALL_ACTIVE_URL = "clients/show_all_active";
    void updateContent()
    {
        if(updating)
        {
            return;
        }
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put("user_email", AppData.UserData.getString("user_email"));
            reqObj.put("active_type", filterTypeMap.get(filterType).intValue());
            EditText titleEdit = findViewById(R.id.edit_active_search_title);
            EditText userEdit = findViewById(R.id.edit_active_search_user_name);
            EditText textEdit = findViewById(R.id.edit_active_search_content);
            reqObj.put("title", titleEdit.getText().toString());
            reqObj.put("poster_name", userEdit.getText().toString());
            reqObj.put("active_text", textEdit.getText().toString());
            reqObj.put("sort_type", sortTypeMap.get(sortType).intValue());
            reqObj.put("poster_type", posterTypeMap.get(posterType).intValue());
            String url = Utils.GetApiUrlByName(SHOW_ALL_ACTIVE_URL);
            Utils.SendJsonPost(url, reqObj, this, new UpdateContentCallback());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void initAllRadios()
    {
        RadioGroup filterTypeGroup = findViewById(R.id.radio_group_active_type_filter);
        RadioGroup posterTypeGroup = findViewById(R.id.radio_group_active_poster);
        RadioGroup sortTypeGroup = findViewById(R.id.radio_group_active_sort);
        filterTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                filterType = i;
            }
        });
        posterTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                posterType = i;
            }
        });

        sortTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                sortType = i;
            }
        });
        filterTypeGroup.check(R.id.radio_type_filter_all);
        posterTypeGroup.check(R.id.radio_active_poster_all);
        sortTypeGroup.check(R.id.radio_active_sort_time);
    }

    void initHeader()
    {
        initAllRadios();
        Button filterButton = findViewById(R.id.button_active_filter);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateContent();
            }
        });
    }

}