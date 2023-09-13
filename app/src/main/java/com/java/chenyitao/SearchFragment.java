package com.java.chenyitao;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bumptech.glide.Glide;
import com.java.chenyitao.model.News;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Response;

public class SearchFragment extends Fragment {

    private Context context;

    private RecyclerView recyclerView;

    private Spinner spinner;

    private Button searchButton, okButton;

    private EditText editWordsText, editStartDateText, editEndDateText;

    private ArrayList<News> newsList = new ArrayList<>();

    private NewsAdapter adapter;

    private MyHandler handler;

    private String words = "", startDate = "", category = "", endDate = getNowDate();

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = getContext();
        handler = new MyHandler(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.rv_news);
        adapter = new NewsAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        okButton = view.findViewById(R.id.btn_ok);
        searchButton = view.findViewById(R.id.btn_search);
        editWordsText = view.findViewById(R.id.et_keyword);
        editStartDateText = view.findViewById(R.id.et_start_date);
        editEndDateText = view.findViewById(R.id.et_end_date);
        spinner = view.findViewById(R.id.spinner_category);

        init_search();

        return view;
    }

    private String getNowDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    private void setVisibility (boolean isSearch) {
        spinner.setVisibility(isSearch ? View.GONE : View.VISIBLE);
        editStartDateText.setVisibility(isSearch ? View.GONE : View.VISIBLE);
        editEndDateText.setVisibility(isSearch ? View.GONE : View.VISIBLE);
        editWordsText.setVisibility(isSearch ? View.GONE : View.VISIBLE);
        okButton.setVisibility(isSearch ? View.GONE : View.VISIBLE);
        searchButton.setVisibility(isSearch ? View.VISIBLE : View.GONE);
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

    private void init_search() {
        setVisibility(true);

        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String selected = spinner.getSelectedItem().toString();
                category = selected.equals("全部") ? "" : selected;
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                category = "";
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(false);
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                words = editWordsText.getText().toString();
                startDate = editStartDateText.getText().toString();
                endDate = editEndDateText.getText().toString().equals("") ? getNowDate() : editEndDateText.getText().toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (getSearchNews()) {
                            Message message = Message.obtain();
                            message.what = 1;
                            handler.sendMessage(message);
                        } else {
                            Message message = Message.obtain();
                            message.what = 0;
                            handler.sendMessage(message);
                        }
                    }
                }).start();
            }
        });
    }

    private boolean getSearchNews() {
        URL url = getUrl("20", startDate, endDate, words, category, "1");
        final OkHttpClient client = new OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            newsList.clear();
            JSONObject news = JSON.parseObject(response.body().string());
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
            return false;
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference<SearchFragment> weakReference;

        public MyHandler(SearchFragment searchFragment) {
            weakReference = new WeakReference(searchFragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            SearchFragment searchFragment = weakReference.get();
            super.handleMessage(msg);
            if (searchFragment != null) {
                switch (msg.what) {
                    case 0:
                        Toast.makeText(searchFragment.context, "网络连接失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        searchFragment.setVisibility(true);
                        searchFragment.adapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public class NewsAdapter extends RecyclerView.Adapter<NewsViewHolder> {
        @Override
        public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);
            return new NewsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(NewsViewHolder holder, int position) {
            final News news = newsList.get(position);
            if (news.getPicUrls().size() > 0) {
                holder.picImage.setVisibility(View.VISIBLE);
                Glide.with(SearchFragment.this).load(news.getPicUrls().get(0)).into(holder.picImage);
            } else {
                holder.picImage.setVisibility(View.GONE);
            }
            holder.titleText.setText(news.getTitle());
            holder.publisherText.setText(news.getPublisher());
            holder.DateText.setText(news.getDate());
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.go2Detail(news);
                }
            });
        }

        @Override
        public int getItemCount() {
            return newsList.size();
        }
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView titleText, publisherText, DateText;
        private ImageView picImage;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.rootItem);
            titleText = itemView.findViewById(R.id.newsTitle);
            publisherText = itemView.findViewById(R.id.newsPublisher);
            DateText = itemView.findViewById(R.id.newsDate);
            picImage = itemView.findViewById(R.id.newsImage);
        }
    }
}