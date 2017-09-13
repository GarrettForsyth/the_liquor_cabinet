package com.games.garrett.theliquorcabinet.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.games.garrett.theliquorcabinet.R;
import com.games.garrett.theliquorcabinet.activities.DetailsActivity;
import com.games.garrett.theliquorcabinet.POJOs.LCBOProductInformation;
import com.games.garrett.theliquorcabinet.fragments.adapters.RatingAdapter;
import com.games.garrett.theliquorcabinet.activities.utils.RatingOp;
import com.games.garrett.theliquorcabinet.provider.LCBOProductRatingContract;
import com.games.garrett.theliquorcabinet.provider.LCBOProductRatingDatabaseHelper;
import com.games.garrett.theliquorcabinet.provider.LCBOProductRatingRecord;
import com.games.garrett.theliquorcabinet.services.RecommendationDBService;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResult;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResultHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment used by the LiquorLogsActivity to display cards for each possible
 * rating value containing all the products the user has rated at that value.
 * Created by Garrett on 8/4/2017.
 */

public class RatingFragment extends Fragment implements ServiceResult{

    /* Tag for logging */
    private static final String TAG = RatingFragment.class.getCanonicalName();

    /* Maps res id of card, to the rating adapter holding ratings it should display */
    private ArrayMap<Integer, RatingAdapter> mCardMap = new ArrayMap<>();

    /* request ids to start LCBO service to find a product that the user has already
        rated, and then either view activity_details of the product, or delete it
     */
    private static final int REQUEST_LCBO_PRODUCT_AND_LAUNCH_DETAILS = 3;
    private static final int REQUEST_LCBO_PRODUCT_AND_DELETE = 4;

    /*  request id for the recommendation service */
    private static final int REQUEST_DELETE_RATING_RECORD = 102;

    /* Handler to communicate with LCBOService */
    private ServiceResultHandler mServiceResultHandler;

    /* Helper to communicate with the database */
    private RatingOp mRatingOp;

    /* A reference to context */
    private Context mContext;

    /* no op constructor */
    public RatingFragment(){}

    /**
     * Initializes view for the fragment.
     * @param inflater            Inflater
     * @param container           ViewGroup of parent view containing this view.
     * @param savedInstanceState  state information
     * @return                    view to display
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_log,
                container,
                false);

        initializeCards(view);

        return view;
    }

    /* Called when fragment is attached to context */
    @Override
    public void onAttach(Context context){
        mContext = context;
        mServiceResultHandler = new ServiceResultHandler(this);
        mRatingOp = new RatingOp(getActivity());

        super.onAttach(context);
    }

    /**
     * Creates map and adapter for each recycler view
     * @param view view containing recycler view and cards
     */
    private void initializeCards(View view){
        mCardMap = new ArrayMap<>();

        mCardMap.put(R.id.like_card,
                new RatingAdapter("1.0", null, mContext, mServiceResultHandler));

        mCardMap.put(R.id.dislike_card,
                new RatingAdapter("-1.0", null, mContext, mServiceResultHandler));

        int span = 1;  // for grid layout

        // Initialize each recycler view with the appropriate
        // adapter and set the CardView titles to display the race
        // name.
        mCardMap.forEach((integer, ratingAdapter) -> {
            // Get the card view instance.
            CardView cardView = (CardView) view.findViewById(integer);

            // Get the recycler view instance.
            RecyclerView recyclerView =
                    (RecyclerView) cardView.findViewById(R.id.list);

            // register each recycler view for context menu
            registerForContextMenu(recyclerView);

            // Set a grid layout manager with the appropriate span
            // count.
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(),
                    span));

            // Install the card's RecyclerView adapter.
            recyclerView.setAdapter(ratingAdapter);

            // Set the card title.
            TextView titleView = (TextView) cardView.findViewById(R.id.title);
            titleView.setText(formatDisplayText(ratingAdapter.getTitle()));

        });
    }

    /**
     * Converts that passed string to uppercase first letter followed
     * by lower case letters.
     */
    private String formatDisplayText(String text) {
        return text.substring(0, 1)
                .toUpperCase() + text.substring(1);
    }

    /**
     * Reset each adapter to the values stored in the passed cursor.
     *
     * @param cursor a database cursor containing a list of ratings
     */
    public void setData(Cursor cursor) {
       //Log.d(TAG, "set data called with cursor size:  " + cursor.getCount());

        mCardMap.forEach((integer, ratingAdapter) -> {
            List<LCBOProductRatingRecord> records =
                    getAllRatingLCBOProducts (ratingAdapter.getRating(),
                            cursor);
            ratingAdapter.setData(records);
            //noinspection ConstantConditions
            getView().findViewById(integer)
                    .setVisibility(records.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    /**
     * Builds and returns a list of products for each rating
     *
     * @param rating   Rating to filter for
     * @param cursor a database cursor to extract race characters from
     * @return all the products belonging to the specified rating
     */
    private List<LCBOProductRatingRecord> getAllRatingLCBOProducts(String rating,
                                                       Cursor cursor) {
        //Log.d(TAG, "Entering getAllLCBORatings with cursor of size " + cursor.getCount());

        ArrayList<LCBOProductRatingRecord> records = new ArrayList<>();
        if (cursor.moveToFirst())
            do {
                LCBOProductRatingRecord record =
                        LCBOProductRatingRecord.fromCursor(cursor);

                //Log.d(TAG, "Comparing: " + Float.toString(record.getRating()) + " ---- to ----- " + rating);
                if ( Float.toString(record.getRating()).equalsIgnoreCase(rating)){
                    records.add(record);
                }
            } while (cursor.moveToNext());

        return records;
    }

    /**
     * Called to handle the response of service is user request to view
     * activity_details of a product, or if the user wishes to delete a product.
     * @param requestCode    request code used to start service.
     * @param resultCode     result code returned by the service.
     * @param data           JSON string response from the LCBO service.
     */
    @Override
    public void onServiceResult(int requestCode, int resultCode, Bundle data){
        if( resultCode == Activity.RESULT_CANCELED){
            handleDownloadFailure();
            return;
        }

        String itemsAsJSONString = data.getString("ITEM_ARRAY_KEY");
        processResponse(requestCode, itemsAsJSONString);
    }

    /**
     * Chooses flow of control for the service.
     * @param requestCode            request code determined direction of flow.
     * @param itemsAsJSONString      data returned from the service.
     */
    private void processResponse(int requestCode, String itemsAsJSONString){
        switch(requestCode){
            case REQUEST_LCBO_PRODUCT_AND_LAUNCH_DETAILS:
                launchItemDetails(itemsAsJSONString);
                break;
            case REQUEST_LCBO_PRODUCT_AND_DELETE:
                deleteItemFromDatabase(itemsAsJSONString);
            default: break;
        }
    }

    /**
     * Remove a rating record from the content provider and the recommendation
     * database.
     *
     * @param itemsAsJSONString  JSON from LCBO API after querying the selected product
     */
    private void  deleteItemFromDatabase(String itemsAsJSONString){

        writeDeleteLogMessage();

        try {
            // parse information from json
            JSONObject page = new JSONObject(itemsAsJSONString);
            JSONObject JSONItem = page.getJSONObject("result");
            LCBOProductInformation item = new LCBOProductInformation (JSONItem);
            try {
                // delete from recommendation database
                deleteFromRecommendationDB(item.getId(), REQUEST_DELETE_RATING_RECORD);

                // delete from content provider
                mRatingOp.deleteByProductId(new String[]{item.getId()});
                mRatingOp.displayAll();

            }catch(RemoteException e){
               // Log.d(TAG, "exception: " + e);
            }
       }catch (JSONException e){
            //Log.e(TAG, "Error parsing store json string : " + e.getMessage());
       }
    }

    /**
     * Logs the size of the database before deletion.
     */
    private void writeDeleteLogMessage(){
        //Log.d(TAG, "DELETING DATABASE ENTRY");
        LCBOProductRatingDatabaseHelper mOpenHelper;
        mOpenHelper = new LCBOProductRatingDatabaseHelper(mContext);
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, LCBOProductRatingContract.RatingEntry.TABLE_NAME);
        //Log.d(TAG, "The size of the database is " + numRows);
    }

    /**
     * Launches the details activity when the user clicks 'details' in
     * the context menu.
     * @param itemsAsJSONString JSON from LCBO API after querying the selected product
     */
    private void launchItemDetails(String itemsAsJSONString){

        try {
            JSONObject page = new JSONObject(itemsAsJSONString);
            JSONObject JSONItem = page.getJSONObject("result");
            LCBOProductInformation item = new LCBOProductInformation(JSONItem);

            Intent detailsIntent = DetailsActivity.makeDetailsActivityIntent(mContext, item);
            startActivity(detailsIntent);

        }catch (JSONException e){
            //Log.e(TAG, "Error parsing store json string : " + e.getMessage());
        }
    }

    /**
     * Sends an intent to to add/update a user rating to the RecommendationService.
     * @param productID     the id of the product to be updated.
     * @param requestId     the request code for the service.
     */
    public void deleteFromRecommendationDB(String productID, int requestId){

        ArrayList<String> userRatingEntry = createUserRatingEntry(productID);

        Intent recommendDBIntent = RecommendationDBService.makeIntent(
                getContext(),
                requestId,
                userRatingEntry,
                // todo UNSAFE CAST HERE. NEED TO THINK OF A BETTER WAY TO HANDLE THIS
                // this cast requires the activity containing the fragment to implement ServiceResult
                // which is probably a poor design decision since there's nothing enforcing this
                new ServiceResultHandler((ServiceResult) getActivity()));

        //Log.d(TAG, "starting RecommendationDBService");
        getActivity().startService(recommendDBIntent);
    }

    /**
     * Creates a string array with information to update or create a user's rating entry for a product
     * @param productID   the id of the product to be updated
     * @return            a list of attributes the RecommendationService uses to update the rating record.
     */
    private ArrayList<String> createUserRatingEntry(String productID){
        SharedPreferences pref = getActivity().getSharedPreferences("activity_settings", Context.MODE_PRIVATE);
        String userID = pref.getString("userID", "");
        ArrayList<String> userRatingEntry = new ArrayList<>();
        userRatingEntry.add(userID);
        userRatingEntry.add(productID);
        return userRatingEntry;
    }

    private void handleDownloadFailure(){
        Toast.makeText(mContext,
                "Could not establish connection to service.\n" +
                        "Please check your internet connection and \n" +
                        "make sure internet permissions are granted.",
                Toast.LENGTH_LONG
        ).show();
    }
}
