package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.CustomXaxisValueFormatter;
import com.sam_chordas.android.stockhawk.rest.QuoteData;
import com.sam_chordas.android.stockhawk.rest.QuoteMarkerView;

import java.text.ParseException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GraphActivity extends AppCompatActivity {
    public static final String GRAPH_DATA = "data";
    private ArrayList<QuoteData> mData;
    private LineChart mChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        mChart = (LineChart) findViewById(R.id.graph_extra);
        mData = getIntent().getParcelableArrayListExtra(GRAPH_DATA);
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar_graph);
        tb.setTitle(getString(R.string.app_name));
        tb.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_arrow_left));
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        formatChart(mChart);
        bindValuesToChart(mData,mChart);
    }

    private void formatChart(LineChart chart){
        chart.setDrawBorders(false);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);

        YAxis leftAxis = chart.getAxisLeft();
        YAxis rightAxis = chart.getAxisRight();
        XAxis xAxis = chart.getXAxis();
        leftAxis.setEnabled(true);
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new CustomXaxisValueFormatter(mData));
        rightAxis.setEnabled(false);
        leftAxis.setDrawGridLines(true);
        QuoteMarkerView marker = new QuoteMarkerView(this,
                R.layout.marker,
                mData);
        chart.setMarker(marker);
        chart.setDescription(null);
        chart.setExtraLeftOffset(10f);
        chart.setExtraBottomOffset(5f);


    }
    private void bindValuesToChart(ArrayList<QuoteData> data, LineChart chart) {
        List<Entry> entries = new ArrayList<>();
        int i = 0;
        for (QuoteData md : data) {
            entries.add(new Entry(i++, (float) md.getClose()));
        }

        LineDataSet dataSet = new LineDataSet(entries,"");
        dataSet.setColor(Color.BLACK);
        dataSet.setValueTextColor(Color.CYAN);

        LineData lineData = new LineData(dataSet);

        chart.setData(lineData);
        chart.invalidate();
    }

}
