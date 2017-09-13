package com.games.garrett.theliquorcabinet.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Messenger;
import android.util.Log;

import com.games.garrett.theliquorcabinet.GlobalStrings;
import com.games.garrett.theliquorcabinet.services.utils.LCBODownload;
import com.games.garrett.theliquorcabinet.services.utils.ResponseMessage;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 * This service request information from the LCBO API and sends its results back
 * via a message to a class implementing the onServiceResult interface.
 */
public class LCBOService extends IntentService {

    /* Tag used for logging */
    private static final String TAG = LCBOService.class.getCanonicalName();

    /* Constructor */
    public LCBOService() {
        super("LCBOService");
    }

    /**
     * Factory method for intents to start this service.
     * @param context           the context of the starting activity
     * @param requestCode       to ensure activity receives correct response
     * @param url               describes what part of the LCBO API to access
     * @param downloadHandler   a handler to communicate with the activity.
     * @return                  an intent suitable to start this service
     */
    public static Intent makeIntent(Context context,
                                    int requestCode,
                                    Uri url,
                                    Handler downloadHandler){

        return new Intent(context, LCBOService.class)
                .setData(url)
                .putExtra(GlobalStrings.getRequestCode(), requestCode)
                .putExtra(GlobalStrings.getMessengerKey(), new Messenger(downloadHandler));
    }

    /**
     * Hook method responsible for logic.
     * Here, it extracts the url from the intent which directs to what part of the
     * LCBO API to access. It then attempts to download the requested information
     * and send it to the activity in a message.
     * @param intent an intent worthy of starting this prestigious service.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
       // Log.d(TAG, " onHandleIntent called in LCBOService.");

        Uri url = intent.getData();
        String items = LCBODownload.downloadLCBOItems(url);

        int requestCode = (int) intent.getExtras().get(GlobalStrings.getRequestCode());
        Messenger messenger = (Messenger) intent.getExtras().get(GlobalStrings.getMessengerKey());

        ResponseMessage.sendEntries(messenger, items, url,new ArrayList<>(), requestCode);
    }

}
