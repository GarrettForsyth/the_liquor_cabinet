package com.games.garrett.theliquorcabinet.services.utils;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.games.garrett.theliquorcabinet.GlobalStrings;

import java.lang.ref.WeakReference;

/**
 * A handler that an activity can pass to a service via an intent to
 * receive the results from the service.
 *
 * Created by Garrett on 7/21/2017.
 */

public class ServiceResultHandler extends Handler {

    /* Tag for logging */
    private final String TAG = getClass().getSimpleName();

    // For garbage collection
    private WeakReference<ServiceResult> mResult;

    /**
     * Hook method dispatched upon receiving results from Service.
     * @param serviceResult  some activity Implementing ServiceResult
     */
    public ServiceResultHandler(ServiceResult serviceResult){
        mResult = new WeakReference<>(serviceResult);
    }

    /**
     * Resets ServiceResult callback instance after a configuration change
     * (the garbage collector would destroy the Service object associated
     * with mResult on a configuration change)
     * @param serviceResult some activity Implementing ServiceResult
     */
    @SuppressWarnings("unused")
    public void onConfigurationChange(ServiceResult serviceResult){
        mResult = new WeakReference<>(serviceResult);
    }

    /**
     * Hook method that handles a message returned by a service
     * @param message returned by a service
     */
   @Override
    public void handleMessage(Message message){

       //.d(TAG, "ServiceResultHandler's handleMessage() callback.");

       final int requestCode = getRequestCode(message);
       final int resultCode  = getResultCode(message);
       final Bundle data     = message.getData();

       if (mResult.get() == null){
         //  Log.w(TAG, "Lost weak reference to ServiceResult. Check configuration change handling.");
       }
       else{
           mResult.get().onServiceResult(requestCode, resultCode, data);
       }
   }

   /* Getter Methods for the Handler: */

    @SuppressWarnings("WeakerAccess")
    public static int getResultCode(Message message){
        return message.arg1;
    }

    @SuppressWarnings("unused")
    public static Uri getRequestUri(Message message){
        Bundle data = message.getData();
        return getRequestUri(data);
    }

    public static Uri getRequestUri(Bundle data){
        String url = data.getString(GlobalStrings.getServiceUrl());
        return Uri.parse(url);
    }

    @SuppressWarnings("WeakerAccess")
    public static int getRequestCode(Message message){
        Bundle data = message.getData();
        return data.getInt(GlobalStrings.getRequestCode());
    }
}
