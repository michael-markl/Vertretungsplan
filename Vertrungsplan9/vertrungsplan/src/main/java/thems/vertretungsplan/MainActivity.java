package thems.vertretungsplan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.Window;

import java.io.File;
import java.util.List;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, DataDisplay {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    public Downloader[] downloaders = new Downloader[0];
    public Data[] lastDatas = null;
    Boolean mProgressBarVisible = true;
    MenuItem mActionRefreshMenuItem;
    Boolean mActionRefreshMenuItemVisible = false;
    public static final String ARG_SECTION_NUMBER = "section_number";
    private CharSequence mTitle;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        setSupportProgressBarIndeterminateVisibility(true);
        downloaders = new Downloader[]{new Downloader(), new Downloader()};
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getString(R.string.title_section1);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        if(Build.VERSION.SDK_INT < 11)
            getSupportActionBar().setLogo(R.drawable.transparent);

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("FirstStart", true))
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("FirstStart", false);
            editor.commit();
            InitiateAlarm(this);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, position + 1);
        switch (position)
        {
            case 0:
                fragment = new TabHostFragment();
                args.putInt(TabHostFragment.ARG_DISPLAY_MODE, TabHostFragment.VAL_DISPLAY_SUBSCRIBED);
                break;
            case 1:
                fragment = new TabHostFragment();
                args.putInt(TabHostFragment.ARG_DISPLAY_MODE, TabHostFragment.VAL_DISPLAY_OVERVIEW);
                break;
        }
        fragment.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        setSupportProgressBarIndeterminateVisibility(true);
        mProgressBarVisible = true;
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_sectionsubscribed);
                break;
            case 2:
                mTitle = getString(R.string.title_sectionoverview);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            mActionRefreshMenuItem = menu.findItem(R.id.action_refresh);
            mActionRefreshMenuItem.setVisible(mActionRefreshMenuItemVisible);
            setSupportProgressBarIndeterminateVisibility(!mActionRefreshMenuItemVisible);
            mProgressBarVisible = !mActionRefreshMenuItemVisible;
            restoreActionBar();
            return true;
        }
        else {
           // setSupportProgressBarIndeterminateVisibility(false);
           // mProgressBarVisible = false;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this,SettingsActivity.class);
            startActivity(i);
            return true;
        }
        else if(id == R.id.action_refresh){
            refreshDatas();
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshDatas() {
        if (lastDatas == null) {
            if(new File(getFilesDir(), "datas.xml").exists()) {
                lastDatas = new Data[1];
                Data[] datas = Data.LoadDatas(this);
                lastDatas[0] = datas[0];
                setData(datas[1],"RefreshDatas");
                return;
            }
        }
        if (downloaders[0].getStatus() != AsyncTask.Status.RUNNING && downloaders[1].getStatus() != AsyncTask.Status.RUNNING) {
            if(downloaders[0].getStatus() == AsyncTask.Status.FINISHED && downloaders[1].getStatus() == AsyncTask.Status.FINISHED){
                downloaders[0] = new Downloader();
                downloaders[1] = new Downloader();
            }
            setSupportProgressBarIndeterminateVisibility(true);
            mProgressBarVisible = true;
            mActionRefreshMenuItemVisible = false;
            if (mActionRefreshMenuItem != null)
                mActionRefreshMenuItem.setVisible(false);
            restoreActionBar();

            downloaders[0].execute(new Object[]{Downloader.URL_TODAY, this, this, lastDatas, "MA RefreshDatas"});
            downloaders[1].execute(new Object[]{Downloader.URL_TOMORROW, this, this, lastDatas, "MA RefreshDatas"});

            lastDatas = new Data[0];
        }

    }

    @Override
    public void setData(Data data, String origin) {
        if(lastDatas == null)
            lastDatas = new Data[0];
        if(lastDatas.length != 1)
            lastDatas = new Data[]{data};
        else if(lastDatas.length == 1){
            Data fdata = lastDatas[0];

            lastDatas = new Data[]{fdata, data};
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mActionRefreshMenuItem.setVisible(true);
                    mActionRefreshMenuItemVisible = true;
                    setSupportProgressBarIndeterminateVisibility(false);
                    mProgressBarVisible = false;
                }
            });

            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (int i = 0; i < fragments.size(); i++)
            {
                if(fragments.get(i) instanceof DatasHolder)
                    ((DatasHolder)fragments.get(i)).setDatas(lastDatas, origin + " + MA SetData");
            }

            Data.SaveDatas(lastDatas, this, origin + "MA SetData");

        }
    }

    public static void InitiateAlarm(Context context) {

        Intent intent1 = new Intent (context, AlarmReceiver.class);

        PendingIntent alarmintent = PendingIntent.getBroadcast(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        manager.cancel(alarmintent);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        int i = Integer.parseInt( preferences.getString("aktualisierungsintervall_list", "-1"));
        if(i > 0)
        {
            long interval = i * 1000 * 60;
            manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, alarmintent);
        }

    }
}
