package org.dtan4.farecolle;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.dtan4.farecolle.util.History;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class HistoryAdapter extends ArrayAdapter<History> {
    private final Context context;
    private final ArrayList<History> historyList;

    public HistoryAdapter(Context context, ArrayList<History> historyList) {
        super(context, R.layout.row_history, historyList);
        this.context = context;
        this.historyList = historyList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = layoutInflater.inflate(R.layout.row_history, parent, false);

        History history = historyList.get(position);

        Calendar postedAt = history.getPostedAt();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        TextView postedView = (TextView)rowView.findViewById(R.id.posted_view);
        postedView.setText(sdf.format(postedAt.getTime()));

        String processType;

        if (history.isBus()) {
            processType = "Bus";
        } else if (history.isShopping()) {
            processType = "Shopping";
        } else {
            processType = "Train";
        }

        TextView typeView = (TextView)rowView.findViewById(R.id.type_view);
        typeView.setText(processType);

        TextView diffView = (TextView)rowView.findViewById(R.id.diff_view);
        int diff = history.getDiff();

        if (diff > 0) {
            diffView.setTextColor(Color.RED);
            diffView.setText("+" + Integer.toString(history.getDiff()));
        } else {
            diffView.setTextColor(Color.BLUE);
            diffView.setText(Integer.toString(history.getDiff()));
        }


        TextView balanceView = (TextView)rowView.findViewById(R.id.balance_view);
        balanceView.setText(Integer.toString(history.getBalance()));

        return rowView;
    }
}
