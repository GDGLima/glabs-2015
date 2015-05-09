package com.gdglima.examplecardboardwebview;

import android.content.Context;
import android.os.Build;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.vrtoolkit.cardboard.CardboardActivity;


public class MainActivity extends CardboardActivity {

    private static final String TAG = "A360CardboardActivity";
    private static final int DELAY = 500;

    private Vibrator mVibrator;
    private WebView mWebView;
    private boolean mInModel;
    private boolean mClicked;
    private boolean mHandled;
    private int mClickCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mClicked = false;
        mHandled = false;
        mClickCount = 0;

        mWebView = new WebView(this);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        // Enable remote debugging via chrome://inspect

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        mWebView.loadUrl("http://autode.sk/gvr");

        this.setContentView(mWebView);

    }

    @Override
    public void onCardboardTrigger() {
        super.onCardboardTrigger();

        if (mInModel) {
            back();
            mVibrator.vibrate(70);
        } else {

            // Otherwise we need to check for single- vs. double-click

            if (mClicked) {

                // On double-click we open the selected model

                mClicked = false;
                mHandled = true;
                mVibrator.vibrate(70);
                enter();
            } else {

                // We only know if it's a single-click by checking after
                // a delay to see whether another click came or not

                mClicked = true;
                mHandled = false;

                mWebView.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // If nothing was handled advance the selection

                        if (!mHandled) {
                            down();
                            mClickCount++;
                            mVibrator.vibrate(50);
                        }
                        mClicked = false;
                    }
                }, DELAY);
            }
        }
    }

    public void down() {
        mWebView.loadUrl("javascript:downButton()");
    }

    public void up() {
        mWebView.loadUrl("javascript:upButton()");
    }

    public void enter() {
        mInModel = true;
        mWebView.loadUrl("javascript:openSelected()");
    }

    public void back() {
        if (mInModel) {
            mInModel = false;
            mWebView.loadUrl("javascript:location.reload()");

            // Select the item that was previously selected based on the number of
            // down clicks since load

            mWebView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:selectItemFromClicks(" + mClickCount + ");");
                }
            }, 1000);
        } else {
            System.exit(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return false;
    }
}
