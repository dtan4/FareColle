package org.dtan4.farecolle;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.dtan4.farecolle.util.History;

public class HistoryDBOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "HistoryDBOpenHelper";
    public static final String HISTORY_TABLE_NAME = "history";
    private static final int HISTORY_TABLE_VERSION = 1;
    private static final String HISTORY_TABLE_FIELDS = History._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            History.FELICA_ID + " TEXT, " +
            History.DEVICE_TYPE + " INTEGER, " +
            History.PROCESS_TYPE + " INTEGER, " +
            History.POSTED_AT + " INTEGER, " +
            History.BALANCE + " INTEGER, " +
            History.SERIAL_NUMBER + " INTEGER, " +
            History.REGION + " INTEGER";
    private static final String HISTORY_TABLE_CREATE =
            "CREATE TABLE " + HISTORY_TABLE_NAME + " (" + HISTORY_TABLE_FIELDS + ");";

    public HistoryDBOpenHelper(Context context) {
        super(context, HISTORY_TABLE_NAME, null, HISTORY_TABLE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Create Fare table");
        db.execSQL(HISTORY_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrade table from " + Integer.toString(oldVersion) + " to " +
                Integer.toString(newVersion));
        db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE_NAME);
    }
}
