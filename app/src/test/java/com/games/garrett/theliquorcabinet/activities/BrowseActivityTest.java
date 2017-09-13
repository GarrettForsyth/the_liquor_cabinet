package com.games.garrett.theliquorcabinet.activities;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Test;



import java.util.HashSet;
import java.util.Set;


import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Browse activity unit tests.
 * Created by Garrett on 7/24/2017.
 */

public class BrowseActivityTest {

    @Test
    public void should_get_local_stores_from_preferences_if_not_first_use() throws Exception {

        Context ctx =mock(Context.class);
        SharedPreferences pref = mock(SharedPreferences.class);
        when(ctx.getSharedPreferences(anyString(), anyInt()))
                .thenReturn(pref);

        Set<String> fakeLocalStoreIds = new HashSet<>();
        fakeLocalStoreIds.add("001");
        fakeLocalStoreIds.add("005");
        fakeLocalStoreIds.add("115");

        when(pref.getStringSet("localStores", any()))
                .thenReturn(fakeLocalStoreIds);


    }

    @Test
    public void should_get_local_stores_from_LCBOService_if_first_use(){

    }
}
