package com.games.garrett.theliquorcabinet.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.games.garrett.theliquorcabinet.R;
import com.games.garrett.theliquorcabinet.activities.utils.RatingOp;
import com.games.garrett.theliquorcabinet.fragments.RatingFragment;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResult;

/**
 * This activity displays all of the items the user has rated. It gives the option
 * for the user to view the details of these items or deleting them from their
 * rating profile by long touching to open a context menu.
 * Created by Garrett on 8/4/2017.
 */
public class LiquorLogActivity extends AppCompatActivity implements ServiceResult{

    /* Tag for logging */
    private static final String TAG = BrowseActivity.class.getCanonicalName();

    /**
     * Hook method called when a new activity is created.  One time
     * initialization code goes here, e.g., initializing views.
     *
     * @param savedInstanceState object that contains saved state information.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view for this Activity.
        setContentView(R.layout.activity_liquor_logs);

        // Setup support toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create a new RatingOps instance.
        RatingOp  RatingOp= new RatingOp(this);

        // If we are creating this activity for the first time
        // (savedInstanceState == null) or if we are recreating this
        // activity after a configuration change (savedInstanceState
        // != null), we always want to display the current contents of
        // the SQLite database.
        try {
            RatingOp.displayAll();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates and returns an intent suitable for starting this activity
     * @param context  The context of the activity that starts this one.
     * @return         An intent to start this activity.
     */
    public static Intent makeLiquorLogsActivityIntent(Context context){
        return new Intent(context, LiquorLogActivity.class);
    }

    // display the contents of the cursor in the product fragment
    public void displayCursor(Cursor cursor){
       // Log.d(TAG, "display cursor called with size  = " + cursor.getCount());

        RatingFragment productFragment =
                (RatingFragment) getFragmentManager().findFragmentById(R.id.product_fragment);
        if ( productFragment != null ){
            productFragment.setData(cursor);
        }
    }

    /* Get the response from the recommendation database when deleting an item.
    *   This is a no op unless there is an error in making a connection.
    */
    @Override
    public void onServiceResult(int requestCode, int resultCode, Bundle data) {
        if( resultCode == Activity.RESULT_CANCELED){
            handleDownloadFailure();
        }
    }

    /** Handles the case of the service returning a canceled download */
    private void handleDownloadFailure(){
        Toast.makeText(this,
                "Could not establish connection to service.\n" +
                        "Please check your internet connection and \n" +
                        "make sure internet permissions are granted.",
                Toast.LENGTH_LONG
        ).show();
    }
}
