package com.orma.amazonapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.RecoverySystem;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ScrollingView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mindorks.placeholderview.PlaceHolderView;

import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import receivers.PriceCheckerReceiver;
import services.PriceCheckerService;
import utils.Helper;
import utils.LeftDrawerMenu;
import utils.MyWebChromeClient;
import utils.SignedRequestsHelper;
import utils.Constants;

public class MainActivity extends AppCompatActivity implements MyWebChromeClient.ProgressListener {


    AsyncHttpClient client = new AsyncHttpClient();
    WebView web_amazon;
    ProgressBar progress_bar;
    ArrayList<String> dataSet;
    FloatingActionButton fab;
    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    Context mContext;
    Helper helper;

    private PendingIntent pendingIntent;
    private AlarmManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mDrawer = (DrawerLayout)findViewById(R.id.drawerLayout);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setupDrawer();
        helper = new Helper();
        CookieManager.getInstance().setAcceptCookie(true);
        web_amazon = (WebView) findViewById(R.id.web_view_amazon);
        progress_bar = (ProgressBar) findViewById(R.id.progressBar);
        WebSettings settings = web_amazon.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        dataSet = new ArrayList<String>();

        web_amazon.loadUrl("https://www.amazon.it/gp/aw");
        web_amazon.setWebChromeClient(new MyWebChromeClient(this));
        web_amazon.setWebViewClient(new MyWebViewClient());

        fab = (FloatingActionButton) findViewById(R.id.myFAB);
        fab.hide();




        /*Intent alarmIntent = new Intent(this, PriceCheckerReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);*/

       // startAlarm();
        startService(new Intent(this, PriceCheckerService.class));
    }

    /*public void startAlarm() {
        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        int interval = 10000;

        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);

    }*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:

                    if(dataSet.size() > 1) {
                        String last_url = dataSet.get(dataSet.size() - 1);
                        dataSet.remove(last_url);
                        String url = dataSet.get(dataSet.size() - 1); // Make sure the list is not empty
                        web_amazon.loadUrl(url);
                    }
                    else{
                        finish();
                    }


                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }
    private class MyWebViewClient extends WebViewClient {
        private RecoverySystem.ProgressListener mListener;
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            Log.i("amazon url", "amazon url: " + url);
            if(url.contains("gp/aw/d")){
                Uri uri = Uri.parse(url);
                String server = uri.getAuthority();
                String path = uri.getPath();
                String protocol = uri.getScheme();
                Set<String> args = uri.getQueryParameterNames();
                Iterator i = args.iterator();
                if(uri.getQueryParameter("pd_rd_i") != null) {
                    Log.i("amazon url", "amazon url product " + uri.getQueryParameter("pd_rd_i"));
                }
                else{
                    String[] splitted_url = url.split("/aw/d/");
                    String[] splitted_url2 = splitted_url[1].split("/");
                    Log.i("amazon url", "amazon url product " + splitted_url2[0]);


                }

            }
            else{
                fab.hide();
            }
            dataSet.add(url);
            view.loadUrl(url);
            return true;
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progress_bar.setVisibility(View.VISIBLE);
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            progress_bar.setVisibility(View.GONE);
            if(url.contains("gp/aw/d") || url.contains("/dp/")){
                fab.show();
            }
            else{
                fab.hide();
            }
        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            handler.proceed();
        }
    }
    public void addProduct(View v) throws JSONException {
        String url = web_amazon.getUrl();
        Uri uri = Uri.parse(url);
        String server = uri.getAuthority();
        String path = uri.getPath();
        String protocol = uri.getScheme();
        Set<String> args = uri.getQueryParameterNames();
        Iterator i = args.iterator();
        String product_code = "";
        if(uri.getQueryParameter("pd_rd_i") != null) {
            Log.i("amazon url", "amazon url product " + uri.getQueryParameter("pd_rd_i"));
            product_code = uri.getQueryParameter("pd_rd_i");
        }
        else if(url.contains("/aw/d/")){
            String[] splitted_url = url.split("/aw/d/");
            String[] splitted_url2 = splitted_url[1].split("/");
            Log.i("amazon url", "amazon url product " + splitted_url2[0]);
            product_code = splitted_url2[0];


        }
        else{
            String[] splitted_url = url.split("/dp/");
            String[] splitted_url2 = splitted_url[1].split("/");
            Log.i("amazon url", "amazon url product " + splitted_url2[0]);
            product_code = splitted_url2[0];
        }
        if(!product_code.equals("")){
            try {

                helper.addTrackedProduct(product_code, mContext);
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void setupDrawer(){
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mDrawer = (DrawerLayout)findViewById(R.id.drawerLayout);
        new LeftDrawerMenu(this, mDrawer, mToolbar);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Log.i("sadmsamlksamdslka","sdskmdasmadsl");
        super.onPostCreate(savedInstanceState);

    }

    @Override
    public void onUpdateProgress(int progressValue) {
        progress_bar.setProgress(progressValue);
        if (progressValue == 100) {
            progress_bar.setVisibility(View.INVISIBLE);
        }
    }

}
