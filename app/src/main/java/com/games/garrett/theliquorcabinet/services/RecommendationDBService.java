package com.games.garrett.theliquorcabinet.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Messenger;
import android.util.Log;

import com.games.garrett.theliquorcabinet.GlobalStrings;
import com.games.garrett.theliquorcabinet.services.utils.HandleRecommendationDBRequest;
import com.games.garrett.theliquorcabinet.services.utils.ResponseMessage;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 * Interacts with a webservice holding the LCBO product profiles and user profiles
 * used to create a recommendation to the user.
 */
public class RecommendationDBService extends IntentService {

    /* Tag used for logging */
    private static final String TAG =  RecommendationDBService.class.getCanonicalName();

    /* Constructor */
    public  RecommendationDBService() {
        super("RecommendationDBService");
    }

    private static final String DOWNLOAD_FAILURE = "DOWNLOAD_FAILURE";

    /**
     * Factory method for intents to start this service.
     * @param context           the context of the starting activity
     * @param requestCode       to ensure activity receives correct response
     * @param userRatingEntry   identifies the record to be operated on (contains: userID and productID)
     * @param downloadHandler   a handler to communicate with the activity.
     * @return                  an intent suitable to start this service
     */
    public static Intent makeIntent(Context context,
                                    int requestCode,
                                    ArrayList<String> userRatingEntry,
                                    Handler downloadHandler){
       // Log.d(TAG, "Creating RecommendationDBService intent...");

        return new Intent(context,  RecommendationDBService.class)
                .putStringArrayListExtra(GlobalStrings.getUserRatingEntry(), userRatingEntry)
                .putExtra(GlobalStrings.getRequestCode(), requestCode)
                .putExtra(GlobalStrings.getMessengerKey(), new Messenger(downloadHandler));
    }

    /**
     * Hook method responsible for logic.
     * Here, it defers logic to the HandleRecommendationDB request class,
     * then sends a response message tot eh initiating activity to indicate
     * the result.
     * @param intent intent send to this service
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandler called in RecommendationDBService.");

        ArrayList<String> userRatingRecord = intent.getStringArrayListExtra(GlobalStrings.getUserRatingEntry());

        int requestCode = (int) intent.getExtras().get(GlobalStrings.getRequestCode());

        String items = HandleRecommendationDBRequest.handleRequest(userRatingRecord, requestCode);
        // this will cause the response message to be set to canceled and the activities will
        // handle a download failure.
        if (items.equals(DOWNLOAD_FAILURE)) items = null;

        Messenger messenger = (Messenger) intent.getExtras().get(GlobalStrings.getMessengerKey());

        ResponseMessage.sendEntries(messenger, items,  Uri.parse(""),userRatingRecord, requestCode);
    }

}
