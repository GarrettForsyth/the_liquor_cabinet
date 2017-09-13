package com.games.garrett.theliquorcabinet.fragments.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.games.garrett.theliquorcabinet.GlobalStrings;
import com.games.garrett.theliquorcabinet.services.LCBOService;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResultHandler;

/**
 * Custom view holder used by the RatingAdapterClass.
 * This view holder implements a context menu on long clicks to
 * allow the abilities of deleting items form the LiquorLogs,
 * as well as launching the activity_details activity on an item.
 */
class RatingViewHolder
        extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, View.OnClickListener,
        MenuItem.OnMenuItemClickListener{

    private static final String TAG = RatingViewHolder.class.getCanonicalName();

    final TextView mTextView;
    private Context mContext;

    @SuppressWarnings("unused")
    public String getProductId() {
        return mProductId;
    }

    void setProductId(String mProductId) {
        this.mProductId = mProductId;
    }

    private String mProductId = "";
    private ServiceResultHandler mServiceResultHandler;

    /**
     * Constructor initializes the field.
     */
    RatingViewHolder(View view, Context context, ServiceResultHandler handler) {
        super(view);
        mContext = context;
        mServiceResultHandler = handler;
        mTextView = (TextView) view;
        mTextView.setClickable(false);
        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle(((TextView) v).getText());
        MenuItem detailsOption = menu.add(Menu.NONE, v.getId(), Menu.NONE,"Details");
        MenuItem deleteOption  = menu.add(Menu.NONE, v.getId(), Menu.NONE, "Delete");
        detailsOption.setOnMenuItemClickListener(this);
        deleteOption.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        int REQUEST_LCBO_PRODUCT_AND_LAUNCH_DETAILS = 3;
        int REQUEST_LCBO_PRODUCT_AND_DELETE = 4;
        String url = GlobalStrings.getLcboApiProductsUrl() + mProductId + "?";

        switch (item.getTitle().toString()){
            case "Details":
                startDownload(Uri.parse(url), REQUEST_LCBO_PRODUCT_AND_LAUNCH_DETAILS);
                break;
            case "Delete":
                startDownload(Uri.parse(url), REQUEST_LCBO_PRODUCT_AND_DELETE );
                break;
            default:
                break;
        }
        return true;
    }

    /* no op */
    @Override
    public void onClick(View view){}

    /**
     * Starts the LCBOService for a result determined by the
     * url and requestId.
     * @param url         URL of LCBO API
     * @param requestId   Type of request
     */
    private void startDownload(Uri url, int requestId){
        Intent downloadIntent = LCBOService.makeIntent(
                mContext,
                requestId,
                url,
                mServiceResultHandler);

        //Log.d(TAG, "starting LCBOService download for " + url.toString());
        mContext.startService(downloadIntent);
    }

    /**
     * Return a string value for this object.
     */
    @Override
    public String toString() {
        return super.toString() + " '" + mTextView.getText() + "'";
    }
}