package com.games.garrett.theliquorcabinet.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.games.garrett.theliquorcabinet.R;
import com.games.garrett.theliquorcabinet.activities.utils.RatingOp;
import com.games.garrett.theliquorcabinet.services.RecommendationDBService;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResult;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResultHandler;

import java.util.ArrayList;

/**
 * Used to adjust activity_settings for the application
 * Created by Garrett on 8/2/2017.
 */

public class SettingsActivity extends AppCompatActivity implements ServiceResult{

    /* tag for logging */
    private static final String TAG = SettingsActivity.class.getCanonicalName();

    /* References to the UI */
    private TextView mSeekBarLabel;
    private SeekBar mSeekBar;
    private DialogInterface.OnClickListener mDialogListener; // used to double check user's selection

    /* Reference to the RatingOp helper class for
        interactions with the content provider.
     */
    private RatingOp mRatingOp;

    /* Reference to shared preferences of the application */
    private SharedPreferences settings;

    /* A reference to the Activity's context */
    private Context mContext;

    /* References to the setting attributes */
    private float mRadius;

    /*  Request codes */
    private static final int REQUEST_DELETE_ALL_RATING_ENTRIES_FOR_USER = 104;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /* Initialize instance variables */
        mRatingOp = new RatingOp(this);
        mContext = this;

        settings = this.getSharedPreferences("activity_settings", Context.MODE_PRIVATE);
        mRadius = settings.getFloat("radius", 20000); // radius is stored in meters

        setUpSeekBar();
        setUpDialogOnClickListener();
        setUpClearRatingsButton();
        setUpShadows();

    }

    /* Assigns shadows to all the text views */
    private void setUpShadows(){
        TextView view = (TextView) findViewById(R.id.settings_title);
        view.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);

        view = (TextView) findViewById(R.id.seek_bar_label);
        view.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);

        view = (TextView) findViewById(R.id.clear_ratings_button);
        view.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);

        view = (TextView) findViewById(R.id.about_title);
        view.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);

        view = (TextView) findViewById(R.id.about);
        view.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    /* Sets up seek bar and its label */
    private void setUpSeekBar(){
        mSeekBarLabel = (TextView) findViewById(R.id.seek_bar_label);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setProgress(((int) mRadius)/1000);

        mSeekBarLabel.setText("Searching for stores within " + mSeekBar.getProgress() + " km of you.");

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = ((int) mRadius)/1000; // convert m to km

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
               // Log.d(TAG, "Changed progress from " + progress + " to " + i);
                progress = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSeekBarLabel.setText("Searching for stores within " + mSeekBar.getProgress() + " km of you.");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSeekBarLabel.setText("Searching for stores within " + progress + " km of you.");
                saveResultsToSharedPreferences();
            }
        });
    }

    /* Setup DialogInterface.OnClickListener */
    private void setUpDialogOnClickListener(){
        mDialogListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // yes is clicked
                        // delete from content provider
                        try{
                            mRatingOp.deleteAll();
                        }catch (Exception e){
                           // Log.d(TAG, "Exception trying to delete from content provider: " + e);
                        }

                        // delete from recommendation database
                        updateRecommendationDB(REQUEST_DELETE_ALL_RATING_ENTRIES_FOR_USER);

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // no button is clicked
                        break;
                }
            }
        };
    }

    /* Sets up listener for the clear ratings button */
    private void setUpClearRatingsButton(){
        Button clearRatingsButton = (Button) findViewById(R.id.clear_ratings_button);
        String text = "Clear all past ratings";
        clearRatingsButton.setText(text);

        clearRatingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Really delete all past ratings?")
                        .setPositiveButton("Yes", mDialogListener)
                        .setNegativeButton("No", mDialogListener).show();
            }
        });
    }

    /**
     * Helper function to save results of finding local stores to SharedPreferences.
     */
    private void saveResultsToSharedPreferences(){
        // add local stores to sharedPreferences to avoid look api calls next time
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("radius", (float)mSeekBar.getProgress()*1000); // convert km to m
        editor.apply();
    }

    /**
     * Creates a string array with information to delete a users rating entries
     * @return            a list of attributes the RecommendationService uses to update the rating record.
     */
    private ArrayList<String> createUserRatingEntry(){
        SharedPreferences pref = this.getSharedPreferences("activity_settings", Context.MODE_PRIVATE);
        String userID = pref.getString("userID", "");
        ArrayList<String> userRatingEntry = new ArrayList<>();
        userRatingEntry.add(userID);
        return userRatingEntry;
    }

    /**
     * Sends an intent to to add/update a user rating to the RecommendationService.
     * @param requestId     the request code for the service.
     */
    public void updateRecommendationDB(int requestId){

        ArrayList<String> userRatingEntry = createUserRatingEntry();

        Intent recommendDBIntent = RecommendationDBService.makeIntent(
                this,
                requestId,
                userRatingEntry,
                new ServiceResultHandler(this));

        //Log.d(TAG, "starting RecommendationDBService");
        this.startService(recommendDBIntent);
    }

    public static Intent makeSettingsActivityIntent(Context context){
        return new Intent(context, SettingsActivity.class);
    }

    /** no op **/
    @Override
    public void onServiceResult(int requestCode, int resultCode, Bundle data) {

    }
}
