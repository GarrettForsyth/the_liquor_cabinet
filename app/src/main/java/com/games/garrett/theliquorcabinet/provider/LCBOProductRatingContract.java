package com.games.garrett.theliquorcabinet.provider;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * This contract defines metadata for the LCBOProductRatingContentProvider
 * including the provider's access URIs and its database constants.
 * Created by Garrett on 8/2/2017.
 */

@SuppressWarnings("WeakerAccess")
public class LCBOProductRatingContract {

    /**
     * ContentProvider's unique identifier
     */
    public static final String CONTENT_AUTHORITY =
            "com.theliquorcabinet";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will
     * use to contact the content provider
     */
    public static final Uri BASE_CONTENT_URI =
            Uri.parse("content://"
                      + CONTENT_AUTHORITY);

    /**
     * Possible paths
     */
    public static final String PATH_RATING =
            RatingEntry.TABLE_NAME;

    /*
    RatingEntry to store in the content provider.
     */
    public static final class RatingEntry implements BaseColumns{
        /**
         * Use BASE_CONTENT_URI to create the unique URI for Acronym
         * Table that apps will use to contact the content provider
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_RATING).build();

        /**
         * When the cursor returned for a given URI b the
         * Content Provider contains 0..x items.
         */
        public static final String CONTENT_ITEMS_TYPE =
                "vnd.android.cursor.dir/"
                + CONTENT_AUTHORITY
                + "/"
                + PATH_RATING;

        /**
         * When the Cursor returned for a given URI by the CP
         * contains one item.
         */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/"
                + CONTENT_AUTHORITY
                + "/"
                + PATH_RATING;

        /**
         * Columns to display.
         */
        public static final String sColumnsToDisplay[] =
                new String[] {
                        LCBOProductRatingContract.RatingEntry._ID,
                        LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_ID,
                        LCBOProductRatingContract.RatingEntry.COLUMN_RATING,
                        LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_NAME

                };

        /**
         * Name of the database table.
         */
        public static final String TABLE_NAME = "rating_table";

        /**
         * Columns to store data.
         */
        public static final String COLUMN_PRODUCT_ID    = "product_id";
        public static final String COLUMN_RATING        = "rating";
        public static final String COLUMN_PRODUCT_NAME = "product_name";

        /**
         * Return a Uri that points to the row containing a given id.
         *
         * @param id row id
         * @return Uri URI for the specified row id
         */
        public static Uri buildUri(Long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
