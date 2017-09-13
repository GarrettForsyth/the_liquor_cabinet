package com.games.garrett.theliquorcabinet.services.utils;

import android.support.v4.util.Pair;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * This class accesses a service given a URL.
 * Created by Garrett on 8/25/2017.
 */

public class ServiceAccess {

    /* Tag for logging */
    private static final String TAG = ServiceAccess.class.getCanonicalName();

    public ServiceAccess(String url, String httpRequestMethod, List<Pair<String,String>> params){

    }

    /**
     * Creates a HttpURLConnection resource from a string url and sets it upa akldjfl;
     * @param urlString  the url as a string
     * @return           the HttpUrlConnection resource object
     * @throws MalformedURLException  thrown on a bad url
     */
    public HttpURLConnection createConnectionResourceFor(String urlString, String httpMethod) throws MalformedURLException{

        URL url = new URL(urlString);
        return createConnectionResourceFor(url, httpMethod);

    }

    public HttpURLConnection createConnectionResourceFor(URL url, String method){

        HttpURLConnection urlConnection = null;
        try{
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            try {
                if (method.equals("POST")) urlConnection.setRequestMethod(method);
            }catch (ProtocolException e){
               // Log.d(TAG, "Invalid Http Request.");
            }

        }catch (IOException e){
           // Log.d(TAG, "Error creating HttpURLConnection to " + url.toString());
        }
        return urlConnection;

    }

    /**
     * Appends the url parameters to the base url with proper format (proper url encoding).
     * @param url       the base url
     * @param params    the url parameters
     * @return          a string with the parameters concatenated to the base url with proper format
     */
    public static String createUrlStringFromParams(String url, List<Pair<String,String>> params)
                                                        throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder(url);
        result.append("?");
        boolean first = true;
        for (Pair<String,String> pair : params){
            if (first){
                first = false;
            }
            else{
                result.append("&");
            }
            result.append(URLEncoder.encode(pair.first, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.second, "UTF-8"));
        }
        return result.toString();
    }

}
