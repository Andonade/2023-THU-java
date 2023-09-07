package com.mide.news;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class DetailActivity extends AppCompatActivity {

    private ScrollView scrollView;

    private ImageView imageView;

    private TextView titleTextView;

    private TextView dateTextView;

    private TextView publisherTextView;

    private TextView contentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        scrollView = findViewById(R.id.scrollview);
        imageView = findViewById(R.id.imageView);
        titleTextView = findViewById(R.id.title);
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

        if (image != "") {
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
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}