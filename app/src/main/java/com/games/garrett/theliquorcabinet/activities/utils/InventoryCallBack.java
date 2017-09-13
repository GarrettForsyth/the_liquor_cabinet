package com.games.garrett.theliquorcabinet.activities.utils;

/**
 * Provides a callback method so implementers of this interface
 * may be notified when an async thread has completed.
 *
 * Created by Garrett on 8/30/2017.
 */
public interface InventoryCallBack {

    void asyncInventoryCallComplete(boolean success);
}
