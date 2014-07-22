package org.dtan4.farecolle.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

public class History {
    private static final String TAG = "farecolle.util.History";

    private int deviceType;
    private int processType;
    private Date postedAt;
    private int balance;
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

        // little-endian
        this.balance = multipleBytesToInt(historyBytes, offset + 11, offset + 10);

        this.region = historyBytes[offset + 15];
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
        Log.d(TAG, Integer.toString(result));

        return result;
    }

    public String toString() {
        return "deviceType: " + Integer.toString(deviceType) + ", " +
                "processType: " + Integer.toString(processType) + ", " +
                "balance: " + Integer.toString(balance);
    }
}
