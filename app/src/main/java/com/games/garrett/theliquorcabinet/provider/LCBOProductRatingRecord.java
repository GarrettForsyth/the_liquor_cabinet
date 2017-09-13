package com.games.garrett.theliquorcabinet.provider;

import android.database.Cursor;
import android.support.annotation.NonNull;

/**
 * A simple POJO that stores the users ratings for LCBO products.
 * Created by Garrett on 8/2/2017.
 */

public class LCBOProductRatingRecord {

    @SuppressWarnings("WeakerAccess")
    static long sInitialId = 0;

    /* Id of rating record */
    private final long mId;

    /* LCBO product's id */
    private final String mProductId;

    /* User's rating of product */
    private final float mRating;

    /* The name of the product */
    private final String mProductName;

    /* Various constructors for creating a new LCBOProductRatingEntry */


    @SuppressWarnings("unused")
    LCBOProductRatingRecord(String productId, float rating, String name ){
        mId          = ++sInitialId;
        mProductId   = productId;
        mRating      = rating;
        mProductName = name;
    }

    @SuppressWarnings("unused")
    LCBOProductRatingRecord(long id, String productId, float rating, String name){
        mId          = id;
        mProductId   = productId;
        mRating      = rating;
        mProductName = name;
    }

    @SuppressWarnings("WeakerAccess")
    LCBOProductRatingRecord(@NonNull Cursor cursor){
        mId = cursor.getInt(cursor.getColumnIndex(LCBOProductRatingContract.RatingEntry._ID));
        mProductId = cursor.getString(cursor.getColumnIndex(LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_ID));
        mRating= cursor.getFloat(cursor.getColumnIndex(LCBOProductRatingContract.RatingEntry.COLUMN_RATING));
        mProductName = cursor.getString(cursor.getColumnIndex(LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_NAME));
    }

    public static LCBOProductRatingRecord fromCursor(Cursor cursor){
        return new LCBOProductRatingRecord(cursor);
    }

    public long getId(){
        return mId;
    }

    public String getProductId(){
        return mProductId;
    }

    public float getRating(){
        return mRating;
    }

    public String getProductName(){ return mProductName; }

}
