package com.games.garrett.theliquorcabinet.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

/**
 * Content provider to track user's ratings of LCBO products.
 * Created by Garrett on 8/2/2017.
 */

public class LCBOProductRatingProvider extends ContentProvider {

    /* Debug tag */
    protected final static String TAG =
            LCBOProductRatingProvider.class.getSimpleName();

    /**
     * Use LCBOProductRatingDatabaseHelper to manage database creation and version
     * management.
     */
    private LCBOProductRatingDatabaseHelper mOpenHelper;

    /**
     * Context for the CP
     */
    private Context mContext;

    /**
     * Return true if successfully started.
     */
    @Override
    public boolean onCreate() {
        mContext = getContext();

        // Select concrete implementor.
        // Create the LCBOProductDatabaseHelper
        mOpenHelper =
                new LCBOProductRatingDatabaseHelper(mContext);
        return true;
    }

    /**
     * The code that is returned when a URI for more than
     * 1 items is matched against the given components. Most be +ve
     */
    public static final int RATINGS = 100;

    /**
     * The code that is returned when a URI for exactly
     * 1 item is matched against the given components. Must be +ve.
     */
    public static final int RATING = 101;

    /**
     * The URI Matcher used by this content provider
     */
    private static final UriMatcher sUriMatcher =
            buildUriMatcher();

    /**
     * Helper method to match each URI to the acronym
     * integers constant defined above.
     *
     * @return UriMatcher
     */
    protected static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding
        // code to return when a  match is found. The code passed
        // into the constructor represents the code to return
        // for the rootURI. It's common to use NO_MATCH as the
        // code for this case

        final UriMatcher matcher =
                new UriMatcher(UriMatcher.NO_MATCH);

        // For each type of URI that is added, a corresponding code is created.
        matcher.addURI(LCBOProductRatingContract.CONTENT_AUTHORITY,
                LCBOProductRatingContract.PATH_RATING,
                RATINGS);

        matcher.addURI(LCBOProductRatingContract.CONTENT_AUTHORITY,
                LCBOProductRatingContract.PATH_RATING
                        + "/#",
                RATING);
        return matcher;
    }

    /**
     * Method called to handle type request from client applications.
     * It returns the MIME type of the data associated with each URI.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        // Match the id returned by the URiMatcher to return appropriate
        // MIME_TYPE
        switch (sUriMatcher.match(uri)) {
            case RATINGS:
                return LCBOProductRatingContract.RatingEntry.CONTENT_ITEMS_TYPE;
            case RATING:
                return LCBOProductRatingContract.RatingEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri : " +
                        uri);
        }
    }

    /**
     * Method called to handle insert request form client apps.
     */

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues cvs) {
        Uri returnUri;

        // Try to match against the path in a uri. It returns the
        // code for the matched node (added using addURI), or -1
        // if there is no matched node. If there's a match insert
        // a new row
        switch (sUriMatcher.match(uri)) {
            case RATINGS:
                returnUri = insertRatings(uri, cvs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:"
                        + uri);
        }

        // Notifies registered observers that  a row was inserted.
        mContext.getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    private Uri insertRatings(Uri uri, ContentValues cvs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        long id =
                db.insert(LCBOProductRatingContract.RatingEntry.TABLE_NAME,
                        null,
                        cvs);

        // Check if a new row is inserted or not.
        if (id > 0)
            return LCBOProductRatingContract.RatingEntry.buildUri(id);
        else
            throw new android.database.SQLException("Failed to insert" +
                    " row into " + uri);
    }

    /**
     * Method that handles bulk insert requests.
     */
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] cvsArray) {

        for (ContentValues cvs : cvsArray)
            printRatings("bulk inserting", cvs, uri);

        // Try to match against the path in a uri. It returns the
        // code for the matched node (added using addURI), or -1
        // if there is no matched node. If there's a match insert
        // a new row
        switch (sUriMatcher.match(uri)) {
            case RATINGS:
                int returnCount = bulkInsertRatings(uri, cvsArray);

                if (returnCount > 0) {
                    // Notifies registered observers that row(s) were inserted
                    mContext.getContentResolver().notifyChange(uri, null);
                }
                return returnCount;
            default:
                throw new UnsupportedOperationException();
        }
    }

    /**
     * Method that handles bulk insert requests
     */
    @SuppressWarnings("UnusedParameters")
    private int bulkInsertRatings(Uri uri, ContentValues[] cvsArray) {
        // Create and/or open a database that will be used for reading
        // and writing. Once opened successfully, the database is
        // cached, so you can call this method every time you need to
        // write to the database
        final SQLiteDatabase db =
                mOpenHelper.getWritableDatabase();

        int returnCount = 0;

        // Begins a transaction in EXCLUSIVE mode.
        db.beginTransaction();
        try {
            for (ContentValues cvs : cvsArray) {
                final long id =
                        db.insert(LCBOProductRatingContract.RatingEntry.TABLE_NAME,
                                null,
                                cvs);
                if (id != -1)
                    returnCount++;
            }

            // Marks the current transaction as successful.
            db.setTransactionSuccessful();
        } finally {
            // End a transaction
            db.endTransaction();
        }
        return returnCount;
    }

    /**
     * Method called to handle query request from client applications
     */
    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectArgs,
                        String sortOrder) {
        Cursor cursor;

        // Match the id returned by UriMatcher to query appropriate rows.
        switch (sUriMatcher.match(uri)) {
            case RATINGS:
                cursor = queryRatings(uri,
                        projection,
                        selection,
                        selectArgs,
                        sortOrder);
                break;
            case RATING:
                cursor = queryRating(uri,
                        projection,
                        selection,
                        selectArgs,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: "
                        + uri);
        }
        return cursor;
    }

    /**
     * Method called to handle query request from client applications.
     */
    @SuppressWarnings("UnusedParameters")
    private Cursor queryRatings(@NonNull Uri uri,
                                String[] projection,
                                String selection,
                                String[] selectArgs,
                                String sortOrder) {
        // Expand the selection if necessary.
        selection = addSelectionArgs(selection,
                selectArgs,
                "OR");
        return mOpenHelper.getReadableDatabase().query
                (LCBOProductRatingContract.RatingEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectArgs,
                        null,
                        null,
                        sortOrder);
    }

    /**
     * Method called to handle query requests from client applications.
     */
    private Cursor queryRating(Uri uri,
                               String[] projection,
                               String selection,
                               String[] selectionArgs,
                               String sortOrder) {
        // Query the SQLite database for the particular rowId based on
        // (a subset of) the parameters passed into the method.
        return mOpenHelper.getReadableDatabase().query
                (LCBOProductRatingContract.RatingEntry.TABLE_NAME,
                        projection,
                        addKeyIdCheckToWhereStatement(selection,
                                ContentUris.parseId(uri)),
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
    }

    /**
     * Method called to handle update request from client applications.
     */
    @Override
    public int update(@NonNull Uri uri,
                      ContentValues cvs,
                      String selection,
                      String[] selectionArgs) {
        int returnCount;

        printRatings("updating", cvs, uri);

        // Try to match against the path in a url.  It returns the
        // code for the matched node (added using addURI), or -1 if
        // there is no matched node.  If there's a match update rows.
        switch (sUriMatcher.match(uri)) {
            case RATINGS:
                returnCount = updateRatings(uri,
                        cvs,
                        selection,
                        selectionArgs);
                break;
            case RATING:
                returnCount = updateRating(uri,
                        cvs,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        if (returnCount > 0) {
            // Notifies registered observers that row(s) were updated
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return returnCount;
    }

    /**
     * Method called to handle update requests.
     */
    @SuppressWarnings("UnusedParameters")
    private int updateRatings(Uri uri,
                              ContentValues cvs,
                              String selection,
                              String[] selectionArgs) {
        // Expand the selection if necessary.
        selection = addSelectionArgs(selection,
                selectionArgs,
                " OR ");
        return mOpenHelper.getWritableDatabase().update
                (LCBOProductRatingContract.RatingEntry.TABLE_NAME,
                        cvs,
                        selection,
                        selectionArgs);
    }

    /**
     * Method called to handle update request.
     */
    private int updateRating(Uri uri,
                             ContentValues cvs,
                             String selection,
                             String[] selectionArgs) {
        // Expand the selection if necessary.
        selection = addSelectionArgs(selection,
                selectionArgs,
                " OR ");
        // Just update a single row in the database.
        return mOpenHelper.getWritableDatabase().update
                (LCBOProductRatingContract.RatingEntry.TABLE_NAME,
                        cvs,
                        addKeyIdCheckToWhereStatement(selection,
                                ContentUris.parseId(uri)),
                        selectionArgs);
    }

    /**
     * Method called to handle delete requests from client
     */
    @Override
    public int delete(@NonNull Uri uri,
                      String selection,
                      String[] selectionArgs) {
        int returnCount;

        printSelectionArgs("deleting", selection, selectionArgs, uri);

        // Try to match against the path in a url.  It returns the
        // code for the matched node (added using addURI), or -1 if
        // there is no matched node.  If there's a match delete rows.
        switch (sUriMatcher.match(uri)) {
            case RATINGS:
                returnCount = deleteRatings(uri,
                        selection,
                        selectionArgs);
                break;
            case RATING:
                returnCount = deleteRating(uri,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        if (selection == null
                || returnCount > 0)
            // Notifies registered observers that row(s) were deleted.
            mContext.getContentResolver().notifyChange(uri,
                    null);

        return returnCount;
    }

    /**
     * Method called to handle delete requests from client
     * applications.
     */
    @SuppressWarnings("UnusedParameters")
    private int deleteRatings(Uri uri,
                              String selection,
                              String[] selectionArgs) {
        // Expand the selection if necessary.
        selection = addSelectionArgs(selection,
                selectionArgs,
                " OR ");
        return mOpenHelper.getWritableDatabase().delete
                (LCBOProductRatingContract.RatingEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
    }

    /**
     * Method called to handle delete requests from client
     * applications.
     */
    private int deleteRating(Uri uri,
                                String selection,
                                String[] selectionArgs) {
        // Expand the selection if necessary.
        selection = addSelectionArgs(selection,
                selectionArgs,
                " OR ");
        // Just delete a single row in the database.
        return mOpenHelper.getWritableDatabase().delete
                (LCBOProductRatingContract.RatingEntry.TABLE_NAME,
                        addKeyIdCheckToWhereStatement(selection,
                                ContentUris.parseId(uri)),
                        selectionArgs);
    }

    /**
     * Concatenate all selection args for a given operation
     * @param selection      the selection
     * @param selectionArgs  selection args
     * @param operation      operation to perform on args
     * @return               concatenated string of selection args
     */
    private String addSelectionArgs(String selection,
                                    String[] selectionArgs,
                                    String operation){
        // Handle null case
        if (selection == null || selectionArgs == null) return null;
        else{
            String selectionResult = "";

            for (int i=0; i < selectionArgs.length -1; ++i){
                selectionResult += (selection
                + " =  ? "
                + operation
                + " ");
            }

            // handle final selection case
            selectionResult += (selection + " = ?");

            printSelectionArgs(operation,
                    selectionResult,
                    selectionArgs,
                    null);

            return selectionResult;
        }
    }

    /**
     * Helper method that appends a given key id to the end of the
     * WHERE statement parameter.
     */
    private static String addKeyIdCheckToWhereStatement(String whereStatement,
                                                        long id) {
        String newWhereStatement;
        if (TextUtils.isEmpty(whereStatement))
            newWhereStatement = "";
        else
            newWhereStatement = whereStatement + " AND ";

        // Append the key id to the end of the WHERE statement.
        return newWhereStatement
                + LCBOProductRatingContract.RatingEntry._ID
                + " = '"
                + id
                + "'";
    }

    /**
     * Print out the ratings to logcat.
     */
    void printRatings(String operation,
                         ContentValues cvs,
                         Uri uri) {
        //Log.d(TAG, operation + " on " + uri);
        for (String key : cvs.keySet()) {
           // Log.d(TAG, key + " " + cvs.get(key));
        }

    }

    /**
     * Printout the selection args to logcat.
     */
    void printSelectionArgs(String operation,
                            String selectionResult,
                            String[] selectionArgs,

                            Uri uri) {
        /*
        Log.d(TAG,
                operation
                        + " on "
                        + (uri == null ? "null" : uri)
                        + " selection = "
                        + selectionResult
                        + " selectionArgs = ");

        if (selectionArgs != null && selectionArgs.length > 0)
            for (String args : selectionArgs)
                Log.d(TAG,
                        args + " ");
        */
    }
}
