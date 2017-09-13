package com.games.garrett.theliquorcabinet.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.games.garrett.theliquorcabinet.R;
import com.games.garrett.theliquorcabinet.POJOs.LCBOProductInformation;
import com.games.garrett.theliquorcabinet.activities.utils.RequestFineLocationPermission;
import com.games.garrett.theliquorcabinet.fragments.DisplayProductsFragment;
import com.games.garrett.theliquorcabinet.receivers.OnSuccessfulFetchReceiver;
import com.games.garrett.theliquorcabinet.services.LCBOService;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResult;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResultHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Browse activity will :
 *   1) Try to load local store ids from SharedPreferences. If none are stored, it will
 *   using RequestFineLocationPermission.class to ask for the user's permission to use
 *   fine location, then try to get the users location and access the LCBO API to find
 *   stores near the user and save the findings to SharedPreferences for next time.
 *
 *   IF THE USER CHANGES LOCATIONS (drastically) THE USER WILL HAVE TO REQUEST TO FIND
 *   LOCAL STORES AGAIN IN THE SETTINGS ACTIVITY.
 *
 *   2) Collect queries from the user, then check with the LCBO API for matches, returning
 *   the results as a paginated list, allowing the user to rate each item and store the rating
 *   in the LCBOProductRatingContentProvider and sending the ratings to the RecommendationService
 *   to create a user profile.
 *
 * Created by Garrett on 7/20/2017.
 */

public class BrowseActivity  extends AppCompatActivity implements ServiceResult {

    /* Tag for logging */
    private static final String TAG = BrowseActivity.class.getCanonicalName();

    /* Root URLs for the LCBO API */
    private final static String LCBO_STORES_URL = "https://lcboapi.com/stores?";
    private final static String LCBO_PRODUCTS_URL= "https://lcboapi.com/products?";

    /* Request codes for the LCBO API */
    private final static int REQUEST_LCBO_STORES = 1;
    private final static int REQUEST_LCBO_PRODUCTS = 2;

    /* Request codes for the RecommendationDatabaseService */
    @SuppressWarnings("unused")
    private static final int REQUEST_UPDATE_RATING_RECORD = 101;
    @SuppressWarnings("unused")
    private static final int REQUEST_DELETE_RATING_RECORD = 102;
    @SuppressWarnings("unused")
    private static final int REQUEST_RECOMMENDATIONS      = 103;

    /* Request code for permission */
    private static final int  MY_PERMISSION_ACCESS_FINE_LOCATION = 1;

    /* Lists of stored information in SharedPreferences
        List are used to preserve insertion order so there
        will be a correspondence between the two list since
        they are updated at the same time
     */
    private ArrayList<String> mLocalStoreIds;
    private ArrayList<String> mLocalStoreAddresses;


    /* Tracks the response page the API returns when requesting local stores */
    private int storeResponsePageNumber;
    /* Tracks the response page the API returns when requesting products */
    private int mQueryPageNumber;
    /* Tracks whether there is any more potential useful information on next API pagination */
    private boolean mShouldGetNextPage;
    private boolean mIsLastPageOfProductQuery;

    /* Handler to communicate with LCBOService */
    private Handler mServiceResultHandler = null;


    /* Used to retrieve the user's last location */
    private FusedLocationProviderClient mFusedLocationClient;
    /* Stores the user's last known location */
    private Location mLastLocation;
    /* Stores user's last known location as string, used as a URL parameter in the API call */
    private String mLastLocationAsStringForUrl;

    /* Value stored in SharedPreferences, determines how far to look for local stores */
    private float radius;
    /* Value last used as radius. Will be different if user has changed radius in settings */
    private float last_radius;
    /* Reference to SharedPreferences */
    private SharedPreferences mPref;

    /* References to UI Objects */
    private EditText mSearchString;
    private Spinner mLocationSpinner;
    private Spinner mSortBySpinner;
    private ToggleButton mSortToggle;

    /* A map to store the different sorting options */
    private HashMap<String, String> mSortByMap;

    /* Reference to a list of the current products being displayed to the user */
    private ArrayList<LCBOProductInformation> mDisplayedProducts;

    /* Reference to context */
    Context mContext;

    /* Tracks if the activity is stopped. The fragment should not update if
        the activity is stopped.
     */
    private boolean isStopped;

    /**
     * Gets list of stores close to the user then sets up the UI.
     * @param savedInstanceState instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        /* Initialize instance variables */
        mServiceResultHandler = new ServiceResultHandler(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mPref = this.getSharedPreferences("activity_settings", Context.MODE_PRIVATE);

        mQueryPageNumber = 0;
        mIsLastPageOfProductQuery = false;
        mContext = this;

        if(savedInstanceState == null){
            getLocalStoreIds();
        }

    }

    /**
     * Creates and returns a suitable intent to start this activity.
     * @param context  context of starting activity.
     * @return         an intent that can start this activity.
     */
    public static Intent makeBrowseActivityIntent(Context context){
        return new Intent(context, BrowseActivity.class);
    }

    ///////////////////////////////////////////////////////////////
    // UI SET UP:
    ///////////////////////////////////////////////////////////////

    /**
     * Sets up the user interface for the activity.
     */
    private void setupUI(){
        setupSearchBox();
        buildSortByMap(); // map to hold possible ways to sort response
        setupSpinners();
        setupButtons();
    }

    private void setupSearchBox(){
        mSearchString = (EditText) findViewById(R.id.query_edit_text);
    }

    private void setupSpinners(){
        mSortBySpinner = (Spinner) findViewById(R.id.sort_by_spinner);

        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_by_choices, android.R.layout.simple_spinner_item);

        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortBySpinner.setAdapter(sortAdapter);

        mLocationSpinner = (Spinner) findViewById(R.id.from_location_spinner);

        mLocalStoreAddresses.add(0,"Any Store");
        ArrayAdapter<String> storeLocations = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mLocalStoreAddresses);

        storeLocations.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLocationSpinner.setAdapter(storeLocations);
    }

    private void setupButtons(){
        mSortToggle = (ToggleButton) findViewById(R.id.sort_toggle);

         final Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                mQueryPageNumber = 1;
               // Log.d(TAG,"Query button has been clicked");
                query();
            }
        });

        final Button previousProductsButton = (Button) findViewById(R.id.prev_products_button);
        final Button moreProductsButton = (Button) findViewById(R.id.more_products_button);

        moreProductsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if( thereHasBeenASearchQuery() && notLastPageOfQueryResults()){
                    mQueryPageNumber++;
                    previousProductsButton.setTextColor(ContextCompat.getColor(mContext, R.color.dark_yellow));
                    query();
                }
            }
        });

        previousProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( thereHasBeenASearchQuery() && notFirstPageOfQueryResults()){
                    mQueryPageNumber--;
                    if (!notFirstPageOfQueryResults()) {
                        previousProductsButton.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                    }
                    query();
                }
            }
        });
    }

    private boolean thereHasBeenASearchQuery(){
        return mQueryPageNumber != 0;
    }

    private boolean notLastPageOfQueryResults(){
        return !mIsLastPageOfProductQuery;
    }

    private boolean notFirstPageOfQueryResults(){
        return mQueryPageNumber != 1;
    }

    /* Map that stores possible ways to sort response. It maps the string displayed
        in the adapter to the URL parameter required by the API
     */
    private void buildSortByMap(){
        mSortByMap = new HashMap<>();
        mSortByMap.put("No Order", "");
        mSortByMap.put("Price", "price_in_cents");
        mSortByMap.put("Sale End Date","limited_time_offer_ends_on");
        mSortByMap.put("Package Volume", "package_unit_volume_in_milliliters");
        mSortByMap.put("Package Units", "total_package_units");
        mSortByMap.put("Volume", "volume_in_milliliters");
        mSortByMap.put("Alcohol Content", "alcohol_content");
        mSortByMap.put("Price per Liter", "price_per_liter_in_cents");
        mSortByMap.put("Price per Alcohol Liter", "price_per_liter_of_alcohol_in_cents");
        mSortByMap.put("Release Date", "released_on");

    }

    ///////////////////////////////////////////////////////////////
    // End UI Setup
    ///////////////////////////////////////////////////////////////

    /**
     * Send the parameters (obtained from the UI) of the user's query to the LCBOService
     */
    private void query(){
       // Log.d(TAG, "Query function has been entered.");
        if (!thereHasBeenASearchQuery()) mQueryPageNumber = 1;
        String queryParameters = buildSearchQueryString();
        startDownload( Uri.parse(LCBO_PRODUCTS_URL + queryParameters), REQUEST_LCBO_PRODUCTS);
        // close the keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    private String buildSearchQueryString(){
        ArrayList<String> storeIdsAsList = new ArrayList<>(mLocalStoreIds);

        String sortUrlParam;
        String sortDirection;

        if (mSortByMap.get(mSortBySpinner.getSelectedItem().toString()).equals("")){
            sortUrlParam = "";
        }
        else{
            if (mSortToggle.isChecked())   sortDirection = ".asc";
            else                           sortDirection = ".desc";
            sortUrlParam = "&order=" + mSortByMap.get(mSortBySpinner.getSelectedItem().toString()) + sortDirection;
        }

        String storeIDUrlParam;

        // "Any Local Store" option is always at spinner position 0
        // --> subtract by 1 while getting the storeId via spinner position
        // since "Any Local Store" does not have a storeId in the list there
        // is an offset.
        if(mLocationSpinner.getSelectedItemPosition() == 0) storeIDUrlParam = "";
        else storeIDUrlParam = "&store_id=" + storeIdsAsList.get(mLocationSpinner.getSelectedItemPosition()-1);

        return (  "q=" + mSearchString.getText().toString().replaceAll(" ","\\+")
                + storeIDUrlParam
                + sortUrlParam
                + "&per_page=100&page=" + mQueryPageNumber);
    }

    /**
     * Will attempt to get local store ids from SharedPreferences.
     * If none are available, it will ask permission for the user's
     * location and make a call to the LCBOService to find stores
     * that are with a distance set by the user's radius (defaulted
     * at 20km), and save them in SharedPreferences for use next time.
     */
    private void getLocalStoreIds(){
        retrieveDataFromSharedPreferences();

       // Log.d(TAG, "Compare radii " + radius + " and " + last_radius);
        boolean hasUserChangedRadiusInSettings = radius != last_radius;

        if(mLocalStoreIds.isEmpty()){
            //Log.d(TAG, "No ids stored in sharedPreferences. Finding user's location, then fetching local stores..");
            findCloseStores();
        }
        else if(hasUserChangedRadiusInSettings){
           // Log.d(TAG, "User has set a new radius found in sharedPreferences. Finding user's location, then fetching local stores..");
            updateRadiusInSharedPreferences();
            // clear the list from the address acquired from the previous radius
            mLocalStoreIds.clear();
            mLocalStoreAddresses.clear();
            findCloseStores();
        }
        else{
            //Log.d(TAG, "Found local stores in sharedPreferences.");
            setupUI();
        }
    }

    private void updateRadiusInSharedPreferences(){
        // update lastRadius
        SharedPreferences.Editor editor = mPref.edit();
        editor.putFloat("lastRadius", radius);
        editor.apply();
    }

    private void retrieveDataFromSharedPreferences(){
        mLocalStoreIds = loadArrayFromSharedPreferences("ids");
        mLocalStoreAddresses = loadArrayFromSharedPreferences("addresses");

        // default radius set to 20 km
        radius = mPref.getFloat("radius", 20000);
        last_radius = mPref.getFloat("lastRadius",20000);
    }

    private ArrayList<String> loadArrayFromSharedPreferences(String item){
        ArrayList<String> arr = new ArrayList<>();
        int size = mPref.getInt(item + "_size", 0);

        for(int i=0; i < size; i++){
            arr.add(mPref.getString(item + "_" + i , null));
        }
        //Log.d(TAG, " loaded " + arr.size() + " items in browse activity");
        return arr;
    }


    /**
     * If permissions are granted, uses Google Play Service API to get the last
     * location of the user.
     */
    private void findCloseStores(){
        if (new RequestFineLocationPermission(BrowseActivity.this).checkPermission()){
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                mLastLocation = location;
                                findStoresCloseTo(location);
                            }
                            else{
                                Toast.makeText(BrowseActivity.this, "Couldn't get the location. Make sure location is enabled on the device",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    /**
     * Calls LCBOService to get stores that are close to location and
     * store them in SharedPreferences.
     * @param location the location to find stores close to.
     */
    private void findStoresCloseTo(Location location){
        storeResponsePageNumber = 1;
        mLastLocationAsStringForUrl = "lat=" + location.getLatitude() + "&lon=" +location.getLongitude() + "&";

        // The api will return results closest to the usersLocation first
        // the service handler will fetch more pages if needed.
        startDownload(Uri.parse(LCBO_STORES_URL + mLastLocationAsStringForUrl +  "page=" + storeResponsePageNumber + "&"), REQUEST_LCBO_STORES);
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

        Toast.makeText(this, "Requesting data..", Toast.LENGTH_LONG).show();

       // Log.d(TAG, "starting LCBOService download for " + url.toString());
        startService(downloadIntent);
    }

    /**
     * Hook method to handle the results from the LCBOService
     * @param requestCode  the request code used to start the service.
     * @param resultCode   the result code returned by the service.
     * @param data         the data returned by the service.
     */
    @Override
    public void onServiceResult(int requestCode, int resultCode, Bundle data){
        if( resultCode == Activity.RESULT_CANCELED){
            handleDownloadFailure(
            );
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
     * @param requestCode          request code used to invoke response.
     * @param itemsAsJSONString    the response data as a JSON string.
     */
    private void processResponse(int requestCode, String itemsAsJSONString){
        switch(requestCode){
            // Request to get local stores
            case REQUEST_LCBO_STORES:{
                processLCBOStoreResponse(itemsAsJSONString);
                break;
            }
            case REQUEST_LCBO_PRODUCTS: {
                Intent broadcastIntent = OnSuccessfulFetchReceiver.makeOnSuccessfulFetchIntent(this, true);
                this.sendBroadcast(broadcastIntent);
                processLCBOProductResponse(itemsAsJSONString);
                break;
            }
            default: break;
        }
    }

    /**
     * Processes the JSON string returned by the API. Records all store ids inside
     * the user's radius, and calls the LCBOService again if the response indicates
     * there may be more stores that are close to the user.
     * @param itemsAsJSONString the response from the LCBO API with store information.
     */
    private void processLCBOStoreResponse(String itemsAsJSONString){
        try{
            JSONObject page = new JSONObject(itemsAsJSONString);
            JSONArray storesOnPage = page.getJSONArray("result");
            mShouldGetNextPage = page.getJSONObject("pager").getString("is_final_page").equals("false");

            addLocalStoresToList(storesOnPage);

            // request next page of results if more applicable results
            if (mShouldGetNextPage){
                storeResponsePageNumber++;
                startDownload(Uri.parse(LCBO_STORES_URL
                                      + mLastLocationAsStringForUrl
                                      + "page=" + storeResponsePageNumber + "&"),
                              REQUEST_LCBO_STORES);
            }
            else{ // all local stores have been found. Can proceed with setup
                saveResultsToSharedPreferences();
                setupUI();
            }

        }catch (JSONException e){
           // Log.e(TAG, "Error parsing store json string in process LCBOStoreResponse: " + e.getMessage());
        }
    }

    /**
     * Helper function to add response from API to the member variables containing
     * store information.
     * @param storesOnPage    response from the api
     * @throws JSONException  thrown if trouble reading JSONArray or JSONObject
     */
    private void addLocalStoresToList(JSONArray storesOnPage) throws JSONException{
        // iterates over all responses from JSON string
        for(int i=0; i < storesOnPage.length(); i ++){
            JSONObject currentStore = (JSONObject)(storesOnPage.get(i));
            if( storeInsideRadius(currentStore) ){
                mLocalStoreIds.add(currentStore.getString("store_no"));
                mLocalStoreAddresses.add(currentStore.getString("address_line_1"));
            }
            else{
                // The api returns locations in order of distance so if
                // one store is too far away, all the rest will be too
                mShouldGetNextPage = false;
                break;
            }
        }
    }

    /**
     * Gets the store's location from a JSONObject and returns true if the store
     * is inside the user's radius.
     * @param store           The store information as  JSONObject.
     * @return                True if the store is within the user's radius.
     * @throws JSONException  Thrown if trouble parsing the JSONObject.
     */
    private boolean storeInsideRadius(JSONObject store) throws JSONException {
        Location storeLocation = new Location("");
        storeLocation.setLatitude(store.getDouble("latitude"));
        storeLocation.setLongitude(store.getDouble("longitude"));

        return (mLastLocation.distanceTo(storeLocation) < radius);
    }

    /**
     * Helper function to save results of finding local stores to SharedPreferences.
     */
    private void saveResultsToSharedPreferences(){
        // add local stores to sharedPreferences to avoid look api calls next time
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt("ids_size", mLocalStoreIds.size());
        editor.putInt("addresses_size", mLocalStoreAddresses.size());

        for (int i=0; i < mLocalStoreIds.size(); i++){
            editor.remove("ids_" + i);
            editor.putString("ids_" + i, mLocalStoreIds.get(i));
        }

        for (int i=0; i <mLocalStoreAddresses.size(); i++){
            editor.remove("addresses_" + i);
            editor.putString("addresses_" + i, mLocalStoreAddresses.get(i));
        }

        editor.apply();
    }


    /**
     * Flow of control for a  REQUEST_LCBO_PRODUCTS request.
     * Parses results from response JSON string and puts them in adapter to
     * be viewed the recycler view.
     * @param itemsAsJSONString response JSON string from API.
     */
    private void processLCBOProductResponse (String itemsAsJSONString){
        try {
            mDisplayedProducts = new ArrayList<>();
            JSONObject page = new JSONObject(itemsAsJSONString);
            JSONArray itemsOnPage = page.getJSONArray("result");

            mIsLastPageOfProductQuery = page.getJSONObject("pager").getString("is_final_page").equals("true");

            for (int i = 0; i < itemsOnPage.length(); i++) {
               mDisplayedProducts.add(new LCBOProductInformation((JSONObject)itemsOnPage.get(i)));
            }

        }catch (JSONException e){
           // Log.e(TAG, "Error parsing store json string : " + e.getMessage());
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



    /**
     * Handles the response the user gives to the permission request.
     * @param requestCode  Permission request code.
     * @param permissions  Which permissions are requested.
     * @param grantResults Indicates if the permissions have been granted.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults){
        switch (requestCode){
            case MY_PERMISSION_ACCESS_FINE_LOCATION:
                handleFinePermissionAccessRequest(grantResults);
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Handles fine permission requests.
     * @param grantResults the result of the request.
     */
    private void handleFinePermissionAccessRequest(int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
           // Log.d(TAG, "User has granted permission!. Proceeding..");
            getLocalStoreIds();
        } else {
           // Log.d(TAG, "User has not granted permission.");
            Toast.makeText(BrowseActivity.this, "ACCESS_FINE_LOCATION Denied", Toast.LENGTH_SHORT).show();
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
}
