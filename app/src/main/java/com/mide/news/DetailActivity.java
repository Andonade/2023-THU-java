package com.mide.news;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

import cn.jzvd.JZMediaSystem;
import cn.jzvd.JzvdStd;

public class DetailActivity extends AppCompatActivity {

    private ScrollView scrollView;

    private JzvdStd jzvdStd;

    private ImageView imageView;

    private TextView titleTextView, dateTextView, publisherTextView, contentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        scrollView = findViewById(R.id.scrollview);
        imageView = findViewById(R.id.imageView);
        titleTextView = findViewById(R.id.title);
        jzvdStd = findViewById(R.id.videoPlayer);
        dateTextView = findViewById(R.id.date);
        publisherTextView = findViewById(R.id.publisher);
        contentTextView = findViewById(R.id.content);

        showDetail();
    }

    private void showDetail() {
        String image = getIntent().getStringExtra("image");
        String title = getIntent().getStringExtra("title");
        String date = getIntent().getStringExtra("date");
        String publisher = getIntent().getStringExtra("publisher");
        String content = getIntent().getStringExtra("content");
        String video = getIntent().getStringExtra("video");
        if (!video.equals("")) {
            jzvdStd.setVisibility(VideoView.VISIBLE);
            jzvdStd.setUp(video, "", JzvdStd.SCREEN_NORMAL);
            jzvdStd.startVideo();
        } else {
            jzvdStd.setVisibility(VideoView.GONE);
        }
        if (!image.equals("")) {
            imageView.setVisibility(ImageView.VISIBLE);
            Glide.with(this).load(image).into(imageView);
        } else {
            imageView.setVisibility(ImageView.GONE);
        }
        titleTextView.setText(title);
        dateTextView.setText(date);
        publisherTextView.setText(publisher);
        contentTextView.setText(content);

        actionBarConfiguration();
    }

    private void actionBarConfiguration() {
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("详情页");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_star) {
            //TODO: star
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (jzvdStd.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        JzvdStd.releaseAllVideos();
    }
}