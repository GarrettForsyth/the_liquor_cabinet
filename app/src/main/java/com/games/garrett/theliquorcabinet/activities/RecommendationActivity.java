package com.games.garrett.theliquorcabinet.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.games.garrett.theliquorcabinet.GlobalStrings;
import com.games.garrett.theliquorcabinet.R;
import com.games.garrett.theliquorcabinet.POJOs.LCBOProductInformation;
import com.games.garrett.theliquorcabinet.fragments.DisplayProductsFragment;
import com.games.garrett.theliquorcabinet.receivers.OnSuccessfulFetchReceiver;
import com.games.garrett.theliquorcabinet.services.RecommendationDBService;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResult;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResultHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Displays a list of drink recommendations based on the user's past ratings.
 *
 * This activity first fetches a list of product IDs from the RecommendationService.
 * The LCBO API doesn't allow batch queries on ids, so after retrieving the ids,
 * a bunch of async tasks are created to fetch product information to create an
 * LCBOProductInformation object to add to the DisplayProductsFragment.
 * Created by Garrett on 8/18/2017.
 */

public class RecommendationActivity extends AppCompatActivity implements ServiceResult {

    /* Tag for logging */
    private static final String TAG = RecommendationActivity.class.getCanonicalName();

    /* Stores the products to be displayed by the fragment */
    private ArrayList<LCBOProductInformation> mDisplayedProducts;

    /* stores the list of ids retrieved from the RecommendationService */
    private ArrayList<String> mProductIDs;

    /* Request code for the RecommendationService */
    private static final int REQUEST_RECOMMENDATIONS = 103;

    /* Reference to SharedPreferences */
    private SharedPreferences mPref;

    /* Handler to communicate with RecommendationService */
    private Handler mServiceResultHandler = null;

    /* Buttons to go through pagination */
    private Button mPrev;
    private Button mMore;
    private int pageNumber = 0;

    /* Reference to context */
    Activity mContext;

    /* Tracks if the activity is stopped. The fragment should not update if
        the activity is stopped.
     */
    private boolean isStopped;

    /**
     * Hook method called when a new activity is created.  One time
     * initialization code goes here, e.g., initializing views.
     *
     * @param savedInstanceState object that contains saved state information.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view for this Activity.
        setContentView(R.layout.activity_recommendation);

        setUpButtons();

        /* Initialize member variables */
        mServiceResultHandler = new ServiceResultHandler(this);
        mDisplayedProducts = new ArrayList<>();
        mPref = this.getSharedPreferences("activity_settings", Context.MODE_PRIVATE);
        mContext = this;

        /* Style text */
        TextView title = (TextView) findViewById(R.id.recommendation_title);
        title.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);

        /* send request to service */
        if(savedInstanceState == null){
            getRecommendationsFromService(REQUEST_RECOMMENDATIONS);
        }

    }

    /*Attach on click listeners to buttons */
    private void setUpButtons(){
        mPrev = (Button) findViewById(R.id.prev_products_button);
        mMore = (Button) findViewById(R.id.more_products_button);

        /* Note here, the recommendation service returns 100 results and the activity will
            display 10 at time, so there will be 10 pages.
         */

        mPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pageNumber != 0){
                    pageNumber--;
                    if (pageNumber == 0) mPrev.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                    if (pageNumber == 8) mMore.setTextColor(ContextCompat.getColor(mContext, R.color.dark_yellow));
                    displayPageOfResults(pageNumber);
                }
            }
        });

        mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pageNumber != 9){
                    pageNumber++;
                    if (pageNumber == 9) mMore.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                    if (pageNumber == 1) mPrev.setTextColor(ContextCompat.getColor(mContext, R.color.dark_yellow));
                    displayPageOfResults(pageNumber);
                }
            }
        });
    }

    /**
     * Creates and returns an intent suitable for starting this activity
     *
     * @param context The context of the activity that starts this one.
     * @return An intent to start this activity.
     */
    public static Intent makeRecommendationActivityIntent(Context context) {
        return new Intent(context, RecommendationActivity.class);
    }

    /**
     * Sends the request intent to start the RecommendationService.
     * Each user is uniquely identified by their id, which is used
     * to find their recommendations from the service.
     * @param requestId  the type of request to ask of the service.
     */
    private void getRecommendationsFromService(int requestId) {

        String userID = mPref.getString("userID", "");
        ArrayList<String> userRatingEntry = new ArrayList<>();
        userRatingEntry.add(userID);

        Toast.makeText(this,
                "Getting recommendations.\n" +
                        "            One moment..",
                Toast.LENGTH_LONG
        ).show();

        Intent recommendDBIntent = RecommendationDBService.makeIntent(
                this,
                requestId,
                userRatingEntry,
                mServiceResultHandler);



        //Log.d(TAG, "starting RecommendationDBService");
        startService(recommendDBIntent);
    }

    /**
     * Hook method to handle the results from the RecommendationService
     *
     * @param requestCode the request code used to start the service.
     * @param resultCode  the result code returned by the service.
     * @param data        the data returned by the service.
     */
    @Override
    public void onServiceResult(int requestCode, int resultCode, Bundle data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            handleDownloadFailure();
            Intent broadcastIntent = OnSuccessfulFetchReceiver.makeOnSuccessfulFetchIntent(this, false);
            this.sendBroadcast(broadcastIntent);
            return;
        }
        String itemsAsJSONString = data.getString("ITEM_ARRAY_KEY");
        processResponse(requestCode, itemsAsJSONString);
    }

    private void handleDownloadFailure(){
        Toast.makeText(this,
                "Could not establish connection to service.\n" +
                        "Please check your internet connection and \n" +
                        "make sure internet permissions are granted.",
                Toast.LENGTH_LONG
        ).show();
    }

    /**
     * Determines the flow of control determined by the request code.
     *
     * @param requestCode       request code used to invoke response.
     * @param itemsAsJSONString the response data as a JSON string.
     */
    private void processResponse(int requestCode, String itemsAsJSONString) {
        switch (requestCode) {
            case  REQUEST_RECOMMENDATIONS: {
                if (itemsAsJSONString.equals("NO_RECOMMENDATIONS")){
                    Toast.makeText(this, "Please rate some products before asking for a recommendation!",
                            Toast.LENGTH_LONG).show();
                }
                else{
                    Intent broadcastIntent = OnSuccessfulFetchReceiver.makeOnSuccessfulFetchIntent(this, true);
                    this.sendBroadcast(broadcastIntent);
                    displayRecommendationServiceResponse(itemsAsJSONString);
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * Displays the user's drink recommendations in a recycler view
     * by parsing the json response.
     *
     * @param itemsAsJSONString response JSON string from API.
     */
    private void displayRecommendationServiceResponse(String itemsAsJSONString) {
        try {
            mProductIDs= new ArrayList<>();
            mDisplayedProducts = new ArrayList<>();
            JSONObject response = new JSONObject(itemsAsJSONString);
            JSONArray productIDs = (JSONArray)response.get("user_recommendations");
            // todo fix response from server, currently wrapped in an extra array
            // messed up the response from server, not sure hot to fix but it's nested in another array
            JSONArray itemsOnPage = (JSONArray) productIDs.get(0);

            // add each id to list of ids
            for (int i = 0; i < itemsOnPage.length(); i++) {
                mProductIDs.add(((JSONObject) itemsOnPage.get(i)).getString("productID"));
            }

        } catch (JSONException e) {
           // Log.e(TAG, "Error parsing store json string : " + e.getMessage());
        }

        displayPageOfResults(pageNumber);

    }


    private void displayPageOfResults(int pageNumber){
        // run async task for each id returned by RecommendationService
        mDisplayedProducts.clear();
        //Log.e(TAG, "page number = " + pageNumber);
        //Log.e(TAG, "first item = " +(pageNumber*10) );
        for (int i = (pageNumber*10);i < pageNumber*10+10; i++){
           // Log.d(TAG, "fetching product id " + mProductIDs.get(i));
            new getProductInformationFromID().execute(mProductIDs.get(i));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStopped = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isStopped = true;
    }

    /**
     * AsyncTask that fetches the the LCBO product information as a json string returned
     * from the LCBO API using the productID.
     */
    private class getProductInformationFromID extends AsyncTask<String,Void, JSONObject>{
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args){
            //Log.d(TAG, "Attempting to fetch from " + GlobalStrings.getLcboApiProductsUrl()  + args[0] +
            //        "&where_not=is_dead" +" ?access_key=" +
            //        GlobalStrings.getLcboDevKey());
            JSONObject json = null;
            try {
                URL url = new URL((GlobalStrings.getLcboApiProductsUrl() + args[0] + "?access_key=" +
                        GlobalStrings.getLcboDevKey()));
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    json = readResponseStream(in);
                } finally {
                    urlConnection.disconnect();
                }
            }catch(Exception e){
              //  Log.d(TAG, "Error connecting to lcbo product page.");
            }
            processLCBOProductResponse(json);
            return json;
        }

        /**
         * Parses the JSON returned from the LCBO API and adds the item to the list of
         * items to be displayed by updating the fragment's adapter.
         * @param page response JSONObject from API.
         *
         */
        private void processLCBOProductResponse (JSONObject page){
            try {
                JSONObject itemOnPage = page.getJSONObject("result");
                mDisplayedProducts.add(new LCBOProductInformation(itemOnPage));

            }catch (JSONException e){
               // Log.e(TAG, "Error parsing store json string : " + e.getMessage());
            }
            updateFragment();
        }

        /**
         * Updates the fragment with the new LCBO product.
         */
        private void updateFragment(){
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("PRODUCT_INFORMATION", mDisplayedProducts);

            DisplayProductsFragment newFragment = new DisplayProductsFragment();
            newFragment.setArguments(bundle);

            if(!isContextInvalid(mContext) && !mContext.isFinishing() && !isStopped){
                android.support.v4.app.FragmentManager fragmentManger = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManger.beginTransaction();
                fragmentTransaction.replace(R.id.product_list_fragment, newFragment);
                fragmentTransaction.commit();
            }
        }

        /* Checks to make sure the activity is alive before updating */
        private boolean isContextInvalid(final Context context) {
            if (context == null ) {
                return true;
            }
            return false;
        }


        private JSONObject readResponseStream(InputStream in){
            String json = null;
            JSONObject jOBj = null;
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
                //Log.e(TAG, "Error parsing JSON data " + e.toString());
            }

            return jOBj;
        }

    }


}