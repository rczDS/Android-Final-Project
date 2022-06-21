package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Person;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PersonalActivity extends AppCompatActivity {
    private JSONObject dataObject = null;
    private boolean isSelf = false;
    private boolean isFollow = false;
    private boolean isBlacklist = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
        initData(null);
        initView();
    }


    @Override
    public void onBackPressed(){
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("ddd", "destroy personal");
        LinearLayout activeContainer = findViewById(R.id.personal_activity_layout);
        activeContainer.removeAllViews();
        System.gc();
    }

    boolean isUserFollowed()
    {
        try {
            JSONArray followList = AppData.UserData.getJSONArray("outlinks");
            String activity_useremail = dataObject.getJSONObject("user_info").getString("user_email");
            return Utils.isUserFollowed(activity_useremail);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean isUserBlackListed()
    {

        try {
            JSONArray blackList = AppData.UserData.getJSONArray("black_list");
            String activity_useremail = dataObject.getJSONObject("user_info").getString("user_email");
            return Utils.isUserBlackListed(activity_useremail);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initFollowerList()
    {
        try {
            GridView view = findViewById(R.id.list_follower);
            JSONArray outlinksData = dataObject.getJSONArray("outlinks");
            FollowerListAdapter followerAdapter = new FollowerListAdapter(outlinksData, this);
            view.setAdapter(followerAdapter);
        }catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void initView()
    {
        initUserInfo();
        initFollowerList();
        initActivesList();
    }

    private void initActivesList()
    {
        try {
            LinearLayout activeContainer = findViewById(R.id.personal_activity_layout);
            activeContainer.removeAllViews();
            LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            JSONArray activeListData = null;
            activeListData = dataObject.getJSONArray("actives_list");

            for(int i = 0; i < activeListData.length(); i ++)
            {
                JSONObject activeData = activeListData.getJSONObject(i);
                View newActiveView = inflater.inflate(R.layout.active_display_template, null, false);
                activeContainer.addView(newActiveView);
                ActiveUtils.fillActive(newActiveView, activeData);
            }

            ScrollView activesScrollView = findViewById(R.id.personal_actives_scroll);
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

    static class FollowBlacklistCallback implements Utils.JsonPostCallback
    {
        Activity cc;
        public FollowBlacklistCallback(Activity c)
        {
            cc = c;
        }
        @Override
        public void call(JSONObject object) {
            try {
                if (object != null && object.getInt("result") == 0) {
                    AppData.UserData = object.getJSONObject("user_info");

                    PersonalActivity c = (PersonalActivity) cc;
                    c.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            c.initData(null);
                            c.initUserInfo();
                        }
                    });

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    static String FOLLOW_USER = "clients/follow_user";
    static String UNFOLLOW_USER = "clients/unfollow_user";
    static String BLACKLIST_USER = "clients/blacklist_user";
    static String UNBLACKLIST_USER = "clients/unblacklist_user";
    public void gotoDraft()
    {
        Intent intent = new Intent(PersonalActivity.this, DraftActivity.class);
        startActivity(intent);
    }
    public void gotoNewMessage()
    {
        Intent intent = new Intent(PersonalActivity.this, NewMessageActivity.class);
        startActivity(intent);
    }

    public void initUserInfo()
    {
        try {
            ImageView userPhoto = findViewById(R.id.user_photo);
            Button followButton = findViewById(R.id.button_follow);
            Button blacklistButton = findViewById(R.id.button_black_list);
            Button draftButton = findViewById(R.id.button_draft);
            Button newMessageButton = findViewById(R.id.button_new_message);
            TextView userNameText = findViewById(R.id.user_name_text);
            String photoUrl = dataObject.getJSONObject("user_info").getString("photo");
            photoUrl = Utils.GetApiUrlByName(photoUrl);
            Utils.loadImageFromNet(userPhoto, photoUrl);
            FollowBlacklistCallback callback = new FollowBlacklistCallback(this);
            if(isSelf)
            {
                followButton.setVisibility(View.INVISIBLE);
                blacklistButton.setVisibility(View.INVISIBLE);
                draftButton.setVisibility((View.VISIBLE));
                newMessageButton.setVisibility((View.VISIBLE));
                draftButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PersonalActivity.this.finish();
                        gotoDraft();
                    }
                });
                newMessageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PersonalActivity.this.finish();
                        gotoNewMessage();
                    }
                });
            }
            else
            {
                followButton.setVisibility(View.VISIBLE);
                blacklistButton.setVisibility(View.VISIBLE);
                draftButton.setVisibility((View.INVISIBLE));
                newMessageButton.setVisibility((View.INVISIBLE));
                if(isFollow)
                {
                    followButton.setText("取关");
                }
                else{
                    followButton.setText("关注");
                }

                if(isBlacklist)
                {
                    blacklistButton.setText("取消拉黑");
                }
                else{
                    blacklistButton.setText("拉黑");
                }

                followButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            JSONObject reqObj = new JSONObject();
                            reqObj.put("from_email", AppData.UserData.getString("user_email"));
                            reqObj.put("to_email", dataObject.getJSONObject("user_info").getString("user_email"));
                            String url = FOLLOW_USER;
                            if(isFollow)
                            {
                                url = UNFOLLOW_USER;
                            }
                            url = Utils.GetApiUrlByName(url);
                            Utils.SendJsonPost(url, reqObj, PersonalActivity.this, callback);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                blacklistButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            JSONObject reqObj = new JSONObject();
                            reqObj.put("from_email", AppData.UserData.getString("user_email"));
                            reqObj.put("to_email", dataObject.getJSONObject("user_info").getString("user_email"));
                            String url = BLACKLIST_USER;
                            if(isBlacklist)
                            {
                                url = UNBLACKLIST_USER;
                            }
                            url = Utils.GetApiUrlByName(url);
                            Utils.SendJsonPost(url, reqObj, PersonalActivity.this, callback);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            String userName = dataObject.getJSONObject("user_info").getString("user_name");
            userNameText.setText(userName);
            String userDesc = dataObject.getJSONObject("user_info").getString("desc");
            TextView userDescText = findViewById(R.id.text_personal_desc);
            userDescText.setText(userDesc);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void initData(JSONObject inputData)
    {

        try {
            if(inputData == null)
            {
                Intent intent = getIntent();
                String dataString = intent.getStringExtra("data");
                dataObject = new JSONObject(dataString);
            }
            else
            {
                dataObject = inputData;
            }
            //Utils.MakeToast("enter data string length is" + dataString.length(), this);
            String activity_useremail = dataObject.getJSONObject("user_info").getString("user_email");
            String app_useremail = AppData.UserData.getString("user_email");
            isSelf = (app_useremail.equals(activity_useremail));
            isBlacklist = isUserBlackListed();
            //Utils.MakeToast("app user email " + app_useremail + " activity user email " + activity_useremail + " " + isSelf, this);
            isFollow = isUserFollowed();
        } catch (JSONException e) {
            e.printStackTrace();
            // 进入失败
            this.finishActivity(-1);
        }
    }



    // 此类提供代码让外部进入
    // ==========================================
    final static String PERSONAL_REQUEST_URL = "clients/get_personal_info";
    static class PersonalCallback implements Utils.JsonPostCallback {
        private Activity activity;
        public PersonalCallback(Activity callInActivity)
        {
            activity = callInActivity;
        }
        @Override
        public void call(JSONObject object) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent intent = new Intent(activity, PersonalActivity.class);
                        if(object == null)
                        {
                            Utils.MakeToast("request personal fail", activity);
                            return;
                        }
                        else {

                            if(object.getInt("result") != 0)
                            {
                                Utils.MakeToast("personal email not exist", activity);
                            }
                        }
                        intent.putExtra("data", object.toString());
                        activity.startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    static void enterPersonalByEmail(String userEmail, Activity callInActivity)
    {
        JSONObject reqObj = new JSONObject();
        try {
            reqObj.put("user_email", userEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String requestUrl = Utils.GetApiUrlByName(PERSONAL_REQUEST_URL);
        Utils.SendJsonPost(requestUrl, reqObj, callInActivity, new PersonalCallback(callInActivity));
    }
    // ==========================================
}