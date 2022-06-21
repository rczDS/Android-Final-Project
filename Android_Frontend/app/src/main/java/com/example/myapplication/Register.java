package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class Register extends AppCompatActivity {
    static String registerApiName = "clients/create_user";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public boolean checkParamValid()
    {
        String strNewId = ((EditText) findViewById(R.id.newuserid_edittext)).getText().toString();
        String strNewPassword = ((EditText) findViewById(R.id.newpassword_edittext)).getText().toString();
        String strNewName = ((EditText) findViewById(R.id.nickname_edittext)).getText().toString();

        return strNewId.length() > 0 && strNewPassword.length() > 0 && strNewName.length() > 0;
    }

    public JSONObject GetJsonParam()
    {
        String strNewId = ((EditText) findViewById(R.id.newuserid_edittext)).getText().toString();
        String strNewPassword = ((EditText) findViewById(R.id.newpassword_edittext)).getText().toString();
        String strNewName = ((EditText) findViewById(R.id.nickname_edittext)).getText().toString();
        JSONObject retObj = new JSONObject();

        try {
            retObj.put("user_email", strNewId);
            retObj.put("user_name", strNewName);
            retObj.put("password", strNewPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retObj;
    }

    public void onRequestCallback(JSONObject resultObj)
    {
        Log.w("REQUEST CALLBACK", "onRequestCallback CALL");
        // 这里是网络线程的回调, 需要回到主线程执行。
        Activity cnt = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(resultObj == null)
                {
                    Utils.MakeToast("[Fail]Register JsonObj null", cnt);
                    return;
                }
                try {
                    int retCode = resultObj.getInt("result");
                    if(retCode == 0)
                    {
                        Utils.MakeToast("[OK]Register A NEW Account finish this activity", cnt);
                        finishActivity(0);
                    }
                    else
                    {
                        Utils.MakeToast("[Fail]register failed check ret code " + retCode, cnt);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onClickRegister(View view){
        Utils.MakeToast("Click Register Button",this);
        Utils.JsonPostCallback callback = new Utils.JsonPostCallback() {
            @Override
            public void call(JSONObject resultObj) {
                onRequestCallback(resultObj);
            }
        };
        if(checkParamValid())
        {
            JSONObject requestObj = GetJsonParam();
            String requestUrl = Utils.GetApiUrlByName(registerApiName);
            Utils.SendJsonPost(requestUrl, requestObj, this, callback);
        }
    }

}