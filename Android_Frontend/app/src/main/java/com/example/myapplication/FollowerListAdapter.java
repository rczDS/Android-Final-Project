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

public class FollowerListAdapter extends BaseAdapter {
    private JSONArray followerListData;
    private Context c;
    public FollowerListAdapter(JSONArray data, Context cc){
        followerListData = data;
        c = cc;
    }
    @Override
    public int getCount() {
        return followerListData.length();
    }

    @Override
    public Object getItem(int i) {
        try {
            return followerListData.getJSONObject(i);
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

    public String getUserName(int i)
    {
        try
        {
            return ((JSONObject) getItem(i)).getString("user_name");
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
            view = inflater.inflate(R.layout.row_item, viewGroup, false);
        }

        ImageView imageView = view.findViewById(R.id.image_view_photo);
        String imageUrl = Utils.GetApiUrlByName(getUrl(i));
        Utils.loadImageFromNet(imageView,imageUrl);
        TextView textView = view.findViewById(R.id.text_view_name);
        textView.setText(getUserName(i));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    JSONObject followerData = (JSONObject) getItem(i);
                    String userEmail = followerData.getString("user_email");
                    PersonalActivity.enterPersonalByEmail(userEmail, (Activity) c);
                    ((Activity) c).finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }
}
