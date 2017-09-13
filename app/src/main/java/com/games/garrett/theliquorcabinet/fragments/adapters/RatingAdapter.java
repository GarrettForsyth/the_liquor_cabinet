package com.games.garrett.theliquorcabinet.fragments.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.games.garrett.theliquorcabinet.R;
import com.games.garrett.theliquorcabinet.provider.LCBOProductRatingRecord;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter used to display user's past ratings.
 * RecyclerViewHolder create and bind view holder pattern.
 */
public class RatingAdapter
        extends RecyclerView.Adapter<RatingViewHolder> {

    /* The rating this adapter represents. either {+1, -1}
        Each adapter holds all the products the user has rated
        at mRating.
     */
    private final String mRating;
    /* List of user's rating records for LCBO products */
    private List<LCBOProductRatingRecord> mRecords;
    /* A reference to the context of the fragment/activity using this adapter */
    private Context mContext;
    /* A handler to communicate with the services */
    private ServiceResultHandler mServiceResultHandler;

    /**
     * Constructor keeps track of both the rating group and the LCBOProductRating
     * records.
     *
     * @param rating    the rating of LCBO products in this adapter
     * @param records all the ratings are belonging to a certain rating { +1, -1 }
     */
    public RatingAdapter(String rating, List<LCBOProductRatingRecord> records,
                         Context context, ServiceResultHandler handler) {
        /* Initialize instance variables */
        mContext = context;
        mServiceResultHandler = handler;
        mRating = rating;
        mRecords = records != null ? records : new ArrayList<>();
    }

    /**
     * Hook method called when a new view holder is required for display.
     */
    @Override
    public RatingViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fargment_log_item,
                                parent,
                                false);

        return new RatingViewHolder(view, mContext, mServiceResultHandler);
    }

    /**
     * Hook method called when an existing view holder is about to
     * be reused to display a recycler list item. Finds which item
     * is being displayed by the adapter and fills out the views
     * accordingly.
     */
    @Override
    public void onBindViewHolder(final RatingViewHolder holder,
                                 int position) {
        LCBOProductRatingRecord record = mRecords.get(position);
        holder.mTextView.setText(formatDisplayText( record.getProductName()));
        holder.setProductId(record.getProductId());
    }

    /**
     * Hook method called to determine the total number of items
     * backing this adapter.
     *
     * @return the total number of list items
     */
    @Override
    public int getItemCount() {
        return mRecords.size();
    }

    /**
     * Helper method that replaces the current list of displayed records
     * with a new list.
     *
     * @param records new list to display
     */
    public void setData(List<LCBOProductRatingRecord> records) {
        mRecords = records != null ? records : new ArrayList<>();
        notifyDataSetChanged();
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
     * Helper method that returns the race of this adapter.
     *
     * @return race string
     */
    public String getRating() {
        return mRating;
    }

    /**
     * Translates rating into a string title:
     */
    public String getTitle(){
        if(getRating().equals("1.0")) return "Drinks I Like";
        if(getRating().equals("-1.0")) return "Drinks I Dislike:";
        return null;
    }
}