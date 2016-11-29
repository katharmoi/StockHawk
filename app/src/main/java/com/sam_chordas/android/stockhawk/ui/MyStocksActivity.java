package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.iid.MessengerCompat;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Constants;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
//import com.melnykov.fab.FloatingActionButton;
import android.support.design.widget.FloatingActionButton;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    public static final String SERVICE_FAILURE = "failure";
    public static final String SERVICE_SUCCESS = "isSuccessful";
    private CharSequence mTitle;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Context mContext;
    private Cursor mCursor;
    private SwipeRefreshLayout swipe;
    ProgressDialog mProgress;
    private BroadcastReceiver mReceiver;
    private TextView mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stocks);
        mContext = this;
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        mProgress = new ProgressDialog(MyStocksActivity.this);
        mProgress.setTitle(getString(R.string.progress_title));
        mProgress.setMessage(getString(R.string.progress_message));
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgress.setIndeterminate(true);
        final View root = findViewById(R.id.main_layout);

        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mServiceIntent = new Intent(this, StockIntentService.class);
        if (savedInstanceState == null) {
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("tag", "init");
            if (isConnected()) {
                startService(mServiceIntent);
                mProgress.show();
            } else {
                recyclerView.setVisibility(View.GONE);
                mEmptyView.setText(getString(R.string.error_data_fetch));
                mEmptyView.setVisibility(View.VISIBLE);
                networkToast();
            }
        }
        Toolbar mToolBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        swipe = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mServiceIntent.putExtra("tag", "init");
                if (isConnected()) {
                    startService(mServiceIntent);
                    swipe.setRefreshing(true);
                } else {
                    networkToast();
                    swipe.setRefreshing(false);
                }
            }
        });
        mCursorAdapter = new QuoteCursorAdapter(this, null);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        Intent intent = new Intent(MyStocksActivity.this, DetailsActivity.class);
                        intent.putExtra(DetailsActivity.SYMBOL, mCursorAdapter.getItemSymbol(position));
                        startActivity(intent);
                    }
                }));
        recyclerView.setAdapter(mCursorAdapter);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.ACTION_DATA_FETCHED)) {
                    if (intent.getBooleanExtra(SERVICE_SUCCESS, false)) {
                        recyclerView.setVisibility(View.VISIBLE);
                        mEmptyView.setVisibility(View.GONE);
                        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, MyStocksActivity.this);
                        Snackbar.make(root,getString(R.string.update_succesful),Snackbar.LENGTH_SHORT).show();

                    } else {
                        if (mCursorAdapter.getItemCount() == 0) {
                            recyclerView.setVisibility(View.GONE);
                            mEmptyView.setText(getString(R.string.error_data_fetch));
                            mEmptyView.setVisibility(View.VISIBLE);

                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            mEmptyView.setVisibility(View.GONE);
                            String message =getString(R.string.error_data_update);
                            Toast.makeText(MyStocksActivity.this,message,Toast.LENGTH_SHORT).show();
                        }

                    }
                }

                if (mProgress.isShowing()) {
                    mProgress.dismiss();
                }
                if (swipe.isRefreshing()) {
                    swipe.setRefreshing(false);
                }


            }

        };


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.attachToRecyclerView(recyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                            .content(R.string.content_test)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .inputRange(1, 5)
                            .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                    // On FAB click, receive user input. Make sure the stock doesn't already exist
                                    // in the DB and proceed accordingly
                                    String symbol = input.toString().trim();
                                    Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                            new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                            new String[]{symbol}, null);
                                    if (c.getCount() != 0) {
                                        Toast toast =
                                                Toast.makeText(MyStocksActivity.this, "This stock is already saved!",
                                                        Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                                        toast.show();
                                        return;
                                    } else {
                                        // Add the stock to DB
                                        if (validateInput(input.toString())) {
                                            mServiceIntent.putExtra("tag", "add");
                                            mServiceIntent.putExtra("symbol", symbol);
                                            startService(mServiceIntent);
                                        } else {
                                            Toast.makeText(MyStocksActivity.this, getString(R.string.a11y_alphanumeric),
                                                    Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                    }
                                }
                            })
                            .show();
                } else {
                    networkToast();
                }

            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        mTitle = getTitle();
        if (isConnected()) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_DATA_FETCHED);
        intentFilter.addAction(SERVICE_FAILURE);
        registerReceiver(mReceiver, intentFilter);
//        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void networkToast() {
        String message = getString(R.string.network_toast);
        if (mCursorAdapter != null && mCursorAdapter.getItemCount() != 0) {
            message += getString(R.string.invalid_data);
        }
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        restoreActionBar();
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_change_units) {
            // this is for changing stock changes from percent value to dollar value
            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
        }

        if (id == R.id.menu_refresh) {

            swipe.setRefreshing(true);
            mServiceIntent.putExtra("tag", "init");
            if (isConnected()) {
                startService(mServiceIntent);
                swipe.setRefreshing(false);
            } else {
                networkToast();
                swipe.setRefreshing(false);
            }
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mCursorAdapter.swapCursor(data);
        mCursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private boolean validateInput(String input) {
        //alphanumeric check
        return input.trim().matches("[A-Za-z0-9]+");
    }

}
