package com.example.redditapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    String ImageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean b=false;
        Thread loading = new Thread(new Runnable() {
            public void run() {
                TextView author = (TextView) findViewById(R.id.author);
                TextView time = (TextView) findViewById(R.id.time);
                ImageView thumbnailView = (ImageView) findViewById(R.id.thumbnail);
                TextView numCommentsView=(TextView)findViewById(R.id.num_comments);
                try {
                    JSONObject top = getJson("https://reddit.com/top.json");
                    JSONObject data = top.getJSONObject("data");
                    JSONObject _0item = data.getJSONArray("children").getJSONObject(0);
                    JSONObject childrendata = _0item.getJSONObject("data");
                    String s = childrendata.getString("author");
                    Long created = childrendata.getLong("created_utc");
                    String thumbnailUrlStr=childrendata.getString("thumbnail");
                    String numCommetns=childrendata.getString("num_comments");
                    author.setText(s);
                    time.setText(String.valueOf((System.currentTimeMillis() / 1000 - created) / 3600) + " hours ago");
                    URL thumbnailUrl = new URL(thumbnailUrlStr);
                    Bitmap thumbnailBmp= BitmapFactory.decodeStream(thumbnailUrl.openConnection().getInputStream());
                    thumbnailView.setImageBitmap(thumbnailBmp);
                    numCommentsView.setText("Comments: "+numCommetns);
                    ImageUrl=childrendata.getString("url");
                } catch (IOException | JSONException e) {
                    author.setText("error");
                }
            }
        });
        loading.start();
        while (loading.isAlive());
    }

    public void ImageOnClick(View viev) {
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("Image_Url",ImageUrl);
        startActivity(intent);
    }

    private JSONObject getJson(String path) throws JSONException, IOException {
        String s=getContent(path);
        JSONObject jsonObject=new JSONObject(s);
        return jsonObject;
    }

    private String getContent(String path) throws IOException {
        BufferedReader reader=null;
        InputStream stream = null;
        HttpsURLConnection connection = null;
        try {
            URL url=new URL(path);
            connection =(HttpsURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000);
            connection.connect();
            stream = connection.getInputStream();
            reader= new BufferedReader(new InputStreamReader(stream));
            return reader.readLine();
        }
        finally {
            if (reader != null) {
                reader.close();
            }
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}