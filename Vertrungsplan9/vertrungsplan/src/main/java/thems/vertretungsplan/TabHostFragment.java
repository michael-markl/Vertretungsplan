package thems.vertretungsplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;


public class TabHostFragment extends Fragment implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener, DatasHolder {

    public static final String ARG_DISPLAY_MODE = "display_mode";
    public static final int VAL_DISPLAY_SUBSCRIBED = 0;
    public static final int VAL_DISPLAY_OVERVIEW = 1;
    private TabHost mTabHost;
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, TabInfo>();
    private FragmentManager fragmentManager;

    @Override
    public void setDatas(Data[] datas, String origin) {
        if (((FragmentPagerAdapter)mPagerAdapter).getItem(0).isResumed() && ((FragmentPagerAdapter)mPagerAdapter).getItem(1).isResumed()) {
            if (datas.length == 2) {
                List<Fragment> fragments = getChildFragmentManager().getFragments();
                int datatoset = 0;
                if (fragments != null && fragments.size() == 2) {
                    if(datas[0] != null && datas[1] != null) {
                        if (datas[0].vPDate.before(datas[1].vPDate)) {
                            ((DataDisplay) fragments.get(0)).setData(datas[0], origin + " + TabHostF setDatas");
                            ((DataDisplay) fragments.get(1)).setData(datas[1], origin + " + TabHostF setDatas");
                        } else {
                            ((DataDisplay) fragments.get(0)).setData(datas[1], origin + " + TabHostF setDatas");
                            ((DataDisplay) fragments.get(1)).setData(datas[0], origin + " + TabHostF setDatas");
                        }
                    }
                    else
                    {
                        ((DataDisplay) fragments.get(0)).setData(null, origin + " + TabHostF setDatas");
                        ((DataDisplay) fragments.get(1)).setData(null, origin + " + TabHostF setDatas");
                    }
                }
            } else {
                ((MainActivity) getActivity()).refreshDatas();
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    private class TabInfo {
        private String tag;
        private Class<?> clss;
        private Bundle args;
        private Fragment fragment;
        TabInfo(String tag, Class<?> clazz, Bundle args) {
            this.tag = tag;
            this.clss = clazz;
            this.args = args;
        }

    }

    class TabFactory implements TabContentFactory {

        private final Context mContext;

        public TabFactory(Context context) {
            mContext = context;
        }

        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }

    }

    public TabHostFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(MainActivity.ARG_SECTION_NUMBER));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_tabhost, container, false);

        initialiseTabHost(savedInstanceState, view);
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
        }
        intialiseViewPager(view);

        if(((MainActivity)getActivity()).lastDatas != null) {
            Thread thr = new Thread(new Runnable() {
                @Override
                public void run() {
                    if((MainActivity)getActivity() != null){
                    if(((MainActivity)getActivity()).lastDatas != null)
                        setDatas(((MainActivity)getActivity()).lastDatas, "TabHostF onCreateView");
                }}
            });
            thr.start();
        }
        return view;
    }

    private void intialiseViewPager(View view) {

        List<Fragment> fragments = new ArrayList<Fragment>();

        Fragment fr = new VertretungsplanFragment();
        Bundle args = new Bundle();
        //args.putString(VertretungsplanFragment.ARG_URL, "http://gym.ottilien.de/images/Service/Vertretungsplan/docs/heute.html");
        args.putInt(ARG_DISPLAY_MODE, this.getArguments().getInt(ARG_DISPLAY_MODE));
        fr.setArguments(args);
        fragments.add(fr);

        fr = new VertretungsplanFragment();
        args = new Bundle();
        //args.putString(VertretungsplanFragment.ARG_URL, "http://gym.ottilien.de/images/Service/Vertretungsplan/docs/morgen.html");
        args.putInt(ARG_DISPLAY_MODE, this.getArguments().getInt(ARG_DISPLAY_MODE));
        fr.setArguments(args);
        fragments.add(fr);

        fragmentManager = getChildFragmentManager();
        this.mPagerAdapter  = new thems.vertretungsplan.PagerAdapter(fragmentManager, fragments);

        this.mViewPager = (ViewPager)view.findViewById(R.id.viewpager);
        this.mViewPager.setAdapter(this.mPagerAdapter);
        this.mViewPager.setOnPageChangeListener(this);
    }

    private void initialiseTabHost(Bundle args, View view) {
        mTabHost = (TabHost) view.findViewById(android.R.id.tabhost);
        mTabHost.setup();
        setupTab(new TextView(getActivity()), "Heute");
        setupTab(new TextView(getActivity()), "Morgen");
        //AddTab((MainActivity)getActivity(), this.mTabHost, this.mTabHost.newTabSpec("Tab1").setIndicator("Heute"), ( tabInfo = new TabInfo("Tab1", VertretungsplanFragment.class, args)));
        //this.mapTabInfo.put(tabInfo.tag, tabInfo);

        //AddTab((MainActivity)getActivity(), this.mTabHost, this.mTabHost.newTabSpec("Tab2").setIndicator("Morgen"), ( tabInfo = new TabInfo("Tab2", VertretungsplanFragment.class, args)));
        //this.mapTabInfo.put(tabInfo.tag, tabInfo);

        mTabHost.setOnTabChangedListener(this);
    }

    private void setupTab(final View view, final String tag) {
        View tabview = createTabView(mTabHost.getContext(), tag);
        TabHost.TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(new TabContentFactory() {
            public View createTabContent(String tag) {return view;}
        });
        AddTab((MainActivity) getActivity(), mTabHost, setContent);
    }

    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    private void AddTab(MainActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec) {
        tabSpec.setContent(new TabFactory(activity));
        tabHost.addTab(tabSpec);
    }

    @Override
    public void onTabChanged(String tag) {
        //TabInfo newTab = this.mapTabInfo.get(tag);
        int pos = this.mTabHost.getCurrentTab();
        this.mViewPager.setCurrentItem(pos);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageSelected(int position) {
        // TODO Auto-generated method stub
        this.mTabHost.setCurrentTab(position);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
