package com.mide.news;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.mide.news.NewsSqliteOpenHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWindowAllocationException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import cn.jzvd.JzvdStd;

public class DetailActivity extends AppCompatActivity {

    private ScrollView scrollView;

    private JzvdStd jzvdStd;

    private ImageView imageView;

    private TextView titleTextView, dateTextView, publisherTextView, contentTextView;

    private String newsID;

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
        newsID = getIntent().getStringExtra("newsID");
        addNews(title, date, publisher, content);
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
        menu.findItem(R.id.action_star).setIcon(getStared() ? R.drawable.ic_star_filled : R.drawable.ic_star_outlined);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            SQLiteDatabase readableDatabase = NewsSqliteOpenHelper.getInstance(this).getReadableDatabase();
            if (readableDatabase.isOpen()) {
                readableDatabase.close();
            }
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_star) {
            star();
            item.setIcon(getStared() ? R.drawable.ic_star_filled : R.drawable.ic_star_outlined);
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

    private void star() {
        SQLiteDatabase readableDatabase = NewsSqliteOpenHelper.getInstance(this).getReadableDatabase();

        if (readableDatabase.isOpen()) {
            String selection = "NewsID = ?";
            String[] selectionArgs = {newsID};
            Cursor cursor = readableDatabase.rawQuery("SELECT * FROM news WHERE " + selection, selectionArgs);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                int index = cursor.getColumnIndex("isStared");
                boolean isStared = cursor.getInt(index) == 1;
                ContentValues values = new ContentValues();
                values.put("isStared", !isStared);
                readableDatabase.update("news", values, selection, selectionArgs);
                cursor.close();
                if (isStared) {
                    Toast.makeText(this, "取消收藏", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show();
                }
            } else {
                cursor.close();
                Toast.makeText(this, "读取数据失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "数据库打开失败", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean newsExist() {
        SQLiteDatabase readableDatabase = NewsSqliteOpenHelper.getInstance(this).getReadableDatabase();

        if (readableDatabase.isOpen()) {
            String selection = "NewsID = ?";
            String[] selectionArgs = {newsID};
            Cursor cursor = readableDatabase.rawQuery("SELECT * FROM news WHERE " + selection, selectionArgs);
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            } else {
                cursor.close();
                return false;
            }
        } else {
            Toast.makeText(this, "数据库打开失败", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean getStared() {
        SQLiteDatabase readableDatabase = NewsSqliteOpenHelper.getInstance(this).getReadableDatabase();

        if (readableDatabase.isOpen()) {
            String selection = "NewsID = ?";
            String[] selectionArgs = {newsID};
            Cursor cursor = readableDatabase.rawQuery("SELECT * FROM news WHERE " + selection, selectionArgs);
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("isStared");
                boolean isStared = cursor.getInt(index) == 1;
                cursor.close();
                return isStared;
            } else {
                cursor.close();
                return false;
            }
        } else {
            Toast.makeText(this, "数据库打开失败", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void addNews(String title, String date, String publisher, String content) {
        SQLiteDatabase readableDatabase = NewsSqliteOpenHelper.getInstance(this).getReadableDatabase();

        if (readableDatabase.isOpen()) {
            if (!newsExist()) {
                ContentValues values = new ContentValues();
                values.put("NewsID", newsID);
                values.put("title", title);
                values.put("date", date);
                values.put("publisher", publisher);
                values.put("content", content);
                values.put("isStared", false);
                readableDatabase.insert("news", null, values);
            }
            readableDatabase.close();
        } else {
            Toast.makeText(this, "数据库打开失败", Toast.LENGTH_SHORT).show();
        }
    }
}