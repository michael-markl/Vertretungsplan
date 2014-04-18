package thems.vertretungsplan;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.ListPreference;
import android.preference.PreferenceManager;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Data {
    Date refreshDate;
    Date aushangDate;
    Date vPDate;
    List<String> lnames;
    List<String>lstunden;
    List<String>ldesc;
    List<String>knames;
    List<String>kstunden;
    List<String>kdesc;
    List<Integer> vstunden;
    List<String> vklassen;
    List<String> vabwesend;
    List<String> vvertretung;
    List<String> vraum;
    List<String> vdesc;
    String annotation;

    public Data(Date refreshDate, Date aushangDate, Date vpDate, List<String> lnames, List<String> lstunden, List<String> ldesc, List<String> knames, List<String> kstunden, List<String> kdesc,
                List<Integer> vstunden, List<String> vklassen,List<String> vabwesend, List<String> vvertretung, List<String> vraum, List<String> vdesc, String annotation) {
        this.refreshDate = refreshDate;
        this.aushangDate = aushangDate;
        this.vPDate = vpDate;
        this.lnames = lnames;
        this.lstunden = lstunden;
        this.ldesc = ldesc;
        this.knames = knames;
        this.kstunden = kstunden;
        this.kdesc = kdesc;
        this.vstunden = vstunden;
        this.vklassen = vklassen;
        this.vabwesend = vabwesend;
        this.vvertretung = vvertretung;
        this.vraum = vraum;
        this.vdesc = vdesc;
        this.annotation = annotation;
    }

    public static Boolean ToNotificate (String klasse, Context context, Activity activity) {
        if (Character.isDigit(klasse.charAt(0))) {
            String stufe = "";

            for (int i = 0; i < klasse.length(); i++) {
                if (Character.isDigit(klasse.charAt(i)))
                    stufe += klasse.charAt(i);
                else
                    break;
            }

            List<String> tolookup = new ArrayList<String>();

            String rest = klasse.substring(stufe.length());
            for (int i = 0; i < rest.length(); i++) {
                if (rest.charAt(i) == 'Q') {
                    tolookup.add("Q" + stufe);
                    break;
                } else if (rest.charAt(i) == 'a' || rest.charAt(i) == 'b' || rest.charAt(i) == 'c' || rest.charAt(i) == 'd') {
                    tolookup.add(stufe + rest.charAt(i));
                }
            }


            String[] Nvalues = ListPreferenceMultiSelect.parseStoredValue(PreferenceManager.getDefaultSharedPreferences(context).getString("klassennotification_list", null));
            String[] Astrings = activity.getResources().getStringArray(R.array.pref_klassennotification_list_strings);
            ArrayList<String> Nstrings = new ArrayList<String>();
            if (Nvalues != null) {
                for (int i = 0; i < Nvalues.length; i++) {
                    try {
                        Nstrings.add(Astrings[Integer.parseInt(Nvalues[i])]);
                    } catch (Exception e) {
                        return false;
                    }
                }

                if (Nstrings != null) {
                    for (int i = 0; i < tolookup.size(); i++) {
                        for (int ii = 0; ii < Nstrings.size(); ii++) {
                            if (Nstrings.get(ii).equals(tolookup.get(i)))
                                return true;
                        }
                    }
                }
            }
        }



        return false;
    }

    private static String getClassStringFromValue(int s) {
        switch (s)
        {
            case 0:
                return "5a";
            case 1:
                return "5b";
            case 2:
                return "5c";
            case 3:
                return "6a";
            case 4:
                return "6b";
            case 5:
                return "6c";
            case 6:
                return "7a";
            case 7:
                return "7b";
            case 8:
                return "7c";
            case 9:
                return "8a";
            case 10:
                return "8b";
            case 11:
                return "8c";
            case 12:
                return "9a";
            case 13:
                return "9b";
            case 14:
                return "9c";
            case 15:
                return "10a";
            case 16:
                return "10b";
            case 17:
                return "10c";
            case 18:
                return "Q11";
            case 19:
                return "Q12";
        }
        return null;
    }

    public static Data FormatString(String toFormat) throws ParseException {
        if(toFormat != null) {
            toFormat = Downloader.ReplaceSpecialCharacters(toFormat);
            Date AushangDate = getDateFromAushangString(toFormat.substring(toFormat.indexOf("Aushang") + 8, toFormat.indexOf("</p>") - 1));
            toFormat = toFormat.substring(toFormat.indexOf("Vertretungsplan für"));
            Date VPDate = getDateFromVPDateString(toFormat.substring(toFormat.indexOf(",") + 2, toFormat.indexOf("</p>") - 1));
            List<String> lnames = new ArrayList<String>();
            List<String> lstunden = new ArrayList<String>();
            List<String> ldesc = new ArrayList<String>();

            List<String> knames = new ArrayList<String>();
            List<String> kstunden = new ArrayList<String>();
            List<String> kdesc = new ArrayList<String>();

            List<Integer> vstunden = new ArrayList<Integer>();
            List<String> vklassen = new ArrayList<String>();
            List<String> vabwesend = new ArrayList<String>();
            List<String> vvertretung = new ArrayList<String>();
            List<String> vraum = new ArrayList<String>();
            List<String> vdesc = new ArrayList<String>();

            String annotation = "";

            while(toFormat.indexOf("<tr class=\"L\">") != -1)
            {
                lnames.add(toFormat.substring(toFormat.indexOf("<th rowspan=\"1\" class=\"L\">") + 26, toFormat.indexOf("</th>")));
                toFormat = toFormat.substring(toFormat.indexOf("<td>") + 4);
                lstunden.add(toFormat.substring(0,toFormat.indexOf("</td>")-1));
                toFormat = toFormat.substring(toFormat.indexOf("<td>") + 4);
                ldesc.add(toFormat.substring(0,toFormat.indexOf("</td>")));
                toFormat = toFormat.substring(toFormat.indexOf("</tr>") + 5);
            }

            while(toFormat.indexOf("<tr class=\"K\">") != -1)
            {
                knames.add(toFormat.substring(toFormat.indexOf("<th rowspan=\"1\" class=\"K\">") + 26, toFormat.indexOf("</th>")));
                toFormat = toFormat.substring(toFormat.indexOf("<td>") + 4);
                kstunden.add(toFormat.substring(0, toFormat.indexOf("</td>") - 1));
                toFormat = toFormat.substring(toFormat.indexOf("<td>") + 4);
                kdesc.add(toFormat.substring(0, toFormat.indexOf("</td>")));
            }

            while(toFormat.indexOf("<tr class=\"s\">") != -1)
            {
                toFormat = toFormat.substring(toFormat.indexOf("<th rowspan") + 26);
                Integer currstunde  = Integer.parseInt(toFormat.substring(0, toFormat.indexOf("</th>")));
                do {
                    vstunden.add(currstunde);
                    toFormat = toFormat.substring(toFormat.indexOf("<td>") + 4);
                    vklassen.add(toFormat.substring(0, toFormat.indexOf("</td>")));
                    toFormat = toFormat.substring(toFormat.indexOf("<td>") + 4);
                    vabwesend.add(toFormat.substring(0, toFormat.indexOf("</td>")));
                    toFormat = toFormat.substring(toFormat.indexOf("<td>") + 4);
                    vvertretung.add(toFormat.substring(0, toFormat.indexOf("</td>")));
                    toFormat = toFormat.substring(toFormat.indexOf("<td>") + 4);
                    vraum.add(toFormat.substring(0, toFormat.indexOf("</td>")));
                    toFormat = toFormat.substring(toFormat.indexOf("<td>") + 4);
                    vdesc.add(toFormat.substring(0, toFormat.indexOf("</td>")));
                    if(toFormat.indexOf("<tr>") == -1)
                        break;
                    if(toFormat.indexOf("<tr>") > toFormat.indexOf("</table>"))
                        break;

                    boolean exists = false;
                    if(toFormat.indexOf("<tr ") != -1)
                        exists = true;
                    if(toFormat.indexOf("<tr>") > toFormat.indexOf("<tr "))
                    {
                        if(exists)
                            break;
                    }
                }  while(toFormat.indexOf("<tr") != -1);

            }

            while(toFormat.indexOf("<th rowspan=\"1\" class=\"F\">") != -1)
            {
                toFormat = toFormat.substring(toFormat.indexOf("<th rowspan=\"1\" class=\"F\">") + 26);
                if(annotation != "" && annotation != null)
                    annotation = annotation.concat("\n");
                annotation = annotation.concat(toFormat.substring(0, toFormat.indexOf("</th>")));
            }
            Calendar calendar = new GregorianCalendar();
            Date refreshDate = calendar.getTime();

            return new Data(refreshDate, AushangDate, VPDate, lnames, lstunden, ldesc, knames, kstunden, kdesc, vstunden, vklassen, vabwesend, vvertretung, vraum, vdesc, annotation);
        }
        return null;
    }

    public static Date getDateFromVPDateString(String VPDateString) {
        String datestring = VPDateString;
        String FormatString = "";
        if(datestring.substring(0, datestring.indexOf(".")).length() == 1)
            FormatString += "d.";
        else
            FormatString += "dd.";
        datestring = datestring.substring(datestring.indexOf(".") + 1);
        if(datestring.substring(0, datestring.indexOf(".")).length() == 1)
            FormatString += "M.yyyy";
        else
            FormatString += "MM.yyyy";
        try {
            return new SimpleDateFormat(FormatString).parse(VPDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date getDateFromAushangString(String Aushang)  {
        String datestring = Aushang;
        String FormatString = "";
        if(datestring.substring(0, datestring.indexOf(".")).length() == 1)
            FormatString += "d.";
        else
            FormatString += "dd.";
        datestring = datestring.substring(datestring.indexOf(".") + 1);

        if(datestring.substring(0, datestring.indexOf(" ") - 1).length() == 1)
            FormatString += "M. ";
        else
            FormatString += "MM. ";
        datestring=datestring.substring(datestring.indexOf(".") + 2);

        if(datestring.substring(0,datestring.indexOf(":")).length() == 1)
            FormatString += "H:";
        else
            FormatString += "HH:";
        datestring = datestring.substring(datestring.indexOf(":") + 1);

        if(datestring.length() == 1)
            FormatString += "m yyyy";
        else
            FormatString += "mm yyyy";

        Calendar calendar = new GregorianCalendar();
        datestring = Aushang + " " + (calendar.getTime().getYear()+1900);

        Date dateAushang = null;
        try {
            dateAushang = new SimpleDateFormat(FormatString).parse(datestring);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateAushang;
    }

    public static void SaveDatas(Data[] datas, Activity activity) {
        if(datas == null || datas.length != 2 || datas[0] == null || datas[1] == null)
        {
            return;
        }

        Document doc = new Document();
        doc.setRootElement(new Element("Datas"));

        for (int i = 0; i < datas.length; i++) {

            Element dataElement = new Element("data");
            dataElement.setAttribute("vPDate", String.valueOf(datas[i].vPDate.getTime()));
            dataElement.setAttribute("aushangDate",  String.valueOf(datas[i].aushangDate.getTime()));
            dataElement.setAttribute("refreshDate", String.valueOf(datas[i].refreshDate.getTime()));
            dataElement.setAttribute("annotation", datas[i].annotation);

            Element klassenElement = new Element("Classes");
            for (int ii = 0; ii < datas[i].knames.size(); ii++) {
                Element klasseElement = new Element("Class");
                klasseElement.setAttribute("name", datas[i].knames.get(ii));
                klasseElement.setAttribute("hours", datas[i].kstunden.get(ii));
                klasseElement.setAttribute("description", datas[i].kdesc.get(ii));
                klassenElement.addContent(klasseElement);
            }
            dataElement.addContent(klassenElement);

            Element lehrerElement = new Element("LehrerAusfälle");
            for (int ii = 0; ii < datas[i].lnames.size(); ii++) {
                Element llehrerElement = new Element("LehrerAusfall");
                llehrerElement.setAttribute("name", datas[i].lnames.get(ii));
                llehrerElement.setAttribute("hours", datas[i].lstunden.get(ii));
                llehrerElement.setAttribute("description", datas[i].ldesc.get(ii));
                lehrerElement.addContent(llehrerElement);
            }
            dataElement.addContent(lehrerElement);

            Element vertretungenElement = new Element("Vertretungen");
            for (int ii = 0; ii < datas[i].vklassen.size(); ii++) {
                Element vertretungElement = new Element("Vertretung");
                vertretungElement.setAttribute("klassen", datas[i].vklassen.get(ii));
                vertretungElement.setAttribute("abwesend", datas[i].vabwesend.get(ii));
                vertretungElement.setAttribute("vertretung", datas[i].vvertretung.get(ii));
                vertretungElement.setAttribute("stunde", datas[i].vstunden.get(ii).toString());
                vertretungElement.setAttribute("raum", datas[i].vraum.get(ii));
                vertretungElement.setAttribute("description", datas[i].vdesc.get(ii));
                vertretungenElement.addContent(vertretungElement);
            }
            dataElement.addContent(vertretungenElement);


            doc.getRootElement().addContent(dataElement);
        }
        try {
            File f = new File(activity.getFilesDir(), "datas.xml");
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f);
            XMLOutputter serializer = new XMLOutputter();
            serializer.output(doc, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Data[] LoadDatas(Activity activity) {
        SAXBuilder builder = new SAXBuilder();
        File file = new File(activity.getFilesDir(), "datas.xml");

        try{
            Document doc = (Document)builder.build(file);

            if(doc.getRootElement().getChildren().size() == 2)
            {
                Data[] datas = new Data[2];
                for (int i =0 ; i < 2; i++)
                {
                    Element dataElement = doc.getRootElement().getChildren().get(i);
                    Date vPDate = new Date(Long.parseLong(dataElement.getAttributeValue("vPDate")));
                    Date aushangDate = new Date(Long.parseLong(dataElement.getAttributeValue("aushangDate")));
                    Date refreshDate = new Date(Long.parseLong(dataElement.getAttributeValue("refreshDate")));
                    String annotation = dataElement.getAttributeValue("annotation");

                    ArrayList<String>knames = new ArrayList<String>();
                    ArrayList<String>kdesc = new ArrayList<String>();
                    ArrayList<String>kstunden = new ArrayList<String>();

                    ArrayList<String>lnames = new ArrayList<String>();
                    ArrayList<String>lstunden = new ArrayList<String>();
                    ArrayList<String>ldesc = new ArrayList<String>();

                    ArrayList<String>vklasse = new ArrayList<String>();
                    ArrayList<String>vabwesend = new ArrayList<String>();
                    ArrayList<String>vvertretung = new ArrayList<String>();
                    ArrayList<Integer>vstunde = new ArrayList<Integer>();
                    ArrayList<String>vraum = new ArrayList<String>();
                    ArrayList<String>vdesc = new ArrayList<String>();


                    Element ClassesElement = dataElement.getChild("Classes");
                    for(int ii = 0; ii < ClassesElement.getChildren().size(); ii++)
                    {
                        Element ClassElement = ClassesElement.getChildren().get(ii);
                        knames.add(ClassElement.getAttributeValue("name"));
                        kstunden.add(ClassElement.getAttributeValue("hours"));
                        kdesc.add(ClassElement.getAttributeValue("description"));
                    }

                    Element LehrerAusfälleElement = dataElement.getChild("LehrerAusfälle");
                    for(int ii = 0; ii < LehrerAusfälleElement.getChildren().size(); ii++)
                    {
                        Element LehrerAusfall = LehrerAusfälleElement.getChildren().get(ii);
                        lnames.add(LehrerAusfall.getAttributeValue("name"));
                        lstunden.add(LehrerAusfall.getAttributeValue("hours"));
                        ldesc.add(LehrerAusfall.getAttributeValue("description"));
                    }

                    Element VertretungenElement = dataElement.getChild("Vertretungen");
                    for (int ii = 0; ii < VertretungenElement.getChildren().size(); ii++)
                    {
                        Element Vertretung = VertretungenElement.getChildren().get(ii);
                        vklasse.add(Vertretung.getAttributeValue("klassen"));
                        vabwesend.add(Vertretung.getAttributeValue("abwesend"));
                        vvertretung.add(Vertretung.getAttributeValue("vertretung"));
                        vstunde.add(Integer.parseInt(Vertretung.getAttributeValue("stunde")));
                        vraum.add(Vertretung.getAttributeValue("raum"));
                        vdesc.add(Vertretung.getAttributeValue("description"));
                    }
                    Data data = new Data(refreshDate,aushangDate,vPDate,lnames,lstunden,ldesc,knames,kstunden,kdesc,vstunde,vklasse,vabwesend,vvertretung,vraum,vdesc,annotation);
                    datas[i] = data;
                }
                return datas;
            }
        }
        catch (Exception e)
        {

        }

        return null;
    }
}
