package com.games.garrett.theliquorcabinet.activities;

import android.content.Intent;

import com.games.garrett.theliquorcabinet.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.Assert.assertTrue;

/**
 * This class test that MainActivity sends the expected intents
 * on button clicks.
 * Created by Garrett on 8/25/2017.
 */

@Config(manifest = "app/src/main/AndroidManifest.xml", packageName = "com.games.garrett.theliquorcabinet")
@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    private MainActivity activity;

    @Test
    public void testSomething() throws Exception {
       // assertTrue(Robolectric.setupActivity(MainActivity.class) != null);
    }

    /*
    @Before
    public void setup(){
        // this will run MainActivity through the life cycle methods:
        // onCreate(..) -> onStart() -> onPostCreate(..) -> onResume(..)
        activity = Robolectric.setupActivity(MainActivity.class);
    }
    */

    @Test
    public void validateBrowseActivityStartedOnButtonClick(){
        //activity.findViewById(R.id.browse_button);

       // Intent expectedIntent = new Intent(activity, BrowseActivity.class);

        //ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        //Intent actualIntent = shadowActivity.getNextStartedActivity();

        //assertTrue(actualIntent.filterEquals(expectedIntent));
    }
}
