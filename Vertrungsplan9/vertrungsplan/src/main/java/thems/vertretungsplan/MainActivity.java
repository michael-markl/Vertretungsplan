package thems.vertretungsplan;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.Window;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, DataDisplay {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    public Downloader[] downloaders = new Downloader[0];
    public Data[] lastDatas = new Data[0];
    MenuItem mActionRefreshMenuItem;
    Boolean mActionRefreshMenuItemVisible = true;
    public static final String ARG_SECTION_NUMBER = "section_number";
    private CharSequence mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        downloaders = new Downloader[]{new Downloader(), new Downloader()};
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getString(R.string.title_section1);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
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
            case 2:
                fragment = PlaceholderFragment.newInstance(position + 1);
                break;
        }
        fragment.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
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
        actionBar.setIcon(R.drawable.white);
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
            restoreActionBar();
            return true;
        }
        else
            setSupportProgressBarIndeterminateVisibility(false);
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

            /*setSupportProgressBarIndeterminateVisibility(true);
            item.setVisible(false);
            mActionRefreshMenuItem = item;
            Downloader downloader = new Downloader();
            downloader.execute(new Object[]{"http://gym.ottilien.de/images/Service/Vertretungsplan/docs/heute.html", this, this});
            downloader = new Downloader();
            downloader.execute(new Object[]{"http://gym.ottilien.de/images/Service/Vertretungsplan/docs/morgen.html", this, this});

            /*FragmentManager fm = getSupportFragmentManager();
            List<Fragment> fr = fm.getFragments();
            for(int i = 0 ; i < fr.size(); i++)
            {
                if(TabHostFragment.class.isInstance(fr.get(i)))
                {
                    if(fr.get(i).isResumed()) {
                        List<Fragment> fr2 = fr.get(i).getChildFragmentManager().getFragments();
                        for(int i2 = 0; i2 < fr2.size(); i2++)
                        {
                            if(VertretungsplanFragment.class.isInstance(fr2.get(i2)))
                            {
                                VertretungsplanFragment fragment = (VertretungsplanFragment) fr2.get(i2);
                                fragment.startDownload();
                            }
                        }
                    }
                }
            }*/
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshDatas() {
        if (downloaders[0].getStatus() != AsyncTask.Status.RUNNING && downloaders[1].getStatus() != AsyncTask.Status.RUNNING) {
            if(downloaders[0].getStatus() == AsyncTask.Status.FINISHED && downloaders[1].getStatus() == AsyncTask.Status.FINISHED){
                downloaders[0] = new Downloader();
                downloaders[1] = new Downloader();
            }
            lastDatas = new Data[0];
            setSupportProgressBarIndeterminateVisibility(true);
            mActionRefreshMenuItemVisible = false;
            if (mActionRefreshMenuItem != null)
                mActionRefreshMenuItem.setVisible(false);
            restoreActionBar();
            downloaders[0].execute(new Object[]{"http://gym.ottilien.de/images/Service/Vertretungsplan/docs/heute.html", this, this});
            downloaders[1].execute(new Object[]{"http://gym.ottilien.de/images/Service/Vertretungsplan/docs/morgen.html", this, this});
        }
    }

    @Override
    public void setData(Data data) {
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
                }
            });

            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (int i = 0; i < fragments.size(); i++)
            {
                if(fragments.get(i) instanceof DatasHolder)
                    ((DatasHolder)fragments.get(i)).setDatas(lastDatas);
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static Fragment newInstance(int sectionNumber) {
            Fragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
