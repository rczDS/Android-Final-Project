package com.example.myapplication;
import android.media.MediaPlayer;
import android.media.AudioManager;
import org.json.JSONObject;

public class AppData {
    static JSONObject UserData;
    static MediaPlayer globalMediaPlayer;
    //static MediaPlayer globalVideoPlayer;
    static
    {
        UserData = null;
        globalMediaPlayer = new MediaPlayer();
        globalMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //globalVideoPlayer = new MediaPlayer();
    }
}
