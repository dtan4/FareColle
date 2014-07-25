package org.dtan4.farecolle.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.echo.holographlibrary.Bar;
import com.echo.holographlibrary.BarGraph;
import com.echo.holographlibrary.Line;
import com.echo.holographlibrary.LineGraph;
import com.echo.holographlibrary.LinePoint;

import org.dtan4.farecolle.R;
import org.dtan4.farecolle.util.History;

import java.util.ArrayList;
import java.util.Collections;

public class GraphActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Intent receivedIntent = getIntent();
        String felicaId = receivedIntent.getStringExtra("felica_id");
        setTitle("Graph: " + felicaId);

        ArrayList<History> historyList = receivedIntent.getParcelableArrayListExtra("history_list");
        Collections.reverse(historyList); // to follow ASC
        ArrayList<Bar> points = new ArrayList<Bar>();

        for (History history : historyList) {
            Bar bar = new Bar();
            bar.setValue((float)history.getBalance());
            points.add(bar);
        }

        BarGraph graph = (BarGraph)findViewById(R.id.holo_graph_view);
        graph.setBars(points);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.graph, menu);
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
