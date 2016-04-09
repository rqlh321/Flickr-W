package com.example.sic.getpicsfromflickr;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns {

    private static final String DATABASE_NAME = "myDatabase.db";
    public static final String DATABASE_TABLE = "Elements";
    public static final String URI_COLUMN = "URI";
    public static final String NAME_COLUMN = "NAME";

    private static final String DATABASE_CREATE_SCRIPT="create table "
            + DATABASE_TABLE + "(" + BaseColumns._ID +" integer primary key autoincrement, "
            + URI_COLUMN + " text not null, "
            + NAME_COLUMN + " text not null);";
    private static final int DATABASE_VERSION = 7;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(sqLiteDatabase);
    }
}
