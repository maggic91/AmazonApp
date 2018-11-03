package com.orma.amazonapp;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import utils.Helper;
import utils.LeftDrawerMenu;

/**
 * Created by andreafurlan on 05/04/18.
 */
public class ProductDetailsActivity extends AppCompatActivity {

    Context mContext;
    Helper helper;
    private DrawerLayout mDrawer;
    Toolbar mToolbar;
    RelativeLayout rel_product;
    String url_MI, url_LI, title, publisher, list_price, lowest_price, code;
    Button add_cart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        mContext = this;
        mDrawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setupDrawer();
        helper = new Helper();
        rel_product = (RelativeLayout) findViewById(R.id.rel_product);
        add_cart = (Button) findViewById(R.id.add_cart);
        Bundle b = getIntent().getExtras();
        if(b != null) {
            url_MI = b.getString("url_MI");
            url_LI = b.getString("url_LI");
            title = b.getString("title");
            publisher = b.getString("publisher");
            list_price = b.getString("list_price");
            lowest_price = b.getString("lowest_price");
            code = b.getString("code");



            ImageView img_product = findViewById(R.id.img_product);
            TextView txt_title = findViewById(R.id.txt_title);
            TextView txt_di = findViewById(R.id.txt_di);
            TextView txt_new_price = findViewById(R.id.txt_new_price);
            TextView txt_list_price = findViewById(R.id.txt_list_price);
            TextView txt_initial_price = findViewById(R.id.txt_initial_price);
            txt_list_price.setPaintFlags(txt_list_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            Picasso.with(getApplicationContext()).load(url_LI).into(img_product);
            txt_title.setText(title);
            txt_di.setText(mContext.getString(R.string.di, publisher));
            txt_new_price.setText(lowest_price);
            txt_list_price.setText(list_price);

            try {
                JSONArray tracked_products = helper.getTrackedProduct(mContext);

                for(int i = 0; i< tracked_products.length(); i++){
                    JSONObject product = tracked_products.getJSONObject(i);
                    if(product.getString("code").equals(code)){
                        txt_initial_price.setText(mContext.getString(R.string.prezzo_iniziale, product.getString("first_price_formatted")));
                    }
                }

            } catch (JSONException e) {
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
    public void addToCart(View v) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, JSONException, KeyManagementException, IOException {
        helper.addCartProduct(code, 1, mContext);
    }

}
