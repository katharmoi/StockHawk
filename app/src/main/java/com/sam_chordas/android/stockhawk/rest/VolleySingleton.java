package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by uyan on 14/11/16.
 */
public class VolleySingleton {
    private volatile static VolleySingleton sInstance = null;
    private RequestQueue mRequestQueue;

    private VolleySingleton(Context context){
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static VolleySingleton getsInstance(Context context){
        if(sInstance == null){
            synchronized (VolleySingleton.class){
                if(sInstance == null){
                    sInstance = new VolleySingleton(context);
                }
            }
        }
        return sInstance;
    }

    public RequestQueue getRequestQueue(){
        return mRequestQueue;
    }
}
