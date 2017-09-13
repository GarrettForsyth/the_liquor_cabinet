package com.games.garrett.theliquorcabinet.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.games.garrett.theliquorcabinet.R;
import com.games.garrett.theliquorcabinet.POJOs.LCBOProductInformation;
import com.games.garrett.theliquorcabinet.activities.utils.RatingOp;
import com.games.garrett.theliquorcabinet.fragments.adapters.DisplayProductsAdapter;
import com.games.garrett.theliquorcabinet.provider.LCBOProductRatingContract;
import com.games.garrett.theliquorcabinet.services.RecommendationDBService;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResult;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResultHandler;

import java.util.ArrayList;

/**
 * Fragment that uses and LCBOProductInformation adapter to display LCBO
 * products to the user in a recycler view.
 *
 * The user can swipe left to rate the product '-1' or right to rate the
 * product '+1". Each rating is sent to the LCBOProductRatingProvider and
 * also to the recommendation database via the RecommendationService.
 *
 * Clicking on an LCBO product will launch the details activity for that
 * product.
 *
 * Created by garrett on 21/08/17.
 */

public class DisplayProductsFragment extends android.support.v4.app.Fragment{

    /* A logging tag */
    private static final String TAG = DisplayProductsFragment.class.getCanonicalName();
    /* List of LCBO products to be displayed by the fragment */
    private ArrayList<LCBOProductInformation> mProductDataSet = new ArrayList<>();
    /* Request code for the RecommendationService */
    private static final int REQUEST_ADD_RATING_RECORD    = 100;

    /* No op constructor */
    public DisplayProductsFragment(){}

    /**
     * onCreate just defers to the super class.
     * @param savedInstanceState the saved state.
     */
    @Override
    public  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    /**
     * Called to initialize the fragment view.
     * The activity will pass which LCBO products it wishes to display via
     * a bundle.
     * @param inflater           Instantiates layout corresponding to a view
     * @param container          the parent the layout is inserted into.
     * @param savedInstanceState saved state.
     * @return                   the view for the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){


        /* get the products data set from bundle which was passed by an activity */
        Bundle bundle = this.getArguments();
        if (bundle != null)  mProductDataSet = bundle.getParcelableArrayList("PRODUCT_INFORMATION");

        if (mProductDataSet != null) {
            Log.d(TAG, "testing if bundle was based is size : " + mProductDataSet.size());
        }

        // inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_display_products, container, false);

        /* Set up the RecyclerView */
        RecyclerView recyclerViewOfProducts = (RecyclerView) rootView.findViewById(R.id.recycler_view_of_products);
        DisplayProductsAdapter adapter = new DisplayProductsAdapter(mProductDataSet, getActivity().getBaseContext());
        recyclerViewOfProducts.setAdapter(adapter);

        setUpSwipe(recyclerViewOfProducts, adapter);

        return rootView;
    }

    /**
     * Sets up left and right swipe for items in the recycler view.
     * Each swipe will remove the object from the adapter and update
     * the content provider and recommendation  service.
     * @param recyclerViewOfProducts  the recycler view that contains swipe-able items.
     * @param adapter                 the adapter used by the recycler view.
     */
    private void setUpSwipe(RecyclerView recyclerViewOfProducts, DisplayProductsAdapter adapter){
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder vh, int swipeDir){
                        String rating;
                        if(swipeDir == ItemTouchHelper.RIGHT) rating = "1";
                        else                                  rating = "-1";

                        onRatingChange(mProductDataSet.get(vh.getAdapterPosition()), rating);
                        mProductDataSet.remove(vh.getAdapterPosition());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target){
                        return false;
                    }

                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewOfProducts);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerViewOfProducts.setLayoutManager(llm);
    }

    /**
     * Update the content provider with the user's rating of a product
     * @param item        item that has been rated.
     * @param rating      the rating the user gave.
     */
    private void onRatingChange(LCBOProductInformation item, String rating){

        Log.d(TAG, "onRatingChanged called on " + item.getName() + " for new rating " + rating);
        // get an instance of the helper class for interacting with the content provider
        RatingOp ratingOp = new RatingOp(getActivity());

        // query for product that was just rated
        Cursor cursor = ratingOp.query(LCBOProductRatingContract.RatingEntry.CONTENT_URI,
                LCBOProductRatingContract.RatingEntry.sColumnsToDisplay,
                LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_ID,
                new String[]{item.getId()},
                null);

        // create new entry, if not already inserted
        if (cursor != null && cursor.getCount() == 0) {
            createNewRatingEntry(item, rating, ratingOp);
        }
        // update if entry is already in table
        else {
            updateRatingEntry(item, rating, ratingOp);
        }

        // also send update to recommendation data base via recommendation service
        // database will determine whether the record needs to be created or updated.
        updateRecommendationDB(item.getId(), rating, REQUEST_ADD_RATING_RECORD);
    }

    /**
     * Creates a new entry in the content provider.
     * @param item        item to be inserted.
     * @param rating      user rating of the item.
     * @param ratingOp    helper to interact with the content provider
     */
    private void createNewRatingEntry(LCBOProductInformation item, String rating, RatingOp ratingOp){
        Log.d(TAG, "onRatingChange creating new entry or updating  for  " +
                item.getName() + " for new rating " + rating);
        try {
            ratingOp.insert(item.getId(), Float.parseFloat(rating), item.getName());
        } catch (RemoteException e) {
            Log.d(TAG, "exception " + e);
        }
    }

    /**
     * Updates an existing entry in the content provider.
     * @param item        item to be updated.
     * @param rating      user rating of the item.
     * @param ratingOp    helper to interact with the content provider
     *
     */
    private void updateRatingEntry(LCBOProductInformation item, String rating, RatingOp ratingOp){
        Log.d(TAG, "onRatingBarChange updating entry for  " + item.getName() + " for new rating " + rating);
        try {
            ratingOp.updateByProductId(item.getId(), Float.parseFloat(rating), item.getName());
        } catch (RemoteException e) {
            Log.d(TAG, "exception " + e);
        }
    }

    /**
     * Sends an intent to to add/update a user rating to the RecommendationService.
     * @param productID     the id of the product to be updated.
     * @param rating        the rating the user gave.
     * @param requestId     the request code for the service.
     */
    public void updateRecommendationDB(String productID, String rating, int requestId){

        ArrayList<String> userRatingEntry = createUserRatingEntry(productID, rating);

        Intent recommendDBIntent = RecommendationDBService.makeIntent(
                getContext(),
                requestId,
                userRatingEntry,
                // todo UNSAFE CAST HERE. NEED TO THINK OF A BETTER WAY TO HANDLE THIS
                // this cast requires the activity containing the fragment to implement ServiceResult
                // which is probably a poor design decision since it requires an activity to implement
                // ServiceResult to use this fragment and this is not enforced at compile time.
                new ServiceResultHandler((ServiceResult) getActivity()));

        Log.d(TAG, "starting RecommendationDBService");
        getActivity().startService(recommendDBIntent);
    }

    /**
     * Creates a string array with information to update or create a user's rating entry for a product
     * @param productID   the id of the product to be updated
     * @param rating      the rating the user gave the product
     * @return            a list of attributes the RecommendationService uses to update the rating record.
     */
    private ArrayList<String> createUserRatingEntry(String productID, String rating){
        SharedPreferences pref = getActivity().getSharedPreferences("activity_settings", Context.MODE_PRIVATE);
        String userID = pref.getString("userID", "");
        ArrayList<String> userRatingEntry = new ArrayList<>();
        userRatingEntry.add(userID);
        userRatingEntry.add(productID);
        userRatingEntry.add(rating);
        return userRatingEntry;
    }

}
