package services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import receivers.PriceCheckerReceiver;

/**
 * Created by andreafurlan on 04/05/18.
 */

public class PriceCheckerService extends Service {
    //PowerManager pm;
    //PowerManager.WakeLock wl;

    private PendingIntent pendingIntent;
    private AlarmManager manager;
    Handler handler = new Handler();
    private Runnable periodicUpdate = new Runnable() {
        @Override
        public void run() {
            //handler.postDelayed(periodicUpdate, 10*1000 - SystemClock.elapsedRealtime()%1000);
            Intent alarmIntent = new Intent(getApplicationContext(), PriceCheckerReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, 0);
            manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000*60*10, pendingIntent);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        handler.post(periodicUpdate);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        //pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GpsTrackerWakelock");
        //wl.acquire();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //wl.release();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
