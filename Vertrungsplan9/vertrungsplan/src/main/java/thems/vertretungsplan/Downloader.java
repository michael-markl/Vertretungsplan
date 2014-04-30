package thems.vertretungsplan;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom2.JDOMException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamCorruptedException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ConnectionPendingException;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by Michael on 12.01.14.
 */
public class Downloader extends AsyncTask<Object, Void, Data> {
    public static final String URL_TODAY = "http://gym.ottilien.de/images/Service/Vertretungsplan/docs/heute.html";
    public static final String URL_TOMORROW = "http://gym.ottilien.de/images/Service/Vertretungsplan/docs/morgen.html";

    DataDisplay dataDisplay;
    Context context;
    Data data;



    public static String ReplaceSpecialCharacters(String input){

        input = input.replace("&szlig;","ß");
        input = input.replace("&Auml;","Ä");
        input = input.replace("&auml;","ä");
        input = input.replace("&Ouml;","Ö");
        input = input.replace("&ouml;","ö");
        input = input.replace("&Uuml;","Ü");
        input = input.replace("&uuml;","ü");
        input = input.replace("&nbsp;","");
        input = input.replace("\n", "");
        input = input.replace("\r", "");
        input = input.replace("</html>", "");

        return input;
    }

    @Override
    protected Data doInBackground(Object... objects) {
        String mURL = "";
        mURL=(String) objects[0];
        dataDisplay = (DataDisplay) objects[1];
        context = (Context) objects[2];
        Data[] lastDatas = (Data[]) objects[3];
        String origin = (String) objects[4];

        String webString = "";

        Date aushang = null;
        try {

            URL url = new URL(mURL);
            HttpGet httpGet = new HttpGet(url.toURI());
            DefaultHttpClient httpClient = new DefaultHttpClient();
            httpClient.getCredentialsProvider().setCredentials(new AuthScope("gym.ottilien.de",80), new UsernamePasswordCredentials("ohio", "doc86941"));

            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity);
            InputStream input = bufferedHttpEntity.getContent();
            InputStreamReader reader = new InputStreamReader(input);

            BufferedReader br = new BufferedReader(reader);
            br.skip(261);

            String line = br.readLine() + br.readLine() + br.readLine();
            webString += line;

            aushang = Data.getDateFromAushangString(line.substring(line.indexOf("Aushang") +  8, line.length() - 5));

            if(lastDatas != null)
            {
                for (int i = 0; i < lastDatas.length; i++)
                {
                    if(mURL.equals(lastDatas[i].sourceURL)) {
                        if (lastDatas[i].aushangDate.equals(aushang)) {
                            lastDatas[i].refreshDate.setTime(System.currentTimeMillis());
                            if(dataDisplay != null)
                                dataDisplay.setData(lastDatas[i], "downloader");

                            br.close();
                            reader.close();
                            input.close();
                            httpGet.abort();
                            return lastDatas[i];
                        }
                    }
                }
            }
            while ((line = br.readLine()) != null) {
                webString += line;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch(StreamCorruptedException e) {
            e.printStackTrace();
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
        } catch (ConnectionPendingException e) {
            e.printStackTrace();
        } catch(ConnectionClosedException e) {
            e.printStackTrace();
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        data = null;
        if(!webString.equals(""))
        {
            try {
                data = Data.FormatString(webString, mURL, aushang);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (JDOMException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(dataDisplay != null)
            dataDisplay.setData(data, "downloader");

        return data;
    }

    @Override
    protected void onPostExecute(Data data) {
        super.onPostExecute(data);
    }


}

