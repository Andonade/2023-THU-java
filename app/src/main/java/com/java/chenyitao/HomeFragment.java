package com.java.chenyitao;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bumptech.glide.Glide;
import com.java.chenyitao.model.MsgType;
import com.java.chenyitao.model.News;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.header.BezierRadarHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private Context context;
    private RefreshLayout refreshLayout;

    private BezierRadarHeader header;

    private BallPulseFooter footer;

    private RecyclerView recyclerView;

    private HomeFragment.MyHandler handler;

    private HomeFragment.newsAdapter adapter;

    private int currentPage, totalPage;

    private String category = "", words = "", startDate = "", endDate = getNowDate();

    List<News> newsList = new ArrayList<News>();
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
         context = getActivity().getApplicationContext();
         handler = new HomeFragment.MyHandler(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        refreshLayout = view.findViewById(R.id.refreshLayout);
        header = (BezierRadarHeader) view.findViewById(R.id.header);
        footer = (BallPulseFooter) view.findViewById(R.id.footer);
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
        recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new HomeFragment.newsAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
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

        return view;
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
                News newsItem = new News(newsObject.getString("title"), new ArrayList<String>(), newsObject.getString("publishTime"), newsObject.getString("publisher"), newsObject.getString("category"), newsObject.getString("content"), newsObject.getString("video"), newsObject.getString("newsID"));
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
                    News newsItem = new News(newsObject.getString("title"), new ArrayList<String>(), newsObject.getString("publishTime"), newsObject.getString("publisher"), newsObject.getString("category"), newsObject.getString("content"), newsObject.getString("video"), newsObject.getString("newsID"));
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

    public void backFromFilter(Intent data) {
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

    public void backFromDetail(String newsID) {
        for (int i = 0; i < newsList.size(); i++) {
            News news = newsList.get(i);
            if (news.getNewsID() == newsID) {
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    public class newsAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.news_item, parent, false);
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
                Glide.with(HomeFragment.this).load(news.getPicUrls().get(0)).into(holder.newsImage);
            }
            holder.newsDate.setText(news.getDate());
            holder.newsPublisher.setText(news.getPublisher());
            holder.newsTitle.setText(news.getTitle());
            SQLiteDatabase readableDatabase = NewsSqliteOpenHelper.getInstance(context).getReadableDatabase();
            if (readableDatabase.isOpen()) {
                Cursor cursor = readableDatabase.rawQuery("select * from news where newsID = ?", new String[]{news.getNewsID()});
                if (cursor.moveToFirst()) {
                    holder.newsTitle.setTextColor(ContextCompat.getColor(context, R.color.customGrey));
                } else {
                    holder.newsTitle.setTextColor(ContextCompat.getColor(context, R.color.black));
                }
                cursor.close();
                readableDatabase.close();
            }
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.go2Detail(news);
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
        private WeakReference<HomeFragment> weakReference;

        public MyHandler(HomeFragment homeFragment) {
            weakReference = new WeakReference(homeFragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            HomeFragment homeFragment = weakReference.get();
            super.handleMessage(msg);
            if (homeFragment != null) {
                switch (msg.what) {
                    case 0:
                        homeFragment.adapter.notifyDataSetChanged();
                        homeFragment.refreshLayout.finishRefresh(2000);
                        break;
                    case 1:
                        homeFragment.adapter.notifyDataSetChanged();
                        homeFragment.refreshLayout.finishLoadMore(2000);
                        break;
                    case 2:
                        homeFragment.refreshLayout.finishLoadMoreWithNoMoreData();
                        break;
                    case 3:
                        homeFragment.refreshLayout.finishRefresh(false);
                        break;
                    case 4:
                        Toast.makeText(homeFragment.context, "网络错误", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(homeFragment.context, "未知错误", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }
}