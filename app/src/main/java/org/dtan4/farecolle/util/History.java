package org.dtan4.farecolle.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.dtan4.farecolle.HistoryDBOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class History implements Parcelable {
    private static final String TAG = "History";

    private String felicaId;
    private int deviceType;
    private int processType;
    private Calendar postedAt;
    private int balance;
    private int serialNumber;
    private int region;

    private int diff;

    public static final String _ID = "_id";
    public static final String FELICA_ID = "felica_id";
    public static final String DEVICE_TYPE = "device_type";
    public static final String PROCESS_TYPE = "process_type";
    public static final String POSTED_AT = "posted_at";
    public static final String BALANCE = "balance";
    public static final String SERIAL_NUMBER = "serial_number";
    public static final String REGION = "region";
    public static final String CREATED_AT = "created_at";


    public static ArrayList<History> getHistoryList(String felicaId, byte[] historyBytes) {
        ArrayList<History> historyList = new ArrayList<History>();

        int historyCount = historyBytes[12];

        for (int i = 0; i < historyCount; i++) {
            History history = new History(felicaId, historyBytes, 13 + i * 16);

            // TODO: pursuit more smart way
            if (history.getPostedAt() != null) {
                historyList.add(history);
            }
        }

        return historyList;
    }

    public static ArrayList<History> getAllByFelicaId(SQLiteDatabase db, String felicaId) {
        ArrayList<History> historyList = new ArrayList<History>();
        Cursor cursor = null;

        try {
            cursor = db.query(HistoryDBOpenHelper.HISTORY_TABLE_NAME, null,
                    History.FELICA_ID + " = ?", new String[] { felicaId }, null, null,
                    SERIAL_NUMBER + " DESC");

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    History history = new History(felicaId, cursor);
                    historyList.add(history);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return historyList;
    }

    public static History getLatestHistory(SQLiteDatabase db, String felicaId) {
        History history = null;
        Cursor cursor = null;

        try {
            cursor = db.query(HistoryDBOpenHelper.HISTORY_TABLE_NAME, null,
                    History.FELICA_ID + " = ?", new String[]{ felicaId }, null, null,
                    SERIAL_NUMBER + " DESC", "1");

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToNext();
                history = new History(felicaId, cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return history;
    }

    public static ArrayList<String> getCardList(SQLiteDatabase db) {
        ArrayList<String> cardList = new ArrayList<String>();
        Cursor cursor = null;

        try {
            cursor = db.query(true, HistoryDBOpenHelper.HISTORY_TABLE_NAME,
                    new String[]{ History.FELICA_ID }, null, null, null, null, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String felicaId =
                            cursor.getString(cursor.getColumnIndex(History.FELICA_ID));
                    cardList.add(felicaId);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return cardList;
    }

    public History(String felicaId, Cursor cursor) {
        this.felicaId = felicaId;
        readFromCursor(cursor);
    }

    public History(String felicaId, byte[] historyBytes, int offset) {
        this.felicaId = felicaId;
        readFromBytes(historyBytes, offset);
    }

    public History(String felicaId, int deviceType, int processType, Calendar postedAt,
                   int balance, int serialNumber, int region) {
        this.felicaId = felicaId;
        this.deviceType = deviceType;
        this.processType = processType;
        this.postedAt = postedAt;
        this.balance = balance;
        this.serialNumber = serialNumber;
        this.region = region;
    }

    public int getBalance() {
        return balance;
    }

    public Calendar getPostedAt() {
        return postedAt;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }

    private void readFromCursor(Cursor cursor) {
        this.deviceType = cursor.getInt(cursor.getColumnIndex(History.DEVICE_TYPE));
        this.processType = cursor.getInt(cursor.getColumnIndex(History.PROCESS_TYPE));

        Calendar postedAt = Calendar.getInstance();
        postedAt.setTimeInMillis(
                cursor.getLong(cursor.getColumnIndex(History.POSTED_AT)));
        this.postedAt = postedAt;

        this.balance = cursor.getInt(cursor.getColumnIndex(History.BALANCE));
        this.serialNumber = cursor.getInt(cursor.getColumnIndex(History.SERIAL_NUMBER));
        this.region = cursor.getInt(cursor.getColumnIndex(History.REGION));
    }

    private void readFromBytes(byte[] historyBytes, int offset) {
        // big-endian
        this.deviceType = historyBytes[offset + 0] & 0xff;
        this.processType = historyBytes[offset + 1] & 0xff;
        this.postedAt = readPostedAt(historyBytes, offset);

        // little-endian
        this.balance = multipleBytesToInt(historyBytes, offset + 11, offset + 10);

        // big-endian
        this.serialNumber = multipleBytesToInt(historyBytes, offset + 12, offset + 14);
        this.region = historyBytes[offset + 15];
    }

    public boolean isBus() {
        return (processType == 13) || (processType == 15) ||
                (processType == 31) || (processType == 35);
    }

    public boolean isShopping() {
        return (processType == 70) || (processType == 73) ||
                (processType == 74) || (processType == 75) ||
                (processType == 198) || (processType == 203);
    }

    public long save(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(History.FELICA_ID, felicaId);
        values.put(History.DEVICE_TYPE, deviceType);
        values.put(History.PROCESS_TYPE, processType);
        values.put(History.POSTED_AT, postedAt.getTimeInMillis());
        values.put(History.BALANCE, balance);
        values.put(History.SERIAL_NUMBER, serialNumber);
        values.put(History.REGION, region);

        long id = db.insert(HistoryDBOpenHelper.HISTORY_TABLE_NAME, null, values);
        Log.d(TAG, "History is saved: " + Long.toString(id));
        return id;
    }

    private Calendar readPostedAt(byte[] historyBytes, int offset) {
        int dateInt;
        dateInt = multipleBytesToInt(historyBytes, offset + 4, offset + 5);

        if (dateInt > 0) {
            int year, month, day;
            Calendar c = Calendar.getInstance();

            year = ((dateInt >> 9) & 0x7f) + 2000; // Suica is available from 2001
            month = (dateInt >> 5) & 0x0f;
            day = dateInt & 0x1f;
            c.set(year, month - 1, day);

            return c;
        } else {
            return null;
        }
    }

    private int multipleBytesToInt(byte[] historyBytes, int from, int to) {
        int result = 0;

        if (from < to) {
            for (int i = from; i <= to; i++) {
                result = result << 8;
                result += (historyBytes[i] & 0xff);
            }
        } else {
            for (int i = from; i >= to; i--) {
                result = result << 8;
                result += (historyBytes[i] & 0xff);
            }
        }

        return result;
    }

    public String toString() {
        return Integer.toString(serialNumber) + ", " +
                "deviceType: " + Integer.toString(deviceType) + ", " +
                "processType: " + Integer.toString(processType) + ", " +
                "postedAt: " + calendarToString(postedAt) + ", " +
                "balance: " + Integer.toString(balance);
    }

    private String calendarToString(Calendar calendar) {
        return calendar.get(Calendar.YEAR) + "/" +
                calendar.get(Calendar.MONTH) + "/" +
                calendar.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(deviceType);
        parcel.writeInt(processType);
        parcel.writeLong(postedAt.getTimeInMillis());
        parcel.writeInt(balance);
        parcel.writeInt(serialNumber);
        parcel.writeInt(region);
    }

    public static final Creator<History> CREATOR
            = new Creator<History>() {
        public History createFromParcel(Parcel parcel) {
            return new History(parcel);
        }

        public History[] newArray(int size) {
            return new History[size];
        }
    };

    private History(final Parcel parcel) {
        deviceType = parcel.readInt();
        processType = parcel.readInt();

        postedAt = Calendar.getInstance();
        postedAt.setTimeInMillis(parcel.readLong());

        balance = parcel.readInt();
        serialNumber = parcel.readInt();
        region = parcel.readInt();
    }
}
