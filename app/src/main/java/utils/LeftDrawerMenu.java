package utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.devspark.robototextview.widget.RobotoTextView;
import com.orma.amazonapp.MainActivity;
import com.orma.amazonapp.TrackedProductsActivity;
import com.orma.amazonapp.R;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import adapters.DrawerAdapter;
import adapters.DrawerItem;

/**
 * Created by elvis.pobric on 15/05/2017.
 */

public class LeftDrawerMenu {

    private Context context;
    private DrawerLayout drawerLayout;
    private DrawerAdapter mAdapter;
    private ListView mDrawerList;
    private Activity activity;
   // private Shared shared;

    private String user_n_messages = "";

    DrawerLayout.DrawerListener myDrawerListener = new DrawerLayout.DrawerListener(){

        @Override
        public void onDrawerClosed(View drawerView) {
            //textPrompt.setText("onDrawerClosed");

            Log.i("draweraaa", "onDrawerClosed");
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            //textPrompt.setText("onDrawerOpened");

            Log.i("draweraaa", "onDrawerOpened");
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            //textPrompt.setText("onDrawerSlide: " + String.format("%.2f", slideOffset));
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            String state;
            switch(newState){
                case DrawerLayout.STATE_IDLE:
                    state = "STATE_IDLE";
                    break;
                case DrawerLayout.STATE_DRAGGING:
                    state = "STATE_DRAGGING";
                    break;
                case DrawerLayout.STATE_SETTLING:
                    state = "STATE_SETTLING";
                    break;
                default:
                    state = "unknown!";
            }

            Log.i("draweraaa", state);
        }
    };

    public LeftDrawerMenu(Activity ctx, DrawerLayout drawerLayout, Toolbar toolbar){
        context = ctx;
        activity = (Activity)ctx;

        //shared = new Shared(activity);

        this.drawerLayout = drawerLayout;

        LinearLayout linear_drawer = (LinearLayout)activity.findViewById(R.id.linear_drawer);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(activity, drawerLayout,toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(myDrawerListener);
        mDrawerToggle.syncState();

        mDrawerList = (ListView) linear_drawer.findViewById(R.id.drawer_list);

        //txt_username.setText(shared.getUserRealName());

        this.addItems();
    }

    private void addItems() {

        mAdapter = new DrawerAdapter(context, this.getItems());
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DrawerItem item = (DrawerItem) parent.getItemAtPosition(position);

                drawerLayout.closeDrawer(GravityCompat.START);

                if(item.id == Constants.AMAZON){
                    if(activity instanceof MainActivity){
                        return;
                    }

                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);

                    return;
                }

                if(item.id == Constants.PRODOTTI_TRACCIATI){
                    if(activity instanceof TrackedProductsActivity){
                        return;
                    }

                    Intent intent = new Intent(context, TrackedProductsActivity.class);
                    context.startActivity(intent);

                    return;
                }

                if(item.id == Constants.OFFERTE){

                }


            }
        });
    }

    private ArrayList<DrawerItem> getItems(){
        ArrayList<DrawerItem> items = new ArrayList<>();

        items.add(new DrawerItem(Constants.AMAZON, R.drawable.ic_shopping_cart_black_24dp, R.drawable.ic_shopping_cart_black_24dp, activity.getString(R.string.amazon), ""));
        items.add(new DrawerItem(Constants.PRODOTTI_TRACCIATI, R.drawable.ic_local_offer_black_24dp, R.drawable.ic_local_offer_black_24dp, activity.getString(R.string.prodotti_tracciati), ""));

        items.add(new DrawerItem(Constants.OFFERTE, R.drawable.ic_shopping_cart_black_24dp, R.drawable.ic_shopping_cart_black_24dp, activity.getString(R.string.offerte), ""));
        //items.add(new DrawerItem(Constants.DRAWER.PROFILO, R.drawable.bg_cyrcle_pallino_light, R.drawable.icon_person, activity.getString(R.string.profilo), ""));

        /*try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            String version = pInfo.versionName;
            items.add(new DrawerItem(Constants.DRAWER.VERSION, null, null, activity.getString(R.string.versione, version), ""));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }*/

        return items;
    }

}
