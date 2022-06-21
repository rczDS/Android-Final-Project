package com.example.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class PostActiveActivity extends AppCompatActivity implements LocationListener {
    int selectType = -1;
    byte[] selectBytes = null;
    String positionStr = "";
    Timer saveDraftTimer = null;
    Uri newFileUri = null;
    static String draftIdStr = "";
    static String POST_ACTIVE_URL = "clients/post_active";

    @Override
    public void onLocationChanged(Location location) {
        getCntPosition(location);
    }

    //
    // https://stackoverflow.com/questions/9409195/how-to-get-complete-address-from-latitude-and-longitude
    void getCntPosition(Location location) {
        if(location == null)
        {
            return;
        }
        if (location == null) {
            Utils.MakeToast("loc none", this);
            return;
        }
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (addresses.size() == 0) {
            return;
        }
        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();
        positionStr = country + " " + state + " " + city + " " + knownName;
        TextView positionText = findViewById(R.id.text_post_position);
        positionText.setText(positionStr);
    }

    static class PostActiveCallBack implements Utils.JsonPostCallback {
        Activity c;

        public PostActiveCallBack(Activity cc) {
            c = cc;
        }

        @Override
        public void call(JSONObject object) {
            try {
                if (object != null && object.getInt("result") == 0) {
                    AppData.UserData = object.getJSONObject("user_info");
                    c.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            c.finish();
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    ActivityResultLauncher<Intent> gallaryResultLauncher;
    ActivityResultLauncher<Intent> createNewResultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_active);



        RadioGroup typeGroup = findViewById(R.id.radio_group_active_type);
        TextView filePathDesc = findViewById(R.id.text_post_path_desc);
        TextView filePathText = findViewById(R.id.text_post_file_path);
        Button selectExist = findViewById(R.id.button_select_phone);
        Button createNew = findViewById(R.id.button_create_new);
        Button postActive = findViewById(R.id.button_active_post_send);
        EditText titleEdit = findViewById(R.id.edit_active_title);
        EditText contenEdit = findViewById(R.id.edit_active_content);

        createNewResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Log.v("camera", "new file uri is "+ newFileUri.getPath());
                            filePathText.setText(Utils.getPath(PostActiveActivity.this, newFileUri));
                        }
                    }
                });

        gallaryResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            if (data != null) {
                                // 得到资源的全路径
                                Uri uri = data.getData();
                                Log.w(this.getClass().getName(), "Uri:" + String.valueOf(uri) + " " + uri.getPath());
                                filePathText.setText(Utils.getPath(PostActiveActivity.this, uri));
                                // 音频文件的傻逼机制.
                                if(true || selectType == R.id.radio_post_audio)
                                {
                                    try {
                                        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                                        InputStream inputStream = PostActiveActivity.this.getContentResolver().openInputStream(uri);
                                        byte[] buffer = new byte[1024];
                                        int i = Integer.MAX_VALUE;
                                        while ((i = inputStream.read(buffer, 0, buffer.length)) > 0)
                                        {
                                            byteArrayStream.write(buffer, 0, i);
                                        }

                                        selectBytes = byteArrayStream.toByteArray();
                                        Log.v("audio", "read bytes" + selectBytes.length);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                });

        typeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.v("radio","RADIO GROUP CHANGED TO " + i);
                selectType = i;
                filePathText.setText("");
                selectBytes = null;
                if(selectType == R.id.radio_post_text)
                {
                    filePathDesc.setVisibility(View.INVISIBLE);
                    filePathText.setVisibility(View.INVISIBLE);
                    selectExist.setVisibility(View.INVISIBLE);
                    createNew.setVisibility(View.INVISIBLE);
                }
                else{
                    filePathDesc.setVisibility(View.VISIBLE);
                    filePathText.setVisibility(View.VISIBLE);
                    selectExist.setVisibility(View.VISIBLE);
                    createNew.setVisibility(View.VISIBLE);

                }
            }
        });
        selectExist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data_type = "";
                String action = Intent.ACTION_PICK;
                if(selectType == R.id.radio_post_image)
                {
                    data_type = "image/*";
                }else if (selectType == R.id.radio_post_video)
                {
                    data_type = "video/*";
                }else if(selectType == R.id.radio_post_audio)
                {
                    action = Intent.ACTION_GET_CONTENT;
                    data_type = "audio/*";
                }
                else{
                    return;
                }
                Intent intent = new Intent(action, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, data_type);
                gallaryResultLauncher.launch(intent);
            }
        });

        createNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionAndCamera();
            }
        });
        postActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titleStr = titleEdit.getText().toString();
                String contentStr = contenEdit.getText().toString();
                String filePath = filePathText.getText().toString();
                if(titleStr.length() == 0 || contentStr.length() == 0)
                {
                    return;
                }
                JSONObject reqObj = new JSONObject();
                PostActiveCallBack callback = new PostActiveCallBack(PostActiveActivity.this);
                try {
                    reqObj.put("poster_email", AppData.UserData.getString("user_email"));
                    reqObj.put("title", titleStr);
                    reqObj.put("active_text", contentStr);
                    if(positionStr.length() > 0)
                    {
                        reqObj.put("active_position", URLEncoder.encode(positionStr, "UTF-8"));
                    }
                    else
                    {
                        reqObj.put("active_position", "dummy position");
                    }

                    if(selectType == R.id.radio_post_image)
                    {
                        reqObj.put("active_type", 1);
                    }else if (selectType == R.id.radio_post_video)
                    {
                        reqObj.put("active_type", 2);
                    }else if(selectType == R.id.radio_post_audio)
                    {
                        reqObj.put("active_type", 3);
                        filePath = "temp_audio.aac";
                    }
                    else{
                        reqObj.put("active_type", 0);

                    }
                    reqObj.put("draft_id", draftIdStr);
                    String url = Utils.GetApiUrlByName(POST_ACTIVE_URL);
                    if(selectType == R.id.radio_post_text)
                    {
                        Utils.SendJsonPost(url, reqObj, PostActiveActivity.this, callback);
                    }
                    else{
                        reqObj.put("active_file_name", filePath);
                        Utils.SendFilePost(url, reqObj, PostActiveActivity.this, callback, filePath, selectBytes);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        // 默认文字
        typeGroup.check(R.id.radio_post_text);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                    Boolean fineLocationGranted = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                    }
                    Boolean coarseLocationGranted = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        coarseLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,false);
                    }
                    if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                        setLocationListener(locationManager);
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        // Only approximate location access granted.
                        setLocationListener(locationManager);
                    } else {
                        // No location access granted.
                    }
                });

            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
        else {
            setLocationListener(locationManager);
        }

        initDraftInfo();
    }

    @SuppressLint("MissingPermission")
    void setLocationListener(LocationManager locationManager)
    {
        int LOCATION_UPDATE_INTERVAL = 1000;
        String [] locationProviders = {LocationManager.GPS_PROVIDER, LocationManager.FUSED_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER};
        for(int i = 0; i < locationProviders.length; i ++ )
        {
            if (locationManager.isProviderEnabled(locationProviders[i])) {
            locationManager.requestLocationUpdates(locationProviders[i], LOCATION_UPDATE_INTERVAL, 0, this);
            }
        }

    }

    static final String SAVE_DRAFT_URL = "clients/save_draft";

    void checkTypeByDraft(int draft_type)
    {
        RadioGroup typeGroup = findViewById(R.id.radio_group_active_type);
        if(draft_type == 1)
        {
            typeGroup.check(R.id.radio_post_image);
        }else if (draft_type == 2)
        {
            typeGroup.check(R.id.radio_post_video);
        }else if(draft_type == 3)
        {
            typeGroup.check(R.id.radio_post_audio);
        }
        else{
            typeGroup.check(R.id.radio_post_text);

        }
    }

    void initDraftInfo()
    {
        EditText titleEdit = findViewById(R.id.edit_active_title);
        EditText contentEdit = findViewById(R.id.edit_active_content);
        try {
            if(getIntent().hasExtra("draft_id"))
            {
                draftIdStr = getIntent().getStringExtra("draft_id");
                JSONObject draftData = AppData.UserData.getJSONObject("draft_dict").getJSONObject(draftIdStr);
                int activeType = draftData.getInt("active_type");
                checkTypeByDraft(activeType);
                String activeText = draftData.getString("active_text");
                String title = draftData.getString("title");
                contentEdit.setText(activeText);
                titleEdit.setText(title);
            }
            else{
                Date t = new Date();
                draftIdStr = String.valueOf(t.getTime());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    int getActiveTypeBySelectType()
    {
        if(selectType == R.id.radio_post_image)
        {
            return 1;
        }else if (selectType == R.id.radio_post_video)
        {
            return 2;
        }else if(selectType == R.id.radio_post_audio)
        {
            return 3;
        }
        else{
            return 0;

        }
    }

    public void postDraft()
    {
        try {
            EditText titleEdit = findViewById(R.id.edit_active_title);
            EditText contenEdit = findViewById(R.id.edit_active_content);
            String url = Utils.GetApiUrlByName(SAVE_DRAFT_URL);
            JSONObject reqObj = new JSONObject();
            reqObj.put("active_type", getActiveTypeBySelectType());
            reqObj.put("draft_id", draftIdStr);
            reqObj.put("title", titleEdit.getText().toString());
            reqObj.put("active_text", contenEdit.getText().toString());
            reqObj.put("user_email", AppData.UserData.getString("user_email"));
            Utils.JsonPostCallback callback = new Utils.JsonPostCallback() {
                @Override
                public void call(JSONObject object) {
                    try {
                        if (object != null && object.getInt("result") == 0) {
                            AppData.UserData = object.getJSONObject("user_info");
                            PostActiveActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.MakeToast("10 秒自动保存草稿", PostActiveActivity.this);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            Utils.SendJsonPost(url, reqObj, this, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume()
    {
        Utils.MakeToast("onResume", PostActiveActivity.this);
        super.onResume();
        saveDraftTimer = new Timer();
        // https://blog.csdn.net/aiynmimi/article/details/53085066 定时上传草稿
        saveDraftTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                postDraft();
            }
        }, 3000, 10000);
    }

    @Override
    public void onPause() {
        Utils.MakeToast("onPause", PostActiveActivity.this);
        super.onPause();
        saveDraftTimer.cancel();
    }

    // 相机相关 https://juejin.cn/post/6844903944095793166

    // 申请相机权限的requestCode
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;

    /**
     * 检查权限并拍照。
     * 调用相机前先检查权限。
     */
    private void checkPermissionAndCamera() {
        Utils.MakeToast("checkPermissionAndCamera", this);
        int hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.CAMERA);
        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
            //有调起相机拍照。
            openCamera();
        } else {
            //没有权限，申请权限。
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CAMERA_REQUEST_CODE);
        }
    }
    // 是否是Android 10以上手机
    private boolean isAndroidQ = Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;
    /**
     * 处理权限申请的回调。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //允许权限，有调起相机拍照。
                openCamera();
            } else {
                //拒绝权限，弹出提示框。
                Toast.makeText(this, "拍照权限被拒绝", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 调起相机拍照
     */
    private void openCamera() {
        String action = "";
        boolean isVideo = false;
        if(selectType == R.id.radio_post_image)
        {

            action = MediaStore.ACTION_IMAGE_CAPTURE;
        }else if (selectType == R.id.radio_post_video)
        {
            isVideo = true;
            action = MediaStore.ACTION_VIDEO_CAPTURE;
        }else
        {
            Utils.MakeToast("not support", this);
            return;
        }

        Intent captureIntent = new Intent(action);
        // 判断是否有相机
        if (captureIntent.resolveActivity(getPackageManager()) != null) {
            //

            File photoFile = null;
            Uri photoUri = null;
            if (isAndroidQ) {
                // 适配android 10
                photoUri = createImageUri(isVideo);
            } else {
                try {
                    photoFile = createImageFile(isVideo);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (photoFile != null) {
                    //mCameraImagePath = photoFile.getAbsolutePath();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                        Log.w("provider", getPackageName() + ".fileprovider");
                        photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", photoFile);
                    } else {
                        photoUri = Uri.fromFile(photoFile);
                    }
                }
            }

            //mCameraUri = photoUri;
            newFileUri = photoUri;
            if (photoUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                createNewResultLauncher.launch(captureIntent);
            }

        }
    }

    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    private Uri createImageUri(boolean isVideo) {
        String status = Environment.getExternalStorageState();
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储

        if (status.equals(Environment.MEDIA_MOUNTED)) {
            if(isVideo)
            {
                return getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            }
            else{
                return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            }

        } else {
            if(isVideo)
            {
                return getContentResolver().insert(MediaStore.Video.Media.INTERNAL_CONTENT_URI, new ContentValues());
            }
            else{
                return getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new ContentValues());
            }

        }
    }

    /**
     * 创建保存图片的文件
     */
    private File createImageFile(boolean isVideo) throws IOException {
        String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = null;
        if(!isVideo) {
            storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }else
        {
            storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageName);
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        return tempFile;
    }


}