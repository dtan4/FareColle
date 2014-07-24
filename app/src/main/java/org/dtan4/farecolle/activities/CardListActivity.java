package org.dtan4.farecolle.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.dtan4.farecolle.HistoryDBOpenHelper;
import org.dtan4.farecolle.R;
import org.dtan4.farecolle.util.History;

import java.util.ArrayList;

public class CardListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        ArrayList<String> cardList;

        HistoryDBOpenHelper helper = new HistoryDBOpenHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        try {
            cardList = History.getCardList(db);
        } finally {
            db.close();
        }

        ListView listView = (ListView)findViewById(R.id.card_list_view);
        ListAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, cardList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ListView listView = (ListView)adapterView;
                String selectedId = (String)listView.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                intent.putExtra("felica_id", selectedId);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.card_list, menu);
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
}
