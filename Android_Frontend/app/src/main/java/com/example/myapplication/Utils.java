package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.loader.content.CursorLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.reflect.Method;
import java.util.TreeMap;
import java.util.UUID;

public class Utils {

    final public static String WebSite = "http://183.172.233.60:8000/";

    public static String GetApiUrlByName(String ApiName)
    {
        return WebSite + ApiName;
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        static TreeMap<String, Bitmap> bitMapCache;
        static
        {
            bitMapCache = new TreeMap<String, Bitmap>();
        }
        public synchronized void addCache(String url, Bitmap bitmap)
        {
            if(!bitMapCache.containsKey(url))
            {
                bitMapCache.put(url, bitmap);
            }
        }

        public synchronized  Bitmap getCache(String url)
        {
            if(bitMapCache.containsKey(url))
            {
                return bitMapCache.get(url);
            }
            return null;
        }

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            mIcon11 = getCache(urldisplay);
            if(mIcon11 != null)
            {
                return mIcon11;
            }
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                in.close();
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            addCache(urldisplay, mIcon11);
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            //if(bmImage.getContext().)
            bmImage.setImageBitmap(result);
        }
    }

    static void loadImageFromNet(ImageView image, String url)
    {
        new DownloadImageTask(image).execute(url);
    }

    static void MakeToast(String toastString, Activity cntActivity)
    {
        Context context = cntActivity.getApplicationContext();
        CharSequence text = toastString;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public static interface JsonPostCallback
    {
        void call(JSONObject object);
    }

    static void SendJsonPost(String urlString, JSONObject jsonData, Activity cntActivity, JsonPostCallback callback)
    {
        // 要在线程中做, 回调要在主线程更新UI
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonData.toString());
                    os.flush();
                    os.close();
                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG", conn.getResponseMessage());
                    JSONObject resultObejct = null;
                    if (200 == conn.getResponseCode()) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String resultString = br.readLine();
                        Log.i("ResponseBody", resultString);
                        resultObejct = new JSONObject(resultString);
                    }
                    conn.disconnect();
                    callback.call(resultObejct);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.call(null);
                }
            }
        }.start();
    }
    // 参考 https://www.programming-books.io/essential/android/upload-post-file-using-httpurlconnection-4b647c18f1ab42679a23212cbdb7047d
    static void SendFilePost(String urlString, JSONObject jsonData, Activity cntActivity, JsonPostCallback callback, String filePath, byte [] fileData)
    {
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                try {
                    String boundary = UUID.randomUUID().toString();
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("Accept", "application/json");
                    DataOutputStream request = new DataOutputStream(conn.getOutputStream());
                    request.writeBytes("--" + boundary + "\r\n");
                    request.writeBytes("Content-Disposition: form-data; name=\"json_data\"; filename=\"" + "json_data" + "\"\r\n\r\n");
                    request.writeBytes(jsonData.toString() + "\r\n");
                    request.writeBytes("--" + boundary + "\r\n");
                    request.writeBytes("Content-Disposition: form-data; name=\"upload_file\"; filename=\"" + filePath + "\"\r\n\r\n");
                    byte[] bytes = null;
                    if(fileData == null)
                    {
                        File file = new File(filePath);
                        int size = (int) file.length();
                        bytes = new byte[size];
                        try {
                            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                            buf.read(bytes, 0, bytes.length);
                            buf.close();
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        bytes = fileData;
                    }

                    Log.w("upload file", "read file length " + bytes.length);
                    request.write(bytes);
                    request.writeBytes("\r\n");
                    request.writeBytes("--" + boundary + "--\r\n");
                    request.flush();
                    request.close();
                    JSONObject resultObejct = null;
                    if (200 == conn.getResponseCode()) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String resultString = br.readLine();
                        Log.i("ResponseBody", resultString);
                        resultObejct = new JSONObject(resultString);
                    }
                    conn.disconnect();
                    callback.call(resultObejct);

                } catch (Exception e) {
                        e.printStackTrace();
                        callback.call(null);
                }
            }
        }.start();
    }

    @SuppressLint("Range")
    static public String getPath(Context context, Uri uri) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        if (cursor.moveToFirst()) {
            try {
                // Get columns name by URI type.
                String columnName = MediaStore.Images.Media.DATA;
                if( uri==MediaStore.Images.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Images.Media.DATA;
                }else if( uri==MediaStore.Audio.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Audio.Media.DATA;
                }else if( uri==MediaStore.Video.Media.EXTERNAL_CONTENT_URI )
                {
                    columnName = MediaStore.Video.Media.DATA;
                }
                int imageColumnIndex = cursor.getColumnIndex(columnName);
                path = cursor.getString(imageColumnIndex);
            } catch (Exception e) {
                cursor.close();
                return getAudioPath(context, uri);
            }


        }
        cursor.close();
        return path;
    }
    @SuppressLint("Range")
    static public String getAudioPath(Context context, Uri uri) {
        String path = null;
        String[] projection = { MediaStore.Audio.AudioColumns.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if(cursor == null){
            path = uri.getPath();
        }
        else{
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
            path = cursor.getString(column_index);
            cursor.close();
        }

        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }

    static public boolean isUserBlackListed(String userEmail)
    {
        try {
            JSONArray blackList = AppData.UserData.getJSONArray("black_list");
            for(int i = 0; i < blackList.length(); i ++)
            {
                String follower_id = blackList.getString(i);
                if(follower_id.equals(userEmail))
                {
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    static public  boolean isUserFollowed(String userEmail)
    {
        try {
            JSONArray followList = AppData.UserData.getJSONArray("outlinks");
            for(int i = 0; i < followList.length(); i ++)
            {
                String follower_id = followList.getString(i);
                if(follower_id.equals(userEmail))
                {
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

}
