package com.games.garrett.theliquorcabinet.services.utils;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;


import com.games.garrett.theliquorcabinet.GlobalStrings;

import java.util.ArrayList;

/**
 * Creates a response message for a service and sends it back to the activity that
 * initiated the service.
 * Created by Garrett on 7/22/2017.
 */

public class ResponseMessage {

    /* Tag for logging */
    private static final String TAG = ResponseMessage.class.getCanonicalName();

    /**
     * Sends the response of a service back to the initiating activity.
     * If there is a null response from the service, RESULT_CANCELED will
     * be returned to the activity and the activity will have to handle
     * the download failure.
     *
     * @param messenger    the messenger used to carry the message to the activity's handler
     * @param items        the response from the service
     * @param url          the url for the service
     * @param requestCode  the request code determining how the response should be handled
     */
    public static void sendEntries(Messenger messenger, String items, Uri url,ArrayList<String> params, int requestCode){
       // Log.d(TAG, " Creating and sending response from service.");
        Message message = makeReplyMessage(items, url, params, requestCode);
        try{
            messenger.send(message);
        }catch (RemoteException e){
         //   Log.e(TAG, "Exception while sending reply message back to Activity: ", e);
        }
    }

    /**
     * Writes the reply message for the messenger.
     */
    private static Message makeReplyMessage (String items, Uri url, ArrayList<String> parameters, int requestCode){
        Message message = new Message();

        if (items != null){
            Bundle data = new Bundle();

            data.putString(GlobalStrings.getItemArrayKey(), items);
            data.putInt(GlobalStrings.getRequestCode(), requestCode);
            data.putStringArrayList(GlobalStrings.getPARAMETERS(), parameters);
            data.putString(GlobalStrings.getServiceUrl(), url.toString());

            message.arg1 = Activity.RESULT_OK;
            message.setData(data);
        }
        else{
            message.arg1 = Activity.RESULT_CANCELED;
        }
        return message;
    }
}
