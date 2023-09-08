package com.mide.news;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import com.alibaba.fastjson2.*;

import okhttp3.*;

import com.mide.news.model.News;
import com.mide.news.model.MsgType;

import com.bumptech.glide.Glide;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.header.BezierRadarHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;

public class MainActivity extends AppCompatActivity {

    private RefreshLayout refreshLayout;

    private BezierRadarHeader header;

    private BallPulseFooter footer;

    private RecyclerView recyclerView;

    private MyHandler handler = new MyHandler(this);

    private newsAdapter adapter;

    private ActivityResultLauncher launcher;

    private int currentPage;

    private int totalPage;

    private String category = "";

    private String words = "";

    private String startDate = "";

    private String endDate = getNowDate();

    List<News> newsList = new ArrayList<News>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshLayout = findViewById(R.id.refreshLayout);
        header = (BezierRadarHeader) findViewById(R.id.header);
        footer = (BallPulseFooter) findViewById(R.id.footer);
        footer.setSpinnerStyle(SpinnerStyle.FixedBehind);
        refreshLayout.setRefreshHeader(header);
        refreshLayout.setRefreshFooter(footer);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (getDefaultNews()) {
                            Message message = Message.obtain();
                            message.what = MsgType.REFRESH_SUCCESS.ordinal();
                            handler.sendMessage(message);
                        } else {
                            Message message = Message.obtain();
                            message.what = MsgType.REFRESH_FAILURE.ordinal();
                            handler.sendMessage(message);
                        }
                    }
                }).start();
            }
        });

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (getMoreNews()) {
                            Message message = Message.obtain();
                            message.what = MsgType.LOAD_SUCCESS.ordinal();
                            handler.sendMessage(message);
                        } else {
                            Message message = Message.obtain();
                            message.what = MsgType.LOAD_FAILURE.ordinal();
                            handler.sendMessage(message);
                        }
                    }
                }).start();
            }
        });
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new newsAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (getDefaultNews()) {
                    Message message = new Message();
                    message.what = MsgType.LOAD_SUCCESS.ordinal();
                    handler.sendMessage(message);
                } else {
                    Message message = new Message();
                    message.what = MsgType.INIT_FAILURE.ordinal();
                    handler.sendMessage(message);
                }
            }
        }).start();

        actionBarConfiguration();

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Intent data = result.getData();
                int resultCode = result.getResultCode();
                if (resultCode == RESULT_OK && data != null) {
                    category = data.getStringExtra("category").equals("全部") ? "" : data.getStringExtra("category");
                    words = data.getStringExtra("words");
                    startDate = data.getStringExtra("startDate");
                    endDate = data.getStringExtra("endDate").equals("") ? getNowDate() : data.getStringExtra("endDate");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (getDefaultNews()) {
                                Message message = Message.obtain();
                                message.what = MsgType.REFRESH_SUCCESS.ordinal();
                                handler.sendMessage(message);
                            } else {
                                Message message = Message.obtain();
                                message.what = MsgType.REFRESH_FAILURE.ordinal();
                                handler.sendMessage(message);
                            }
                        }
                    }).start();
                }
            }
        });
    }

    private String getNowDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    private URL getUrl(String size, String startDate, String endDate, String words, String categories, String page) {
        StringBuffer Url = new StringBuffer("https://api2.newsminer.net/svc/news/queryNewsList?");
        Url.append("size=" + size);
        Url.append("&startDate=" + startDate);
        Url.append("&endDate=" + endDate);
        Url.append("&words=" + words);
        Url.append("&categories=" + categories);
        Url.append("&page=" + page);
        try {
            return new URL(Url.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean getDefaultNews() {
        URL url = getUrl("", startDate, endDate, words, category, "1");
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            newsList.clear();
            JSONObject news = JSON.parseObject(response.body().string());
            currentPage = news.getInteger("currentPage");
            totalPage = news.getInteger("pageSize");
            JSONArray newsArray = news.getJSONArray("data");
            for (int i = 0; i < newsArray.size(); i++) {
                JSONObject newsObject = newsArray.getJSONObject(i);
                News newsItem = new News(newsObject.getString("title"), new ArrayList<String>(), newsObject.getString("publishTime"), newsObject.getString("publisher"), newsObject.getString("category"), newsObject.getString("content"), newsObject.getString("videoUrl"), newsObject.getString("newsID"));
                String images = newsObject.getString("image");
                String[] pics = images.split(",");
                String image = pics[0];
                if (image.length() > 2) {
                    if (image.charAt(image.length() - 1) == ']') {
                        newsItem.addPicUrl(image.substring(1, image.length() - 1));
                    } else {
                        newsItem.addPicUrl(image.substring(1));
                    }
                }
                Log.i("title", newsItem.getTitle());
                Log.i("date", newsItem.getDate());
                Log.i("publisher", newsItem.getPublisher());
                newsList.add(newsItem);
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean getMoreNews() {
        if (currentPage < totalPage) {
            currentPage++;
            URL url = getUrl("", startDate, endDate, words, category, String.valueOf(currentPage));
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Call call = client.newCall(request);
            try (Response response = call.execute()) {
                JSONObject news = JSON.parseObject(response.body().string());
                currentPage = news.getInteger("currentPage");
                totalPage = news.getInteger("pageSize");
                JSONArray newsArray = news.getJSONArray("data");
                for (int i = 0; i < newsArray.size(); i++) {
                    JSONObject newsObject = newsArray.getJSONObject(i);
                    News newsItem = new News(newsObject.getString("title"), new ArrayList<String>(), newsObject.getString("publishTime"), newsObject.getString("publisher"), newsObject.getString("category"), newsObject.getString("content"), newsObject.getString("videoUrl"), newsObject.getString("newsID"));
                    String images = newsObject.getString("image");
                    String[] pics = images.split(",");
                    String image = pics[0];
                    if (image.length() > 2) {
                        if (image.charAt(image.length() - 1) == ']') {
                            newsItem.addPicUrl(image.substring(1, image.length() - 1));
                        } else {
                            newsItem.addPicUrl(image.substring(1));
                        }
                    }
                    newsList.add(newsItem);
                }
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private void actionBarConfiguration() {
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle("News");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            filterNews();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void filterNews() {
        launcher.launch(new Intent(MainActivity.this, FilterActivity.class));
    }

    public class newsAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.news_item, parent, false);
            MyViewHolder holder = new MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int pos) {
            News news = newsList.get(pos);

            if (news.getPicUrls().size() == 0) {
                holder.newsImage.setVisibility(View.GONE);
            } else{
                holder.newsImage.setVisibility(View.VISIBLE);
                Glide.with(MainActivity.this).load(news.getPicUrls().get(0)).into(holder.newsImage);
            }
            holder.newsDate.setText(news.getDate());
            holder.newsPublisher.setText(news.getPublisher());
            holder.newsTitle.setText(news.getTitle());
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra("title", news.getTitle());
                    intent.putExtra("date", news.getDate());
                    intent.putExtra("publisher", news.getPublisher());
                    intent.putExtra("image", news.getPicUrls().isEmpty() ? "" : news.getPicUrls().get(0));
                    intent.putExtra("content", news.getContent());
                    intent.putExtra("video", news.getVideoUrl());
                    startActivity(intent);
                }
            });
        }

        public int getItemCount() {
            return newsList.size();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

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

    private static class MyHandler extends Handler {
        private WeakReference<MainActivity> weakReference;

        public MyHandler(MainActivity mainActivity) {
            weakReference = new WeakReference(mainActivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            MainActivity mainActivity = weakReference.get();
            super.handleMessage(msg);
            if (mainActivity != null) {
                switch (msg.what) {
                    case 0:
                        mainActivity.adapter.notifyDataSetChanged();
                        mainActivity.refreshLayout.finishRefresh(2000);
                        break;
                    case 1:
                        mainActivity.adapter.notifyDataSetChanged();
                        mainActivity.refreshLayout.finishLoadMore(2000);
                        break;
                    case 2:
                        mainActivity.refreshLayout.finishLoadMoreWithNoMoreData();
                        break;
                    case 3:
                        mainActivity.refreshLayout.finishRefresh(false);
                        break;
                    case 4:
                        Toast.makeText(mainActivity, "网络错误", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(mainActivity, "未知错误", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }
}