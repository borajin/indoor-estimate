package com.example.indoorestimate;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

public class DBAdapter {
    private Context context;
    private SQLiteDatabase db;

    private static String DB_PATH = "/data/data/com.example.wifi_fp_estimate/databases";
    private static String DB_NAME = "/test.db";

    public DBAdapter(Context context) {
        super();
        context = context;

        File dbCopy = new File(DB_PATH + DB_NAME);

        if (dbCopy.exists()) {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(dbCopy.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            this.db = db;
        }
    }

    public void close() {
        this.db.close();
    }

    public Cursor search(String sql) {
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }
}
