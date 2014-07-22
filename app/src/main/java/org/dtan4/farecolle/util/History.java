package org.dtan4.farecolle.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

public class History {
    private static final String TAG = "farecolle.util.History";

    private int deviceType;
    private int processType;
    private Calendar postedAt;
    private int balance;
    private int serialNumber;
    private int region;

    public static ArrayList<History> getHistoryList(byte[] historyBytes) {
        ArrayList<History> historyList = new ArrayList<History>();

        int historyCount = historyBytes[12];

        for (int i = 0; i < historyCount; i++) {
            History history = new History(historyBytes, 13 + i * 16);
            historyList.add(history);
        }

        return historyList;
    }

    public History(byte[] historyBytes, int offset) {
        readFromBytes(historyBytes, offset);
    }

    // each byte follows big-endian (except balance [10, 11])
    private void readFromBytes(byte[] historyBytes, int offset) {
        this.deviceType = historyBytes[offset + 0] & 0xff;
        this.processType = historyBytes[offset + 1] & 0xff;
        this.postedAt = readPostedAt(historyBytes, offset);

        // little-endian
        this.balance = multipleBytesToInt(historyBytes, offset + 11, offset + 10);
        this.serialNumber = multipleBytesToInt(historyBytes, offset + 12, offset + 14);
        this.region = historyBytes[offset + 15];
    }

    private Calendar readPostedAt(byte[] historyBytes, int offset) {
        int dateInt;
        int year, month, day;
        Calendar c = Calendar.getInstance();

        dateInt = multipleBytesToInt(historyBytes, offset + 4, offset + 5);
        year = (dateInt >> 9) & 0x7f + 2000; // Suica is available from 2001
        month = (dateInt >> 5) & 0x0f;
        day = dateInt & 0x1f;
        c.set(year, month - 1, day);

        return c;
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
}
