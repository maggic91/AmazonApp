package receivers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;
import com.orma.amazonapp.MainActivity;
import com.orma.amazonapp.ProductDetailsActivity;
import com.orma.amazonapp.R;
import com.orma.amazonapp.TrackedProductsActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import utils.Helper;
import utils.SignedRequestsHelper;

import static java.lang.Math.abs;

/**
 * Created by andreafurlan on 01/05/18.
 */

public class PriceCheckerReceiver extends BroadcastReceiver {

    JSONArray tracked_products;
    JSONArray products_code;
    Helper helper;
    Context mContext;
    AsyncHttpClient client = new AsyncHttpClient();
    @Override
    public void onReceive(Context arg0, Intent arg1) {
        // For our recurring task, we'll just display a message
        Toast.makeText(arg0, "I'm running", Toast.LENGTH_SHORT).show();
        helper = new Helper();
        mContext = arg0;

        products_code = new JSONArray();
        try {
            tracked_products = helper.getTrackedProduct(mContext);
            for (int i = 0; i < tracked_products.length(); i++){
                JSONObject product = tracked_products.getJSONObject(i);
                products_code.put(product.getString("code"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(tracked_products != null) {
            checkPrice();
        }
    }

    private void checkPrice(){
        try {



            Log.i("Signed Array", "Signed Array reciver" + products_code.toString());

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

            client.get(mContext,requestUrl, params2, new AsyncHttpResponseHandler() {
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
                            /*final String new_lowest_price = js_new_lowest_price.getString("FormattedPrice");
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
                                if (js_item.getString("ASIN").equals(tracked_products.getJSONObject(j).getString("code"))) {
                                    int first_amount = tracked_products.getJSONObject(j).getInt("first_price");
                                    int last_amount = tracked_products.getJSONObject(j).getInt("last_price");
                                    float f_amount = amount / 100;
                                    float f_first_amount = first_amount / 100;
                                    float f_last_amount = last_amount / 100;

                                    if(f_amount< f_first_amount ){
                                        if (f_amount < f_last_amount){
                                            Log.i("notificationnot", "notificationnot " + tracked_products.getJSONObject(j).getString("code") + " --- " + f_amount + " --- " + f_first_amount + " --- " + f_last_amount);

                                            //  scheduleNotification(getNotification(mContext.getString(R.string.di, title)), 0);
                                            notifyNow(mContext.getString(R.string.prezzo_calato, title), i);

                                            JSONObject prod = new JSONObject();
                                            prod.put("code", tracked_products.getJSONObject(j).getString("code"));
                                            prod.put("first_price", tracked_products.getJSONObject(j).getString("first_price"));
                                            prod.put("first_price_formatted", tracked_products.getJSONObject(j).getString("first_price_formatted"));
                                            prod.put("last_price", amount);
                                            prod.put("last_price_formatted", new_lowest_price);
                                            helper.setProduct(prod,j,mContext);
                                        }
                                    }



                                }
                            }




                            Log.i("idsidsids", "idsidsids" + new_lowest_price + "---");
                        }


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

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.i("Signed response", "Signed response: " + statusCode);
                    checkPrice();
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
    private void scheduleNotification(Notification notification, int delay) {
        Log.i("idsidsids", "notnotnotnot");
        Intent notificationIntent = new Intent(mContext, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String content) {
        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_shopping_cart_white_24dp);
        return builder.build();
    }

    public void notifyNow(String title, int id){
        Log.i("idsidsids", "notnotnotnot " + title);
        NotificationManager notificationManager= (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent repeating_activity=new Intent(mContext, MainActivity.class);
        repeating_activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(mContext,100,repeating_activity,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(mContext)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_shopping_cart_white_24dp)
                .setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(title)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000,1000,1000,1000,1000})
                .setLights(Color.RED,3000,3000);
        notificationManager.notify(id,builder.build());
    }

}
