package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.CustomXaxisValueFormatter;
import com.sam_chordas.android.stockhawk.rest.NewsRecyclerAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.rest.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sam_chordas.android.stockhawk.rest.QuoteData;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String SYMBOL = "symbol";
    private static final String QUOTE_DATA="quoteData";
    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();
    private LineChart mLineChart;
    private RequestQueue mRequestQueue;
    private String mSymbol;
    private ArrayList<QuoteData> mQuoteData;
    private ArrayList<NewsData> mNewsData;
    private RecyclerView mNewsRecycler;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private String mFeedUrl = "http://finance.yahoo.com/rss/headline?s=";
    private String mNewsUrl = "http://query.yahooapis.com/v1/public/yql?q=";
    private ProgressBar mChartProgress;

    //Fab Buttons
    private SubActionButton oneMonthBtn;
    private SubActionButton threeMonthsBtn;
    private SubActionButton sixMonthsBtn;
    private SubActionButton oneYearBtn;
    //Fab Button tags
    private final String TAG_1M="onemonth";
    private final String TAG_3M="threemonths";
    private final String TAG_6M = "sixmonths";
    private final String TAG_1Y="oneyear";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        mQuoteData = new ArrayList<>();

        mNewsData = new ArrayList<>();
        mChartProgress = (ProgressBar) findViewById(R.id.chartProgressBar);
        mChartProgress.setIndeterminate(true);
        //Recycler Setup
        mNewsRecycler = (RecyclerView) findViewById(R.id.news_recycler);
        mLayoutManager = new LinearLayoutManager(this);
        mNewsRecycler.setLayoutManager(mLayoutManager);
        mAdapter = new NewsRecyclerAdapter(mNewsData);
        mNewsRecycler.setAdapter(mAdapter);
        mNewsRecycler.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {

                    }
                }));
        VolleySingleton vs = VolleySingleton.getsInstance(this);
        mRequestQueue = vs.getRequestQueue();
        //Initiate and configure chart
        mLineChart = (LineChart) findViewById(R.id.chart);
        formatChart(mLineChart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.details_ac_toolbar);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mSymbol = extras.getString(SYMBOL);
        } else {
            Log.d(LOG_TAG, "EXTRA IS NULLL");
        }
        toolbar.setTitle(mSymbol.toUpperCase());
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_arrow_left));
        toolbar.setNavigationContentDescription(getString(R.string.a11y_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        buildFAB();

        getHistoryData(12);
        getNewsFeedJson();
    }



    private void buildFAB(){
        ImageView mainIcon = new ImageView(this);
        mainIcon.setImageResource(R.drawable.ic_sort_black_192x192);

        FloatingActionButton fab = new FloatingActionButton.Builder(this)
                .setContentView(mainIcon)
                .setBackgroundDrawable(R.drawable.sort_fab)
                .build();

        fab.setContentDescription(getString(R.string.a11y_select_period));

        //Create MenuItems
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);


        //1 month
        ImageView oneMonthIcon = new ImageView(this);
        oneMonthIcon.setImageResource(R.drawable.ic_1m);
        oneMonthBtn = itemBuilder.setContentView(oneMonthIcon).build();
        oneMonthBtn.setTag(TAG_1M);
        oneMonthBtn.setContentDescription(getString(R.string.a11y_period_one_month));
        oneMonthBtn.setBackgroundResource(R.drawable.sort_fab_menu_inactive);
        oneMonthBtn.setOnClickListener(this);

        //3 months
        ImageView threeMonthsIcon = new ImageView(this);
        threeMonthsIcon.setImageResource(R.drawable.ic_3m);
        threeMonthsBtn = itemBuilder.setContentView(threeMonthsIcon).build();
        threeMonthsBtn.setTag(TAG_3M);
        threeMonthsBtn.setContentDescription(getString(R.string.a11y_period_three_months));
        threeMonthsBtn.setBackgroundResource(R.drawable.sort_fab_menu_inactive);
        threeMonthsBtn.setOnClickListener(this);

        //6 Months
        ImageView sixMonthsIcon = new ImageView(this);
        sixMonthsIcon.setImageResource(R.drawable.ic_6m);
        sixMonthsBtn = itemBuilder.setContentView(sixMonthsIcon).build();
        sixMonthsBtn.setTag(TAG_6M);
        sixMonthsBtn.setContentDescription(getString(R.string.a11y_period_six_months));
        sixMonthsBtn.setBackgroundResource(R.drawable.sort_fab_menu_inactive);
        sixMonthsBtn.setOnClickListener(this);

        //1 year
        ImageView oneYearIcon = new ImageView(this);
        oneYearIcon.setImageResource(R.drawable.ic_1y);
        oneYearBtn = itemBuilder.setContentView(oneYearIcon).build();
        oneYearBtn.setTag(TAG_1Y);
        oneYearBtn.setContentDescription(getString(R.string.a11y_period_one_year));
        oneYearBtn.setBackgroundResource(R.drawable.sort_fab_menu_active);
        oneYearBtn.setOnClickListener(this);

        //Create the Menu
        FloatingActionMenu fabMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(oneMonthBtn)
                .addSubActionView(threeMonthsBtn)
                .addSubActionView(sixMonthsBtn)
                .addSubActionView(oneYearBtn)
                .attachTo(fab)
                .build();

    }

    private void formatChart(LineChart chart){
        chart.setDrawBorders(false);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        YAxis leftAxis = chart.getAxisLeft();
        YAxis rightAxis = chart.getAxisRight();
        XAxis xAxis = chart.getXAxis();
        leftAxis.setEnabled(true);
        leftAxis.setDrawGridLines(false);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisLineWidth(0.5f);
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new CustomXaxisValueFormatter(mQuoteData));
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(3,true);
        xAxis.setTextColor(Color.WHITE);
        rightAxis.setEnabled(false);
        chart.setDescription(null);
        chart.setNoDataText("");
        chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsActivity.this,GraphActivity.class);
                intent.putParcelableArrayListExtra(GraphActivity.GRAPH_DATA,mQuoteData);
                startActivity(intent);
            }
        });

    }
    private void getHistoryData(int delta) {
        Log.d(LOG_TAG, Utils.getHistoryUrl(mSymbol,delta));
        mChartProgress.setVisibility(View.VISIBLE);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                Utils.getHistoryUrl(mSymbol,delta),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        getDataFromJson(response);
                        bindValuesToChart(mQuoteData, mLineChart);
                        mChartProgress.setVisibility(View.GONE);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Toast.makeText(DetailsActivity.this, getString(R.string.connection_error),
                                    Toast.LENGTH_SHORT).show();
                            mChartProgress.setVisibility(View.GONE);
                        } else if (error instanceof ParseError) {
                            Toast.makeText(DetailsActivity.this, getString(R.string.parse_error), Toast.LENGTH_SHORT).show();
                            mChartProgress.setVisibility(View.GONE);

                        } else if (error instanceof ServerError) {
                            Toast.makeText(DetailsActivity.this, getString(R.string.connection_error),
                                    Toast.LENGTH_SHORT).show();
                            mChartProgress.setVisibility(View.GONE);

                        } else if (error instanceof NetworkError) {
                            Toast.makeText(DetailsActivity.this, getString(R.string.connection_error),
                                    Toast.LENGTH_SHORT).show();
                            mChartProgress.setVisibility(View.GONE);

                        } else if (error instanceof AuthFailureError) {
                            mChartProgress.setVisibility(View.GONE);

                        }

                    }
                }
        );

        mRequestQueue.add(request);
    }

    private void getDataFromJson(JSONObject response) {
        if (response == null || response.length() == 0) {
            return;

        }
        try {
            if (response.has("query") && !response.isNull("query")) {
                JSONObject results = response.getJSONObject("query").getJSONObject("results");
                JSONArray quotes = results.getJSONArray("quote");
                mQuoteData.clear();
                if (quotes != null && quotes.length() > 0) {
                    for (int i = 0; i < quotes.length(); i++) {

                        JSONObject result = quotes.getJSONObject(i);
                        Log.d(LOG_TAG, Double.toString(result.getDouble("Open")));
                        double open = result.getDouble("Open");
                        double high = result.getDouble("High");
                        double low = result.getDouble("Low");
                        double close = result.getDouble("Close");
                        String date = result.getString("Date");
                        QuoteData data = new QuoteData(open,high,low,close,date);
                        mQuoteData.add(data);

                    }

                }

            } else {
                Log.d(LOG_TAG, "No results");
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getStackTrace().toString());
        }

    }

    private void bindValuesToChart(ArrayList<QuoteData> data, LineChart chart) {
        List<Entry> entries = new ArrayList<>();
        int i = 0;
        for (QuoteData md : data) {
            entries.add(new Entry(i++, (float) md.getClose()));

        }

        LineDataSet dataSet = new LineDataSet(entries,"");
        dataSet.setColor(Color.CYAN);
        dataSet.setValueTextColor(Color.CYAN);
        dataSet.setDrawCircles(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    private void getNewsFeedJson() {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                getNewsFeedUrl(mSymbol),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseNewsJson(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
        mRequestQueue.add(request);
    }

    private void parseNewsJson(JSONObject response) {
        if (response == null || response.length() == 0) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (response.has("query") && !response.isNull("query")) {
            try {
                JSONObject results = response.getJSONObject("query").getJSONObject("results");
                JSONArray items = results.getJSONArray("item");
                if (items != null && items.length() > 0) {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        sb.append(item.getString("title") + "\n");
                        mNewsData.add(new NewsData(item.getString("title"),item.getString("description")
                        ,item.getString("link"),item.getString("pubDate")));

                    }
                    mAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    private String getNewsFeedUrl(String symbol) {
        StringBuilder newsUrl = new StringBuilder();
        String feedUrl = mFeedUrl + symbol;
        String yql = "select title, link, description, pubDate from rss where " +
                "url = \"" + feedUrl + "\"";
        String end = "&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=";
        newsUrl.append(mNewsUrl);
        try {
            newsUrl.append(URLEncoder.encode(yql, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        newsUrl.append(end);
        Log.d("NEWS URL", newsUrl.toString());
        return newsUrl.toString();
    }

    @Override
    public void onClick(View v) {
        String tag = (String)v.getTag();

        if(tag.equals(TAG_1M)){
            getHistoryData(1);
            oneMonthBtn.setBackgroundResource(R.drawable.sort_fab_menu_active);
            threeMonthsBtn.setBackgroundResource(R.drawable.sort_fab_menu_inactive);
            sixMonthsBtn.setBackgroundResource(R.drawable.sort_fab_menu_inactive);
            oneYearBtn.setBackgroundResource(R.drawable.sort_fab_menu_inactive);
        }
        if(tag.equals(TAG_3M)){
            getHistoryData(3);
            oneMonthBtn.setBackgroundResource(R.drawable.sort_fab_menu_inactive);
            threeMonthsBtn.setBackgroundResource(R.drawable.sort_fab_menu_active);
            sixMonthsBtn.setBackgroundResource(R.drawable.sort_fab_menu_inactive);
            oneYearBtn.setBackgroundResource(R.drawable.sort_fab_menu_inactive);
        }
        if(tag.equals(TAG_6M)){
            getHistoryData(6);
            oneMonthBtn.setBackgroundResource(R.drawable.sort_fab_menu_inactive);
            threeMonthsBtn.setBackgroundResource(R.drawable.sort_fab_menu_inactive);
            sixMonthsBtn.setBackgroundResource(R.drawable.sort_fab_menu_active);
            oneYearBtn.setBackgroundResource(R.drawable.sort_fab_menu_inactive);
        }
        if(tag.equals(TAG_1Y)){
            getHistoryData(12);
            oneMonthBtn.setBackgroundResource(R.drawable.sort_fab_menu_inactive);
            threeMonthsBtn.setBackgroundResource(R.drawable.sort_fab_menu_inactive);
            sixMonthsBtn.setBackgroundResource(R.drawable.sort_fab_menu_inactive);
            oneYearBtn.setBackgroundResource(R.drawable.sort_fab_menu_active);
        }
    }

//    private class QuoteData {
//        double Open;
//        double High;
//        double Low;
//        double Close;
//
//    }

    public class NewsData{
        String newsTitle;
        String newsDescription;
        String newsUrl;
        String newsDate;

        public NewsData(String title, String description,String url,String date){
            newsTitle = title;
            newsDescription = description;
            newsUrl = url;
            newsDate=date;
        }
        public String getTitle(){
            return newsTitle;
        }

        public String getDescription(){
            return newsDescription;
        }

        public String getUrl(){
            return  newsUrl;
        }

        public String getDate(){
            return newsDate;
        }
    }
}

