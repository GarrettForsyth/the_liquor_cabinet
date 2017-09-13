package com.games.garrett.theliquorcabinet.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

/**
 * A helper class to interact with the SQLite database used by
 * the content provider.
 * Created by Garrett on 8/2/2017.
 */

@SuppressWarnings("WeakerAccess")
public class LCBOProductRatingDatabaseHelper extends SQLiteOpenHelper {

    /**
     * Database name
     */
    private static final String DATABASE_NAME =
            "com_theliquorcabinet_rating_db";

    /**
     * Database version number
     */
    private static int DATABASE_VERSION = 1;

    /*
    SQL create table statements
     */

    final String SQL_CREATE_RATING_TABLE =
            "CREATE TABLE "
            + LCBOProductRatingContract.RatingEntry.TABLE_NAME + " ("
            + LCBOProductRatingContract.RatingEntry._ID + " INTEGER PRIMARY KEY, "
            + LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
            + LCBOProductRatingContract.RatingEntry.COLUMN_PRODUCT_ID + " TEXT NOT NULL, "
            + LCBOProductRatingContract.RatingEntry.COLUMN_RATING + " TEXT NOT NULL "
            + " );";

    /**
     * Constructor
     * Initialize name and version.
     * Doesn't construct the database (done in onCreate() hook)
     * Places database in application's cache dir, which is auto cleaned up by
     * Android if the device runs low on storage space.
     *
     * @param context Any context
     */
    public LCBOProductRatingDatabaseHelper(Context context){
        super(  context,
                context.getCacheDir()
                + File.separator
                + DATABASE_NAME,
                null,
                DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //create the table
        db.execSQL(SQL_CREATE_RATING_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {
        // Delete the existing tables
        db.execSQL("DROP TABLE IF EXISTS "
                    + LCBOProductRatingContract.RatingEntry.TABLE_NAME);
        // Create new table
        onCreate(db);
    }
}
