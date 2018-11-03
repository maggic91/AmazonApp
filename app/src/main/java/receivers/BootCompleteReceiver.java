package receivers;

/**
 * Created by andreafurlan on 04/05/18.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import services.PriceCheckerService;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent service = new Intent(context, PriceCheckerService.class);
        context.startService(service);

    }

}
