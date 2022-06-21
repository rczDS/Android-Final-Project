package com.example.myapplication;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VideoDownloader {

    static Map<String, String> videoCached = new HashMap<String, String>();
    static Set<String> videoDownloading = new HashSet<String>();
    static synchronized boolean isDownloading(String url)
    {
        return videoDownloading.contains(url);
    }
    static synchronized boolean isCached(String url)
    {
        return videoCached.containsKey(url);
    }
    static void playVideo(VideoView view, String url)
    {
        String[] segments = url.split("/");
        String fileStr = segments[segments.length-1];
        if (isDownloading(url))
        {
            return;
        }
        if(!isCached(url))
        {
            videoDownloading.add(url);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String root = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            root = view.getContext().getApplicationContext().getExternalFilesDir("").getPath();
                        }
                        else{
                            root = Environment.getExternalStorageDirectory().toString();
                        }
                        root = view.getContext().getApplicationContext().getExternalFilesDir("").getPath();
                        File myDir = new File(root + "/saved_videos");
                        if (!myDir.exists()) {
                            myDir.mkdirs();
                        }
                        File videoFile = new File(myDir, fileStr);
                        if (videoFile.exists ())
                            videoFile.delete ();
                        InputStream in = new java.net.URL(url).openStream();
                        FileOutputStream out = new FileOutputStream(videoFile);
                        byte[] buf = new byte[8192];
                        int length;
                        while ((length = in.read(buf)) > 0) {
                            out.write(buf, 0, length);
                        }
                        in.close();
                        out.close();
                        videoCached.put(url, videoFile.getAbsolutePath());
                        videoDownloading.remove(url);
                        ((Activity)view.getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                view.setVideoPath(videoCached.get(url));
                                view.start();
                            }
                        });
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                        e.printStackTrace();
                        videoDownloading.remove(url);
                    }
                }
            }).start();


        }else
        {
            view.setVideoPath(videoCached.get(url));
            view.start();
        }



    }
}
