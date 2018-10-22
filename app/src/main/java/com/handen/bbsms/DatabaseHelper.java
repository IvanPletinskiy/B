package com.handen.bbsms;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "base.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table SMSTABLE (" +
                "SUMM REAL," +
                "CURR TEXT," +
                "KURS REAL," +
                "CARD INTEGER," +
                "DATE TEXT," +
                "RECEIVER TEXT," +
                "RECEIVER_CODE INTEGER," +
                "BALANCE REAL");

        String SQL = "create table RENAMES (ORIG TEXT UNIQUE, RENAMED TEXT)";
        db.execSQL(SQL);

        SQL = "create table COMMENTS (DATE TEXT UNIQUE, COMMENT TEXT)";
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
