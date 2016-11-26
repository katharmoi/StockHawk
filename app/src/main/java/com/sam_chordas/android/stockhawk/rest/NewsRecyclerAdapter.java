package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.DetailsActivity;
import com.sam_chordas.android.stockhawk.ui.NewsWebActivity;

import java.util.ArrayList;

/**
 * Created by uyan on 16/11/16.
 */
public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.NewsViewHolder> {

    private ArrayList<DetailsActivity.NewsData> mData;


    public NewsRecyclerAdapter(ArrayList<DetailsActivity.NewsData> data){
        mData = data;
    }

    @Override
    public NewsRecyclerAdapter.NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View root = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_item,parent,false);
        NewsViewHolder vh = new NewsViewHolder(parent.getContext(),root);
        return  vh;
    }

    @Override
    public void onBindViewHolder(NewsRecyclerAdapter.NewsViewHolder holder, int position) {
        String desc = mData.get(position).getDescription().equals("null") ?
                "No Description" : mData.get(position).getDescription() ;
        SpannableString titleText = new SpannableString(mData.get(position).getTitle());
        int pos = 0;
        for (int i = 0, ei = titleText.length(); i < ei; i++) {
            char c = titleText.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                pos = i;
                break;
            }
        }
        titleText.setSpan(new RelativeSizeSpan(2.0f), pos, pos + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.mTitleText.setText(titleText, TextView.BufferType.SPANNABLE);
        holder.mTitleText.setContentDescription(mData.get(position).getTitle());
        holder.mDescText.setText(desc);
        holder.mDateText.setText(mData.get(position).getDate());
        holder.mUrl = mData.get(position).getUrl();


    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView mTitleText;
        TextView mDescText;
        TextView mDateText;
        String mUrl;
        private Context mContext;


        public NewsViewHolder(Context context,View itemView) {
            super(itemView);
            mTitleText = (TextView) itemView.findViewById(R.id.news_title_text);
            mDescText = (TextView)itemView.findViewById(R.id.news_desc_text);
            mDateText = (TextView)itemView.findViewById(R.id.news_date_view);
            mContext = context;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION){
                Intent intent = new Intent(mContext, NewsWebActivity.class);
                intent.putExtra(NewsWebActivity.URL_STOCK,mUrl);
                mContext.startActivity(intent);
            }
        }
    }
}
