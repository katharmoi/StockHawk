package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.sam_chordas.android.stockhawk.R;

import java.util.List;

/**
 * Created by uyan on 18/11/16.
 */
public class QuoteMarkerView extends MarkerView {
    private TextView dateView;
    private TextView highView;
    private TextView lowView;
    private TextView closeView;
    private List<QuoteData> mData;
    private DisplayMetrics displayMetrics;
    private int mWidth;
    private int mHeight;

    public QuoteMarkerView(Context context, int layoutResource, List<QuoteData> data) {
        super(context, layoutResource);
        dateView = (TextView) findViewById(R.id.marker_date_view);
        highView = (TextView) findViewById(R.id.marker_high_view);
        lowView = (TextView) findViewById(R.id.marker_low_view);
        closeView = (TextView) findViewById(R.id.marker_close_view);
        mData = data;
        displayMetrics = context.getResources().getDisplayMetrics();
        mWidth = displayMetrics.widthPixels;
        mHeight = displayMetrics.heightPixels;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int index = (int) e.getX();
        dateView.setText(mData.get(index).getDate());
        highView.setText(Double.toString(mData.get(index).getHigh()));
        lowView.setText(Double.toString(mData.get(index).getLow()));
        closeView.setText(Double.toString(mData.get(index).getClose()));
    }

    @Override
    public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
        float drawPosX =0f;
        float drawPosY =0f;
        if((posX + getWidth()/2) > mWidth ){
            drawPosX = -getWidth();
        }
        else if((posX - getWidth()/2) < 0) {
            drawPosX = -posX;
        }
        else {
            drawPosX = -getWidth()/2;
        }

        if((posY - getHeight()) < 0){
            drawPosY = -posY;
        }else{
            drawPosY = -getHeight();
        }

        return MPPointF.getInstance(drawPosX,drawPosY);

    }



}
