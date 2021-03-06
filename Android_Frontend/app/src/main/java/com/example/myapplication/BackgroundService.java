package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.service.autofill.UserData;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {

    private static Timer timer = new Timer();
    private Context ctx;
    public BackgroundService() {
    }

    public void onCreate()
    {
        super.onCreate();
        ctx = this;
        startService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startService()
    {
        Context context = getApplicationContext();
        CharSequence text = "start service";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        timer.schedule(new MainTask(), 0, 30000);
    }

    static String NEW_MESSAGE_URL = "clients/touch_new_message";
    public void postMessageRenew()
    {
        JSONObject reqObj = new JSONObject();
        try {
            reqObj.put("user_email", AppData.UserData.getString("user_email"));
            Utils.JsonPostCallback callback = new Utils.JsonPostCallback() {
                @Override
                public void call(JSONObject object) {
                    try {
                        if (object != null && object.getInt("result") == 0) {
                            AppData.UserData = object.getJSONObject("user_info");
                            String message = "";
                            JSONArray messageList = AppData.UserData.getJSONArray("messages");
                            if (messageList.length() > 0)
                            {
                                message = "????????????: " + messageList.getString(messageList.length() - 1);
                            }
                            final String sendMessageStr = message;
                            //??????????????????????????????????????????????????????
                            Intent intent = new Intent(BackgroundService.this, NewMessageActivity.class);
                            PendingIntent pendingIntent = PendingIntent.getActivity(BackgroundService.this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            //??????????????????,?????????????????????
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                boolean enabled = mNotificationManager.areNotificationsEnabled();
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    boolean bubbleEnabled = mNotificationManager.areBubblesEnabled();
                                    Log.v("manager enable", "data "+ enabled + " " + bubbleEnabled);
                                }
                            }
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(BackgroundService.this, NOTIFICATION_CHANNEL_ID);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);

                                // Configure the notification channel.
                                notificationChannel.setDescription("Channel description");
                                notificationChannel.enableLights(true);
                                notificationChannel.setLightColor(Color.RED);
                                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                                notificationChannel.enableVibration(true);
                                notificationManager.createNotificationChannel(notificationChannel);
                                mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                            }



                            //?????????????????????
                            mBuilder.setContentTitle("??????????????????")
                                    //???????????????????????????
                                    .setContentText(sendMessageStr)
                                    //?????????????????????????????????????????????????????????
                                    .setTicker("???????????????")
                                    //???????????????????????????????????????????????????????????????????????????????????????
                                    .setWhen(System.currentTimeMillis())
                                    //????????????????????????
                                    .setPriority(Notification.PRIORITY_DEFAULT)
                                    //????????????????????????????????????????????????????????????????????????
                                    .setAutoCancel(true)
                                    //?????????????????????????????????
                                    .setDefaults(Notification.DEFAULT_VIBRATE).setSmallIcon(R.drawable.ic_launcher_foreground)
                                    .setContentIntent(pendingIntent)
                                    .setFullScreenIntent(pendingIntent, true);
                            mNotificationManager.notify(0, mBuilder.build());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            String url = Utils.GetApiUrlByName(NEW_MESSAGE_URL);
            Utils.SendJsonPost(url, reqObj, null, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    //https://www.jianshu.com/p/5bbc2a73ba9c
    private class MainTask extends TimerTask
    {
        public void run()
        {
            if(AppData.UserData == null)
            {
                return;
            }
            postMessageRenew();
        }
    }

    public void onDestroy()
    {
        super.onDestroy();
    }

}