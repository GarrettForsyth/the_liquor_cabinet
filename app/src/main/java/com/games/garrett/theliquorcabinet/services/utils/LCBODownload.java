package com.games.garrett.theliquorcabinet.services.utils;

import android.net.Uri;
import android.util.Log;

import com.games.garrett.theliquorcabinet.GlobalStrings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * The class contains the logic for fetching information from the LCBO API
 * Created by Garrett on 7/22/2017.
 */

public class LCBODownload {

    /* tag for logging */
    private static final String TAG = LCBODownload.class.getCanonicalName();

    /**
     * LCOBService calls this to get information from LCBO API
     * @param url  The url to access from LCBO API
     * @return     Formatted data from the LCBO API. Either store,
     *             inventory or product information.
     */
    public static String downloadLCBOItems(Uri url){
       // Log.d(TAG, "Start download from LCBO.. ");
        String itemsFromLCBOAPI = null;

        try{
            checkIfThreadIsInterrupted();
            itemsFromLCBOAPI = fetchItemsFrom(url);

        }catch(Exception ex){
            //Log.e(TAG, "Exception downloading items. Thread has been interrupted : " + ex.getMessage());
        }
        return itemsFromLCBOAPI;
    }

    private static void checkIfThreadIsInterrupted() throws Exception {
        if (Thread.currentThread().isInterrupted()){
           // Log.e(TAG, "Download thread interrupted");
            throw new Exception("Thread interrupted. Halting download.");
        }
    }

    /**
     * Follows the given url and returns items as JSONObjects.
     * @param url   url with the location of the LCBO API to access
     * @return      the response from the LCBO API as a JSON string
     */
    private static String fetchItemsFrom(Uri url){
        // API returns paginated JSON strings
        String fetchedItemsPageAsString = null;

        try {
            URL queryUrl = urlForPage(url);
            HttpsURLConnection urlConnection = (HttpsURLConnection) queryUrl.openConnection();
           // Log.d(TAG, "Fetching from  " + queryUrl);
            try {
                fetchedItemsPageAsString= buildStringFrom(urlConnection);
            }
            finally{
                urlConnection.disconnect();
            }
        }catch (MalformedURLException e){
            //Log.e(TAG, "URL could not be formed: " + e.getMessage());

        }catch (IOException e){
            //Log.e(TAG, "Could not open Http connection: " + e.getMessage());
            return null;
        }
       // Log.d(TAG, "Finished fetching items from LCBO API.");

        return fetchedItemsPageAsString;
    }

    /**
     * Reads the response from the LCBO api and returns it as a string.
     * @param urlConnection  the Connection resource to the LCBO API
     * @return               a json string of the response from the API
     * @throws IOException   an exception thrown if there is problem establishing
     *                       the input stream from the connection resource.
     */
    private static String buildStringFrom(HttpsURLConnection urlConnection) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }

        br.close();
        return sb.toString();
    }

    /**
     * Helper to compose a url for the correct page of the paginated response from
     * the LCBO API.
     * @param url        URL to LCBO API
     * @return           a URL for the inputted page number of the paginated response.
     * @throws MalformedURLException thrown if there is problem creating the URL object
     */
    private static URL urlForPage(Uri url) throws MalformedURLException{
        return new URL(url.toString() + "&where_not=is_dead"
                                      + "&per_page=100"
                                      + "&access_key="
                                      + GlobalStrings.getLcboDevKey());

    }

}
