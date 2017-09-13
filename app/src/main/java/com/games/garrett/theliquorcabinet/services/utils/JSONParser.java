package com.games.garrett.theliquorcabinet.services.utils;


import android.util.Log;
import android.util.Pair;



import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * This class is used for parsing JSON responses from servers.
 * Created by Garrett on 8/15/2017.
 */

@SuppressWarnings("WeakerAccess")
public class JSONParser {

    private static final String TAG = JSONParser.class.getCanonicalName();

    /* the stream returned by the server */
    private static InputStream is = null;
    /* the response of the server as a JSON object */
    private JSONObject jOBj = null;
    /* the response of the server as a JSON string */
    private static String json = "";

    /* no op constructor */
    public JSONParser(){}

    /**
     * Makes a request to a server and returns its response as a JSONObject
     * @param stringUrl  the address of the server
     * @param method     the HTTP Request
     * @param params     any url parameters
     * @return           the server response as a JSON object.
     */
    public JSONObject makeHttpsRequest(String stringUrl, String method, List<Pair<String,String>> params){
        /* Create the URL Resource */
        URL url = createURLFrom(stringUrl);
        /* Establish the connection between the URL Resource with a HTTP request */
        HttpsURLConnection urlConnection = setUpUrlConnection(url, method);

        /*
        try{
            urlConnection.connect();
        }catch(IOException e){
            Log.d(TAG, "Error establishing connection with HttpURLConnection object.");
            return null;
        }
        */

        /* Open an output stream and write parameters to it */
        if (!params.isEmpty()) addParameters(urlConnection, params);

        //  .connec() called implicitly in .getInputStream()
        getInputStreamResponse(urlConnection);
        InputStream in = new BufferedInputStream(is);

        JSONObject response = readResponseStream(in);
        urlConnection.disconnect();
        return response;
    }

    /* Creates a URL object from string address */
    private URL createURLFrom(String urlAddress){
        URL url = null;
        try{
            url = new URL(urlAddress);
        }catch (MalformedURLException e){
            //Log.d(TAG, "Error forming addRatingUrl: " + e);
        }
        return url;
    }

    /**
     * Creates a connection resource for a URL and sets it to read/write and expect an http request
     * @param url      the url to connect with
     * @param method   th http request
     * @return         an HttpURLConnection resource for the inputted url
     */
    private HttpsURLConnection setUpUrlConnection(URL url, String method){
      //  Log.d(TAG, "Creating HttpURLConnection to " + url.toString() + " for http request : " + method);
        HttpsURLConnection urlConnection = null;
        try{
            urlConnection = (HttpsURLConnection) url.openConnection();
            //urlConnection.setReadTimeout(10000);
           // urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestProperty( "Content-type", "application/x-www-form-urlencoded");


           // urlConnection.setChunkedStreamingMode(0);

            try {
                if (method.equals("POST")) {
                   // Log.d(TAG, "setting urlconnection to POST");
                    urlConnection.setRequestMethod(method);
                    urlConnection.setDoOutput(true);

                }
                else{
                    urlConnection.setDoInput(true);
                }
            }catch (ProtocolException e){
               // Log.d(TAG, "Invalid Http Request.");
            }

        }catch (IOException e){
            //Log.d(TAG, "Error creating HttpURLConnection to " + url.toString());
        }
        return urlConnection;
    }

    /**
     * Writes to the output stream obtained from urlConnection resource.
     * @param urlConnection url connection resource.
     * @param params        the parameters to write to the resource.
     */
    private void addParameters(HttpsURLConnection urlConnection, List<Pair<String,String>> params){
        OutputStream os = null;
        BufferedWriter writer = null;
        try{
            os = urlConnection.getOutputStream();
        }catch (IOException e){
           // Log.d(TAG, "Error creating output stream from urlConnection: " + e);
        }
        try{
            assert os != null;
            writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
        }catch(UnsupportedEncodingException e){
           // Log.d(TAG, "Error creating buffered writer from output stream");
        }
        try{
          //  Log.d(TAG, "writing the following query parameters: " + getQuery(params));
            assert writer != null;
           // Log.d(TAG, "printing to buffered writer :" + getQuery(params));
            writer.write(getQuery(params));
        }catch(IOException e){
          //  Log.d(TAG, "Error writing to Buffered writer.");
        }

        try{
            assert writer != null;
            writer.flush();
            writer.close();
            os.close();
        }catch(IOException e){
          //  Log.d(TAG, "Error closing writer or output stream.");
        }

        try{
          //  Log.d(TAG,"Response from connection attempt was " + urlConnection.getResponseCode());
        }catch(Exception e){
         //   Log.d(TAG, "error getting response code from HttpURLConnection object.");
        }

    }

    /**
     * Gets the input stream from the HttpURLConnected resource and assigns it to the
     * instance variable.
     * @param urlConnection the connection resource.
     */
    private void getInputStreamResponse(HttpsURLConnection urlConnection){
        is = null;
        try{
            //Log.d(TAG,"Response from connection attempt was " + urlConnection.getResponseCode());
        }catch(Exception e){
           // Log.d(TAG, "error getting response code from HttpURLConnection object.");
        }
        try {
            is = urlConnection.getInputStream();
        } catch (IOException ioe) {
           // Log.d(TAG, "Trouble getting input stream from UrlHttpConnection object. Attempting to get error stream:");

            int statusCode = -1;
            try{
                statusCode = urlConnection.getResponseCode();
            }catch(IOException e){
                //Log.d(TAG, "Error getting response code from server.");
            }
            if (statusCode != 200) {
               // Log.d(TAG, "Response code == " + statusCode + ", returning error stream");
                is = urlConnection.getErrorStream();
            }

        }
    }

    /**
     * Interprets the response form the sever, turns it into a JSONObject
     * and returns it.
     * @param in the input stream from the URL connection resource
     * @return a JSON object created from the servers response.
     */
    private JSONObject readResponseStream(InputStream in){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            in.close();

            //Log.d(TAG, "RESPONSE : " + sb.toString());
            json = sb.toString();
        }catch (IOException e){
           // Log.d(TAG, "Buffer error while trying to read server response.");
        }

        try{
            jOBj = new JSONObject(json);
        }catch (JSONException e){
           // Log.e(TAG, "Error parsing JSON data " + e.toString());
        }

        return jOBj;
    }

    /**
     * Creates a suitable string for the query parameters.
     * @param params  the URL parameters
     * @return        a formatted string for the URL parameters.
     * @throws UnsupportedEncodingException thrown if problem encoding parameters
     */
    private String getQuery(List<Pair<String,String>> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Pair<String,String> pair : params){
            if (first){
                first = false;
            }
            else{
                result.append("&");
            }
            // encode only values
            result.append(URLEncoder.encode(pair.first, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.second, "UTF-8"));
        }

        return result.toString();

    }
}
