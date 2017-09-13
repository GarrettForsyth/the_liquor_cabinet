package com.games.garrett.theliquorcabinet.services.utils;


import android.os.Bundle;

/**
 * Implemented by an activity that wants a result from a Service.
 * Created by Garrett on 7/21/2017.
 */

public interface ServiceResult {
    void onServiceResult(int requestCode, int resultCode, Bundle data);
}
