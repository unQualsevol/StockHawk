package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.utils.XAxisDateFormatter;
import com.udacity.stockhawk.utils.YAxisPriceFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailFragment extends Fragment {

    private String fragmentDataType;
    private String historyData;

    @BindView(R.id.lc_stock_chart) public LineChart stockLineChart;
    @BindColor(R.color.textColorPrimary) public int color;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //TODO: save status && restore
            fragmentDataType = getArguments().getString(getString(R.string.fragment_period_key));
            historyData = getArguments().getString(getString(R.string.period_value_key));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, view);
        if(!TextUtils.isEmpty(historyData)) {
            configureLineChart();
        }
        // Inflate the layout for this fragment
        return view;
    }

    private void configureLineChart() {
        List<Entry> dataPairs = getFormattedStockHistory(historyData);
        LineDataSet dataSet = new LineDataSet(dataPairs, "");
        dataSet.setColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setCircleColor(color);
        dataSet.setHighLightColor(color);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        stockLineChart.setData(lineData);

        XAxis xAxis = stockLineChart.getXAxis();
        xAxis.setValueFormatter(new XAxisDateFormatter(getDateFormatByPeriod(fragmentDataType)));
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(color);
        xAxis.setAxisLineWidth(1.5f);
        xAxis.setTextColor(color);
        xAxis.setTextSize(12f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisRight = stockLineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxis = stockLineChart.getAxisLeft();
        yAxis.setValueFormatter(new YAxisPriceFormatter());
        yAxis.setDrawGridLines(false);
        yAxis.setAxisLineColor(color);
        yAxis.setAxisLineWidth(1.5f);
        yAxis.setTextColor(color);
        yAxis.setTextSize(12f);

        Legend legend = stockLineChart.getLegend();
        legend.setEnabled(false);

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext(),
                R.layout.marker_view);

        stockLineChart.setMarker(customMarkerView);

        //disable all interactions with the graph
        stockLineChart.setDragEnabled(false);
        stockLineChart.setScaleEnabled(false);
        stockLineChart.setDragDecelerationEnabled(false);
        stockLineChart.setPinchZoom(false);
        stockLineChart.setDoubleTapToZoomEnabled(false);
        Description description = new Description();
        description.setText(" ");
        stockLineChart.setDescription(description);
        stockLineChart.setExtraOffsets(10, 0, 0, 10);

        stockLineChart.invalidate();
    }

    private String getDateFormatByPeriod(String fragmentDataType) {
        String dateFormat;
        if(fragmentDataType.equals(getString(R.string.period_monthly_key))) {
            dateFormat = getString(R.string.month_name_format);
        } else {
            dateFormat = getString(R.string.day_month_format);
        }
        return dateFormat;
    }

    private List<Entry> getFormattedStockHistory(String historyData) {
        List<Entry> result = new ArrayList<>();
        String[] dataPairs = historyData.split("\n");

        for (String pair : dataPairs) {
            String[] entry = pair.split(", ");
            result.add(new Entry(Float.valueOf(entry[0]), Float.valueOf(entry[1])));
        }
        Collections.reverse(result);
        return result;
    }
}
