package com.games.garrett.theliquorcabinet.activities.utils;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;


/**
 * Contains logic for requesting a permission for fine location.
 * Fine location is needed to find LCBO stores that are close to the
 * user's location to give more relevant inventory information.
 * Created by Garrett on 7/29/2017.
 */

public class RequestFineLocationPermission {

    /* A tag for logging */
    private final String TAG = RequestFineLocationPermission.class.getCanonicalName();
    /* An integer representing the type of request */
    private final int  MY_PERMISSION_ACCESS_FINE_LOCATION = 1;
    /* A reference to the activity that is asking for the permission. */
    private Activity mActivity;

    /* Constructor */
    public RequestFineLocationPermission(Activity activity){
        mActivity = activity;
    }

    /**
     * Returns true if the user has permission for fine location. If not, it will show
     * the rationale and request the permission from the user, and then return false.
     * Response to the permission request are handled in the activity.
     * @return true if user has fine location permission, false otherwise.
     */
    public boolean checkPermission(){
        if ( ContextCompat.checkSelfPermission( mActivity.getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ) {
           // Log.d(TAG, "Location permission is not granted. Requesting permission..");

            if(!mActivity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
               // Log.d(TAG, "Showing rationale message to user.");

                showMessageOkCancel("This app need GPS to find stores close to you.",
                        (dialog, which) -> mActivity.requestPermissions(new String[]{
                                        Manifest.permission.ACCESS_FINE_LOCATION},
                                         MY_PERMISSION_ACCESS_FINE_LOCATION));

            }
            return false;
        }
       // Log.d(TAG, "Location permission granted. Proceeding..");
        return true;
    }

    /**
     * Shows dialog if user denies permissions and click 'don't ask again'
     * @param message     The message to display to the user.
     * @param okListener  The listener for the ok button.
     */
    private void showMessageOkCancel(String message, DialogInterface.OnClickListener okListener){
        new AlertDialog.Builder(mActivity)
                .setMessage(message)
                .setPositiveButton("Ok", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();

    }
}
