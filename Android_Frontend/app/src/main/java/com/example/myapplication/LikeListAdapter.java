package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LikeListAdapter extends BaseAdapter {

    private JSONArray likeListData;
    private Context c;
    public LikeListAdapter(JSONArray data, Context cc){
        likeListData = data;
        c = cc;
    }
    @Override
    public int getCount() {
        return likeListData.length();
    }

    @Override
    public Object getItem(int i) {
        try {
            return likeListData.get(i);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public String getUrl(int i )
    {
        try
        {
            return ((JSONObject) getItem(i)).getString("photo");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
        {
            LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.like_template, viewGroup, false);
        }

        ImageView imageView = view.findViewById(R.id.image_user_thumbup);
        String imageUrl = Utils.GetApiUrlByName(getUrl(i));
        Utils.loadImageFromNet(imageView,imageUrl);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject likeData = (JSONObject) getItem(i);
                try {
                    String fromEmail = likeData.getString("from_email");
                    PersonalActivity.enterPersonalByEmail(fromEmail, (Activity) view.getContext());
                    ((Activity) view.getContext()).finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        return view;
    }
}
