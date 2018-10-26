package com.drobisch.partkeeprscannrapp;

import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class httpCon {
    public static Pair<InputStream, HttpURLConnection> doHttpConnection(String urlStr, String user, String password, String json) {
       return doHttpConnection(urlStr,user,password,json,"PUT");
    }

   public static Pair<InputStream, HttpURLConnection> doHttpConnection(String urlStr, String user, String password, String json, String type) {
        InputStream in = null;
        HttpURLConnection httpConn = null;
        String restURI = urlStr; // "http://" + urlStr + "/api/parts/1/addStock";
        int resCode = -1;

        try {
            URL url = new URL(restURI);
            URLConnection urlConn = url.openConnection();

            if (!(urlConn instanceof HttpURLConnection)) {
                throw new IOException("URL is not an Http URL");
            }

            String userToken= user + ":" + password;
            byte[] data = userToken.getBytes("UTF-8");
            String encode = Base64.encodeToString(data,  Base64.NO_WRAP);
            httpConn = (HttpURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod(type);
            httpConn.setRequestProperty("Authorization", "Basic " + encode);
            httpConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");

            httpConn.setRequestProperty("Content-length", json.getBytes().length + "");
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            httpConn.setUseCaches(false);

            OutputStream outputStream = httpConn.getOutputStream();
            outputStream.write(json.getBytes("UTF-8"));
            outputStream.close();

            httpConn.connect();

            resCode = httpConn.getResponseCode();

            boolean redirect = false;

            if (resCode != HttpURLConnection.HTTP_OK) {
                if (resCode == HttpURLConnection.HTTP_MOVED_TEMP
                        || resCode == HttpURLConnection.HTTP_MOVED_PERM
                        || resCode == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;

            }

            if (resCode == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
                Log.d("LoginActivity","Successful URL-connection" + String.valueOf(resCode));
            }
            else {
                Log.d("LoginActivity","Error URL-connection" + String.valueOf(resCode));
                return Pair.create(null,httpConn);
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return Pair.create(in,httpConn);
    }
}
