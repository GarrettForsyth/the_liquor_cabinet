package com.games.garrett.theliquorcabinet.activities.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.games.garrett.theliquorcabinet.activities.LiquorLogActivity;
import com.games.garrett.theliquorcabinet.provider.LCBOProductRatingContract;

import static android.content.ContentValues.TAG;

/**
 * Support class to simplify operations with LCBOProductRatingProvider.
 * Created by Garrett on 8/4/2017.
 */

public class RatingOp {

    /* A reference to the activity using this helper class */
    private final Activity mActivity;

    /* Proxy for accessing the provider */
    private ContentResolver mCr;

    /**
     * Constructor
     * @param activity the activity using this helper class
     */
    public RatingOp(Activity activity){
        mActivity = activity;
        mCr = mActivity.getContentResolver();
    }



    /**
     * Insert a product into the content provider
     * @param productId   product id to be inserted
     * @param rating      the user's rating of the product to be inserted
     * @param productName the product's name to be inserted
     * @return            the uri address of the inserted product
     * @throws RemoteException thrown if unable to insert data into content provider.
     */
    public Uri insert(String productId, float rating, String productName) throws RemoteException{
        final ContentValues cvs = new ContentValues();

        cvs.put(LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_ID,
                productId);

        cvs.put(LCBOProductRatingContract.RatingEntry.COLUMN_RATING,
                rating);

        cvs.put(LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_NAME,
                productName);

        return insert(LCBOProductRatingContract.RatingEntry.CONTENT_URI,
                cvs);
    }

    private Uri insert(Uri uri, ContentValues cvs){
        return mCr.insert(uri, cvs);
    }

    /* Insert an array of products into the provider */
    @SuppressWarnings("unused")
    public int bulkInsert(String[] productIds, float[] ratings, String[] productNames){
        ContentValues[] cvsArray =
                new ContentValues[productIds.length];

        int i = 0;

        for (String productId : productIds) {
            ContentValues cvs = new ContentValues();

            cvs.put(LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_ID,
                    productId);

            cvs.put(LCBOProductRatingContract.RatingEntry.COLUMN_RATING,
                    ratings[i]);

            cvs.put(LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_NAME,
                    productNames[i]);

            cvsArray[i++] = cvs;
        }

        return bulkInsert(LCBOProductRatingContract.RatingEntry.CONTENT_URI,
                cvsArray);
    }

    private  int bulkInsert(Uri uri,
                             ContentValues[] cvsArray){
        return mCr.bulkInsert(uri, cvsArray);
    }

    /* return cursor from a query to the provider */
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder){
        return mCr.query(uri,
                projection,
                selection,
                selectionArgs,
                sortOrder);
    }

    /* update */
    @SuppressWarnings("unused")
    public int updateByUri(Uri uri,
                           String productId,
                           float rating,
                           String productName) throws RemoteException {
        final ContentValues cvs = new ContentValues();

        cvs.put(LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_ID,
                productId);

        cvs.put(LCBOProductRatingContract.RatingEntry.COLUMN_RATING,
                rating);

        cvs.put(LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_NAME,
                productName);

        return update(uri, cvs, null, null);
    }

    public int updateByProductId(String productId, float rating, String productName) throws RemoteException {
        final ContentValues cvs = new ContentValues();

        cvs.put(LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_ID,
                productId);

        cvs.put(LCBOProductRatingContract.RatingEntry.COLUMN_RATING,
                rating);

        cvs.put(LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_NAME,
                productName);

        return update(LCBOProductRatingContract.RatingEntry.CONTENT_URI,
                cvs,
                LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_ID,
                new String[] { productId } );
    }

    private int update(Uri uri,
                      ContentValues cvs,
                      String selection,
                      String[] selectionArgs){
        return mCr.update(uri,
                cvs,
                selection,
                selectionArgs);
    }

    /* delete an array of ratings by their product ids */
    public int deleteByProductId(String[] productIds) throws RemoteException{
        return delete(LCBOProductRatingContract.RatingEntry.CONTENT_URI,
                LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_ID,
                productIds);
    }

    private int delete(Uri uri,
                         String selection,
                         String[] selectionArgs){
        return mCr.delete(uri,
                selection,
                selectionArgs);
    }

    @SuppressWarnings("unused")
    public int deleteAll() throws RemoteException {
        return delete(LCBOProductRatingContract.RatingEntry.CONTENT_URI,
                null,
                null);
    }

    /* queries the content provider for all rating records
        and returns a cursor the the calling activity.
        If the activity is an instance of the LiquorLogsActivity
        (which it is expected to be), it will call the activity's
        displayAll(Cursor cursor) method with the findings of
        the query.
     */
    public void displayAll() throws RemoteException {
        /* Cursor to store rating records */
        Cursor cursor;
        // query for all ratings
        cursor = query(LCBOProductRatingContract.RatingEntry.CONTENT_URI,
                LCBOProductRatingContract.RatingEntry.sColumnsToDisplay,
                LCBOProductRatingContract.RatingEntry.COLUMN_RATING,
                null,
                null);

        //Log.d(TAG, "DISPLAY ALL : CURSOR RETURNED HAS SIZE = " + cursor.getCount());

        //inform user if nothing to show
        if (cursor.getCount() == 0){
            Toast.makeText( mActivity, "No items to display!",
                    Toast.LENGTH_SHORT).show();

            // remove display
            if( mActivity instanceof LiquorLogActivity) {
                ((LiquorLogActivity)mActivity).displayCursor(cursor);
            }
            else {
               // Log.d(TAG, "not LiquorLogActivity, not displaying cursor. ");
            }
        }

        else{
            if( mActivity instanceof LiquorLogActivity) {
                ((LiquorLogActivity)mActivity).displayCursor(cursor);
            }
            else {
              //  Log.d(TAG, "not LiquorLogActivity, not displaying cursor. ");
            }
        }
    }


}
