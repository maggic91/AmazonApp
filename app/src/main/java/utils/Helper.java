package utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;
import com.orma.amazonapp.R;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import fr.arnaudguyon.xmltojsonlib.XmlToJson;

/**
 * Created by andreafurlan on 18/01/18.
 */

public class Helper {
    public static boolean addTrackedProduct(String product, Context mContext) throws JSONException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String products = sharedPreferences.getString("tracked_product", "");
        if(products != null && !products.equals("")) {
            Log.i("trackedProducts", "trackedProducts " + products);
            JSONArray product_array = new JSONArray(products);
            int product_yet_tracked = 0;
            for (int i = 0; i < product_array.length(); i++) {
                if (product_array.getJSONObject(i).getString("code").equals(product)) {
                    product_yet_tracked = 1;
                }
            }
            if (product_yet_tracked == 0) {
                product_array.put(product);
                Log.i("trackedProducts", "trackedProducts " + product_array.toString());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("tracked_product", product_array.toString());
                editor.commit();
                setProductPrice(product,mContext);
                Toast.makeText(mContext, mContext.getString(R.string.prodotto_aggiunto), Toast.LENGTH_SHORT).show();
                return true;

            } else {
                Toast.makeText(mContext, mContext.getString(R.string.prodotto_presente), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        else{
            Log.i("trackedProducts", "trackedProducts null");
            JSONArray product_array = new JSONArray();
            product_array.put(product);
            Log.i("trackedProducts", "trackedProducts " + product_array.toString());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("tracked_product", product_array.toString());
            editor.commit();
            setProductPrice(product,mContext);
            return true;
        }



    }

    public static boolean setProduct(JSONObject product, int pos, Context mContext) throws JSONException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String products = sharedPreferences.getString("tracked_product", "");
        JSONArray product_array = new JSONArray(products);

        product_array.put(pos,product);
        Log.i("trackedProducts", "trackedProducts " + product_array.toString());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("tracked_product", product_array.toString());
        editor.commit();
        return true;





    }

    public static void deleteTrackedProduct(String product, Context mContext) throws JSONException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String products = sharedPreferences.getString("tracked_product", "");

        if(products != null && !products.equals("")) {
            JSONArray product_array = new JSONArray(products);
            for (int i = 0; i < product_array.length(); i++) {
                if (product_array.getString(i).equals(product)) {
                    product_array.remove(i);
                }
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString( "tracked_product", product_array.toString() );
            editor.commit();
        }




    }

    public static JSONArray getTrackedProduct(Context mContext) throws JSONException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String products = sharedPreferences.getString("tracked_product", "");


        JSONArray product_array = new JSONArray(products);

        return product_array;
    }
    public static JSONArray getCartProducts(Context mContext) throws JSONException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String cart_product = sharedPreferences.getString("cart_product", "");


        JSONArray product_array = new JSONArray(cart_product);

        return product_array;
    }

    public void setCartProducts(JSONArray cart_json, Context mContext){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString( "cart_product", cart_json.toString() );
        editor.commit();
    }

    public void addCartProduct(final String product, int quantity, final Context mContext) throws JSONException, KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException {
        AsyncHttpClient client = new AsyncHttpClient();
        SignedRequestsHelper sHelper;
        try {
            sHelper = SignedRequestsHelper.getInstance(utils.Constants.ENDPOINT, utils.Constants.ACCESS_KEY_ID, utils.Constants.SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDate =  new SimpleDateFormat("YYYY-MM-DD hh:mm:ssZ");

        String strDt = simpleDate.format(currentTime);
        String requestUrl = null;
        if(this.getCartId(mContext) == null || this.getCartId(mContext).equals("")) {
            Map<String, String> params = new HashMap<String, String>();

            params.put("Service", "AWSECommerceService");
            params.put("Operation", "CartCreate");
            params.put("AWSAccessKeyId", utils.Constants.ACCESS_KEY_ID);
            params.put("AssociateTag", "andrewf91-21");
            params.put("Item.1.OfferListingId", product);
            params.put("Item.1.Quantity", quantity + "");
            params.put("Timestamp", strDt);

            //params.put("ResponseGroup", "Offers");
//["B0777CXB73","8804685433","B06ZY6BPY2"]
            requestUrl = sHelper.sign(params);
            Log.i("requestUrlcart", "requestUrlcart " + requestUrl);

            RequestParams params2 = new RequestParams();
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setTimeout(20000);
            client.setSSLSocketFactory(socketFactory);
            client.get(mContext, requestUrl, params2, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    String response = null;
                    try {
                        response = new String(responseBody, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.i("Signed response cart", "Signed response cart: " + response);
                    XmlToJson xmlToJson = new XmlToJson.Builder(response).build();
                    JSONObject jsonObject = xmlToJson.toJson();
                    try {
                        saveCartId(jsonObject.getString("CartId"), mContext);
                        saveHMAC(jsonObject.getString("HMAC"), mContext);
                        /*JSONObject objCartItems = jsonObject.getJSONObject("CartItems");
                        JSONArray arrayCartItem = objCartItems.getJSONArray("CartItem");
                        JSONArray arrayCart = new JSONArray();
                        for(int i = 0; i<arrayCartItem.length(); i++){
                            JSONObject productCart = arrayCartItem.getJSONObject(i);
                            JSONObject productToAdd = new JSONObject();
                            productToAdd.put("cart_id", productCart.getString("CartItemId"));
                            productToAdd.put("quantity", productToAdd.getInt("Quantity"));
                            arrayCart.put(productToAdd);
                        }
                        Helper.this.setCartProducts(arrayCart,mContext);*/

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
        else{
            Map<String, String> params = new HashMap<String, String>();

            params.put("Service", "AWSECommerceService");
            params.put("Operation", "CartAdd");
            params.put("AWSAccessKeyId", utils.Constants.ACCESS_KEY_ID);
            params.put("AssociateTag", "andrewf91-21");
            params.put("CartId", this.getCartId(mContext));
            params.put("HMAC", this.getHMAC(mContext));
            params.put("Item.1.OfferListingId", product);
            params.put("Item.1.Quantity", quantity + "");
            params.put("Timestamp", strDt);

            //params.put("ResponseGroup", "Offers");
//["B0777CXB73","8804685433","B06ZY6BPY2"]
            requestUrl = sHelper.sign(params);
            Log.i("requestUrlcart", "requestUrlcart " + requestUrl);

            RequestParams params2 = new RequestParams();
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setTimeout(20000);
            client.setSSLSocketFactory(socketFactory);
            client.get(mContext, requestUrl, params2, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = null;
                    try {
                        response = new String(responseBody, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.i("Signed add cart", "Signed response cart: " + response);
                    XmlToJson xmlToJson = new XmlToJson.Builder(response).build();
                    JSONObject jsonObject = xmlToJson.toJson();
                    /*try {
                        JSONObject jsCartAddRequest = jsonObject.getJSONObject("CartAddRequest");
                        JSONObject objItems = jsonObject.getJSONObject("Items");
                        JSONArray arrayItem = objItems.getJSONArray("Item");
                        JSONArray arrayCart = Helper.this.;
                        for(int i = 0; i<arrayCartItem.length(); i++){
                            JSONObject productCart = arrayCartItem.getJSONObject(i);
                            JSONObject productToAdd = new JSONObject();
                            productToAdd.put("cart_id", productCart.getString("CartItemId"));
                            productToAdd.put("quantity", productToAdd.getInt("Quantity"));
                            arrayCart.put(productToAdd);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
    }



    public void saveCartId(String cart_id, Context mContext){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString( "cart_id", cart_id );
        editor.commit();
    }

    public String getCartId(Context mContext){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String cart_id = sharedPreferences.getString("cart_id", "");

        return cart_id;
    }
    public void saveHMAC(String HMAC, Context mContext){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString( "HMAC", HMAC );
        editor.commit();
    }

    public String getHMAC(Context mContext){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String HMAC = sharedPreferences.getString("HMAC", "");

        return HMAC;
    }

    public static void setProductPrice(final String product_code, final Context mContext) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException {
        SignedRequestsHelper sHelper;
        try {
            sHelper = SignedRequestsHelper.getInstance(utils.Constants.ENDPOINT, utils.Constants.ACCESS_KEY_ID, utils.Constants.SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String requestUrl = null;
        AsyncHttpClient client = new AsyncHttpClient();

        Map<String, String> params = new HashMap<String, String>();

        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemLookup");
        params.put("AWSAccessKeyId", utils.Constants.ACCESS_KEY_ID);
        params.put("AssociateTag", "andrewf91-21");
        params.put("ItemId", product_code);
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
                String response = null;
                try {
                    response = new String(responseBody, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                XmlToJson xmlToJson = new XmlToJson.Builder(response).build();
                JSONObject jsonObject = xmlToJson.toJson();

                JSONObject jsItemLookupResponse = null;
                try {
                    jsItemLookupResponse = jsonObject.getJSONObject("ItemLookupResponse");
                    JSONObject jsItems = jsItemLookupResponse.getJSONObject("Items");
                    //JSONArray array_items = jsItems.getJSONArray("Item");
                    JSONObject js_item = jsItems.getJSONObject("Item");
                    //JSONObject js_item = array_items.getJSONObject(0);
                    JSONObject js_offer_summary = js_item.getJSONObject("OfferSummary");
                    JSONObject js_new_lowest_price= js_offer_summary.getJSONObject("LowestNewPrice");/*
                    final String new_lowest_price = js_new_lowest_price.getString("Amount");
                    final String new_lowest_price_formatted = js_new_lowest_price.getString("FormattedPrice");*/

                    JSONObject js_offers = js_item.getJSONObject("Offers");

                    int new_lowest_price = 0;
                    String new_lowest_price_formatted = "";
                    Object obj = js_offers.get("Offer");
                    if (obj instanceof JSONObject) {
                        JSONObject js_offer = js_offers.getJSONObject("Offer");
                        new_lowest_price = js_offer.getJSONObject("OfferListing").getJSONObject("Price").getInt("Amount");
                        new_lowest_price_formatted = js_offer.getJSONObject("OfferListing").getJSONObject("Price").getString("FormattedPrice");


                    } else {
                        JSONArray js_offer_array = js_offers.getJSONArray("Offer");
                        for (int x = 0; x < js_offer_array.length();x++){
                            if(js_offer_array.getJSONObject(x).getJSONObject("OfferAttributes").getString("Contidion").toLowerCase().equals("new")){
                                if(new_lowest_price == 0 || js_offer_array.getJSONObject(x).getJSONObject("OfferListing").getJSONObject("Price").getInt("Amount")< new_lowest_price){
                                    new_lowest_price = js_offer_array.getJSONObject(x).getJSONObject("OfferListing").getJSONObject("Price").getInt("Amount");
                                    new_lowest_price_formatted = js_offer_array.getJSONObject(x).getJSONObject("OfferListing").getJSONObject("Price").getString("FormattedPrice");

                                }
                            }
                        }
                    }







                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    String products = sharedPreferences.getString("tracked_product", "");
                    if(products != null && !products.equals("")) {
                        JSONArray tracked_products = new JSONArray(products);
                        for (int i = 0 ; i <tracked_products.length(); i++){
                            String code = tracked_products.getString(i);
                            if(code.equals(product_code)){
                                JSONObject prod = new JSONObject();
                                prod.put("code", code);
                                prod.put("first_price", new_lowest_price);
                                prod.put("first_price_formatted", new_lowest_price_formatted);
                                prod.put("last_price", new_lowest_price);
                                prod.put("last_price_formatted", new_lowest_price_formatted);
                                tracked_products.put(i, prod);
                                Log.i("prodprod", "prodprod "+ tracked_products.toString());
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("tracked_product", tracked_products.toString());
                                editor.commit();
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    setProductPrice(product_code, mContext);
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
