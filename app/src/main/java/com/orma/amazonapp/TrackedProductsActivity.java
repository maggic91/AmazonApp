package com.orma.amazonapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import utils.Helper;

import utils.*;

import static java.lang.Math.abs;

/**
 * Created by andreafurlan on 29/01/18.
 */

public class TrackedProductsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    Context mContext;
    Helper helper;
    private DrawerLayout mDrawer;
    LinearLayout lin_products;
    JSONArray tracked_products;
    JSONArray products_code;
    AsyncHttpClient client = new AsyncHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracked_products);
        mContext = this;
        mDrawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setupDrawer();
        helper = new Helper();
        lin_products = (LinearLayout) findViewById(R.id.lin_products);

        products_code = new JSONArray();
        try {
            tracked_products = helper.getTrackedProduct(mContext);
            Log.i("fmskd", "mdkdkglml " + tracked_products);
            for (int i = 0; i < tracked_products.length(); i++){
                JSONObject product = tracked_products.getJSONObject(i);
                products_code.put(product.getString("code"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        loadData();
    }


    private void loadData(){
        try {

//            Log.i("Signed Array", "Signed Array" + tracked_products.toString());

            SignedRequestsHelper sHelper;
            try {
                sHelper = SignedRequestsHelper.getInstance(utils.Constants.ENDPOINT, utils.Constants.ACCESS_KEY_ID, utils.Constants.SECRET_KEY);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            String requestUrl = null;

            Map<String, String> params = new HashMap<String, String>();

            params.put("Service", "AWSECommerceService");
            params.put("Operation", "ItemLookup");
            params.put("AWSAccessKeyId", utils.Constants.ACCESS_KEY_ID);
            params.put("AssociateTag", "andrewf91-21");
            String ids = products_code.join(",");
            ids = ids.replace("\"","");
            params.put("ItemId", ids);
            params.put("ResponseGroup", "Images,ItemAttributes,Offers");
            //params.put("ResponseGroup", "Offers");
//["B0777CXB73","8804685433","B06ZY6BPY2"]
            requestUrl = sHelper.sign(params);
            Log.i("requestUrl", "requestUrl "+ requestUrl);

            RequestParams params2 = new RequestParams();
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setTimeout(20000);
            client.setSSLSocketFactory(socketFactory);

            client.get(getApplicationContext(),requestUrl, params2, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.i("Signed response", "Signed response: ");
                    String response = null;
                    try {
                        response = new String(responseBody, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    XmlToJson xmlToJson = new XmlToJson.Builder(response).build();
                    JSONObject jsonObject = xmlToJson.toJson();
                    try {
                        JSONObject jsItemLookupResponse = jsonObject.getJSONObject("ItemLookupResponse");
                        JSONObject jsItems = jsItemLookupResponse.getJSONObject("Items");
                        JSONArray array_items;

                        if(products_code.length() == 1){
                            array_items = new JSONArray();
                            array_items.put(jsItems.getJSONObject("Item"));
                        }
                        else{
                            array_items = jsItems.getJSONArray("Item");
                        }


                        for(int i = 0; i < array_items.length(); i++){

                            View child = getLayoutInflater().inflate(
                                    R.layout.item_product, null);
                            lin_products.addView(child);
                            RelativeLayout rel_product_item = child.findViewById(R.id.rel_product_item);
                            ImageView img_product = child.findViewById(R.id.img_product);
                            TextView txt_title = child.findViewById(R.id.txt_title);
                            TextView txt_di = child.findViewById(R.id.txt_di);
                            TextView txt_new_price = child.findViewById(R.id.txt_new_price);
                            TextView txt_list_price = child.findViewById(R.id.txt_list_price);
                            TextView txt_diff_price = child.findViewById(R.id.txt_diff_price);
                            txt_list_price.setPaintFlags(txt_list_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            final JSONObject js_item = array_items.getJSONObject(i);
                            JSONObject js_MI = js_item.getJSONObject("MediumImage");
                            JSONObject js_LI = js_item.getJSONObject("LargeImage");
                            final String ASIN = js_item.getString("ASIN");
                            final String url_MI = js_MI.getString("URL");
                            final String url_LI = js_LI.getString("URL");

                            JSONObject js_attributes = js_item.getJSONObject("ItemAttributes");
                            JSONObject js_prezzo_listino = js_attributes.getJSONObject("ListPrice");
                            final String title = js_attributes.getString("Title");
                            final String di = js_attributes.getString("Publisher");
                            final String listPrice = js_prezzo_listino.getString("FormattedPrice");

                            JSONObject js_offer_summary = js_item.getJSONObject("OfferSummary");
                            JSONObject js_new_lowest_price= js_offer_summary.getJSONObject("LowestNewPrice");
                            /*String new_lowest_price = js_new_lowest_price.getString("FormattedPrice");
                            String currency_code = js_new_lowest_price.getString("CurrencyCode");
                            int amount = js_new_lowest_price.getInt("Amount");*/

                            JSONObject js_offers = js_item.getJSONObject("Offers");

                            int amount = 0;
                            String currency_code = "";
                            String new_lowest_price = "";

                            Object obj = js_offers.get("Offer");
                            if (obj instanceof JSONObject) {
                                JSONObject js_offer = js_offers.getJSONObject("Offer");
                                amount = js_offer.getJSONObject("OfferListing").getJSONObject("Price").getInt("Amount");
                                currency_code = js_offer.getJSONObject("OfferListing").getJSONObject("Price").getString("CurrencyCode");
                                new_lowest_price = js_offer.getJSONObject("OfferListing").getJSONObject("Price").getString("FormattedPrice");

                            } else {
                                JSONArray js_offer_array = js_offers.getJSONArray("Offer");
                                for (int x = 0; x < js_offer_array.length();x++){
                                    if(js_offer_array.getJSONObject(x).getJSONObject("OfferAttributes").getString("Contidion").toLowerCase().equals("new")){
                                        if(amount == 0 || js_offer_array.getJSONObject(x).getJSONObject("OfferListing").getJSONObject("Price").getInt("Amount")< amount){
                                            amount = js_offer_array.getJSONObject(x).getJSONObject("OfferListing").getJSONObject("Price").getInt("Amount");
                                            currency_code = js_offer_array.getJSONObject(x).getJSONObject("OfferListing").getJSONObject("Price").getString("CurrencyCode");
                                            new_lowest_price = js_offer_array.getJSONObject(x).getJSONObject("OfferListing").getJSONObject("Price").getString("FormattedPrice");

                                        }
                                    }
                                }
                            }


                            for(int j = 0 ; j < tracked_products.length(); j++){
                                if (js_item.getString("ASIN").equals(tracked_products.getJSONObject(i).getString("code"))) {
                                    int first_amount = tracked_products.getJSONObject(i).getInt("first_price");

                                    float f_amount = amount / 100;
                                    float f_first_amount = first_amount / 100;
                                    DecimalFormat df = new DecimalFormat("#.00");
                                    if(f_first_amount - f_amount > 0 ){
                                        txt_diff_price.setTextColor(mContext.getResources().getColor(R.color.green));
                                        txt_diff_price.setText("- " + currency_code + " " + df.format(abs(f_first_amount - f_amount)));

                                    }
                                    else if(f_first_amount - f_amount == 0 ){
                                        txt_diff_price.setTextColor(mContext.getResources().getColor(R.color.black));
                                        txt_diff_price.setText("Il prezzo attuale è pari a quello iniziale");

                                    }
                                    else{
                                        txt_diff_price.setTextColor(mContext.getResources().getColor(R.color.red));
                                        txt_diff_price.setText("+ " + currency_code + " " + df.format(abs(f_first_amount - f_amount)));

                                    }

                                }
                            }

                            Picasso.with(getApplicationContext()).load(url_MI).into(img_product);
                            txt_title.setText(title);
                            txt_di.setText(mContext.getString(R.string.di, di));
                            txt_new_price.setText(new_lowest_price);
                            txt_list_price.setText(listPrice);

                            final String finalNew_lowest_price = new_lowest_price;
                            rel_product_item.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View v){
                                    Intent intent = new Intent(TrackedProductsActivity.this, ProductDetailsActivity.class);
                                    Bundle b = new Bundle();
                                    b.putString("url_MI", url_MI);
                                    b.putString("url_LI", url_LI);
                                    b.putString("title", title);
                                    b.putString("publisher", di);
                                    b.putString("list_price", listPrice);
                                    b.putString("lowest_price", finalNew_lowest_price);
                                    b.putString("code", ASIN);
                                    intent.putExtras(b); //Put your id to your next Intent
                                    startActivity(intent);
                                }
                            });

                            Log.i("idsidsids", "idsidsids" + new_lowest_price + "---");
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.i("Signed response", "Signed response: " + statusCode);
                    loadData();
                }


            });

            Log.i("Signed URL", "Signed URL: " + requestUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

    }

    private void setupDrawer(){
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mDrawer = (DrawerLayout)findViewById(R.id.drawerLayout);
        new LeftDrawerMenu(this, mDrawer, mToolbar);

    }

}
