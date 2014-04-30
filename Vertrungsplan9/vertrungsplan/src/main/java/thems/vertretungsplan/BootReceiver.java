package thems.vertretungsplan;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by Michael on 21.04.2014.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            MainActivity.InitiateAlarm( context);

        }
    }
}
