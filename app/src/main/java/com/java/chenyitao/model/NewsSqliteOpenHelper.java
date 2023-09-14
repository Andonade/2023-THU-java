package com.java.chenyitao.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

public class NewsSqliteOpenHelper extends SQLiteOpenHelper {
    private static SQLiteOpenHelper instance;

    public static synchronized SQLiteOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NewsSqliteOpenHelper(context, "news.db", 1);
        }
        return instance;
    }

    private NewsSqliteOpenHelper(@NonNull Context context, @NonNull String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS news (NewsID VARCHAR PRIMARY KEY, title VARCHAR, date VARCHAR, publisher VARCHAR, content VARCHAR, isStared BOOLEAN, createTime TIMESTAMP, starTime TIMESTAMP)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
