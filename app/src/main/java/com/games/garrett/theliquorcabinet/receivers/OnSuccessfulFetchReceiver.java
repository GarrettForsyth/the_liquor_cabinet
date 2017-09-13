package com.games.garrett.theliquorcabinet.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * This trivial receiver sends the user a toast when it
 * receives a broadcast. The toast indicates whether
 * an attempted server fetch was successful or not.
 *
 * Created by Garrett on 8/4/2017.
 */

public class
OnSuccessfulFetchReceiver extends BroadcastReceiver{

    /* Tag for logging */
    protected final String TAG = getClass().getSimpleName();

    /* Name for action this receiver listens for*/
    public final static String ACTION_VIEW_FETCH_SUCCESS = "com.games.garrett.theliquorcabinet.action.NOTIFY_SUCCESSFUL_FETCH";

    /**
     * Hook method called by the AndroidManagerService (AMS)
     * framework after a broadcast has been sent.
     * @param context the caller's context.
     * @param intent  the intent sent to this receiver.
     */
    @Override
    public void onReceive(Context context, Intent intent){
        //Log.d(TAG, "onReceive() called in OnSuccessfulFetchReceiver");
        boolean success = intent.getBooleanExtra("SUCCESS", false);
        String message;
        if(success)   message = "Successfully fetched data!";
        else          message = "Did not successfully fetch data.";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Factory method to make a OnSuccessfulFetch intent.
     * @param context the context of the calling activity.
     * @param success the outcome of the attempted fetch.
     * @return        an intent that this receiver is tuned to.
     */
    public static Intent makeOnSuccessfulFetchIntent(Context context, boolean success){
        return new Intent(OnSuccessfulFetchReceiver.ACTION_VIEW_FETCH_SUCCESS)
                .putExtra("SUCCESS",success)
                .setPackage(context.getPackageName());
    }

}
