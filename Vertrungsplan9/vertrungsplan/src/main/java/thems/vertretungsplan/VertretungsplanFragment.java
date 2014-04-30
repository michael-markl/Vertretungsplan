package thems.vertretungsplan;

import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Michael on 12.01.14.
 */

public class VertretungsplanFragment extends Fragment implements DataDisplay {
    TextView vpDateTextView;
    TextView aushangTextView;
    TextView refreshTextView;
    RelativeLayout rootView;
    LinearLayout datesLinearLayout;
    int mDisplayMode;

    LinearLayout lehrerLinearLayout;
    LinearLayout klassenLinearLayout;
    LinearLayout vertretungenLinearLayout;
    TextView annotationTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vertretungsplan, container, false);
        mDisplayMode = this.getArguments().getInt(TabHostFragment.ARG_DISPLAY_MODE);
        vpDateTextView = (TextView) view.findViewById(R.id.datevp);
        aushangTextView = (TextView) view.findViewById(R.id.dateaushang);
        refreshTextView = (TextView) view.findViewById(R.id.daterefresh);
        rootView = (RelativeLayout) view.findViewById(R.id.rootview);
        datesLinearLayout = (LinearLayout) view.findViewById(R.id.dates);
        klassenLinearLayout = (LinearLayout) view.findViewById(R.id.klassencontent);
        lehrerLinearLayout = (LinearLayout) view.findViewById(R.id.lehrercontent);
        vertretungenLinearLayout = (LinearLayout) view.findViewById(R.id.vertretungencontent);
        annotationTextView = (TextView) view.findViewById(R.id.annotationtextview);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Thread thr = new Thread(new Runnable() {
            @Override
            public void run() {
                if (((MainActivity)getActivity()).mProgressBarVisible == true)
                    ((DatasHolder) getParentFragment()).setDatas(((MainActivity) getActivity()).lastDatas, "VertretungsplanF onCreateView");
            }
        });
        thr.start();
    }

    public void setData(Data data, String origin) {
        if (this.isAdded()) {
            if (data != null) {
                getActivity().runOnUiThread(new ObjectRunnable(data) {
                    @Override
                    public void run() {
                        Data data = (Data) object;
                        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
                        vpDateTextView.setText(df.format(data.vPDate));
                        df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                        aushangTextView.setText("Aushang: " + df.format(data.aushangDate));
                        df = DateFormat.getTimeInstance(DateFormat.SHORT);
                        refreshTextView.setText("Aktualisiert: " + df.format(data.refreshDate));
                        datesLinearLayout.setVisibility(View.VISIBLE);
                    }
                });

                if (mDisplayMode == TabHostFragment.VAL_DISPLAY_OVERVIEW) {
                    setLehrerLinearLayout(data);
                    setAnnotationLinearLayout(data);
                }
                else if(mDisplayMode == TabHostFragment.VAL_DISPLAY_SUBSCRIBED) {
                    Boolean showannotations = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("annotationatsubscribedswitch", true);
                    if(showannotations)
                    {
                        setAnnotationLinearLayout(data);
                    }
                }
                setKlassenLinearLayout(data);
                setVertretungenLinearLayout(data, origin);

            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Fehler bei der Verbindung.", 2000);
                    }
                });
            }
        }
    }

    public void setKlassenLinearLayout(Data data) {
        String[] fromk = new String[]{"name", "absenttime", "description"};
        int[] tok = new int[]{R.id.name, R.id.absenttime, R.id.description};


        List<HashMap<String, String>> fillMapsk = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < data.knames.size(); i++) {
            if (Data.ToNotificate(data.knames.get(i), getActivity()) || mDisplayMode == TabHostFragment.VAL_DISPLAY_OVERVIEW) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("name", data.knames.get(i));
                map.put("absenttime", data.kstunden.get(i));
                map.put("description", data.kdesc.get(i));
                fillMapsk.add(map);
            }
        }
        if (fillMapsk.size() != 0) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout ll = (LinearLayout) klassenLinearLayout.getParent();
                    ll.setVisibility(View.VISIBLE);
                }
            });
            SimpleAdapter adapter = new SimpleAdapter(getActivity().getApplicationContext(), fillMapsk, R.layout.lehrer_linearlayout, fromk, tok);

            getActivity().runOnUiThread(new ObjectRunnable(adapter) {
                @Override
                public void run() {
                    klassenLinearLayout.removeAllViews();
                    SimpleAdapter adapter = (SimpleAdapter) object;
                    for (int i = 0; i < adapter.getCount(); i++)
                        klassenLinearLayout.addView(adapter.getView(i, null, null));
                }
            });

        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout ll = (LinearLayout) klassenLinearLayout.getParent();
                    ll.setVisibility(View.GONE);
                }
            });
        }
    }

    public void setLehrerLinearLayout(Data data) {
        if (data.lnames.size() != 0) {
            if (((LinearLayout) lehrerLinearLayout.getParent()).getVisibility() != View.VISIBLE) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayout ll = (LinearLayout) lehrerLinearLayout.getParent();
                        ll.setVisibility(View.VISIBLE);
                    }
                });
            }

            String[] froml = new String[]{"teachername", "absenttime", "description"};
            int[] tol = new int[]{R.id.name, R.id.absenttime, R.id.description};


            List<HashMap<String, String>> fillMapsl = new ArrayList<HashMap<String, String>>();
            for (int i = 0; i < data.lnames.size(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("teachername", data.lnames.get(i));
                map.put("absenttime", data.lstunden.get(i));
                map.put("description", data.ldesc.get(i));
                fillMapsl.add(map);
            }

            SimpleAdapter adapter = new SimpleAdapter(getActivity().getApplicationContext(), fillMapsl, R.layout.lehrer_linearlayout, froml, tol);
            getActivity().runOnUiThread(new ObjectRunnable(adapter) {
                @Override
                public void run() {
                    lehrerLinearLayout.removeAllViews();
                    SimpleAdapter adapter = (SimpleAdapter) object;
                    for (int i = 0; i < adapter.getCount(); i++) {
                        lehrerLinearLayout.addView(adapter.getView(i, null, null));
                    }
                }
            });

        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout ll = (LinearLayout) lehrerLinearLayout.getParent();
                    ll.setVisibility(View.GONE);
                }
            });
        }
    }

    public void setVertretungenLinearLayout(Data data, String origin) {


        String[] fromv = new String[]{"vklasse", "vabwesend", "vvertretung", "vraum", "vdesc"};
        int[] tov = new int[]{R.id.vklasse, R.id.vabwesend, R.id.vvertretung, R.id.vraum, R.id.vdesc};

        List<Integer> vstunden = null;
        List<String> vklassen = null;
        List<String> vabwesend = null;
        List<String> vvertretung = null;
        List<String> vraum = null;
        List<String> vdesc = null;

        if (mDisplayMode == TabHostFragment.VAL_DISPLAY_SUBSCRIBED) {
            vstunden = new ArrayList<Integer>();
            vklassen = new ArrayList<String>();
            vabwesend = new ArrayList<String>();
            vvertretung = new ArrayList<String>();
            vraum = new ArrayList<String>();
            vdesc = new ArrayList<String>();
            for (int i = 0; i < data.vklassen.size(); i++) {
                if (Data.ToNotificate(data.vklassen.get(i), getActivity())) {
                    vstunden.add(data.vstunden.get(i));
                    vklassen.add(data.vklassen.get(i));
                    vabwesend.add(data.vabwesend.get(i));
                    vvertretung.add(data.vvertretung.get(i));
                    vraum.add(data.vraum.get(i));
                    vdesc.add(data.vdesc.get(i));
                }
            }
        } else if (mDisplayMode == TabHostFragment.VAL_DISPLAY_OVERVIEW) {
            vstunden = data.vstunden;
            vklassen = data.vklassen;
            vabwesend = data.vabwesend;
            vvertretung = data.vvertretung;
            vraum = data.vraum;
            vdesc = data.vdesc;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() { vertretungenLinearLayout.removeAllViews(); }});
        if (vklassen.size() != 0) {
            List<HashMap<String, String>> stunden = new ArrayList<HashMap<String, String>>();
            List<HashMap<String, String>> fillMapsv = new ArrayList<HashMap<String, String>>();
            int currstunde = 0;
            for (int i = 0; i < vklassen.size(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("vklasse", vklassen.get(i));
                map.put("vabwesend", vabwesend.get(i));
                map.put("vvertretung", vvertretung.get(i));
                map.put("vraum", vraum.get(i));
                map.put("vdesc", vdesc.get(i));
                fillMapsv.add(map);
                HashMap<String, String> hashmap = new HashMap<String, String>();
                hashmap.put("stunde_name", vstunden.get(i).toString() + ". Stunde");
                if (vstunden.get(i) != currstunde)
                    stunden.add(hashmap);
                currstunde = vstunden.get(i);
            }
            SimpleAdapter adapter = new SimpleAdapter(getActivity().getApplicationContext(), fillMapsv, R.layout.vertretungen_linearlayout, fromv, tov);
            SimpleAdapter adapter2 = new SimpleAdapter(getActivity().getApplicationContext(), stunden, R.layout.stunde_linearlayout, new String[]{"stunde_name"}, new int[]{R.id.stunden_name});

            List<LinearLayout> views = new ArrayList<LinearLayout>();

            for (int i = 0; i < adapter2.getCount(); i++) {
                views.add((LinearLayout) adapter2.getView(i, null, null));
            }
            for (int i = 0; i < adapter2.getCount(); i++) {
                LinearLayout ll = (LinearLayout) adapter2.getView(i, null, null);
                TextView tv = (TextView) ll.findViewById(R.id.stunden_name);
                String stunde = tv.getText().toString();
                for (int i2 = 0; i2 < vstunden.size(); i2++) {
                    if (stunde.equals(vstunden.get(i2).toString() + ". Stunde"))
                        ll.addView(adapter.getView(i2, null, null));
                }

                getActivity().runOnUiThread(new ObjectRunnable(ll) {
                    @Override
                    public void run() {
                        LinearLayout ll = (LinearLayout) object;
                        vertretungenLinearLayout.addView(ll);
                    }
                });
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout ll = (LinearLayout) vertretungenLinearLayout.getParent();
                    ll.setVisibility(View.VISIBLE);
                }
            });
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView textView = new TextView(getActivity());
                    textView.setText("Es stehen derzeit keine Vertretungen an.");
                    textView.setTextAppearance(getActivity(), R.style.nowCardExceptionStyle);
                    vertretungenLinearLayout.addView(textView);
                    ((LinearLayout.LayoutParams)textView.getLayoutParams()).setMargins(5,30,5,10);
                ((LinearLayout)vertretungenLinearLayout.getParent()).setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void setAnnotationLinearLayout(Data data) {
        if (data.annotation != null && !data.annotation.equals("")) {

            getActivity().runOnUiThread(new ObjectRunnable(data.annotation) {
                @Override
                public void run() {
                    annotationTextView.setText((String) object);
                    ((LinearLayout) annotationTextView.getParent()).setVisibility(View.VISIBLE);;
                }
            });
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() { ((LinearLayout) annotationTextView.getParent()).setVisibility(View.GONE); }});
        }
    }
}