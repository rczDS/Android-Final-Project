package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class ActiveUtils {
    static void fillActive(View activeView, JSONObject activeData) {
        try {
            ImageView user_photo = activeView.findViewById(R.id.image_active_user);
            String activeUserPhoto = activeData.getJSONObject("active_user").getString("photo");
            activeUserPhoto = Utils.GetApiUrlByName(activeUserPhoto);
            Utils.loadImageFromNet(user_photo, activeUserPhoto);
            String activeUserName = activeData.getJSONObject("active_user").getString("user_name");
            String activeEmail = activeData.getJSONObject("active_user").getString("user_email");
            TextView activeUserText = activeView.findViewById(R.id.text_active_user_name);
            activeUserText.setText(activeUserName);
            TextView activeTitleText = activeView.findViewById(R.id.text_active_title);
            String activeTitleStr = activeData.getString("title");
            activeTitleText.setText(activeTitleStr);
            TextView activeContent = activeView.findViewById(R.id.text_active_content);
            String activeTextStr = activeData.getString("active_text");
            activeContent.setText(activeTextStr);
            user_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PersonalActivity.enterPersonalByEmail(activeEmail, (Activity) view.getContext());
                    ((Activity) view.getContext()).finish();
                }
            });
            // 显示已关注
            TextView textActiveUserFollowed = activeView.findViewById(R.id.text_active_user_followed);
            if(Utils.isUserFollowed(activeEmail))
            {
                Log.v("Follow", "followed " + activeUserName);
                textActiveUserFollowed.setVisibility(View.VISIBLE);
            }
            else{
                Log.v("Follow", "unfollowed " + activeUserName);
                textActiveUserFollowed.setVisibility(View.GONE);
            }

            int activeType = activeData.getInt("active_type");
            handleImageTypeActive(activeView, activeData, activeType);
            handleVideoTypeActive(activeView, activeData, activeType);
            handleAudioTypeActive(activeView, activeData, activeType);
            handleActiveLikedList(activeView, activeData);
            handleLikeReplyButton(activeView, activeData);
            handleActiveReplies(activeView, activeData);

            // 位置信息
            String activePosition = "";
            if(activeData.has("active_position"))
            {
                ;
                try {
                    activePosition = activeData.getString("active_position");
                    activePosition = URLDecoder.decode(activePosition, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    activePosition = "";
                    e.printStackTrace();
                }
            }
            TextView activeText = activeView.findViewById(R.id.text_active_position);
            activeText.setText(activePosition);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static void handleImageTypeActive(View activeView, JSONObject activeData, int activeType) {
        try {
            ImageView imageActive = activeView.findViewById(R.id.image_active);
            if (activeType == 1) {
                String fileUrl = activeData.getString("file_url");
                fileUrl = Utils.GetApiUrlByName(fileUrl);
                Utils.loadImageFromNet(imageActive, fileUrl);
                imageActive.setVisibility(View.VISIBLE);
            } else {
                imageActive.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static void handleAudioTypeActive(View activeView, JSONObject activeData, int activeType) {
        try {
            LinearLayout audioCtrlLayout = activeView.findViewById(R.id.active_audio_layout);
            if (activeType == 3) {
                audioCtrlLayout.setVisibility(View.VISIBLE);
                Button btnPlayAudio = activeView.findViewById(R.id.button_play_audio);
                String fileUrl = Utils.GetApiUrlByName(activeData.getString("file_url"));
                btnPlayAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(AppData.globalMediaPlayer.isPlaying()) {
                            AppData.globalMediaPlayer.pause();
                        }
                        final String audioUrl = fileUrl;
                        Utils.MakeToast("play audio " + audioUrl, (Activity) activeView.getContext());
                        try {
                            AppData.globalMediaPlayer.reset();
                            AppData.globalMediaPlayer.setDataSource(audioUrl);
                            // below line is use to prepare
                            // and start our media player.
                            AppData.globalMediaPlayer.prepare();
                            AppData.globalMediaPlayer.start();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Button btnStopPlayAudio = activeView.findViewById(R.id.button_stop_play);
                btnStopPlayAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(AppData.globalMediaPlayer.isPlaying()) {
                            AppData.globalMediaPlayer.pause();
                        }
                    }
                });
            } else {
                audioCtrlLayout.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static void handleVideoTypeActive(View activeView, JSONObject activeData, int activeType) {
        try {
            VideoView videoView = activeView.findViewById(R.id.video_active);
            LinearLayout videoCtrlLayout = activeView.findViewById(R.id.active_video_layout);
            if (activeType == 2) {
                //activeData.getString("file_url");
                final String fileUrl = Utils.GetApiUrlByName(activeData.getString("file_url"));
                //final String fileUrl = "/storage/emulated/0/Pictures/video.mp4";
                //Uri uri = Uri.parse(fileUrl);
                // sets the resource from the
                // videoUrl to the videoView

                //videoView.setVideoURI(uri);
                videoView.setVisibility(View.VISIBLE);
                videoCtrlLayout.setVisibility(View.VISIBLE);
                Button btnPlayVideo = activeView.findViewById(R.id.button_play_video);
                btnPlayVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        VideoDownloader.playVideo(videoView, fileUrl);
                        //videoView.setVideoPath(fileUrl);
                        //videoView.start();
                    }
                });
                Button btnStopVideo = activeView.findViewById(R.id.button_stop_play_video);
                btnStopVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        videoView.pause();
                    }
                });
            } else {
                videoView.setVisibility(View.GONE);
                videoCtrlLayout.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static void handleActiveLikedList(View activeView, JSONObject activeData) {
        try {
            GridView layoutLikes = activeView.findViewById(R.id.layout_likes);
            //Log.w("thumbup_list", activeData.getJSONArray("thumbup_list").toString());
            LikeListAdapter adapter = new LikeListAdapter(activeData.getJSONArray("thumbup_list"), activeView.getContext());
            layoutLikes.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static class LikeCallback implements Utils.JsonPostCallback {
        View toUpdateView;
        JSONObject originalActiveData;

        public LikeCallback(View view, JSONObject data) {
            originalActiveData = data;
            toUpdateView = view;
        }

        @Override
        public void call(JSONObject object) {
            try {
                if (object != null && object.getInt("result") == 0) {
                    JSONArray thumupList = object.getJSONObject("active").getJSONArray("thumbup_list");
                    originalActiveData.put("thumbup_list", thumupList);
                    ((Activity) toUpdateView.getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            handleActiveLikedList(toUpdateView, originalActiveData);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    final static String LIKE_URL = "clients/like_active";
    final static String DISLIKE_URL = "clients/dislike_active";

    static String getLikeDislikeUrl(JSONObject activeData) {
        try {
            JSONArray likeArray = activeData.getJSONArray("thumbup_list");
            String user_email = AppData.UserData.getString("user_email");
            boolean likedBefore = false;
            for (int i = 0; i < likeArray.length(); i++) {
                JSONObject likeData = likeArray.getJSONObject(i);
                if (likeData.getString("from_email").equals(user_email)) {
                    likedBefore = true;
                    break;
                }
            }
            String url = LIKE_URL;
            if (likedBefore) {
                url = DISLIKE_URL;
            }
            url = Utils.GetApiUrlByName(url);
            return url;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    static String REPLY_ACTIVE_URL = "clients/reply_active";
    static void handleLikeReplyButton(View activeView, JSONObject activeData) {

        Button likeButton = activeView.findViewById(R.id.button_like_dislike);

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Utils.MakeToast("Click like dislike", (Activity) activeView.getContext());
                try {
                    JSONObject reqObj = new JSONObject();
                    reqObj.put("from_email", AppData.UserData.getString("user_email"));
                    reqObj.put("active_id", activeData.getString("active_id"));
                    String apiUrl = getLikeDislikeUrl(activeData);
                    Utils.JsonPostCallback callback = new LikeCallback(activeView, activeData);
                    Utils.SendJsonPost(apiUrl, reqObj, (Activity) activeView.getContext(), callback);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        // 参考 https://www.cnblogs.com/tangchun/p/9546868.html
        Button replyButton = activeView.findViewById(R.id.button_reply_active);
        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("click", "click reply button");
                final EditText inputServer = new EditText(activeView.getContext());
                AlertDialog.Builder builder = new AlertDialog.Builder(activeView.getContext());
                builder.setTitle("输入回复").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String text = inputServer.getText().toString();
                            Log.v("app", text);
                            String activeId = activeData.getString("active_id");
                            String fromEmail = AppData.UserData.getString("user_email");
                            JSONObject reqObj = new JSONObject();
                            reqObj.put("active_id", activeId);
                            reqObj.put("from_email", fromEmail);
                            reqObj.put("reply_text", text);
                            String url = Utils.GetApiUrlByName(REPLY_ACTIVE_URL);
                            Utils.JsonPostCallback callback = new ReplyCallback(activeView, activeData);
                            Utils.SendJsonPost(url, reqObj, (Activity) activeView.getContext(), callback);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.show();
            }
        });

    }

    static class ReplyCallback implements Utils.JsonPostCallback {
        View toUpdateView;
        JSONObject originalActiveData;

        public ReplyCallback(View view, JSONObject data) {
            originalActiveData = data;
            toUpdateView = view;
        }
        @Override
        public void call(JSONObject object) {
            try {
                if (object != null && object.getInt("result") == 0) {
                    JSONArray replyList = object.getJSONObject("active").getJSONArray("reply_list");
                    originalActiveData.put("reply_list", replyList);
                    ((Activity) toUpdateView.getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleActiveReplies(toUpdateView, originalActiveData);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    final static String WITHDRAW_REPLY = "clients/withdraw_reply";
    static void handleDeleteReplyButton(View activeView, JSONObject activeData, JSONObject replyData, View replyView)
    {
        try {
            Button replyDelete = replyView.findViewById(R.id.button_delete_reply);
            String reply_email = replyData.getString("from_email");
            boolean isSelfReply =  reply_email.equals(AppData.UserData.getString("user_email"));
            String replyId = replyData.getString("reply_id");

            if (isSelfReply)
            {
                replyDelete.setVisibility(View.VISIBLE);
                replyDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            JSONObject reqObj = new JSONObject();
                            reqObj.put("from_email", AppData.UserData.getString("user_email"));
                            reqObj.put("active_id", activeData.getString("active_id"));
                            reqObj.put("reply_id", replyId);
                            String apiUrl = Utils.GetApiUrlByName(WITHDRAW_REPLY);
                            Utils.JsonPostCallback callback = new ReplyCallback(activeView, activeData);
                            Utils.SendJsonPost(apiUrl, reqObj, (Activity) activeView.getContext(), callback);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            else
            {
                replyDelete.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static void handleActiveReplies(View activeView, JSONObject activeData)
    {
        try {
            LinearLayout replyContainerLayout = activeView.findViewById(R.id.active_reply_layout);
            replyContainerLayout.removeAllViews();
            JSONArray replyDataList = activeData.getJSONArray("reply_list");
            LayoutInflater inflater = (LayoutInflater)activeView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for(int i = 0; i < replyDataList.length(); i ++)
            {
                JSONObject replyData = replyDataList.getJSONObject(i);
                View newReplyView = inflater.inflate(R.layout.active_reply_template, null, false);
                replyContainerLayout.addView(newReplyView);
                ImageView imageReplyUserPhoto = newReplyView.findViewById(R.id.image_reply_user);
                TextView textReplyUserName = newReplyView.findViewById(R.id.text_reply_user_name);
                TextView textReplyContent = newReplyView.findViewById(R.id.text_reply_content);
                String imageUrl = Utils.GetApiUrlByName(replyData.getString("photo"));
                Utils.loadImageFromNet(imageReplyUserPhoto, imageUrl);
                String strUserName = replyData.getString("user_name");
                Log.w("replydata", replyData.toString() + strUserName);
                textReplyUserName.setText(strUserName);
                textReplyContent.setText(replyData.getString("reply_text"));
                handleDeleteReplyButton(activeView, activeData, replyData, newReplyView);

                imageReplyUserPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            String fromEmail = replyData.getString("from_email");
                            PersonalActivity.enterPersonalByEmail(fromEmail, (Activity) view.getContext());
                            ((Activity) view.getContext()).finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
