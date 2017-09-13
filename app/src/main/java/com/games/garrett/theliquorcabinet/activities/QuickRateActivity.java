package com.games.garrett.theliquorcabinet.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.games.garrett.theliquorcabinet.POJOs.LCBOProductInformation;
import com.games.garrett.theliquorcabinet.R;
import com.games.garrett.theliquorcabinet.fragments.DisplayProductsFragment;
import com.games.garrett.theliquorcabinet.receivers.OnSuccessfulFetchReceiver;
import com.games.garrett.theliquorcabinet.services.LCBOService;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResult;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResultHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

/**
 * This activity shows a random page of products from the LCBO API products.
 *
 * It quickly shows the user a diverse product set so the user can quickly
 * build up an accurate rating profile.
 *
 * Created by Garrett on 8/29/2017.
 */
// todo there is a lot of duplicate code between browse, recommend, log, and quick
    // rate activities. It is probably best to try to encapsulate service result handling
    // into some kind of helper class containing all the duplicate/similar methods or
    // create and abstract class and extend these classes from it

public class QuickRateActivity extends AppCompatActivity implements ServiceResult {

    /* Tag for logging */
    private static final String TAG = QuickRateActivity.class.getCanonicalName();

    /* Stores the products to be displayed by the fragment */
    private ArrayList<LCBOProductInformation> mDisplayedProducts;

    /* Request code for the LCBOService */
    private static final int REQUEST_NUMBER_OF_PAGES      = 105;
    private static final int REQUEST_RANDOM_PAGE          = 106;

    /* URL for LCBO Products */
    private final static String LCBO_PRODUCTS_URL= "https://lcboapi.com/products?";

    /* Handler to communicate with the LCBOService*/
    private Handler mServiceResultHandler = null;

    /* Tracks if the activity is stopped. The fragment should not update if
        the activity is stopped.
     */
    private boolean isStopped;

    /*Reference to fragment */
    private android.support.v4.app.Fragment mFragment;

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
        setContentView(R.layout.activity_quick_rate);


        /* Initialize member variables */
        isStopped = false;
        mServiceResultHandler = new ServiceResultHandler(this);
        mDisplayedProducts = new ArrayList<>();

        TextView title = (TextView) findViewById(R.id.quick_rate_title);
        title.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);

        if(savedInstanceState == null){
            /* send request to service */
            startDownload( Uri.parse(LCBO_PRODUCTS_URL), REQUEST_NUMBER_OF_PAGES);
        }

    }

    /**
     * Creates and returns an intent suitable for starting this activity
     *
     * @param context The context of the activity that starts this one.
     * @return An intent to start this activity.
     */
    public static Intent makeQuickRateActivityIntent(Context context) {
        return new Intent(context, QuickRateActivity.class);
    }

    /**
     * Starts the LCBOService for a result determined by the
     * url and requestId.
     * @param url         URL of LCBO API
     * @param requestId   Type of request
     */
    public void startDownload(Uri url, int requestId){
        //Log.d(TAG, "StartDownload has been entered.");
        Intent downloadIntent = LCBOService.makeIntent(
                this,
                requestId,
                url,
                mServiceResultHandler);



        //Log.d(TAG, "starting LCBOService download for " + url.toString());
        startService(downloadIntent);
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
            case  REQUEST_NUMBER_OF_PAGES: {
                getNumberOfPagesAndFetchARandomPage(itemsAsJSONString);
                break;
            }
            case REQUEST_RANDOM_PAGE: {
                Intent broadcastIntent = OnSuccessfulFetchReceiver.makeOnSuccessfulFetchIntent(this, true);
                this.sendBroadcast(broadcastIntent);
                displayRandomPage(itemsAsJSONString);
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
    private void getNumberOfPagesAndFetchARandomPage(String itemsAsJSONString) {
        int mNumberOfPages;
        try {
            JSONObject response = new JSONObject(itemsAsJSONString);
            JSONObject pager = response.getJSONObject("pager");
            mNumberOfPages = pager.getInt("total_record_count")/100;
            Random randomGen = new Random();
            // Get a random page to display
            int randomPageNumber = randomGen.nextInt(mNumberOfPages) + 1;
            String pageUrlParam = "page=" + randomPageNumber  + "&";
            Toast.makeText(this, "Requesting data.. ", Toast.LENGTH_LONG).show();
            startDownload( Uri.parse(LCBO_PRODUCTS_URL + pageUrlParam ), REQUEST_RANDOM_PAGE);

        } catch (JSONException e) {
           // Log.e(TAG, "Error parsing store json string : " + e.getMessage());
        }
    }

    /**
     * Displays a random page from the LCBO's API
     * by parsing the json response.
     *
     * @param itemsAsJSONString response JSON string from API.
     */
    private void displayRandomPage(String itemsAsJSONString) {
        try {
            mDisplayedProducts = new ArrayList<>();
            JSONObject response = new JSONObject(itemsAsJSONString);
            JSONArray itemsOnPage = (JSONArray)response.get("result");

            // add each id to list of ids
            for (int i = 0; i < itemsOnPage.length(); i++) {
                mDisplayedProducts.add(new LCBOProductInformation((JSONObject)itemsOnPage.get(i)));
            }
        } catch (JSONException e) {
           //Log.e(TAG, "Error parsing store json string : " + e.getMessage());
        }

       updateFragment();

    }

    /**
     * Puts results parsed from the LCBO response into a bundle
     * and sends it to the DisplayProductsFragment.
     */
    private void updateFragment(){
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("PRODUCT_INFORMATION", mDisplayedProducts);

        DisplayProductsFragment newFragment = new DisplayProductsFragment();
        newFragment.setArguments(bundle);

        if(!isContextInvalid(this) && !this.isFinishing() && !isStopped){
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
}
