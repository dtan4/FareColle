package org.dtan4.farecolle.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.dtan4.farecolle.HistoryDBOpenHelper;
import org.dtan4.farecolle.R;
import org.dtan4.farecolle.activities.HistoryActivity;
import org.dtan4.farecolle.util.FelicaReader;
import org.dtan4.farecolle.util.History;

import java.util.ArrayList;

public class ScanActivity extends Activity {
    private static final String TAG = "ScanActivity";
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);


        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), getString(R.string.nfc_disabled),
                        Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, getString(R.string.nfc_enabled));
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.nfc_unsupported),
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scan, menu);
        return true;
    }

    @Override
    public void onNewIntent(Intent intent) {
        ArrayList<History> historyList = new ArrayList<History>();
        StringBuilder sb = new StringBuilder();

        super.onNewIntent(intent);
        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            if (tag == null) {
                Log.d(TAG, getString(R.string.nfc_null_tag));
                Toast.makeText(getApplicationContext(), getString(R.string.nfc_null_tag),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            FelicaReader reader = new FelicaReader(tag);
            String felicaId = reader.felicaId();
            historyList = reader.getHistory();

            if (historyList != null) {
                HistoryDBOpenHelper helper = new HistoryDBOpenHelper(this);
                SQLiteDatabase db = helper.getWritableDatabase();

                try {
                    History latestHistory = History.getLatestHistory(db, felicaId);
                    int latestSerialNumber;

                    if (latestHistory != null) {
                        latestSerialNumber = latestHistory.getSerialNumber();
                    } else {
                        latestSerialNumber = -1;
                    }

                    for (History history : historyList) {
                        if ((latestSerialNumber < 0) ||
                                (latestSerialNumber < history.getSerialNumber())) {
                            history.save(db);
                        }
                    }
                } finally {
                    db.close();
                }
            }

            Intent intentForHistory = new Intent(this, HistoryActivity.class);
            intentForHistory.putExtra("felica_id", felicaId);
            startActivity(intentForHistory);
        }
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


    @Override
    public void onPause() {
        super.onPause();

        if (nfcAdapter == null) {
            return;
        }

        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (nfcAdapter == null) {
            return;
        }

        Intent intent = new Intent(this, this.getClass())
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, intent, 0);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }
}
