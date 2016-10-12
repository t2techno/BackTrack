package com.example.tt.backtrack.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.tt.backtrack.data.ItemContract.ItemLocation;

/* Manages a local database for location data*/
public class ItemDbHelper extends SQLiteOpenHelper {

    // Must increment the database version if I change the schema.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "itemList.db";

    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + ItemLocation.TABLE_NAME + " (" +
                ItemLocation._ID + " INTEGER PRIMARY KEY," +

                ItemLocation.ITEM_NAME + " TEXT NOT NULL," +

                ItemLocation.COLUMN_COORD_LAT + " REAL NOT NULL," +

                ItemLocation.COLUMN_COORD_LONG + " REAL NOT NULL," +

                ItemLocation.COLUMN_IMAGE_PATH + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ItemLocation.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
