package thems.vertretungsplan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Michael on 25.04.2014.
 */
public class AlarmReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Context oAppContext = context.getApplicationContext();

        if (oAppContext == null) {
            oAppContext = context;
        }

        Intent serviceIntent = new Intent(oAppContext, DownloadIntent.class);
        oAppContext.startService(serviceIntent);

        MainActivity.InitiateAlarm(oAppContext);
    }
}
