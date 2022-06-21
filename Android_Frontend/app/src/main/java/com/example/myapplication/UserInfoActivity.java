package com.example.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
//import android.net.Uri;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class UserInfoActivity extends AppCompatActivity {

    String localImagePath = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        initInfo();
        initSubmitButton();
    }

    class UploadCallBack implements  Utils.JsonPostCallback
    {
        Activity cc;
        UploadCallBack(Activity c)
        {
            cc = c;
        }
        @Override
        public void call(JSONObject object) {
            try {
                if(object != null && object.getInt("result") == 0)
                {
                    AppData.UserData = object.getJSONObject("user_info");
                    cc.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.MakeToast("update user info ok", cc);
                            cc.setResult(0);
                            cc.finish();
                        }
                    });
                }
                else{
                    cc.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.MakeToast("update user info failed", cc);
                        }
                    });

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    final static String UPDATE_USER = "clients/update_user";
    public  void initSubmitButton()
    {
        Button submitButton = findViewById(R.id.button_upldate_user);
        EditText userNameEdit = findViewById(R.id.user_info_user_name_edit);
        EditText passwordEdit = findViewById(R.id.user_info_user_pass_edit);
        EditText descEdit = findViewById(R.id.user_info_desc_edit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    UploadCallBack callBack = new UploadCallBack(UserInfoActivity.this);
                    String username = userNameEdit.getText().toString();
                    String password = passwordEdit.getText().toString();
                    String desc = descEdit.getText().toString();
                    if(username.length() == 0 || password.length() == 0 || desc.length() == 0 )
                    {
                        Utils.MakeToast("input text should be null", UserInfoActivity.this);
                        return;
                    }
                    JSONObject reqObj = new JSONObject();
                    reqObj.put("user_name", username);
                    reqObj.put("password", password);
                    reqObj.put("desc", desc);
                    reqObj.put("user_email", AppData.UserData.getString("user_email"));
                    String reqUrl = Utils.GetApiUrlByName(UPDATE_USER);
                    if(localImagePath.length() > 0)
                    {
                        reqObj.put("photo", localImagePath);
                        Utils.SendFilePost(reqUrl, reqObj, UserInfoActivity.this, callBack, localImagePath, null);
                    } else{

                        Utils.SendJsonPost(reqUrl, reqObj, UserInfoActivity.this, callBack);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void initInfo()
    {
        try {
            EditText userNameEdit = findViewById(R.id.user_info_user_name_edit);
            EditText passwordEdit = findViewById(R.id.user_info_user_pass_edit);
            EditText descEdit = findViewById(R.id.user_info_desc_edit);
            userNameEdit.setText(AppData.UserData.getString("user_name"));
            passwordEdit.setText(AppData.UserData.getString("password"));
            descEdit.setText(AppData.UserData.getString("desc"));
            ImageView userPhoto = findViewById(R.id.image_user_info);
            String photoUrl = Utils.GetApiUrlByName(AppData.UserData.getString("photo"));
            Utils.loadImageFromNet(userPhoto, photoUrl);
            ActivityResultLauncher<Intent> gallaryResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            if (data != null) {
                                // 得到图片的全路径
                                Uri uri = data.getData();
                                userPhoto.setImageURI(uri);
                                Log.w(this.getClass().getName(), "Uri:" + String.valueOf(uri) + " " + uri.getPath());
                                localImagePath = Utils.getPath(UserInfoActivity.this, uri);
                                File f = new File(localImagePath);
                                Log.w(this.getClass().getName(), "file ?? " + f.getAbsolutePath() + " " + f.exists());
                            }
                        }
                    }
                });
            // 参考 https://bbs.huaweicloud.com/blogs/318306
            // https://stackoverflow.com/questions/62671106/onactivityresult-method-is-deprecated-what-is-the-alternative
            //https://blog.csdn.net/djzhao627/article/details/123269644
            userPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.MakeToast("click photo", UserInfoActivity.this);
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    gallaryResultLauncher.launch(intent);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}