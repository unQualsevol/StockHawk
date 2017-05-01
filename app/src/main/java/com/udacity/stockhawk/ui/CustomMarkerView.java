package com.udacity.stockhawk.ui;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.udacity.stockhawk.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomMarkerView extends MarkerView {

    private final Date date;
    private final SimpleDateFormat dateFormat;
    private final TextView textView;
    private final DecimalFormat dollarFormat;

    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        textView = (TextView) findViewById(R.id.tv_marker_text);
        dateFormat = new SimpleDateFormat("d MMM y", Locale.getDefault());
        date = new Date();
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        super.refreshContent(e, highlight);
        Float stockValue = e.getY();
        date.setTime((long) e.getX());
        String formattedDate = dateFormat.format(date);
        textView.setText(getContext().getString(R.string.marker_text, dollarFormat.format(stockValue), formattedDate));

        setOffset(-256, -32);
    }
}
