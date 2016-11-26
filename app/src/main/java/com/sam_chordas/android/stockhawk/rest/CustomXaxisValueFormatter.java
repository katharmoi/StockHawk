package com.sam_chordas.android.stockhawk.rest;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by uyan on 18/11/16.
 */
public class CustomXaxisValueFormatter implements IAxisValueFormatter {

    private ArrayList<QuoteData> mData;
    private String[] mMonths= new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };
    public CustomXaxisValueFormatter(ArrayList<QuoteData> data){
        mData = data;
    }
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int index = (int) value;
        String date = mData.get(index).getDate();
        String[] dateTokens= date.split("-");
        String year = dateTokens[0];
        String month = mMonths[Integer.parseInt(dateTokens[1]) -1];
        String day = dateTokens[2];
        return month +" " +day +" " + year;

    }

    @Override
    public int getDecimalDigits() {
        return 0;
    }
}
