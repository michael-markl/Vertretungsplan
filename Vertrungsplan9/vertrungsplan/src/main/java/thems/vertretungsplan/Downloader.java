package thems.vertretungsplan;

import android.content.Context;
import android.net.Credentials;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamCorruptedException;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URL;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ConnectionPendingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Michael on 12.01.14.
 */
public class Downloader extends AsyncTask<Object, Void, Data> {
    DataDisplay dataDisplay;
    Context context;
    Boolean alreadyTriedAuthentication = false;

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

        return input;
    }

    @Override
    protected Data doInBackground(Object... objects) {
        String mURL = "";
        mURL=(String) objects[0];
        dataDisplay = (DataDisplay) objects[1];
        context = (Context) objects[2];


        String webString = "";
        HttpURLConnection urlConnection = null;
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                if(!alreadyTriedAuthentication) {
                    alreadyTriedAuthentication = true;
                    return new PasswordAuthentication("ohio", "doc86941".toCharArray());
                }
                else
                {
                    return null;
                }
            }
        });

        URL url = null;
        try {

            url = new URL(mURL);
            HttpGet httpRequest = null;
            httpRequest = new HttpGet(url.toURI());
            DefaultHttpClient httpClient = new DefaultHttpClient();
            httpClient.getCredentialsProvider().setCredentials(new AuthScope("gym.ottilien.de",80), new UsernamePasswordCredentials("ohio", "doc86941"));

            HttpResponse response = (HttpResponse)httpClient.execute(httpRequest);
            HttpEntity entity = response.getEntity();
            BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity);
            InputStream input = bufferedHttpEntity.getContent();

/*
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream in = urlConnection.getInputStream();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);*/
            InputStreamReader reader = new InputStreamReader(input);

            BufferedReader br = new BufferedReader(reader);
            br.skip(301);

            String line = br.readLine();
            webString += line;

            Date aushang = Data.getDateFromAushangString(line.substring(8, line.length() - 5));
             //aushang abgleichen mit gespeicherten um Datenverbrauch zu schonen
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
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        Data data = null;
        if(!webString.equals(""))
        {
            try {
                data = Data.FormatString(webString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        dataDisplay.setData(data, "downloader");
        return data;
    }

    @Override
    protected void onPostExecute(Data data) {
        super.onPostExecute(data);
    }
}

