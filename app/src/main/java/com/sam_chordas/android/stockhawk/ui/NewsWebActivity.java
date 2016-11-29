package com.sam_chordas.android.stockhawk.ui;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;

public class NewsWebActivity extends AppCompatActivity {

    public static final String URL_STOCK = "url";
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_web);
        pb = (ProgressBar) findViewById(R.id.web_progress);
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar_web);
        tb.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_arrow_left));
        tb.setTitle(getString(R.string.app_name));
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        WebView web = (WebView) findViewById(R.id.news_web_view);

        web.setWebViewClient(new MyWebView());
        Bundle extra = getIntent().getExtras();
        String url = extra.getString(URL_STOCK);
        if(url!= null){
            web.loadUrl(url);
        }

    }

    private class MyWebView extends WebViewClient{

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            pb.setVisibility(View.GONE);
        }

    }
}
