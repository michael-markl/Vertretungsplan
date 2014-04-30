package thems.vertretungsplan;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;

/**
 * Created by Michael on 21.04.2014.
 */

public class DownloadIntent extends IntentService implements DataDisplay {

    public DownloadIntent() {
        super("DownloadIntent");
    }

    ArrayList<Data> mDatas = new ArrayList<Data>();

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();

        Data[] lastdatas = Data.LoadDatas(context);
        Downloader downloader = new Downloader();
        downloader.execute(new Object[]{Downloader.URL_TODAY, this, context, lastdatas, "DownloadIntent"});
        Downloader downloader2 = new Downloader();
        downloader2.execute(new Object[]{Downloader.URL_TOMORROW, this, context, lastdatas, "DownloadIntent"});

        long sleeptime = 100;
        while(downloader.getStatus() != AsyncTask.Status.FINISHED || downloader2.getStatus() != AsyncTask.Status.FINISHED)
        {
            SystemClock.sleep(sleeptime);
            sleeptime *= 1.5;
        }

        Data[] datas = mDatas.toArray(new Data[mDatas.size()]);

        if(datas[0] != null && datas[1] != null)
        {
            Data.SaveDatas(datas, context, "DownloadIntent");

            for(int i = 0; i < datas.length; i++)
            {
                for(int ii = 0; ii < datas[i].vklassen.size(); ii++)
                {

                    if(Data.ToNotificate(datas[i].vklassen.get(ii),context)){
                        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                        builder.setContentText("Vertretung für " + datas[i].vklassen.get(ii));
                        builder.setContentTitle("Vertretung");
                        builder.setSmallIcon(R.drawable.ic_launcher);
                        builder.setAutoCancel(true);

                        Intent intent1 = new Intent(this, MainActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                        builder.setContentIntent(pendingIntent);


                        manager.notify(0, builder.build());
                    }
                }
            }
        }
    }

    @Override
    public void setData(Data data, String origin) {
        mDatas.add(data);
    }
}