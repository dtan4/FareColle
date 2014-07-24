package org.dtan4.farecolle.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.dtan4.farecolle.HistoryAdapter;
import org.dtan4.farecolle.HistoryDBOpenHelper;
import org.dtan4.farecolle.R;
import org.dtan4.farecolle.util.History;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class HistoryActivity extends Activity {
    private static final String TAG = "farecolle.HistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Intent receivedIntent = getIntent();
        String felicaId = receivedIntent.getStringExtra("felica_id");
        ArrayList<History> historyList;
        ArrayList<Integer> diffList;

        HistoryDBOpenHelper helper = new HistoryDBOpenHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        try {
            historyList = History.getAllByFelicaId(db, felicaId);
        } finally {
            db.close();
        }

        calculateDifferenceOfBalance(historyList);

        setTitle("History: " + felicaId);
        ListView listView = (ListView)findViewById(R.id.history_list_view);
        ListAdapter adapter = new HistoryAdapter(this, historyList);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void calculateDifferenceOfBalance(ArrayList<History> historyList) {
        History currentHistory, nextHistory;
        currentHistory = historyList.get(0);
        int diff;

        for (int i = 1; i < historyList.size(); i++) {
            nextHistory = historyList.get(i);
            diff = currentHistory.getBalance() - nextHistory.getBalance();
            currentHistory.setDiff(diff);
            Log.d(TAG, Integer.toString(diff));
            currentHistory = nextHistory;
        }

        currentHistory.setDiff(currentHistory.getBalance());
    }
}
