package com.udacity.stockhawk.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class XAxisDateFormatter implements IAxisValueFormatter {

    private final SimpleDateFormat dateFormat;
    private final Date date;

    public XAxisDateFormatter(String dateFormat) {
        this.dateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());
        this.date = new Date();
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        date.setTime((long) value);
        return dateFormat.format(date);
    }
}
