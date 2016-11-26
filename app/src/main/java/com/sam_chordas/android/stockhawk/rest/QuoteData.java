package com.sam_chordas.android.stockhawk.rest;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by uyan on 18/11/16.
 */
public class QuoteData implements Parcelable {
    private double open;
    private double high;
    private double low;
    private double close;
    private String date;

    public QuoteData(double open, double high, double low, double close,String date){
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.date = date;
    }

    protected QuoteData(Parcel in) {
        open = in.readDouble();
        high = in.readDouble();
        low = in.readDouble();
        close = in.readDouble();
        date = in.readString();
    }

    public static final Creator<QuoteData> CREATOR = new Creator<QuoteData>() {
        @Override
        public QuoteData createFromParcel(Parcel in) {
            return new QuoteData(in);
        }

        @Override
        public QuoteData[] newArray(int size) {
            return new QuoteData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public double getClose() {
        return close;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getOpen() {
        return open;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Open: " +open
                +"High: " +high
                +"Low: " +low
                +"Close: " +close
                +"Date: " +date;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(open);
        dest.writeDouble(high);
        dest.writeDouble(low);
        dest.writeDouble(close);
        dest.writeString(date);
    }
}
