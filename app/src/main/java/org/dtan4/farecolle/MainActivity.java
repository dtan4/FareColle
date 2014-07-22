package org.dtan4.farecolle;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;
import android.widget.Toast;

import org.dtan4.farecolle.util.FelicaReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class MainActivity extends Activity {
    private NfcAdapter nfcAdapter;
    private static final String TAG = "farecolle.main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onNewIntent(Intent intent) {
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
            TextView tv = (TextView)findViewById(R.id.cardIdView);
            tv.setText(reader.felicaIDStr());

            reader.getHistory();
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}
