package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class LogIn extends AppCompatActivity {
    final static String LOGIN_API_NAME = "clients/login_user";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

    }
    @Override
    public void onResume()
    {
        super.onResume();
        // 进入登录界面就清空用户信息先。
        AppData.UserData = null;
    }

    public void launchRegisterActivity(View view){
        Intent intent = new Intent(LogIn.this, Register.class);
        startActivity(intent);
    }

    public void LaunchNavigationActivity()
    {
        Intent intent = new Intent(LogIn.this, NavigationActivity.class);
        startActivity(intent);
    }

    public void onLoginResultCallback(JSONObject resultObj)
    {
        Activity cnt = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(resultObj == null)
                {
                    Utils.MakeToast("[Fail]login JsonObj null", cnt);
                    return;
                }
                try {
                    int retCode = resultObj.getInt("result");
                    if(retCode == 0)
                    {
                        Utils.MakeToast("[OK]login ok", cnt);
                        JSONObject userData = resultObj.getJSONObject("user_info");
                        AppData.UserData = userData;
                        if(userData != null)
                        {
                            LaunchNavigationActivity();
                        }
                    }
                    else
                    {
                        Utils.MakeToast("[Fail]login failed check ret code " + retCode, cnt);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onLogin(View view){
        /*
        userid_edittext = (EditText) findViewById(R.id.userid_edittext);
        password_edittext = (EditText) findViewById(R.id.password_edittext);
        String userid = userid_edittext.getText().toString();
        String password = password_edittext.getText().toString();
        Log.v("id is",userid+",password is"+password);
         */
        Utils.JsonPostCallback callback = new Utils.JsonPostCallback() {
            @Override
            public void call(JSONObject object) {
                onLoginResultCallback(object);
            }
        };
        if(!checkParamValid())
        {
            Utils.MakeToast("should input username and password to login", this);
            return;
        }
        JSONObject requestObj = GetJsonParam();
        String requestUrl = Utils.GetApiUrlByName(LOGIN_API_NAME);
        Utils.SendJsonPost(requestUrl, requestObj, this, callback);
    }

    public JSONObject GetJsonParam()
    {
        String strLoginId = ((EditText) findViewById(R.id.userid_edittext)).getText().toString();
        String strPassword = ((EditText) findViewById(R.id.password_edittext)).getText().toString();
        JSONObject retObj = new JSONObject();
        try {
            retObj.put("user_email", strLoginId);
            retObj.put("password", strPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retObj;
    }

    public boolean checkParamValid()
    {
        String strLoginId = ((EditText) findViewById(R.id.userid_edittext)).getText().toString();
        String strPassword = ((EditText) findViewById(R.id.password_edittext)).getText().toString();
        return strLoginId.length() > 0 && strPassword.length() > 0;
    }
}