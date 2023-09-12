package com.java.chenyitao;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.java.chenyitao.model.News;

import java.util.ArrayList;

public class ViewedActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private newsAdapter adapter;

    private TextView textView;

    private ArrayList<News> newsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewed);

        textView = findViewById(R.id.text_no_news);
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new newsAdapter();
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        initNews();

        actionBarConfiguration();
    }

    private void initNews() {
        getNewsHistory();
        adapter.notifyDataSetChanged();
        if (newsList.size() == 0) {
            textView.setVisibility(View.VISIBLE);
            textView.setText("暂无历史记录");
            textView.setTextColor(ContextCompat.getColor(this, R.color.viewedNews));
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    private void getNewsHistory() {
        SQLiteDatabase readableDatabase = NewsSqliteOpenHelper.getInstance(this).getReadableDatabase();
        if (readableDatabase.isOpen()){
            String sql = "select * from news ORDER BY createTime DESC";
            Cursor cursor = readableDatabase.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                int newsIDIndex = cursor.getColumnIndex("NewsID");
                int titleIndex = cursor.getColumnIndex("title");
                int dateIndex = cursor.getColumnIndex("date");
                int publisherIndex = cursor.getColumnIndex("publisher");
                int contentIndex = cursor.getColumnIndex("content");
                String newsID = cursor.getString(newsIDIndex);
                String title = cursor.getString(titleIndex);
                String date = cursor.getString(dateIndex);
                String publisher = cursor.getString(publisherIndex);
                String content = cursor.getString(contentIndex);
                News news = new News(title, new ArrayList<String>(), date, publisher, "", content, "", newsID);
                newsList.add(news);
            }
            cursor.close();
        } else {
            Toast.makeText(this, "数据库连接失败", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void actionBarConfiguration() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("历史记录");
    }

    @Override
    public boolean onSupportNavigateUp() {
        SQLiteDatabase readableDatabase = NewsSqliteOpenHelper.getInstance(this).getReadableDatabase();
        if (readableDatabase.isOpen()) {
            readableDatabase.close();
        }
        newsList.clear();
        finish();
        return super.onSupportNavigateUp();
    }

    private class newsAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ViewedActivity.this).inflate(R.layout.news_item, parent, false);
            ViewedActivity.MyViewHolder holder = new ViewedActivity.MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int pos) {
            News news = newsList.get(pos);
            holder.newsImage.setVisibility(View.GONE);
            holder.newsTitle.setText(news.getTitle());
            holder.newsTitle.setTextColor(ContextCompat.getColor(ViewedActivity.this, R.color.black));
            holder.newsDate.setText(news.getDate());
            holder.newsPublisher.setText(news.getPublisher());
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ViewedActivity.this, DetailActivity.class);
                    intent.putExtra("image", "");
                    intent.putExtra("title", news.getTitle());
                    intent.putExtra("date", news.getDate());
                    intent.putExtra("publisher", news.getPublisher());
                    intent.putExtra("content", news.getContent());
                    intent.putExtra("video", "");
                    intent.putExtra("newsID", news.getNewsID());
                    startActivity(intent);
                }
        });
        }

        @Override
        public int getItemCount() {
            return newsList.size();
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImage;
        TextView newsTitle;
        TextView newsDate;
        TextView newsPublisher;
        CardView cardView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImage = (ImageView) itemView.findViewById(R.id.newsImage);
            newsTitle = (TextView) itemView.findViewById(R.id.newsTitle);
            newsDate = (TextView) itemView.findViewById(R.id.newsDate);
            newsPublisher = (TextView) itemView.findViewById(R.id.newsPublisher);
            cardView = (CardView) itemView.findViewById(R.id.rootItem);
        }
    }
}