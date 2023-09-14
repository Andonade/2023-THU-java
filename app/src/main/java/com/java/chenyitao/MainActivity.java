package com.java.chenyitao;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.java.chenyitao.model.News;

import me.majiajie.pagerbottomtabstrip.NavigationController;
import me.majiajie.pagerbottomtabstrip.PageNavigationView;
import me.majiajie.pagerbottomtabstrip.listener.SimpleTabItemSelectedListener;

public class MainActivity extends AppCompatActivity {

    private PageNavigationView navigationView;

    private FragmentTransaction fragmentTransaction;

    private FragmentManager fragmentManager;

    private ActivityResultLauncher launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigationConfiguration();
        actionBarConfiguration();

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Intent data = result.getData();
                int resultCode = result.getResultCode();
                if (resultCode == 20 && data != null) {
                    if (fragmentManager.findFragmentById(R.id.frameLayout) instanceof HomeFragment) {
                        HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentById(R.id.frameLayout);
                        String newsID = data.getStringExtra("newsID");
                        homeFragment.backFromDetail(newsID);
                    }
                }
            }
        });
    }

    private void navigationConfiguration() {
        navigationView = findViewById(R.id.tab);
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frameLayout, new HomeFragment()).commit();

        NavigationController navigationController = navigationView.material()
                .addItem(R.drawable.ic_home_black_24dp, "主页")
                .addItem(android.R.drawable.ic_menu_search, "搜索")
                .addItem(android.R.drawable.ic_menu_recent_history, "历史")
                .build();

        navigationController.addSimpleTabItemSelectedListener(new SimpleTabItemSelectedListener() {
            @Override
            public void onSelected(int index, int old) {
                if (index != old) {
                    if (index == 0) {
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frameLayout, new HomeFragment()).commit();
                    } else if (index == 1) {
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frameLayout, new SearchFragment()).commit();
                    } else if (index == 2) {
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frameLayout, new HistoryFragment()).commit();
                    }
                }
            }
        });
    }

    private void actionBarConfiguration() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("News");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_setting) {
            if (fragmentManager.findFragmentById(R.id.frameLayout) instanceof HomeFragment) {
                HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentById(R.id.frameLayout);
                homeFragment.categoryDialog();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void go2Detail(News news) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("title", news.getTitle());
        intent.putExtra("date", news.getDate());
        intent.putExtra("publisher", news.getPublisher());
        intent.putExtra("image", news.getPicUrls().isEmpty() ? "" : news.getPicUrls().get(0));
        intent.putExtra("content", news.getContent());
        intent.putExtra("video", news.getVideoUrl());
        intent.putExtra("newsID", news.getNewsID());
        launcher.launch(intent);
    }
}